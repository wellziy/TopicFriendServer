package topicfriend.netmessage;

public class NetMessageRegister extends NetMessage
{
	private String m_userName;
	private String m_password;
	private int m_sex;
	
	public NetMessageRegister(String userName,String password,int sex)
	{
		setMessageID(NetMessageID.REGISTER);
		
		m_userName=userName;
		m_password=password;
		m_sex=sex;
	}
	
	public String getUserName()
	{
		return m_userName;
	}
	
	public String getPassword()
	{
		return m_password;
	}
	
	public int getSex()
	{
		return m_sex;
	}
}
