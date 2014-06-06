package topicfriend.netmessage;

public class NetMessageChatFriend extends NetMessage
{
	private int m_fid;
	private String m_content;
	
	//////////////////////////////
	//public
	public NetMessageChatFriend(int fid,String content)
	{
		setMessageID(NetMessageID.CHAT_FRIEND);
		
		m_fid=fid;
		m_content=content;
	}
	
	public int getFriendID()
	{
		return m_fid;
	}
	
	public String getContent()
	{
		return m_content;
	}
}
