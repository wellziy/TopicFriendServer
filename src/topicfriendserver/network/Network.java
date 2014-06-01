package topicfriendserver.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.Vector;

import org.apache.http.impl.io.SocketInputBuffer;
import org.apache.http.util.ByteArrayBuffer;

public class Network 
{
	public static final int NULL_CONNECTION=0;
	
	//store all client connections after handshake in server
	//store all connection call from connectHostPort
	private static HashMap<Integer,Socket> s_connectionSocketMap;
	private static int s_lastConnectionID;
	private static ArrayList<Integer> s_connections;
	private static HashSet<Integer> s_waitingConnections;
	//s_waitingPings contains all connections the same as s_waitingConnections,but with a different data structure
	private static LinkedList<Integer> s_waitingPings;
	private static int s_waitingConectionsMaxSize;
	private static HashSet<Integer> s_badConnections;
	
	//only for server
	private static HashSet<Socket> s_handshakeSockets;
	private static int s_handshakeSocketsMaxSize;
	private static ServerSocket s_serverSocket;
	private static Thread s_acceptThread;
	private static boolean s_isServerActive;
	
	//public interfaces
	public static synchronized void initNetwork(int minThreadCount,int maxThreadCount,int waitingConectionsMaxSiz)
	{
		NetworkWorkerPool.initNetworkWorkerPool(minThreadCount, maxThreadCount);
		
		s_connectionSocketMap=new HashMap<Integer,Socket>();
		s_lastConnectionID=NULL_CONNECTION;
		
		s_connections=new ArrayList<Integer>();
		
		s_waitingConnections=new HashSet<Integer>();
		s_waitingConectionsMaxSize=waitingConectionsMaxSiz;
		s_waitingPings=new LinkedList<Integer>();
		
		s_badConnections=new HashSet<Integer>();
		
		s_serverSocket=null;
	}
	
	public static synchronized void startServer(int port,int handshakeSocketsMaxSize) throws IOException
	{
		assert(isServerRunning()==false);
		
		s_handshakeSockets=new HashSet<Socket>();
		s_handshakeSocketsMaxSize=handshakeSocketsMaxSize;
		
		s_serverSocket=new ServerSocket();
		s_serverSocket.setSoTimeout(1);
		s_serverSocket.bind(new InetSocketAddress(port));
		
		s_acceptThread=new Thread(new Runnable()
		{
			@Override
			public void run() 
			{
				while(true)
				{
					try
					{
						Socket socket=s_serverSocket.accept();
						synchronized (s_handshakeSockets)
						{
							if(s_handshakeSockets.size()>=s_handshakeSocketsMaxSize)
							{
								closeSocket(socket);
							}
							else
							{
								s_handshakeSockets.add(socket);
							}
						}
					} 
					catch (SocketTimeoutException e)
					{
						//accept timeout,nothing to do here
					}
					catch (IOException e) 
					{
						e.printStackTrace();
						assert(false);
					}
					
					//check whether network is active or not
					if(!s_isServerActive)
					{
						break;
					}
				}
			}
		});
		//must set s_isServerActive to true before start the thread
		s_isServerActive=true;
		s_acceptThread.start();
	}
	
	public static synchronized void stopServer()
	{
		assert(isServerRunning());
		//must set s_isServerActive to false before stop the thread
		s_isServerActive=false;
		//stop the thread,must stop the thread before set s_serverSocket to null
		try
		{
			s_acceptThread.join();
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
		
		s_handshakeSockets.clear();
		try 
		{
			s_serverSocket.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		s_serverSocket=null;
	}
	
	public static synchronized void destroyNetwork()
	{
		if(isServerRunning())
		{
			System.out.println("must stop server before");
			assert(false);
		}
		
		Iterator<Integer> iter=s_connections.iterator();
		while(iter.hasNext())
		{
			int connection=iter.next();
			disconnectWithoutChangeConnection(connection);
		}
		s_connections.clear();
		
		NetworkWorkerPool.destroyNetworkWorkerPool();
		s_connectionSocketMap.clear();
		s_waitingConnections.clear();
		s_waitingPings.clear();
		s_badConnections.clear();
	}
	
	public static synchronized int getConnectionCount()
	{
		return s_connections.size();
	}
	
	public static synchronized boolean isServerRunning()
	{
		return s_serverSocket!=null&&s_isServerActive==true;
	}
	
	public static synchronized int connectHostPort(String host,int port,int timeout) throws IOException
	{
		Socket socket=new Socket(host,port);
		
		OutputStream outputStream = socket.getOutputStream();
		byte[] handshakeByte=new byte[4];
		outputStream.write(handshakeByte);
		outputStream.flush();
		
		int connection=createConnection(socket);
		if(s_waitingConnections.size()>=s_waitingConectionsMaxSize)
		{
			closeSocket(socket);
			return NULL_CONNECTION;
		}
		
		//add to waiting connection
		s_waitingConnections.add(connection);
		s_waitingPings.addLast(connection);
		
		//add to connections
		s_connections.add(connection);
		
		return connection;
	}
	
	public static synchronized int acceptConnection()
	{
		assert(isServerRunning());
		
		int connection = acceptHankshakeSocket();
		
		return connection;
	}
	
	//disconnect a connection,remove all thing about it
	public static synchronized boolean disconnect(int connection)
	{
		//remove from connections
		Iterator<Integer> iter = s_connections.iterator();
		while(iter.hasNext())
		{
			int iterConnection=iter.next();
			if(iterConnection==connection)
			{
				disconnectWithoutChangeConnection(iterConnection);
				iter.remove();
				break;
			}
		}
		return true;
	}
	
	
	//may be change to get all bad connections at once
	public static synchronized int getBadConnectionWithoutRemove()
	{
		if(s_badConnections.size()>0)
		{
			return s_badConnections.iterator().next();
		}
		
		updateBadConnectionSet();
		if(s_badConnections.size()>0)
		{
			return s_badConnections.iterator().next();
		}
		
		return NULL_CONNECTION;
	}
	
	public static synchronized void makeBadConnection(int connection)
	{
		s_badConnections.add(connection);
	}
	
	//@notice do not ping the waiting queue too much,since it will make the send buffer full of ping packets,and make the server slow down too much
	//TODO: change not to ping all waiting connections all together,just ping them at different time
	public static synchronized void pingFrontWaitingConnection()
	{
		ListIterator<Integer> iter = s_waitingPings.listIterator();
		if(iter.hasNext())
		{
			int connection=iter.next();
			Socket socket=getSocketByConnection(connection);
			NetworkWorkerPool.queueData(socket, new ByteArrayBuffer(1));
			iter.remove();
			s_waitingPings.addLast(connection);
		}
	}
	
	//recvConnection == NULL_CONNECTION means receive the first socket which has data received
	public static synchronized int receiveData(ByteArrayBuffer buf,int recvConnection)
	{
		//check wating queue
		Iterator<Integer> iter = s_waitingConnections.iterator();
		while(iter.hasNext())
		{
			int connection=iter.next();
			//TODO:check the return socket may be null???
			Socket socket=getSocketByConnection(connection);
			try 
			{
				InputStream inputStream = socket.getInputStream();
				int available=inputStream.available();
				if(available>=4)
				{
					iter.remove();
					s_waitingPings.remove(new Integer(connection));
					NetworkWorkerPool.queueRecieveData(socket);
					continue;
				}
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
				//add to the bad connection set
				s_badConnections.add(connection);
				iter.remove();
				s_waitingPings.add(connection);
			}
		}
		
		Socket recvSocket=null;
		//find the socket with the connection
		if(recvConnection!=NULL_CONNECTION)
		{
			recvSocket=getSocketByConnection(recvConnection);
		}
		
		//receive data from the socket,the socket may be null here
		recvSocket=NetworkWorkerPool.getReceivedData(buf, recvSocket);
		//if no socket has data to received
		if(recvSocket==null)
		{
			return NULL_CONNECTION;
		}
		
		if(s_waitingConnections.size()>=s_waitingConectionsMaxSize)
		{
			//if the waiting set is full,just close the socket
			closeSocket(recvSocket);
			return NULL_CONNECTION;
		}
		
		if(recvConnection==NULL_CONNECTION)
		{
			recvConnection=getConnectionBySocket(recvSocket);
		}
		assert(recvConnection!=NULL_CONNECTION);
		//add the connection back to waiting queue
		s_waitingConnections.add(recvConnection);
		s_waitingPings.addLast(recvConnection);
		
		//NOTICE:
		//receive a empty packet,just ignore it
		if(buf.length()<=0)
		{
			return NULL_CONNECTION;
		}
		return recvConnection;
	}
	
	public static synchronized void sendDataOne(ByteArrayBuffer buf,int connection)
	{
		Socket socket=getSocketByConnection(connection);
		assert(socket!=null);
		
		updateBadConnectionSet();
		if(s_badConnections.contains(connection))
		{
			System.out.println("send data to a bad connection, will ignore it");
			return;
		}
		
		NetworkWorkerPool.queueData(socket, buf);
	}
	
	public static synchronized void sendDataAllExcept(ByteArrayBuffer buf,int connection)
	{
		Iterator<Integer> iter = s_connections.iterator();
		while(iter.hasNext())
		{
			int iterConnection=iter.next();
			if(iterConnection==connection)
			{
				continue;
			}
			sendDataOne(buf, iterConnection);
		}
	}
	
	public static synchronized void sendDataMany(ByteArrayBuffer buf,Vector<Integer> connections)
	{
		Iterator<Integer> iter = connections.iterator();
		while(iter.hasNext())
		{
			int iterConnection=iter.next();
			sendDataOne(buf, iterConnection);
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	//private methods
	private static int createConnection(Socket socket)
	{
		int nextID=s_lastConnectionID+1;
		while(true)
		{
			if(nextID!=NULL_CONNECTION&&s_connectionSocketMap.containsKey(nextID)==false)
			{
				break;
			}
			nextID++;
		}
		s_connectionSocketMap.put(nextID, socket);
		s_lastConnectionID=nextID;
		
		return nextID;
	}
	
	private static int getConnectionBySocket(Socket socket)
	{
		Iterator<Entry<Integer, Socket>> iter=s_connectionSocketMap.entrySet().iterator();
		while(iter.hasNext())
		{
			Entry<Integer, Socket> entry=iter.next();
			if(entry.getValue()==socket)
			{
				return entry.getKey();
			}
		}
		return NULL_CONNECTION;
	}
	
	private static Socket getSocketByConnection(int connection)
	{
		return s_connectionSocketMap.get(connection);
	}
	
	private static void removeWaitingConnection(int connection)
	{
		s_waitingConnections.remove(connection);
		s_waitingPings.remove(new Integer(connection));
	}
	
	private static int acceptHankshakeSocket()
	{
		Socket socket=null;
		
		synchronized (s_handshakeSockets) 
		{
			//find the socket ready to read
			Iterator<Socket>iter=s_handshakeSockets.iterator();
			while(iter.hasNext())
			{
				Socket iterSocket=iter.next();
				try
				{
					if(iterSocket.getInputStream().available()>=4)
					{
						socket=iterSocket;
						iter.remove();
						break;
					}
				} 
				catch (IOException e)
				{
					e.printStackTrace();
					closeSocket(iterSocket);
					iter.remove();
				}
			}
		}

		if(socket==null)
		{
			return NULL_CONNECTION;
		}
		
		try 
		{
			byte[] handshakeByte=new byte[4];
			socket.getInputStream().read(handshakeByte,0,4);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			closeSocket(socket);
		}
		
		//waiting queue is full
		if(s_waitingConnections.size()>=s_waitingConectionsMaxSize)
		{
			closeSocket(socket);
			return NULL_CONNECTION;
		}
		
		//create connection
		int connection=createConnection(socket);
		s_waitingConnections.add(connection);
		s_waitingPings.addLast(connection);
		s_connections.add(connection);
		
		return connection;
	}
	
	private static void closeSocket(Socket socket)
	{
		try
		{
			socket.close();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private static void disconnectWithoutChangeConnection(int connection)
	{
		//remove from waiting queue
		removeWaitingConnection(connection);
		//remove from bad connection
		s_badConnections.remove(connection);
		
		//close the socket here instead of in network worker pool
		Socket socket=getSocketByConnection(connection);
		closeSocket(socket);
		//remove from connection and socket map
		s_connectionSocketMap.remove(connection);
		
		//remove from network worker pool
		NetworkWorkerPool.removeReceiveSocket(socket);
	}
	
	private static void updateBadConnectionSet()
	{
		HashSet<Socket> badSocketSet = NetworkWorkerPool.getBadSocketSet();
		synchronized (badSocketSet)
		{
			Iterator<Socket> iter = badSocketSet.iterator();
			while(iter.hasNext())
			{
				Socket socket=iter.next();
				int connection=getConnectionBySocket(socket);
				//the connection may be null if it is added to the bad socket set multiple times
				if(connection!=NULL_CONNECTION)
				{
					s_badConnections.add(connection);
				}
			}
			badSocketSet.clear();
		}
	}
}
