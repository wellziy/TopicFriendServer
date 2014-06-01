package topicfriendserver.network;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.http.util.ByteArrayBuffer;

public class TopicFriendServer 
{
	public static void main(String[] args)
	{
		Network.initNetwork(4, 4, 10000);
		try
		{
			Network.startServer(55555, 1000);
			long lastPing=System.currentTimeMillis();
			long nowPing;
			
			while(true)
			{
				//deal with new connection
				int newConnection=Network.acceptConnection();
				if(newConnection!=Network.NULL_CONNECTION)
				{
					System.out.println("accept a new connection");
					System.out.println("current connections: "+Network.getConnectionCount());
				}
				
				//deal with receive data
				ByteArrayBuffer buf=new ByteArrayBuffer(100);
				int connection=Network.receiveData(buf,Network.NULL_CONNECTION);
				if(connection!=Network.NULL_CONNECTION)
				{
					String str=new String(buf.buffer());
					System.out.println("connection "+connection+" say:"+str);
				}
				
				nowPing=System.currentTimeMillis();
				if(nowPing-lastPing>1)
				{
					Network.pingFrontWaitingConnection();
					lastPing=nowPing;
				}
				//deal with bad sockets
				while(true)
				{
					int badConnection=Network.getBadConnectionWithoutRemove();
					if(badConnection==Network.NULL_CONNECTION)
					{
						break;
					}
					
					Network.disconnect(badConnection);
					System.out.println("a connection disconnected");
					System.out.println("current connections: "+Network.getConnectionCount());
				}
			}
		} 
		catch (IOException e)
		{
			e.printStackTrace();
			System.out.println("failed to start server");
		}

		Network.stopServer();
		Network.destroyNetwork();
	}
}
