package source;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

public class KeywordsFetcher 
{
	private Map<String, Set<String>> keywordsSetMap;
	private static KeywordsFetcher instance;

	public static KeywordsFetcher getInstance(InputStream inputStream)
	{
		if(instance == null)
		{
			try {
				instance = new KeywordsFetcher(inputStream);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return instance;
	}

	private KeywordsFetcher(InputStream inputStream) throws FileNotFoundException, IOException
	{
		loadKeywordsFile(inputStream);
	}

	public Map<String, Set<String>> loadKeywordsFile(InputStream inputStream) throws FileNotFoundException, IOException{
		keywordsSetMap = new HashMap<>();
		String readLine;
		StringBuffer buffer = new StringBuffer();
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))){
			while((readLine = reader.readLine()) != null){
				buffer.append(readLine);
			}
		}

		JSONObject keywordsObject = new JSONObject(buffer.toString());
		String[] categoryList = JSONObject.getNames(keywordsObject);
		JSONArray jsonArray;
		int len;
		Set<String> categoryKeywordsSet = new HashSet<>();
		for(String category : categoryList){
			jsonArray = keywordsObject.getJSONArray(category);
			len = jsonArray.length();
			for(int index = 0; index < len; index++){
				categoryKeywordsSet.add(jsonArray.get(index).toString().toLowerCase());
			}

			keywordsSetMap.put(category, categoryKeywordsSet);
		}

		return keywordsSetMap;
	}

	public Set<String> getCategoryKeywords(String category) {
		return keywordsSetMap.get(category);
	}
	
	public Map<String, Set<String>> getCategoryKeywordsMap(){
		return keywordsSetMap;
	}
}
