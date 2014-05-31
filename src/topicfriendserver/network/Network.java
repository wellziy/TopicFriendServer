package topicfriendserver.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Vector;

public class Network 
{
	public static int NULL_CONNECTION=0;
	
	//store all client connections after handshake in server
	//store all connection call from connectHostPort
	private static HashMap<Integer,Socket> s_connectionSocketMap;
	private static int s_lastConnectionID;
	private static ArrayList<Integer> s_connections;
	private static HashSet<Integer> s_waitingConnections;
	private static int s_waitingConectionsMaxSize;
	
	//only for server
	private static HashSet<Socket> s_handshakeSockets;
	private static int s_handshakeSocketsMaxSize;
	private static ServerSocket s_serverSocket;
	
	//public interfaces
	public static void initNetwork(int minThreadCount,int maxThreadCount,int waitingConectionsMaxSiz)
	{
		NetworkWorkerPool.initNetworkWorkerPool(minThreadCount, maxThreadCount);
		s_waitingConectionsMaxSize=waitingConectionsMaxSiz;
		s_lastConnectionID=NULL_CONNECTION;
		s_serverSocket=null;
	}
	
	public static void startServer(int port,int handshakeSocketsMaxSize) throws IOException
	{
		assert(isServerRunning()==false);
		
		s_handshakeSockets=new HashSet<Socket>();
		s_handshakeSocketsMaxSize=handshakeSocketsMaxSize;
		
		s_serverSocket=new ServerSocket();
		s_serverSocket.bind(new InetSocketAddress(port));
		
	}
	
	public static void stopServer()
	{
		
	}
	
	public static void destroyNetwork()
	{
		
	}
	
	public static int getConnectionCount()
	{
		//TODO:
		return 0;
	}
	
	public static boolean isServerRunning()
	{
		//TODO:
		return false;
	}
	
	public static int connectHostPort(String host,int port)
	{
		//TOOD:
		return 0;
	}
	
	public static int acceptConnection()
	{
		//TODO:
		return 0;
	}
	
	public static boolean disconnect(int connection)
	{
		//TODO:
		return false;
	}
	
	
	//may be change to get all bad connections at once
	public static int getBadConnection()
	{
		//TODO:
		return 0;
	}
	
	public static void makeBadConnection(int connection)
	{
		
	}
	
	public static int receiveData(ByteBuffer buf)
	{
		//TODO:
		return 0;
	}
	
	public static void sendData(ByteBuffer buf,int connection)
	{
		
	}
	
	public static void sendDataAllExcept(ByteBuffer buf,int connection)
	{
		
	}
	
	public static void sendDataMany(ByteBuffer buf,Vector<Integer> connections)
	{
		
	}
	
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
		Socket socket=getSocketByConnection(connection);
		if(socket!=null)
		{
			s_waitingConnections.remove(socket);
		}
	}
	
	private static int acceptHankshakeSocket()
	{
		Socket socket=null;
		
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
}
