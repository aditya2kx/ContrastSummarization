package source;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Restaurant_Review_Extractor 
{
	public static void main(String[] args) 
	{
		FileInputStream fis = null;
		FileOutputStream fos1 = null;
		FileOutputStream fos2 = null;
		BufferedReader br = null;
		HashSet<String> business_ids = new HashSet<>();
		
		try
		{
			fis = new FileInputStream(args[0]);
			String input = args[1];
			fos1 = new FileOutputStream(input);
			String currentLine = null;
			br = new BufferedReader(new InputStreamReader(fis));
			JSONObject json = null;
			while( (currentLine=br.readLine())!=null)
			{
				try
				{
					json = new JSONObject(currentLine);
				}
				catch(JSONException e)
				{
					System.out.println("Line: "+currentLine+" not a json object");
					continue;
				}
				JSONArray cat = null;
				try
				{
					cat = json.getJSONArray("categories");
					for(int i=0; i<cat.length();i++)
					{
						if(cat.getString(i).equalsIgnoreCase("Restaurants"))
						{
							fos1.write((currentLine+"\n").getBytes("UTF8"));
							String id = json.getString("business_id");
							business_ids.add(id);
							
							break;
						}
					}
				}
				catch(JSONException e)
				{
					//System.out.println("Line: "+currentLine+" doesn't have categories");
					
				}
				
				try
				{
					String id = json.getString("business_id");
					int stars = json.getInt("stars");
					String reviews_content = json.getString("text");
					reviews_content.replaceAll("\n|\r", " ");
					if(reviews_content.length()<3)
					{
						continue;
					}
					
					if(business_ids.contains(id))
					{
						fos2 = new FileOutputStream(input+"_"+id, true);
						fos2.write(("{\"stars\":"+stars+",\"text\":\""+reviews_content+"\"}"+"\n").getBytes("UTF8"));
						fos2.close();
					}
				}
				catch(JSONException e)
				{
					//System.out.println("Line: "+currentLine+" doesn't have categories");
					continue;
				}
			}
			fos1.close();
			fis.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}



