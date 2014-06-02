package topicfriend.netmessage;

public class NetMessageRigister extends NetMessage
{
	private String m_userName;
	private String m_password;
	
	public NetMessageRigister(String userName,String password)
	{
		setMessageID(NetMessageID.RIGISTER);
		
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
