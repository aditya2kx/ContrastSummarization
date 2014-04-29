package source;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;

public class KeywordsFetcher 
{
	public HashSet<String> keywords;
	private static KeywordsFetcher instance;
	
	public static KeywordsFetcher getInstance(String filename)
	{
		if(instance == null)
		{
			instance = new KeywordsFetcher(filename);
		}
		return instance;
	}
	
	private KeywordsFetcher(String filename)
	{
		HashSet<String> keywords = new HashSet<String>();
		FileInputStream fis = null;
		BufferedReader br = null;
		String currentLine = null;
		try
		{
			fis = new FileInputStream(filename);
			br = new BufferedReader(new InputStreamReader(fis));
			while((currentLine = br.readLine())!=null)
			{
				keywords.add(currentLine.toLowerCase());
			}
		}
		catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	public HashSet<String> getKeywords() {
		return keywords;
	}
}
