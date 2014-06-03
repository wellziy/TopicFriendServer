package topicfriend.netmessage.data;

import java.sql.Timestamp;

public class MessageInfo 
{
	private int m_sid;
	private int m_tid;
	private Timestamp m_ts;
	private String m_content;
	
	///////////////////////////////
	//public
	public MessageInfo(int sid,int tid,Timestamp ts,String content)
	{
		m_sid=sid;
		m_tid=tid;
		m_ts=ts;
		m_content=content;
	}
	
	public int getSenderID()
	{
		return m_sid;
	}
	
	public int getTargetID()
	{
		return m_tid;
	}
	
	public String getContent()
	{
		return m_content;
	}
	
	public Timestamp getTimetamp()
	{
		return m_ts;
	}
}
