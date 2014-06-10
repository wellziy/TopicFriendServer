package topicfriend.netmessage;

import topicfriend.netmessage.data.UserInfo;

public class NetMessageNewFriend extends NetMessage 
{
	private UserInfo m_newFriendInfo;
	
	//////////////////////////////////////////
	//public
	public NetMessageNewFriend(UserInfo newFriendInfo)
	{
		setMessageID(NetMessageID.NEW_FRIEND);
		m_newFriendInfo=newFriendInfo;
	}
	
	public UserInfo getNewFriendInfo()
	{
		return m_newFriendInfo;
	}
}
