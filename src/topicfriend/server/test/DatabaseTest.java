package topicfriend.server.test;

import java.sql.Timestamp;
import java.util.ArrayList;

import junit.framework.TestCase;
import topicfriend.netmessage.data.MessageInfo;
import topicfriend.netmessage.data.TopicInfo;
import topicfriend.netmessage.data.UsrInfo;
import topicfriend.server.database.FriendTable;
import topicfriend.server.database.MessageTable;
import topicfriend.server.database.TopicFriendDB;
import topicfriend.server.database.TopicTable;
import topicfriend.server.database.UsrTable;

public class DatabaseTest extends TestCase
{
	///////////////////////////////
	//helper functions
	public void printMessageList(ArrayList<MessageInfo> messageList)
	{
		System.out.println("--begin messageList");
		for(int i=0;i<messageList.size();i++)
		{
			MessageInfo info=messageList.get(i);
			int sid=info.getSenderID();
			int tid=info.getTargetID();
			Timestamp ts=info.getTimetamp();
			String content=info.getContent();
			System.out.println("sid="+sid+",tid="+tid+",ts="+ts+",content="+content);
		}
		System.out.println("--end messageList");
	}
	
	public void printUsrInfo(UsrInfo info)
	{
		System.out.println("--begin user info");
		int id=info.getID();
		int sex=info.getSex();
		String name=info.getName();
		String sig=info.getSignature();
		String icon=info.getIcon();
		System.out.println("id="+id+",sex="+sex+",name="+name+",sig="+sig+",icon="+icon);
		System.out.println("--end user info");
	}
	
	public void printFriendList(ArrayList<Integer> friendList)
	{
		System.out.println("--begin friendList");
		for(int i=0;i<friendList.size();i++)
		{
			int fid=friendList.get(i);
			System.out.print(fid+",");
		}
		System.out.println("\n--end friendList");
	}
	
	public void printTopicList(ArrayList<TopicInfo> topicList)
	{
		System.out.println("--begin topicList");
		for(int i=0;i<topicList.size();i++)
		{
			TopicInfo info=topicList.get(i);
			int id=info.getID();
			String title=info.getTitle();
			String description=info.getDescription();
			System.out.println("id="+id+",title="+title+",description="+description);
		}
		System.out.println("--end topicList");
	}
	
	///////////////////////////////////
	//test cases
	public void testInstance()
	{
		TopicFriendDB ins=TopicFriendDB.getInstance();
		assertNotNull(ins);
	}
	
	public void testMessageTable()
	{
		//test insert
		boolean selectRes=MessageTable.putUnreadMessage(0, 1, new Timestamp(System.currentTimeMillis()), "good''''' day");
		assertTrue(selectRes==true);
		
		ArrayList<MessageInfo> messageList = MessageTable.getUnreadMessage(1);
		printMessageList(messageList);
	}
	
	public void testUsrTable()
	{
		//test user name existed
		boolean isUserExisted=UsrTable.isUserNameExisted("wellziy");
		assertTrue(isUserExisted==true);
		
		//test create user
		int createUID=UsrTable.createUser(0, "user_create_by_program", "hehehe", "this is a test", "hello");
		System.out.println("createUID="+createUID);
		assertTrue(createUID>=0);
		
		//test get user info by id
		UsrTable.getUserInfoWithID(1);
		UsrInfo info=UsrTable.getUserInfoWithID(createUID);
		printUsrInfo(info);
		
		//test get user info by name
		UsrInfo info2 = UsrTable.getUserInfoWithName("wellziy");
		if(info2!=null)
		{
			printUsrInfo(info2);
		}
		
		int uid=UsrTable.getIDWithUserNameAndPassword("user_create_by_program", "hehehe");
		assertTrue(uid>=0);
	}
	
	public void testFriendTable()
	{
		boolean make01=FriendTable.makeFriend(0, 1);
		assertTrue(make01);
		
		boolean make02=FriendTable.makeFriend(0, 2);
		assertTrue(make02);
		
		ArrayList<Integer> friendList = FriendTable.getFriendList(0);
		printFriendList(friendList);
		
		boolean remove01=FriendTable.removeFriend(0, 1);
		assertTrue(remove01);
		
		boolean remove02=FriendTable.removeFriend(0, 2);
		assertTrue(remove02);
		
		ArrayList<Integer> friendList2 = FriendTable.getFriendList(0);
		printFriendList(friendList2);
	}
	
	public void testTopicTable()
	{
		ArrayList<TopicInfo> topicList = TopicTable.getAllTopicInfo();
		printTopicList(topicList);
		
		int topicID = TopicTable.createTopic("sql topic", "this is a topic created by program");
		assertTrue(topicID>=0);
		
		ArrayList<TopicInfo> topicList2 = TopicTable.getAllTopicInfo();
		printTopicList(topicList2);
		
		boolean isRemove=TopicTable.removeTopic(topicID);
		assertTrue(isRemove);
		
		ArrayList<TopicInfo> topicList3 = TopicTable.getAllTopicInfo();
		printTopicList(topicList3);
	}
}
