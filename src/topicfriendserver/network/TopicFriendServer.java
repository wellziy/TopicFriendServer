package topicfriendserver.network;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.http.util.ByteArrayBuffer;

public class TopicFriendServer 
{
	public static void main(String[] args)
	{
		Network.initNetwork(1, 5, 10000);
		try
		{
			Network.startServer(55555, 1000);
			while(true)
			{
				//deal with new connection
				int newConnection=Network.acceptConnection();
				if(newConnection!=Network.NULL_CONNECTION)
				{
					System.out.println("accpet a new connection");
					System.out.println("current connections: "+Network.getConnectionCount());
				}
				
				//deal with receive data
				ByteArrayBuffer buf=new ByteArrayBuffer(100);
				int connection=Network.receiveData(buf,Network.NULL_CONNECTION);
				if(connection==Network.NULL_CONNECTION)
				{
					continue;
				}
				String str=new String(buf.buffer());
				System.out.println("connection "+connection+" say:"+str);
				
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
