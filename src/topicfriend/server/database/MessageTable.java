package topicfriend.server.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import topicfriend.server.Util;


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
	public static final String KEY_TIMESTAMP="ts";
	//content of the message
	public static final String KEY_CONTENT="content";
	
	//@return all unread message for user with uid,and then remove them from database
	public static boolean getUnreadMessage(int uid)
	{
		try
		{
			Connection dbConn=TopicFriendDB.getInstance().getConnection();
			
			//select
			PreparedStatement selectSt=dbConn.prepareStatement("select sid,ts,content from message where tid=?");
			selectSt.setInt(1, uid);
			ResultSet selectRes=selectSt.executeQuery();
			while(selectRes.next())
			{
				int sid=selectRes.getInt("sid");
				Timestamp ts=selectRes.getTimestamp("ts");
				String content=selectRes.getString("content");
				//TODO: save them as return value
				System.out.println(sid+","+ts+","+content);
			}
			//close the result set if no use it any more
			selectRes.close();
			
			//delete
			PreparedStatement deleteSt=dbConn.prepareStatement("delete from message where tid=?");
			deleteSt.setInt(1, uid);
			deleteSt.executeUpdate();
			
			//close all statement and commit
			selectSt.close();
			deleteSt.close();
			dbConn.commit();
			return true;
		} 
		catch (SQLException e) 
		{
			Util.printSQLException(e);
		}
		return false;
	}
	
	public static boolean putUnreadMessage(int sid,int tid,Timestamp ts,String content)
	{
		try 
		{
			Connection dbConn=TopicFriendDB.getInstance().getConnection();
			PreparedStatement st=dbConn.prepareStatement("insert into message(sid,tid,ts,content) values(?,?,?,?)");
			st.setInt(1, sid);
			st.setInt(2, tid);
			st.setTimestamp(3, ts);
			st.setString(4, content);
			st.executeUpdate();
			
			st.close();
			dbConn.commit();
			return true;
		} 
		catch (SQLException e) 
		{
			Util.printSQLException(e);
		}
		return false;
	}
}
