package topicfriendserver.test;

import topicfriendserver.database.TopicFriendDB;
import junit.framework.TestCase;

public class DatabaseTest extends TestCase
{
	@Override
	protected void setUp() throws Exception 
	{
		super.setUp();
	}
	
	@Override
	protected void tearDown() throws Exception 
	{
		super.tearDown();
	}
	
	public void testInstance()
	{
		TopicFriendDB ins=TopicFriendDB.getInstance();
		assertNotNull(ins);
		TopicFriendDB.getInstance().shutdownDB();
	}
}
