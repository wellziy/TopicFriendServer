package topicfriend.server.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

//class for operate the table 'user' in database
public class UsrTable
{
	//user id
	public static final String KEY_ID="id";
	//user sex
	public static final String KEY_SEX="sex";
	//user name
	public static final String KEY_NAME="name";
	//user password
	public static final String KEY_PWD="pwd";
	//user signature
	public static final String KEY_SIG="sig";
	//user icon
	public static final String KEY_ICON="icon";
	
	//@return the user name existed or not
	public static boolean isUserNameExisted(String name)
	{
		boolean isExisted=true;
		
		try
		{
			Connection dbConn=TopicFriendDB.getInstance().getConnection();
			PreparedStatement selectStmt=dbConn.prepareStatement("select id from usr where name=?");
			selectStmt.setString(1, name);
			ResultSet selectRes=selectStmt.executeQuery();
			if(selectRes.next()==false)
			{
				isExisted=false;
			}
			selectRes.close();
			
			selectStmt.close();
			dbConn.commit();
		}
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		
		return isExisted;
	}
	
	//@return the uid of the created user
	public static int createUser(int sex,String name,String password,String signature,String icon)
	{
		int resID=-1;
		
		try 
		{
			Connection dbConn=TopicFriendDB.getInstance().getConnection();
			PreparedStatement insertStmt=dbConn.prepareStatement("insert into usr(sex,name,pwd,sig,icon) values(?,?,?,?,?)",new String[]{"id"});
			insertStmt.setInt(1, sex);
			insertStmt.setString(2, name);
			insertStmt.setString(3, password);
			insertStmt.setString(4, signature);
			insertStmt.setString(5, icon);
			insertStmt.executeUpdate();
			
			ResultSet genKeys=insertStmt.getGeneratedKeys();
			if(genKeys.next())
			{
				resID=genKeys.getInt("id");
			}
			genKeys.close();
			
			insertStmt.close();
			dbConn.commit();
		}
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		
		return resID;
	}
	
	//@return the name,sig,icon information for user with uid
	public static boolean getUserInformation(int uid)
	{
		try
		{
			Connection dbConn=TopicFriendDB.getInstance().getConnection();
			PreparedStatement selectStmt=dbConn.prepareStatement("select sex,name,sig,icon from usr where id=?");
			selectStmt.setInt(1, uid);
			ResultSet selectRes=selectStmt.executeQuery();
			if(selectRes.next())
			{
				//TODO: put the result as return value
				int sex=selectRes.getInt("sex");
				String name=selectRes.getString("name");
				String sig=selectRes.getString("sig");
				String icon=selectRes.getString("icon");
				System.out.println(""+sex+","+name+","+sig+","+icon);
			}
			selectRes.close();
			
			selectStmt.close();
			dbConn.commit();
			return true;
		}
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		return false;
	}
}
