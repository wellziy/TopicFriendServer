package topicfriend.server.database;

//class for operate the table 'user' in database
public class UserTable
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
	public boolean isUserNameExisted(String name)
	{
		//TODO:
		return false;
	}
	
	//@return the uid of the created user
	public int createUser(String name,String password,String signature,String icon)
	{
		//TODO:
		return 0;
	}
	
	//@return the name,sig,icon information for user with uid
	public void getUserInformation(int uid)
	{
		//TODO:
	}
}
