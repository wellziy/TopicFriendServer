package topicfriend.netmessage;

public class NetMessageChatRoom extends NetMessage
{
	private String m_content;
	
	////////////////////////////////
	//public
	public NetMessageChatRoom(String content)
	{
		setMessageID(NetMessageID.CHAT_ROOM);
		
		m_content=content;
	}
}
