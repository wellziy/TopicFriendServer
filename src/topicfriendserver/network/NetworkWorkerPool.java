package topicfriendserver.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;

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
	
	//public interfaces
	public static void initNetworkWorkerPool(int minThreadCount,int maxThreadCount)
	{
		
	}
	
	public static void destroyNetworkWorkerPool()
	{
		
	}
	
	public static void queueRecieveData(Socket socket)
	{
		
	}
	
	public static Socket getReceivedData(ByteArrayBuffer buf,Socket socket)
	{
		//TODO:
		return null;
	}
	
	public static void queueData(Socket socket,ByteArrayBuffer buf)
	{
		
	}
	
	public static void removePendingReceiveSocket(Socket socket)
	{
		
	}
	
	public static void removeBadSocket(Socket socket)
	{
		
	}
	
	public static HashSet<Socket> getBadSocketSet()
	{
		//TODO:
		return null;
	}
	
	//private methods
	private static int packInt(byte[] networkByte)
	{
		//TODO:
		return 0;
	}
	
	private static byte[] unpackInt(int value)
	{
		//TODO:
		return null;
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
				
				int readLen=inputStream.read(byteBuffer,0,byteBuffer.length);
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
			outputStream.write(buf.content.buffer());
			outputStream.flush();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
}
