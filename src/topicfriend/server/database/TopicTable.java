package topicfriend.server.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import topicfriend.netmessage.data.TopicInfo;
import topicfriend.server.Util;

public class TopicTable
{
	//topic id
	public static final String KEY_ID="id";
	//topic title
	public static final String KEY_TITLE="title";
	//topic description
	public static final String KEY_DESCRIPTION="description";
	
	//@return all topic information
	public static ArrayList<TopicInfo> getAllTopicInfo()
	{
		ArrayList<TopicInfo> topicList=new ArrayList<TopicInfo>();
		
		try 
		{
			Connection dbConn=TopicFriendDB.getInstance().getConnection();
			PreparedStatement selectStmt=dbConn.prepareStatement("select * from topic");
			ResultSet selectRes=selectStmt.executeQuery();
			while(selectRes.next())
			{
				int id=selectRes.getInt(KEY_ID);
				String title=selectRes.getString(KEY_TITLE);
				String description=selectRes.getString(KEY_DESCRIPTION);
				
				topicList.add(new TopicInfo(id, title, description));
			}
			selectRes.close();
			
			selectStmt.close();
			dbConn.commit();
			return topicList;
		} 
		catch (SQLException e)
		{
			Util.printSQLException(e);
		}
		
		return null;
	}
	
	public static int createTopic(String title,String description)
	{
		try 
		{
			Connection dbConn=TopicFriendDB.getInstance().getConnection();
			PreparedStatement insertStmt=dbConn.prepareStatement("insert into topic(title,description) values(?,?)", new int[]{1});
			insertStmt.setString(1, title);
			insertStmt.setString(2, description);
			insertStmt.executeUpdate();
			
			ResultSet genKeys = insertStmt.getGeneratedKeys();
			int topicID=-1;
			if(genKeys.next())
			{
				topicID=genKeys.getInt(1);
			}
			genKeys.close();
			
			insertStmt.close();
			dbConn.commit();
			return topicID;
		}
		catch (SQLException e) 
		{
			Util.printSQLException(e);
		}
		
		return -1;
	}
	
	public static boolean removeTopic(int id)
	{
		try 
		{
			Connection dbConn=TopicFriendDB.getInstance().getConnection();
			PreparedStatement deleteStmt=dbConn.prepareStatement("delete from topic where id=?");
			deleteStmt.setInt(1, id);
			deleteStmt.executeUpdate();
			
			deleteStmt.close();
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
