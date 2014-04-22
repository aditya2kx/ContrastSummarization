package source;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CategoryExtractor 
{
	public static void main(String[] args) 
	{
		FileInputStream fis = null;
		FileOutputStream fos = null;
		BufferedReader br = null;
		String category = null;
		Pattern p = null;
		Matcher m;
		String currentLine = null;
		boolean foundCategory = false;
		
		try 
		{
			category = "food";
			p = Pattern.compile("<"+category+">");
			fis = new FileInputStream(args[0]);
			br = new BufferedReader(new InputStreamReader(fis));
			fos = new FileOutputStream(args[0]+"."+category);
			while((currentLine = br.readLine()) != null)
			{
				foundCategory = false;
				m = p.matcher(currentLine);
				foundCategory = m.matches();
				if(foundCategory)
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
