package topicfriend.server.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import topicfriend.server.Util;

//class for operate the table 'friend'
public class FriendTable 
{
	//the id of record
	public static final String KEY_ID="id";
	//my id
	public static final String KEY_MID="mid";
	//friend id
	public static final String KEY_FID="fid";
	
	//@return all friend uid for the user with uid
	public static void getFriendList(int uid)
	{
		//TODO
	}
	
	//@return if update db succeed,return true
	public static boolean makeFriend(int uid1,int uid2)
	{
		Connection dbConn=TopicFriendDB.getInstance().getConnection();
		PreparedStatement st;
		try 
		{
			st = dbConn.prepareStatement("insert into friend(mid,fid) values(?,?)");
			
			st.setInt(1, uid1);
			st.setInt(2, uid2);
			st.executeUpdate();
			
			st.setInt(1, uid2);
			st.setInt(2, uid1);
			st.executeUpdate();
			
			dbConn.commit();
		} 
		catch (SQLException e)
		{
			Util.printSQLException(e);
			return false;
		}
		
		return true;
	}
	
	//@return if remove friend succeed,return true
	public static boolean removeFriend(int uid1,int uid2)
	{
		//TODO:
		return false;
	}
}
