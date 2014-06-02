package topicfriend.server;

import java.sql.SQLException;

public class Util 
{
	// ## DERBY EXCEPTION REPORTING CLASSES ##
	// Exception reporting methods with special handling of SQLExceptions
	public static void printError(Throwable e) 
	{
		if (e instanceof SQLException)
		{
			printSQLException((SQLException) e);
		} 
		else 
		{
			System.out.println("A non SQL error occured.");
			e.printStackTrace();
		}
	} // END errorPrint

	// Iterates through a stack of SQLExceptions
	public static void printSQLException(SQLException sqle)
	{
		while (sqle != null) 
		{
			System.out.println("\n---SQLException Caught---\n");
			System.out.println("SQLState:   " + (sqle).getSQLState());
			System.out.println("Severity: " + (sqle).getErrorCode());
			System.out.println("Message:  " + (sqle).getMessage());
			sqle.printStackTrace();
			sqle = sqle.getNextException();
		}
	} // END SQLExceptionPrint
}
