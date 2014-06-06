package topicfriend.netmessage;

import java.util.HashMap;

public class NetMessageFactory
{
	private static NetMessageFactory s_instance=null;
	
	private HashMap<Integer,Class<? extends NetMessage>> m_IDClassMap;
	
	//register id to the factory here
	//NOTICE: must register all the message you need here
	static
	{
		NetMessageFactory f=NetMessageFactory.getInstance();
		f.addNetMessageClass(NetMessageID.LOGIN, NetMessageLogin.class);
		f.addNetMessageClass(NetMessageID.ERROR, NetMessageError.class);
		f.addNetMessageClass(NetMessageID.REGISTER, NetMessageRegister.class);
		f.addNetMessageClass(NetMessageID.LOGIN_SUCCEED, NetMessageLoginSucceed.class);
		f.addNetMessageClass(NetMessageID.CHAT_ROOM, NetMessageChatRoom.class);
		f.addNetMessageClass(NetMessageID.CHAT_FRIEND, NetMessageChatFriend.class);
		f.addNetMessageClass(NetMessageID.UPDATE_USER_INFO, NetMessageUpdateUserInfo.class);
		f.addNetMessageClass(NetMessageID.UPDATE_USER_INFO_SUCCEED, NetMessageUpdateUserInfoSucceed.class);
	}
	
	//public interfaces
	public static NetMessageFactory getInstance()
	{
		if(s_instance==null)
		{
			s_instance=new NetMessageFactory();
		}
		return s_instance;
	}
	
	public void addNetMessageClass(Integer id,Class<? extends NetMessage> c)
	{
		assert(m_IDClassMap.containsKey(id)==false
				&&id.equals(NetMessageID.UNKNOWN)==false);
		
		m_IDClassMap.put(id, c);
	}
	
	public Class<? extends NetMessage> getMessageClass(Integer id)
	{
		Class<? extends NetMessage> c=m_IDClassMap.get(id);
		assert(c!=null);
		return c;
	}
	
	//////////////////////////////////////////
	//private methods
	private NetMessageFactory()
	{
		m_IDClassMap=new HashMap<>();
	}
}
