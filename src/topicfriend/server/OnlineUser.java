package topicfriend.server;

public class OnlineUser
{
	private int m_connection;
	private int m_uid;
	private TopicRoom m_room;
	
	////////////////////////
	//public
	public OnlineUser(int connection,int uid)
	{
		m_connection=connection;
		m_uid=uid;
		m_room=null;
	}
	
	public int getConnection()
	{
		return m_connection;
	}
	
	public int getUID()
	{
		return m_uid;
	}
	
	public void setTopicRoom(TopicRoom room)
	{
		m_room=room;
	}
	
	public TopicRoom getTopicRoom()
	{
		return m_room;
	}
}
