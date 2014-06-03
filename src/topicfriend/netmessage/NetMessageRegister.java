package topicfriend.netmessage;

public class NetMessageRegister extends NetMessage
{
	private String m_userName;
	private String m_password;
	
	public NetMessageRegister(String userName,String password)
	{
		setMessageID(NetMessageID.REGISTER);
		
		m_userName=userName;
		m_password=password;
	}
	
	public String getUserName()
	{
		return m_userName;
	}
	
	public String getPassword()
	{
		return m_password;
	}
}
