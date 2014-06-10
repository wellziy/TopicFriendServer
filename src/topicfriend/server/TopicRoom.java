package topicfriend.server;

public class TopicRoom 
{
	private static int s_nextRoomID=0;
	
	private int m_roomID;
	private int m_topicID;
	private OnlineUser m_user1;
	private boolean m_user1LikeFlag;
	private OnlineUser m_user2;
	private boolean m_user2LikeFlag;
	private boolean m_isMatched;
	
	//////////////////////////
	//public
	public TopicRoom(int topicID)
	{
		m_roomID=s_nextRoomID;
		s_nextRoomID++;
		
		m_topicID=topicID;
		m_user1=m_user2=null;
		m_isMatched=false;
		m_user1LikeFlag=m_user2LikeFlag=false;
	}
	
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
	
	public boolean isFull()
	{
		return m_user1!=null&&m_user2!=null;
	}
	
	public void addUser(OnlineUser user)
	{
		if(m_user1==null)
		{
			m_user1=user;
		}
		else if(m_user2==null)
		{
			m_user2=user;
		}
		else
		{
			assert(false);
		}
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
	
	public void setUserLikeFlag(OnlineUser user)
	{
		if(user==m_user1)
		{
			m_user1LikeFlag=true;
		}
		else if(user==m_user2)
		{
			m_user2LikeFlag=true;
		}
		else
		{
			assert(false);
		}
	}
	
	public void setIsMatched(boolean isMatched)
	{
		m_isMatched=isMatched;
	}
	
	public boolean getIsMatched()
	{
		return m_isMatched;
	}
	
	public int getTopicID()
	{
		return m_topicID;
	}
	
	public boolean canUsersMakeFriend()
	{
		return m_user1!=null&&m_user2!=null&&m_user1LikeFlag&&m_user2LikeFlag;
	}
}
