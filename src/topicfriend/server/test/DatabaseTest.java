package topicfriend.server.test;

import java.sql.Timestamp;

import junit.framework.TestCase;
import topicfriend.server.database.FriendTable;
import topicfriend.server.database.MessageTable;
import topicfriend.server.database.TopicFriendDB;
import topicfriend.server.database.UsrTable;

public class DatabaseTest extends TestCase
{
	public void testInstance()
	{
		TopicFriendDB ins=TopicFriendDB.getInstance();
		assertNotNull(ins);
	}
	
	public void testFriendTable()
	{
		boolean res=FriendTable.makeFriend(100, 200);
		assertTrue(res==true);
	}
	
	public void testMessageTable()
	{
		//test insert
		boolean selectRes=MessageTable.putUnreadMessage(0, 1, new Timestamp(System.currentTimeMillis()), "good''''' day");
		assertTrue(selectRes==true);
		
		boolean deleteRes=MessageTable.getUnreadMessage(1);
		assertTrue(deleteRes==true);
	}
	
	public void testUsrTable()
	{
		boolean isUserExisted=UsrTable.isUserNameExisted("wellziy");
		assertTrue(isUserExisted==true);
	}
}
