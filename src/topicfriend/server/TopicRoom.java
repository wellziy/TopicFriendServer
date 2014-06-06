package topicfriend.server;

public class TopicRoom 
{
	private int m_roomID;
	private OnlineUser m_user1;
	private OnlineUser m_user2;
	
	//////////////////////////
	//public
	public void removeUser(OnlineUser user)
	{
		if(m_user1==user)
		{
			m_user1=null;
		}
		else if(m_user2==user)
		{
			m_user2=null;
		}
		else
		{
			assert(false);
		}
	}
	
	public boolean isEmpty()
	{
		return m_user1==null&&m_user2==null;
	}
	
	public OnlineUser getUserNotMe(OnlineUser me)
	{
		if(m_user1==me)
		{
			return m_user2;
		}
		else
		{
			return m_user1;
		}
	}
}
