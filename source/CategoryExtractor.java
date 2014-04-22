package source;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class CategoryExtractor 
{
	public static void main(String[] args) 
	{
		FileInputStream fis = null;
		FileOutputStream fos = null;
		BufferedReader br = null;
		String category = null;
		String currentLine = null;
		
		try 
		{
			category = "Food";
			fis = new FileInputStream(args[0]);
			br = new BufferedReader(new InputStreamReader(fis));
			fos = new FileOutputStream(args[0]+"."+category);
			while((currentLine = br.readLine()) != null)
			{
				if(currentLine.contains("<"+category+">"))
				{
					fos.write((currentLine+"\n").getBytes("UTF8"));
				}
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		

	}

}
