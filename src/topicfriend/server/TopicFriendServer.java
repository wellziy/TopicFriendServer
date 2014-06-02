package topicfriend.server;

import java.io.IOException;

import org.apache.http.util.ByteArrayBuffer;

import topicfriend.netmessage.NetMessage;
import topicfriend.network.Network;

public class TopicFriendServer 
{
	public static void main(String[] args)
	{
		//create net message handler
		NetMessageHandler m_handler=new NetMessageHandler();
		
		//init network
		Network.initNetwork(4, 4, 10000);
		try
		{
			//start the server at port 55555 with max handshake queue 1000
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
					
					//handle the new connection
					m_handler.handleNewConnection(newConnection);
				}
				
				//deal with receive data
				ByteArrayBuffer buf=new ByteArrayBuffer(100);
				int recvConnection=Network.receiveData(buf,Network.NULL_CONNECTION);
				if(recvConnection!=Network.NULL_CONNECTION)
				{
					String str=new String(buf.buffer());
					System.out.println("connection "+recvConnection+" received:"+str);
					
					NetMessage msg=NetMessage.fromJsonString(new String(buf.buffer()));
					if(msg==null)
					{
						//receive a bad message,make it bad connection
						Network.makeBadConnection(recvConnection);
					}
					else
					{
						//handle the message,remember that the message will still be invalid,if so,make it bad connection when handle it
						m_handler.handleMessage(recvConnection, msg);
					}
				}
				
				//deal with ping
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
					
					//handle the bad connection
					m_handler.handleBadConnection(badConnection);
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
