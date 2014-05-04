package source;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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

	public static KeywordsFetcher getInstance(String filename)
	{
		if(instance == null)
		{
			try {
				instance = new KeywordsFetcher(filename);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return instance;
	}

	private KeywordsFetcher(String filename) throws FileNotFoundException, IOException
	{
		loadKeywordsFile(filename);
	}

	public Map<String, Set<String>> loadKeywordsFile(String keywordsFile) throws FileNotFoundException, IOException{
		Map<String, Set<String>> keywordsSetMap = new HashMap<>();
		String readLine;
		StringBuffer buffer = new StringBuffer();
		try(BufferedReader reader = new BufferedReader(new FileReader(new File(keywordsFile)))){
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
