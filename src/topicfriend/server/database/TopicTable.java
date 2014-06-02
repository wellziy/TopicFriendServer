package topicfriend.server.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TopicTable
{
	//topic id
	public static final String KEY_ID="id";
	//topic title
	public static final String KEY_TITLE="title";
	//topic description
	public static final String KEY_DESCRIPTION="description";
	
	//@return all topic information
	public static boolean getAllTopicInformation()
	{
		try 
		{
			Connection dbConn=TopicFriendDB.getInstance().getConnection();
			PreparedStatement selectStmt=dbConn.prepareStatement("select * from topic");
			ResultSet selectRes=selectStmt.executeQuery();
			while(selectRes.next())
			{
				//TODO: put the result as return value
				int id=selectRes.getInt(KEY_ID);
				String title=selectRes.getString(KEY_TITLE);
				String description=selectRes.getString(KEY_DESCRIPTION);
				System.out.println(""+id+","+title+","+description);
			}
			selectRes.close();
			
			selectStmt.close();
			dbConn.close();
			return true;
		} 
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return false;
	}
}
