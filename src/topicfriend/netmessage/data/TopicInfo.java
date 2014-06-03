package topicfriend.netmessage.data;

public class TopicInfo 
{
	private int m_id;
	private String m_title;
	private String m_description;
	
	////////////////////////////
	//public
	public TopicInfo(int id,String title,String description)
	{
		m_id=id;
		m_title=title;
		m_description=description;
	}
	
	public int getID()
	{
		return m_id;
	}
	
	public String getTitle()
	{
		return m_title;
	}
	
	public String getDescription()
	{
		return m_description;
	}
}
