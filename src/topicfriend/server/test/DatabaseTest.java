package topicfriend.server.test;

import topicfriend.server.database.FriendTable;
import topicfriend.server.database.TopicFriendDB;
import junit.framework.TestCase;

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
}
