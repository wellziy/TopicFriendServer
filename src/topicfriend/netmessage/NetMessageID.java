package topicfriend.netmessage;

//store all net message id here
public class NetMessageID
{
	public static final int UNKNOWN=0;
	public static final int LOGIN=1;
	public static final int ERROR=2;
	public static final int REGISTER=3;
	//TODO: add more message id here
	//NOTICE: remember register the id in the factory and set the id in NetMessage constructor
}
