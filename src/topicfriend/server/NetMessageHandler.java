package topicfriend.server;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import topicfriend.netmessage.NetMessage;
import topicfriend.netmessage.NetMessageChatFriend;
import topicfriend.netmessage.NetMessageChatRoom;
import topicfriend.netmessage.NetMessageError;
import topicfriend.netmessage.NetMessageJoinTopic;
import topicfriend.netmessage.NetMessageLeaveRoom;
import topicfriend.netmessage.NetMessageLike;
import topicfriend.netmessage.NetMessageLogin;
import topicfriend.netmessage.NetMessageLoginSucceed;
import topicfriend.netmessage.NetMessageMatchSucceed;
import topicfriend.netmessage.NetMessageNewFriend;
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
	//store pair<topicid,TopicRoom>
	private HashMap<Integer,TopicRoom> m_unmatchTopicIDMap;
	//store all matched room
	private HashSet<TopicRoom> m_matchedRooms;
	
	public NetMessageHandler()
	{
		m_unloginConnections=new HashSet<Integer>();
		m_loginConnectionMap=new HashMap<Integer, OnlineUser>();
		m_loginUidMap=new HashMap<Integer, OnlineUser>();
		m_unmatchTopicIDMap=new HashMap<Integer, TopicRoom>();
		m_matchedRooms=new HashSet<TopicRoom>();
		
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
		
		handleMessageOnline(connection, msg);
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
		
		OnlineUser user=m_loginConnectionMap.get(connection);
		Integer uid=user.getUID();
		
		//remove from login connection map
		m_loginConnectionMap.remove(connection);
		//remove from login uid map
		m_loginUidMap.remove(uid);
		
		//remove from the room
		TopicRoom room=user.getTopicRoom();
		if(room!=null)
		{
			room.removeUser(user);
			if(room.getIsMatched())
			{
				OnlineUser oppositeUser=room.getUserNotMe(user);
				
				if(oppositeUser!=null)
				{
					//notify the opposite user if the other one is still here
					NetMessageLeaveRoom msgLeaveRoom=new NetMessageLeaveRoom();
					Network.sendDataOne(msgLeaveRoom.toByteArrayBuffer(), oppositeUser.getConnection());
				}
				else
				{
					//there is no others in the room,just remove it from matched rooms
					m_matchedRooms.remove(room);
				}
			}
			else
			{
				//remove the room from unmatched room map
				m_unmatchTopicIDMap.remove(room.getTopicID());
			}
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
		
		//can not login twice
		OnlineUser loginedUser=m_loginUidMap.get(uid);
		if(loginedUser!=null)
		{
			NetMessageError error=new NetMessageError(0, "this account has been logined in somewhere else");
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
	
	private void makeUserLoginSucceed(Integer connection,Integer uid)
	{
		m_unloginConnections.remove(connection);
		OnlineUser usr=new OnlineUser(connection, uid);
		m_loginUidMap.put(uid, usr);
		m_loginConnectionMap.put(connection,usr);
	}
	
	private void handleMessageOnline(Integer connection,NetMessage msg)
	{
		//find the logined user
		OnlineUser user=m_loginConnectionMap.get(connection);
		TopicRoom room=user.getTopicRoom();
		assert(user!=null);
		
		//handle net message in room
		if(room!=null)
		{
			OnlineUser oppositeUser=room.getUserNotMe(user);
			
			if(msg instanceof NetMessageLeaveRoom)
			{
				room.removeUser(user);
				if(room.getIsMatched())
				{
					if(oppositeUser!=null)
					{
						//notify the opposite user if the other one is still here
						NetMessageLeaveRoom msgLeaveRoom=new NetMessageLeaveRoom();
						Network.sendDataOne(msgLeaveRoom.toByteArrayBuffer(), oppositeUser.getConnection());
					}
					else
					{
						//there is no others in the room,just remove it from matched rooms
						m_matchedRooms.remove(room);
					}
				}
				else
				{
					//remove the room from unmatched room map
					m_unmatchTopicIDMap.remove(room.getTopicID());
				}
				return;
			}
			
			//handle chat message
			//NOTICE:it allow the user to send message even the oppositeUser is null(it happened when the oppositeUser finished talk,and exit the room)
			if(msg instanceof NetMessageChatRoom)
			{
				if(oppositeUser!=null)
				{
					Network.sendDataOne(msg.toByteArrayBuffer(), oppositeUser.getConnection());
				}
				return;
			}
			
			//only the room has matched,you can send NetMessageLike
			if(room.getIsMatched()&&msg instanceof NetMessageLike)
			{
				room.setUserLikeFlag(user);
				if(room.canUsersMakeFriend()&&oppositeUser!=null)
				{
					boolean isFriend=FriendTable.isFriend(user.getUID(), oppositeUser.getUID());
					if(!isFriend)
					{
						boolean isMakeFriendSucceed=FriendTable.makeFriend(user.getUID(), oppositeUser.getUID());
						if(isMakeFriendSucceed)
						{
							//make friend succeed, send new friend message here
							UserInfo friendInfo1=UsrTable.getUserInfoWithID(user.getUID());
							UserInfo friendInfo2=UsrTable.getUserInfoWithID(oppositeUser.getUID());
							NetMessageNewFriend msgNewFriend1=new NetMessageNewFriend(friendInfo1);
							NetMessageNewFriend msgNewFriend2=new NetMessageNewFriend(friendInfo2);
							
							Network.sendDataOne(msgNewFriend1.toByteArrayBuffer(), oppositeUser.getConnection());
							Network.sendDataOne(msgNewFriend2.toByteArrayBuffer(), user.getConnection());
							return;
						}
						else
						{
							//create friend failed,send error message here
							NetMessageError error=new NetMessageError(0, "database error when make friend");
							Network.sendDataOne(error.toByteArrayBuffer(), connection);
							return;
						}
					}
					else
					{
						//they are already friends,send error message back
						NetMessageError error=new NetMessageError(0, "you are already friend");
						Network.sendDataOne(error.toByteArrayBuffer(), connection);
						return;
					}
				}
				//can not make friend now,just return;
				return;
			}
			
			//invalid message
			Network.makeBadConnection(connection);
			return;
		}

		if(msg instanceof NetMessageJoinTopic)
		{
			NetMessageJoinTopic msgJoinTopic=(NetMessageJoinTopic)msg;
			int topicID=msgJoinTopic.getTopicID();
			TopicRoom waitingRoom=m_unmatchTopicIDMap.get(topicID);
			
			if(waitingRoom!=null)
			{
				//match succeed
				waitingRoom.addUser(user);
				user.setTopicRoom(waitingRoom);
				OnlineUser oppositeUser=waitingRoom.getUserNotMe(user);
				
				assert(waitingRoom.isFull()==true);
				waitingRoom.setIsMatched(true);
				
				//remove the room from unmatched room map
				m_unmatchTopicIDMap.remove(topicID);
				m_matchedRooms.add(waitingRoom);
				
				UserInfo info1=UsrTable.getUserInfoWithID(user.getUID());
				UserInfo info2=UsrTable.getUserInfoWithID(oppositeUser.getUID());
				assert(info1!=null&&info2!=null);
				
				NetMessageMatchSucceed msgMatchSucceed1=new NetMessageMatchSucceed(info1);
				NetMessageMatchSucceed msgMatchSucceed2=new NetMessageMatchSucceed(info2);
				
				Network.sendDataOne(msgMatchSucceed1.toByteArrayBuffer(), oppositeUser.getConnection());
				Network.sendDataOne(msgMatchSucceed2.toByteArrayBuffer(), user.getConnection());
				return;
			}
			else
			{
				TopicRoom newRoom=new TopicRoom(topicID);
				newRoom.addUser(user);
				user.setTopicRoom(newRoom);
				
				m_unmatchTopicIDMap.put(newRoom.getTopicID(), newRoom);
				return;
			}
		}
		
		//handle net message chat friend
		//NOTICE: there is no check if the user is now in a chat room, if it is,it can not send NetMessageChatFriend
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
}
