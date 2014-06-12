package topicfriend.server.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

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
	public static ArrayList<Integer> getFriendList(int uid)
	{
		ArrayList<Integer> friendList=new ArrayList<Integer>();
		
		try
		{
			Connection dbConn=TopicFriendDB.getInstance().getConnection();
			PreparedStatement selectStmt = dbConn.prepareStatement("select fid from friend where mid=?");
			selectStmt.setInt(1, uid);
			ResultSet selectRes = selectStmt.executeQuery();
			while(selectRes.next())
			{
				int fid=selectRes.getInt(KEY_FID);

				friendList.add(fid);
			}
			selectRes.close();
			
			selectStmt.close();
			dbConn.commit();
			return friendList;
		} 
		catch (SQLException e)
		{
			Util.printSQLException(e);
		}
		return null;
	}
	
	//@return if update db succeed,return true
	public static boolean makeFriend(int uid1,int uid2)
	{
		try 
		{
			Connection dbConn=TopicFriendDB.getInstance().getConnection();
			PreparedStatement insertStmt = dbConn.prepareStatement("insert into friend(mid,fid) values(?,?)");
			
			insertStmt.setInt(1, uid1);
			insertStmt.setInt(2, uid2);
			insertStmt.executeUpdate();
			
			insertStmt.setInt(1, uid2);
			insertStmt.setInt(2, uid1);
			insertStmt.executeUpdate();
			
			//close all statement and commit
			insertStmt.close();
			dbConn.commit();
			return true;
		} 
		catch (SQLException e)
		{
			Util.printSQLException(e);
		}
		
		return false;
	}
	
	//@return if remove friend succeed,return true
	public static boolean removeFriend(int uid1,int uid2)
	{
		try
		{
			Connection dbConn=TopicFriendDB.getInstance().getConnection();
			PreparedStatement deleteStmt=dbConn.prepareStatement("delete from friend where (mid=? and fid=?) or (mid=? and fid=?)");
			deleteStmt.setInt(1, uid1);
			deleteStmt.setInt(2, uid2);
			deleteStmt.setInt(3, uid2);
			deleteStmt.setInt(4, uid1);
			deleteStmt.executeUpdate();
			
			deleteStmt.close();
			dbConn.commit();
			return true;
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}

		return false;
	}
	
	public static boolean isFriend(int uid1,int uid2)
	{
		try
		{
			Connection dbConn=TopicFriendDB.getInstance().getConnection();
			PreparedStatement selectStmt = dbConn.prepareStatement("select id from friend where mid=? and fid=?");
			selectStmt.setInt(1, uid1);
			selectStmt.setInt(2, uid2);
			ResultSet selectRes = selectStmt.executeQuery();
			boolean res=false;
			if(selectRes.next())
			{
				res=true;
			}
			selectRes.close();
			
			selectStmt.close();
			dbConn.commit();
			return res;
		} 
		catch (SQLException e)
		{
			Util.printSQLException(e);
		}
		
		return false;
	}
}
