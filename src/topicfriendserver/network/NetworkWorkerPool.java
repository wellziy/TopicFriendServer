package topicfriendserver.network;

import java.net.Socket;
import java.nio.ByteBuffer;

public class NetworkWorkerPool
{
	public static void initNetworkWorkerPool(int minThreadCount,int maxThreadCount)
	{
		
	}
	
	public static void destroyNetworkWorkerPool()
	{
		
	}
	
	public static void queueRecieveData(Socket socket)
	{
		
	}
	
	public static Socket getReceivedData(ByteBuffer buf,Socket socket)
	{
		//TODO:
		return null;
	}
	
	public static void queueData(Socket socket,ByteBuffer buf)
	{
		
	}
	
	public static void removePendingReceiveSocket(Socket socket)
	{
		
	}
	
	public static void removeBadSocket(Socket socket)
	{
		
	}
	
	public static Socket getBadSocket()
	{
		//TODO:
		return null;
	}
	
	public static void makeBadSocket(Socket socket)
	{
		
	}
}
