package topicfriend.server.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class TopicFriendDB
{
	private static final String DB_NAME="topicfrienddb";
	
	private static TopicFriendDB s_instance=null;
	private Connection m_dbConnection;
	
	//public interfaces
	public static synchronized TopicFriendDB getInstance()
	{
		if(s_instance==null)
		{
			s_instance=new TopicFriendDB();
			try
			{
				s_instance.initDB();
			} 
			catch (InstantiationException | IllegalAccessException
					| ClassNotFoundException | SQLException e) 
			{
				e.printStackTrace();
				s_instance=null;
				System.out.println("failed to initDB");
				assert(false);
			}
		}
		return s_instance;
	}
	
	//do not close and change the setting of the connection,remember to commit when finish execute all sql statements
	public Connection getConnection()
	{
		return m_dbConnection;
	}
	
	///////////////////////////////////////////////////
	//private methods
	private TopicFriendDB()
	{
	}
	
	private void initDB() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException
	{
		System.out.println("initDB...");
		
		Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
		//create database with name 'topicfriend'
		m_dbConnection=DriverManager.getConnection("jdbc:derby:"+DB_NAME);
		//set auto commit to false
		m_dbConnection.setAutoCommit(false);
		
		System.out.println("initDB succeed!");
	}
	
	//do not shutdown the db until you exit the application
	public void shutdownDB()
	{
		System.out.println("shutdownDB...");
		try 
		{
			DriverManager.getConnection("jdbc:derby:"+DB_NAME+";shutdown=true");
			//when the database is shutdown,it will always throw a Exception,just ignore it;
		}
		catch (SQLException e) 
		{
		}
		System.out.println("shutdownDB succeed!");
	}
	
}
