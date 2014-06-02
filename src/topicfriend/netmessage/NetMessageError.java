package topicfriend.netmessage;

public class NetMessageError extends NetMessage
{
	private int m_errorCode;
	private String m_errorStr;
	
	////////////////////////////////////////////////
	//public
	public NetMessageError(int errorCode,String errorStr)
	{
		setMessageID(NetMessageID.ERROR);
		
		m_errorCode=errorCode;
		m_errorStr=errorStr;
	}
	
	public int getErrorCode()
	{
		return m_errorCode;
	}
	
	public String getErrorStr()
	{
		return m_errorStr;
	}
}
