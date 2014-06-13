package topicfriend.server.test;

import topicfriend.network.NetworkWorkerPool;
import junit.framework.TestCase;

public class NetworkTest extends TestCase
{
	public void testPackAndUnpack()
	{
		int value=455;
		byte[] byteArr = NetworkWorkerPool.unpackInt(value);
		int packValue=NetworkWorkerPool.packInt(byteArr);
		assertTrue(packValue==value);
	}
}
