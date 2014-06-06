package topicfriend.netmessage;

import topicfriend.netmessage.data.UserInfo;

public class NetMessageUpdateUserInfo extends NetMessage
{
	private UserInfo m_newInfo;
	
	///////////////////
	//public
	public NetMessageUpdateUserInfo(UserInfo newInfo)
	{
		setMessageID(NetMessageID.UPDATE_USER_INFO);
		
		m_newInfo=newInfo;
	}
	
	public UserInfo getNewInfo()
	{
		return m_newInfo;
	}
}
