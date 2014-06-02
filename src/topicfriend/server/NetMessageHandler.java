package topicfriend.server;

import java.util.HashMap;
import java.util.HashSet;

import topicfriend.netmessage.NetMessage;

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
		
	}
	
	public void handleNewConnection(int connection)
	{
		
	}
	
	public void handleBadConnection(int connection)
	{
		
	}
}
