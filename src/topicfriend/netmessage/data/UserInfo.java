package topicfriend.netmessage.data;

public class UserInfo 
{
	public static final int SEX_MALE=0;
	public static final int SEX_FEMALE=1;
	
	private int m_id;
	private int m_sex;
	private String m_name;
	private String m_sig;
	private String m_icon;
	
	//////////////////////////////////
	//public
	public UserInfo(int id,int sex,String name,String sig,String icon)
	{
		m_id=id;
		m_sex=sex;
		m_name=name;
		m_sig=sig;
		m_icon=icon;
	}
	
	public int getID()
	{
		return m_id;
	}
	
	public int getSex()
	{
		return m_sex;
	}
	
	public String getName()
	{
		return m_name;
	}
	
	public String getSignature()
	{
		return m_sig;
	}
	
	public String getIcon()
	{
		return m_icon;
	}
}
