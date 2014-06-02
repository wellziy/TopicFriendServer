package topicfriend.netmessage;

public class NetMessageLogin extends NetMessage
{
	private String m_userName;
	private String m_password;
	
	/////////////////////////////////////////////
	//public
	public NetMessageLogin(String userName,String password)
	{
		setMessageID(NetMessageID.LOGIN);
		
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
