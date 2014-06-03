package topicfriend.server;

import java.util.HashMap;
import java.util.HashSet;

import topicfriend.netmessage.NetMessage;
import topicfriend.netmessage.NetMessageError;
import topicfriend.netmessage.NetMessageLogin;
import topicfriend.netmessage.NetMessageRegister;
import topicfriend.netmessage.data.UsrInfo;
import topicfriend.network.Network;
import topicfriend.server.database.UsrTable;

public class NetMessageHandler
{
	//store all connection has not login
	private HashSet<Integer> m_unloginConnections;
	//store pair<connection,OnlineUser>
	private HashMap<Integer,OnlineUser> m_loginConnectionMap;
	//store pair<uid,OnlineUser>
	private HashMap<Integer,OnlineUser> m_loginUidMap;
	//store pair<topicid,uid>,means a user with uid is waiting for join topic with topicid
	private HashMap<Integer,Integer> m_unmatchTopicUidMap;
	//store all matched room
	private HashSet<TopicRoom> m_topicRooms;
	
	public NetMessageHandler()
	{
		//TODO:
		//load the topic from database
	}
	
	public void handleMessage(int connection,NetMessage msg)
	{
		if(m_unloginConnections.contains(connection))
		{
			if(msg instanceof NetMessageRegister)
			{
				handleRegister(connection, msg);
			}
			else if(msg instanceof NetMessageLogin)
			{
				handleLogin(connection,msg);
			}
			else
			{
				Network.makeBadConnection(connection);
			}
			return;
		}
		
		
		//TDOO: handle all the message here,but now it just echo the message back to the client
//		Network.sendDataOne(msg.toByteArrayBuffer(),connection);
	}

	public void handleNewConnection(int connection)
	{
		
	}
	
	public void handleBadConnection(int connection)
	{
		
	}
	
	////////////////////////////////////
	//private
	private void handleRegister(int connection,NetMessage msg)
	{
		NetMessageRegister msgRegister=(NetMessageRegister)msg;
		String name=msgRegister.getUserName();
		String password=msgRegister.getPassword();
		
		//TODO:
		//validate the name and password here
		if(UsrTable.isUserNameExisted(name))
		{
			NetMessageError error=new NetMessageError(0, "the user name has been existed");
			Network.sendDataOne(error.toByteArrayBuffer(), connection);
			return;
		}
		
		//TODO:
		//create the user and update server state
//		UsrTable.createUser(sex, name, password, signature, icon)
		
		//TODO:
		//login succeed,send self info , friend list and info, unread message back to client(need topic list???)
		
	}
	
	private void handleLogin(int connection, NetMessage msg)
	{
		NetMessageLogin msgLogin=(NetMessageLogin)msg;
		String name=msgLogin.getUserName();
		String password=msgLogin.getPassword();
		
		//TODO:
		//validate the name and password here
		int uid=UsrTable.getIDWithUserNameAndPassword(name, password);
		if(uid<0)
		{
			NetMessageError error=new NetMessageError(0, "user name and password does not matched");
			Network.sendDataOne(error.toByteArrayBuffer(), connection);
			return;
		}
		
		//TODO:
		//login succeed,send self info,friend list and info,unread message back to client(need topic list???)
	}
}
