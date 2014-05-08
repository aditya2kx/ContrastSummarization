package source;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

public class SummaryCache 
{
	private HashMap<String, CategorySummaryBean> cacheSumBean;	
	private static SummaryCache summaryCache;
	
	public static SummaryCache getInstance() throws FileNotFoundException, IOException, ClassNotFoundException
	{
		if(summaryCache == null)
		{
			summaryCache = new SummaryCache();
		}
		return summaryCache;
	}
	
	private SummaryCache()throws FileNotFoundException, IOException, ClassNotFoundException
	{
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		File f = null;
		
		try
		{
			f = new File("summaryCache.dat");
			if(f.exists())
			{
				fis = new FileInputStream(f);
				ois = new ObjectInputStream(fis);
				cacheSumBean = (HashMap<String, CategorySummaryBean>)ois.readObject();
			}
			else
			{
				cacheSumBean = new HashMap<String, CategorySummaryBean>();
			}
		}
		finally
		{
			if(fis!=null)
			{
				fis.close();
			}
		}
	}
	
	public void saveCacheToFile() throws IOException
	{
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		
		try
		{
			fos = new FileOutputStream("summaryCache.dat");
			oos = new ObjectOutputStream(fos);
			oos.writeObject(cacheSumBean);
		}
		finally
		{
			if(fos!=null)
			{
				fos.close();
			}
		}
		
	}
	
	public CategorySummaryBean fetchSummaryBean(String businessName)
	{
		return cacheSumBean.get(businessName);
	}
	
	public void saveSummaryBean(String businessName, CategorySummaryBean catSumBean)
	{
		cacheSumBean.put(businessName, catSumBean);
	}
}
