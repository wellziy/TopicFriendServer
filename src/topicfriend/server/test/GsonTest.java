package topicfriend.server.test;

import junit.framework.TestCase;
import topicfriend.netmessage.NetMessage;
import topicfriend.netmessage.NetMessageError;
import topicfriend.netmessage.NetMessageLogin;

public class GsonTest extends TestCase
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
	
	public void testNetMessageLogin()
	{
		NetMessageLogin login=new NetMessageLogin("hello","world");
		String str=login.toJsonString();
		
		System.out.println(str);
		
		NetMessage msg=NetMessage.fromJsonString(str);
		assert(msg instanceof NetMessageLogin);
		System.out.println(msg.toJsonString());
	}
	
	public void testNetMessageError()
	{
		NetMessageError error=new NetMessageError(111,"the username has existed");
		String str=error.toJsonString();
		
		System.out.println(str);
		
		NetMessage msg=NetMessage.fromJsonString(str);
		assert(msg instanceof NetMessageError);
		System.out.println(msg.toJsonString());
	}
}
