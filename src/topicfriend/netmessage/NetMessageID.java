package topicfriend.netmessage;

//store all net message id here
public class NetMessageID
{
	public static final int UNKNOWN=0;
	public static final int LOGIN=1;
	public static final int ERROR=2;
	public static final int REGISTER=3;
	public static final int LOGIN_SUCCEED=4;
	public static final int CHAT_ROOM=5;
	public static final int CHAT_FRIEND=6;
	public static final int UPDATE_USER_INFO=7;
	public static final int UPDATE_USER_INFO_SUCCEED=8;
	public static final int LIKE=9;
	public static final int NEW_FRIEND=10;
	public static final int JOIN_TOPIC=11;
	public static final int MATCH_SUCCEED=13;
	public static final int LEAVE_ROOM=14;
	//TODO: add more message id here
	//NOTICE: remember register the id in the factory and set the id in NetMessage constructor
}
