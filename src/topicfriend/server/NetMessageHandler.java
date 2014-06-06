package topicfriend.server;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import topicfriend.netmessage.NetMessage;
import topicfriend.netmessage.NetMessageChatFriend;
import topicfriend.netmessage.NetMessageChatRoom;
import topicfriend.netmessage.NetMessageError;
import topicfriend.netmessage.NetMessageLogin;
import topicfriend.netmessage.NetMessageLoginSucceed;
import topicfriend.netmessage.NetMessageRegister;
import topicfriend.netmessage.NetMessageUpdateUserInfo;
import topicfriend.netmessage.NetMessageUpdateUserInfoSucceed;
import topicfriend.netmessage.data.MessageInfo;
import topicfriend.netmessage.data.TopicInfo;
import topicfriend.netmessage.data.UserInfo;
import topicfriend.network.Network;
import topicfriend.server.database.FriendTable;
import topicfriend.server.database.MessageTable;
import topicfriend.server.database.TopicTable;
import topicfriend.server.database.UsrTable;

public class NetMessageHandler
{
	//user management
	//store all connection has not login
	private HashSet<Integer> m_unloginConnections;
	//store pair<connection,OnlineUser>
	private HashMap<Integer,OnlineUser> m_loginConnectionMap;
	//store pair<uid,OnlineUser>
	private HashMap<Integer,OnlineUser> m_loginUidMap;
	
	//room management
	//store pair<topicid,uid>,means a user with uid is waiting for join topic with topicid
	private HashMap<Integer,Integer> m_unmatchTopicUidMap;
	//store all matched room
	private HashSet<TopicRoom> m_topicRooms;
	
	public NetMessageHandler()
	{
		m_unloginConnections=new HashSet<>();
		m_loginConnectionMap=new HashMap<>();
		m_loginUidMap=new HashMap<>();
		m_unmatchTopicUidMap=new HashMap<>();
		m_topicRooms=new HashSet<>();
		
		//TODO:
		//load the topic from database
	}
	
	public void handleMessage(Integer connection,NetMessage msg)
	{
		//the user if not logined
		if(m_unloginConnections.contains(connection))
		{
			if(msg instanceof NetMessageRegister)
			{
				handleRegister(connection, msg);
			}
			else if(msg instanceof NetMessageLogin)
			{
				handleLogin(connection,msg);
			}
			else
			{
				Network.makeBadConnection(connection);
			}
			return;
		}
		
		OnlineUser user=m_loginConnectionMap.get(connection);
		assert(user!=null);
		
		//handle net message chat room
		if(msg instanceof NetMessageChatRoom)
		{
			TopicRoom r=user.getTopicRoom();
			if(r==null)
			{
				Network.makeBadConnection(connection);
				return;
			}
			OnlineUser oppositeUser=r.getUserNotMe(user);
			Network.sendDataOne(msg.toByteArrayBuffer(), oppositeUser.getConnection());
			return;
		}
		
		//handle net message chat friend
		if(msg instanceof NetMessageChatFriend)
		{
			NetMessageChatFriend msgChatFriend=(NetMessageChatFriend)msg;
			//NOTICE: the user with tid may not be the friend of sender...but for simplicity,there is no check for that case
			int fid=msgChatFriend.getFriendID();
			String content=msgChatFriend.getContent();
			OnlineUser friend=m_loginUidMap.get(fid);
			
			if(friend==null)
			{
				//the friend is offline now
				MessageTable.putUnreadMessage(user.getUID(), fid, new Timestamp(System.currentTimeMillis()), content);
				return;
			}
			
			NetMessageChatFriend msgChatFriendSend=new NetMessageChatFriend(fid, content);
			Network.sendDataOne(msgChatFriendSend.toByteArrayBuffer(), friend.getConnection());
			return;
		}
		
		//handle update user info
		if(msg instanceof NetMessageUpdateUserInfo)
		{
			NetMessageUpdateUserInfo msgUpdateUserInfo=(NetMessageUpdateUserInfo)msg;
			UserInfo newInfo=msgUpdateUserInfo.getNewInfo();
			boolean isUpdateSucceed=UsrTable.updateUserInfo(user.getUID(), newInfo.getSex(), newInfo.getName(), newInfo.getSignature(), newInfo.getIcon());
			if(isUpdateSucceed)
			{
				NetMessageUpdateUserInfoSucceed msgUpdateUserInfoSucceed=new NetMessageUpdateUserInfoSucceed();
				Network.sendDataOne(msgUpdateUserInfoSucceed.toByteArrayBuffer(), connection);
				return;
			}
			else
			{
				NetMessageError error=new NetMessageError(0,"failed to update user info");
				Network.sendDataOne(error.toByteArrayBuffer(), connection);
				return;
			}
		}
		
		//TDOO: handle all the message here,but now it just echo the message back to the client
//		Network.sendDataOne(msg.toByteArrayBuffer(),connection);
	}

	public void handleNewConnection(Integer connection)
	{
		m_unloginConnections.add(connection);
	}
	
	public void handleBadConnection(Integer connection)
	{
		if(m_unloginConnections.contains(connection))
		{
			m_unloginConnections.remove(connection);
			return;
		}
		
		OnlineUser usr=m_loginConnectionMap.get(connection);
		Integer uid=usr.getUID();
		
		//remove from login connection map
		m_loginConnectionMap.remove(connection);
		//remove from login uid map
		m_loginUidMap.remove(uid);
		//remove from unmatch topic uid map
		Iterator<Entry<Integer, Integer>> iter = m_unmatchTopicUidMap.entrySet().iterator();
		while(iter.hasNext())
		{
			Entry<Integer, Integer> entry = iter.next();
			if(entry.getValue().equals(uid))
			{
				iter.remove();
				break;
			}
		}
		
		//remove from the room
		TopicRoom r=usr.getTopicRoom();
		if(r!=null)
		{
			r.removeUser(usr);
		}
		
		//if the room is empty,remove it from topic rooms
		if(r.isEmpty())
		{
			m_topicRooms.remove(r);
		}
	}
	
	////////////////////////////////////
	//private
	private void handleRegister(Integer connection,NetMessage msg)
	{
		NetMessageRegister msgRegister=(NetMessageRegister)msg;
		String name=msgRegister.getUserName();
		String password=msgRegister.getPassword();
		int sex=msgRegister.getSex();
		
		//validate the name and password here
		if(UsrTable.isUserNameExisted(name))
		{
			NetMessageError error=new NetMessageError(0, "the user name has been existed");
			Network.sendDataOne(error.toByteArrayBuffer(), connection);
			return;
		}
		
		//create the user and update server state
		String signatureDefault="test_signature";
		String iconDefault="test_icon.png";
		int uid=UsrTable.createUser(sex, name, password, signatureDefault, iconDefault);
		if(uid<0)
		{
			NetMessageError error=new NetMessageError(0,"create user failed");
			Network.sendDataOne(error.toByteArrayBuffer(), connection);
			return;
		}
		
		//login succeed,send self info , friend list and info, unread message back to client(need topic list???)
		NetMessageLoginSucceed msgLoginSucceed=makeNetMessageLoginSucceed(uid);
		if(msgLoginSucceed==null)
		{
			NetMessageError error=new NetMessageError(0,"create login succeed message failed");
			Network.sendDataOne(error.toByteArrayBuffer(), connection);
			return;
		}
		
		makeUserLoginSucceed(connection,uid);
		//send login succeed message
		Network.sendDataOne(msgLoginSucceed.toByteArrayBuffer(), connection);
	}
	
	private void handleLogin(Integer connection, NetMessage msg)
	{
		NetMessageLogin msgLogin=(NetMessageLogin)msg;
		String name=msgLogin.getUserName();
		String password=msgLogin.getPassword();
		
		//validate the name and password here
		int uid=UsrTable.getIDWithUserNameAndPassword(name, password);
		if(uid<0)
		{
			NetMessageError error=new NetMessageError(0, "user name and password does not matched");
			Network.sendDataOne(error.toByteArrayBuffer(), connection);
			return;
		}
		
		//login succeed,send self info,friend list and info,unread message back to client(need topic list???)
		NetMessageLoginSucceed msgLoginSucceed=makeNetMessageLoginSucceed(uid);
		if(msgLoginSucceed==null)
		{
			NetMessageError error=new NetMessageError(0,"create login succeed message failed");
			Network.sendDataOne(error.toByteArrayBuffer(), connection);
			return;
		}
		
		makeUserLoginSucceed(connection,uid);
		//send login succeed message
		Network.sendDataOne(msgLoginSucceed.toByteArrayBuffer(), connection);
	}
	
	private NetMessageLoginSucceed makeNetMessageLoginSucceed(Integer uid)
	{
		//create my info
		UserInfo myInfo=UsrTable.getUserInfoWithID(uid);
		if(myInfo==null)
		{
			return null;
		}
		
		ArrayList<Integer> friendList=FriendTable.getFriendList(uid);
		if(friendList==null)
		{
			return null;
		}
		
		//create fried info list
		ArrayList<UserInfo> friendInfoList=new ArrayList<UserInfo>();
		for(int i=0;i<friendList.size();i++)
		{
			UserInfo friendInfo=UsrTable.getUserInfoWithID(friendList.get(i));
			friendInfoList.add(friendInfo);
		}
		
		//create unread message list
		ArrayList<MessageInfo> unreadMessageList=MessageTable.getUnreadMessage(uid);
		if(unreadMessageList==null)
		{
			return null;
		}
		
		//create my unread message
		ArrayList<TopicInfo> topicList=TopicTable.getAllTopicInfo();
		if(topicList==null)
		{
			return null;
		}
		
		return new NetMessageLoginSucceed(myInfo,friendInfoList,unreadMessageList,topicList);
	}
	
	public void makeUserLoginSucceed(Integer connection,Integer uid)
	{
		m_unloginConnections.remove(uid);
		OnlineUser usr=new OnlineUser(connection, uid);
		m_loginUidMap.put(uid, usr);
		m_loginConnectionMap.put(connection,usr);
	}
}
