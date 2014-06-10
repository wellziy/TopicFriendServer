package topicfriend.netmessage;

public class NetMessageJoinTopic extends NetMessage
{
	private int m_topicID;
	
	///////////////////////////////
	//public
	public NetMessageJoinTopic(int topicID)
	{
		setMessageID(NetMessageID.JOIN_TOPIC);
		m_topicID=topicID;
	}
	
	public int getTopicID()
	{
		return m_topicID;
	}
}
