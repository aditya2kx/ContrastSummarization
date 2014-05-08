package source;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Restaurant_Review_Extractor 
{
	/*public static void main(String[] args) throws IOException 
	{
		FileInputStream fis = null;
		FileOutputStream fos1 = null;
		FileOutputStream fos2 = null;
		BufferedReader br = null;
		HashSet<String> business_ids = new HashSet<>();
		Map<String, String> businessNameToIdMap = new HashMap<String, String>();

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
							String name = json.getString("name");
							businessNameToIdMap.put(name, id);
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
					reviews_content = reviews_content.replaceAll("\n|\r", " ");
					reviews_content = reviews_content.replaceAll("\"", "'");
					reviews_content = reviews_content.replaceAll("\\.\\.+", ".");

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

		String path = "C:\\Users\\Santosh\\Google Drive\\Natural Language Processing Project\\Dataset\\Dataset Reviews Per Business\\yelp_phoenix_academic_dataset.rest_business_";
		//Write the business name to id mapping
		try(BufferedWriter writer = new BufferedWriter(new FileWriter(new File("business_name_id_mapping.txt")))){
			for(String key : businessNameToIdMap.keySet()){
				String val = businessNameToIdMap.get(key);
				if(Files.exists(Paths.get(path + val))){
					writer.write(key + "-->" + val);
					writer.write("\n");
				}else{
					System.out.println(key + "-->" + path + val);
				}
			}
		}
	}*/


	public static void main(String[] args) throws FileNotFoundException, IOException {
		try(BufferedReader reader = new BufferedReader(new FileReader(new File("C:\\Users\\Santosh\\google drive\\Natural Language Processing Project\\Dataset\\Restaurants Reviews Per Restaurant (1)\\Rest_Business")))){
			String readLine;
			JSONObject jsonObj;
			Map<String, String> businessNameToIdMap = new HashMap<String, String>();

			while((readLine = reader.readLine()) != null){
				try{
					jsonObj = new JSONObject(readLine);
					String id = jsonObj.getString("business_id");
					String name = jsonObj.getString("name");
					businessNameToIdMap.put(name, id);
				}catch(JSONException e){
					System.out.println("Incorrect json line: " + readLine);
				}
			}

			String path = "C:\\Users\\Santosh\\Google Drive\\Natural Language Processing Project\\Dataset\\Restaurants Reviews Per Restaurant (1)\\Rest_Business_";
			//Write the business name to id mapping
			try(BufferedWriter writer = new BufferedWriter(new FileWriter(new File("business_name_id_mapping.txt")))){
				for(String key : businessNameToIdMap.keySet()){
					String val = businessNameToIdMap.get(key);
					if(Files.exists(Paths.get(path + val))){
						writer.write(key + "-->" + val);
						writer.write("\n");
					}else{
						System.out.println(key + "-->" + path + val);
					}
				}
			}

		}
	}
}



