package topicfriend.netmessage;

import topicfriend.netmessage.data.UserInfo;

public class NetMessageMatchSucceed extends NetMessage
{
	private UserInfo m_matchedUserInfo;
	
	/////////////////////////////////
	//public
	public NetMessageMatchSucceed(UserInfo matchedUserInfo)
	{
		setMessageID(NetMessageID.MATCH_SUCCEED);
		m_matchedUserInfo=matchedUserInfo;
	}
	
	public UserInfo getMatchedUserInfo()
	{
		return m_matchedUserInfo;
	}
}
