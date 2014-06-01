package topicfriendserver.database;

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
	public void getFriendList(int uid)
	{
		//TODO
	}
	
	//@return if update db succeed,return true
	public boolean makeFriend(int uid1,int uid2)
	{
		//TODO:
		return false;
	}
	
	//@return if remove friend succeed,return true
	public boolean removeFriend(int uid1,int uid2)
	{
		//TODO:
		return false;
	}
}
