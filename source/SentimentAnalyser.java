package source;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class SentimentAnalyser 
{
	public static void main(String[] args) 
	{
		FileInputStream fis = null;
		BufferedReader br = null;
		FileOutputStream fos = null;
		String filename = args[0];
		
		try
		{
			fis = new FileInputStream(filename);
			br = new BufferedReader(new InputStreamReader(fis));
			fos = new FileOutputStream(filename+".sentiment");
			String currentLine = null;
			while((currentLine = br.readLine())!=null)
			{
				fos.write((EnsembledSentimentAnalyzer.getSentimentClass(currentLine)+
						" "+currentLine).getBytes("UTF8"));
			}
			fos.close();
			fis.close();
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

}
