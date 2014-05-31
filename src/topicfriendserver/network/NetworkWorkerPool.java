package topicfriendserver.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.http.util.ByteArrayBuffer;

class NetworkBuffer
{
	public Socket socket;
	public ByteArrayBuffer content;
}

public class NetworkWorkerPool
{
	public static final int MAX_DATA_SIZE=1000000;
	
	private static HashSet<Socket> s_receiveSockets;
	private static HashSet<Socket> s_badSockets;
	private static ArrayList<NetworkBuffer> s_sendBuffers;
	private static ArrayList<NetworkBuffer> s_receiveBuffers;
	private static ArrayList<Thread> s_threads;
	
	private static Integer s_waitingThreadCount;
	private static int s_minThreadCount;
	private static int s_maxThreadCount;
	private static Boolean s_isActive;
	
	private static Runnable s_worker=new Runnable()
	{
		@Override
		public void run()
		{
			if(!s_isActive)
			{
				return;
			}
			
			while(true)
			{
				Socket recvSocket=null;
				NetworkBuffer sendBuffer=null;
				
				//lock s_isActive
				synchronized (s_isActive)
				{
					if(!s_isActive)
					{
						return;
					}
					
					//lock s_threads
					synchronized (s_threads) 
					{
						//lock s_waitingThreadCount
						synchronized (s_waitingThreadCount) 
						{
							//remove thread if there more thread are waiting for work
							if(s_waitingThreadCount>=s_minThreadCount)
							{
								Iterator<Thread> iter = s_threads.iterator();
								Thread th=iter.next();
								if(th==Thread.currentThread())
								{
									iter.remove();
									System.out.println("exit one thread");
									break;
								}
							}
						}
					}
				}
				
				//increase the waiting thread counter
				synchronized (s_waitingThreadCount)
				{
					s_waitingThreadCount++;
				}
				
				while(true)
				{
					//enter a dead loop,need to check exit in some place.
					if(!s_isActive)
					{
						return;
					}
					
					//check send buffers
					synchronized (s_sendBuffers) 
					{
						Iterator<NetworkBuffer> iter = s_sendBuffers.iterator();
						if(iter.hasNext())
						{
							sendBuffer=iter.next();
							iter.remove();
							break;
						}
					}
					
					if(sendBuffer!=null)
					{
						break;
					}
					
					//check receive sockets
					synchronized (s_receiveSockets) 
					{
						Iterator<Socket> iter = s_receiveSockets.iterator();
						if(iter.hasNext())
						{
							recvSocket=iter.next();
							iter.remove();
							break;
						}
					}
					
					if(recvSocket!=null)
					{
						break;
					}
				}//end of while
				
				//decrease the waiting thread counter
				synchronized (s_waitingThreadCount)
				{
					s_waitingThreadCount--;
				}
				
				//check whether the thread is too busy,if it is,add a new thread necessary
				synchronized(s_isActive)
				{
					if(!s_isActive)
					{
						return;
					}
					synchronized(s_threads)
					{
						synchronized(s_waitingThreadCount)
						{
							if(s_waitingThreadCount==0&&s_threads.size()<s_maxThreadCount)
							{
								Thread th=new Thread(s_worker);
								s_threads.add(th);
								th.start();
							}
						}
					}
				}
				
				//do the job found
				System.out.println("the thread "+Thread.currentThread().getId()+" fond a job");
				if(sendBuffer!=null)
				{
					//send the buffer to socket
					boolean isSendOK=sendNetworkBuffer(sendBuffer);
					if(!isSendOK)
					{
						synchronized(s_receiveSockets)
						{
							synchronized(s_badSockets)
							{
								removeReceiveSocket(sendBuffer.socket);
								s_badSockets.add(sendBuffer.socket);
							}
						}
					}
				}
				else
				{
					//receive the buffer from the socket
					NetworkBuffer buf=receiveNetworkBuffer(recvSocket);
					if(buf==null)
					{
						//error happened when received from the socket
						synchronized(s_receiveSockets)
						{
							synchronized(s_badSockets)
							{
								removeReceiveSocket(recvSocket);
								s_badSockets.add(recvSocket);
							}
						}
					}
					else
					{
						//receive ok,add the received buffer to queue
						synchronized(s_receiveBuffers)
						{
							s_receiveBuffers.add(buf);
						}
					}
				}
			}
			//end of function run
		}
	};
	
	//public interfaces
	public static void initNetworkWorkerPool(int minThreadCount,int maxThreadCount)
	{
		s_minThreadCount=minThreadCount;
		s_maxThreadCount=maxThreadCount;
		
		s_receiveSockets=new HashSet<>();
		s_badSockets=new HashSet<>();
		s_receiveBuffers=new ArrayList<>();
		s_sendBuffers=new ArrayList<>();
		s_threads=new ArrayList<>();
		
		s_waitingThreadCount=0;
		s_isActive=true;
		
		//start all threads here
		for(int i=0;i<s_minThreadCount;i++)
		{
			Thread th=new Thread(s_worker);
			synchronized(s_threads)
			{
				s_threads.add(th);
				th.start();
			}
		}
	}
	
	public static void destroyNetworkWorkerPool()
	{
		//set s_isActive to false
		synchronized (s_isActive)
		{
			s_isActive=false;
		}
		
		//stop all threads here
		synchronized (s_threads) 
		{
			Iterator<Thread> iter = s_threads.iterator();
			while(iter.hasNext())
			{
				Thread th=iter.next();
				try
				{
					th.join();
				} 
				catch (InterruptedException e) 
				{
					e.printStackTrace();
				}
			}
			s_threads.clear();
		}
		
		removeReceiveSocket(null);
		synchronized(s_badSockets)
		{
			s_badSockets.clear();
		}
	}
	
	public static void queueRecieveData(Socket socket)
	{
		synchronized(s_receiveSockets)
		{
			s_receiveSockets.add(socket);
		}
	}
	
	//get a socket with data,if the socket is null,it just return the first socket which has received data
	//@param socket null 
	public static Socket getReceivedData(ByteArrayBuffer buf,Socket socket)
	{
		NetworkBuffer networkBuf=null;
		
		synchronized(s_receiveBuffers)
		{
			if(s_receiveBuffers.size()==0)
			{
				return null;
			}
			
			Iterator<NetworkBuffer> iter = s_receiveBuffers.iterator();
			if(socket==null)
			{
				networkBuf=iter.next();
				iter.remove();
			}
			else
			{
				while(iter.hasNext())
				{
					NetworkBuffer iterBuffer=iter.next();
					if(iterBuffer.socket==socket)
					{
						networkBuf=iterBuffer;
						iter.remove();
						break;
					}
				}
			}
		}
		
		if(networkBuf==null)
		{
			return null;
		}
		buf.clear();
		buf.append(networkBuf.content.toByteArray(), 0, networkBuf.content.length());
		
		return networkBuf.socket;
	}
	
	public static void queueData(Socket socket,ByteArrayBuffer buf)
	{
		synchronized(s_sendBuffers)
		{
			NetworkBuffer networkBuf=new NetworkBuffer();
			networkBuf.socket=socket;
			networkBuf.content=buf;
			s_sendBuffers.add(networkBuf);
		}
	}
	
	//remove socket, and its receive buffer, send buffer and so on.
	public static void removeReceiveSocket(Socket socket)
	{
		removeSocket(socket);
		removeReceiveBuffer(socket);
		removeSendBuffer(socket);
	}
	
	public static HashSet<Socket> getBadSocketSet()
	{
		return s_badSockets;
	}
	
	//private methods
	private static int packInt(byte[] networkByte)
	{
		int res=(networkByte[0]<<24)+(networkByte[1]<<16)
				+(networkByte[2]<<8)+(networkByte[3]<<0);
		return res;
	}
	
	private static byte[] unpackInt(int value)
	{
		byte[] res=new byte[4];
		res[0]=(byte) ((value>>24)&0xff);
		res[1]=(byte) ((value>>16)&0xff);
		res[2]=(byte) ((value>>8)&0xff);
		res[3]=(byte) ((value>>0)&0xff);
		
		return res;
	}
	
	private static boolean receiveWithTimeout(Socket socket,ByteArrayBuffer buf,int len,int eachTimeout,int totalTimeout)
	{
		long totalCounter=0;
		
		InputStream inputStream=null;
		try
		{
			inputStream = socket.getInputStream();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			return false;
		}
		
		byte[] byteBuffer=new byte[1024];
		while(len>0)
		{
			long startLoopTime=System.currentTimeMillis();
			try 
			{
				while(inputStream.available()<=0)
				{
					Thread.sleep(1);
					long diff=System.currentTimeMillis()-startLoopTime;
					if(diff>=eachTimeout)
					{
						return false;
					}
				}
				totalCounter+=System.currentTimeMillis()-startLoopTime;
				if(totalCounter>=totalTimeout)
				{
					return false;
				}
				
				int placeLen=Math.min(len, byteBuffer.length);
				int readLen=inputStream.read(byteBuffer,0,placeLen);
				buf.append(byteBuffer, 0, readLen);
				len-=readLen;
			}
			catch (IOException e)
			{
				e.printStackTrace();
				return false;
			}
			catch (InterruptedException e) 
			{
				e.printStackTrace();
				return false;
			}
			
		}
		return true;
	}
	
	private static NetworkBuffer receiveNetworkBuffer(Socket socket)
	{
		ByteArrayBuffer header=new ByteArrayBuffer(8);
		
		boolean isHeaderOK=receiveWithTimeout(socket, header, 4, 3000, 30000);
		if(!isHeaderOK)
		{
			return null;
		}
		
		int size=packInt(header.toByteArray());
		System.out.println("receive buffer with size: "+size);
		ByteArrayBuffer content=new ByteArrayBuffer(size+100);
		boolean isContentOK=receiveWithTimeout(socket, content, size, 3000, 30000);
		if(!isContentOK)
		{
			return null;
		}
		
		NetworkBuffer res=new NetworkBuffer();
		res.socket=socket;
		res.content=content;
		
		return res;
	}
	
	private static boolean sendNetworkBuffer(NetworkBuffer buf)
	{
		Socket socket=buf.socket;
		
		try
		{
			OutputStream outputStream = socket.getOutputStream();
			int len=buf.content.length();
			byte[] header=unpackInt(len);
			outputStream.write(header,0,4);
			outputStream.write(buf.content.buffer(),0,len);
			outputStream.flush();
			
			System.out.println("finished send one buffer with size:"+len);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	//@param socket null means remove all buffer, otherwise remove the buffer of the socket
	private static void removeSendBuffer(Socket socket)
	{
		synchronized(s_sendBuffers)
		{
			if(socket==null)
			{
				s_sendBuffers.clear();
				return;
			}
			
			Iterator<NetworkBuffer> iter = s_sendBuffers.iterator();
			while(iter.hasNext())
			{
				NetworkBuffer buf=iter.next();
				if(buf.socket==socket)
				{
					iter.remove();
				}
			}
		}
	}
	
	//@param socket null means remove all buffer, otherwise remove the buffer of the socket
	private static void removeReceiveBuffer(Socket socket)
	{
		synchronized(s_receiveBuffers)
		{
			if(socket==null)
			{
				s_receiveBuffers.clear();
				return;
			}
			
			Iterator<NetworkBuffer> iter = s_receiveBuffers.iterator();
			while(iter.hasNext())
			{
				NetworkBuffer buf=iter.next();
				if(buf.socket==socket)
				{
					iter.remove();
				}
			}
		}
	}
	
	//@param socket null means remove all socket,otherwise remove the socket
	private static void removeSocket(Socket socket)
	{
		synchronized(s_receiveSockets)
		{
			if(socket==null)
			{
				s_receiveSockets.clear();
				return;
			}
			
			s_receiveSockets.remove(socket);
		}
	}
}
