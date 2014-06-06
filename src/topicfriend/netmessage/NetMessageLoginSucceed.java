package topicfriend.netmessage;

import java.util.ArrayList;

import topicfriend.netmessage.data.MessageInfo;
import topicfriend.netmessage.data.TopicInfo;
import topicfriend.netmessage.data.UserInfo;

public class NetMessageLoginSucceed extends NetMessage
{
	private UserInfo m_myInfo;
	private ArrayList<UserInfo> m_friendInfoList;
	private ArrayList<MessageInfo> m_unreadMessageList;
	private ArrayList<TopicInfo> m_topicList;
	
	public NetMessageLoginSucceed(UserInfo myInfo,ArrayList<UserInfo> friendInfos,ArrayList<MessageInfo> unreadMessages,ArrayList<TopicInfo> topicList)
	{
		setMessageID(NetMessageID.LOGIN_SUCCEED);
		
		m_myInfo=myInfo;
		m_friendInfoList=friendInfos;
		m_unreadMessageList=unreadMessages;
		m_topicList=topicList;
	}
	
	public UserInfo getMyInfo()
	{
		return m_myInfo;
	}
	
	public ArrayList<UserInfo> getFriendInfoList()
	{
		return m_friendInfoList;
	}
	
	public ArrayList<MessageInfo> getUnreadMessageList()
	{
		return m_unreadMessageList;
	}
	
	public ArrayList<TopicInfo> getTopicList()
	{
		return m_topicList;
	}
}
