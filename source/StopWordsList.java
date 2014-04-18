package source;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class StopWordsList {
	private static Set<String> stopWordsSet; 
	
	static{
		try
		{
			FileInputStream fis = new FileInputStream("C:\\Users\\Rockstar\\Google Drive\\Natural Language Processing Project\\Dataset\\stop words.txt");
			FileOutputStream fos = new FileOutputStream("C:\\Users\\Rockstar\\Google Drive\\Natural Language Processing Project\\Dataset\\stop words unique.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			String word = null;
			stopWordsSet = new HashSet<String>();
			while((word = br.readLine())!=null)
			{
				stopWordsSet.add(word);
				fos.write((word+"\n").getBytes("UTF8"));
			}
			fis.close();
			fos.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}

	}
	
	public static Set<String> getStopWordsSet(){
		return stopWordsSet;
	}
	
	public static boolean isStopWord(String word){
		return stopWordsSet.contains(word);
	}
}
