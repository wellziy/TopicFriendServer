package topicfriend.netmessage;

import org.apache.http.util.ByteArrayBuffer;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class NetMessage
{
	private int m_id;

	/////////////////////////////////////////////
	//public
	public int getMessageID()
	{
		return m_id;
	}
	
	public NetMessage()
	{
		m_id=NetMessageID.UNKNOWN;
	}
	
	public static NetMessage fromJsonString(String str)
	{
		JsonParser parser=new JsonParser();
		JsonObject obj=parser.parse(str).getAsJsonObject();
		JsonElement idElement=(obj==null?null:obj.get("m_id"));
		if(idElement==null)
		{
			return null;
		}
		
		int id=idElement.getAsInt();
		Gson gs=new Gson();
		Class<? extends NetMessage> messageClass = NetMessageFactory.getInstance().getMessageClass(id);
		NetMessage res=gs.fromJson(obj, messageClass);
		
		return res;
	}
	
	public String toJsonString()
	{
		Gson gs=new Gson();
		String res=gs.toJson(this);
		return res;
	}
	
	@Override
	public String toString()
	{
		return toJsonString();
	}
	
	public ByteArrayBuffer toByteArrayBuffer()
	{
		String str=toJsonString();
		byte[] byteArr=str.getBytes();
		ByteArrayBuffer buf=new ByteArrayBuffer(byteArr.length);
		buf.append(byteArr, 0, byteArr.length);
		
		return buf;
	}
	
	////////////////////////////////////////////
	//protected
	protected void setMessageID(int id)
	{
		m_id=id;
	}
}
