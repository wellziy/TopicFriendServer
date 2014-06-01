package topicfriendserver.database;

import java.sql.Date;


//class for operate table 'message'
public class MessageTable 
{
	//the id of record
	public static final String KEY_ID="id";
	//sender uid
	public static final String KEY_SID="sid";
	//target uid
	public static final String KEY_TID="tid";
	//timestamp of the message
	public static final String KEY_TIMESTAMP="timestamp";
	//content of the message
	public static final String KEY_CONTENT="content";
	
	//@return all unread message for user with uid,and then remove them from database
	public void getUnreadMessage(int uid)
	{
		//TODO:
	}
	
	public void putUnreadMessage(int sid,int tid,Date timestamp,String content)
	{
		//TODO:
	}
}
