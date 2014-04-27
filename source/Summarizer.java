package source;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Summarizer 
{
	public static void main(String[] args) 
	{
		FileInputStream fis = null;
		BufferedReader br = null;
		String currentLine = null;
		ArrayList<String> lines = new ArrayList<String>();
		LTCGenerator ltc;
		KMean km;
		
		try
		{
			fis = new FileInputStream(args[0]);
			br = new BufferedReader(new InputStreamReader(fis));
			while((currentLine = br.readLine()) !=null)
			{
				lines.add(currentLine);
			}
			ltc = new LTCGenerator(lines);
			double dataset[][] = ltc.calculateTLC();
			km = new KMean(5, dataset.length, dataset[0].length, dataset);
			
			System.out.println("\n\n");
			boolean t=true;
			while(t)
			{
				km.displayClusterCenterValues();
				System.out.println("\n\n");
				km.refactorClusterStructure();
				km.displayClusterIndices();
				t=km.hasClustersChanged();
				if(t==true)
				{
					for(int i=0;i<=km.K;i++)
					{
						for(int j=0;j<km.clusterCount[i];j++)
						{
							for(int k=0;k<dataset[0].length;k++)
							{
								km.previousClusters[i][j] = km.clusters[i][j]; 
							}
						}
					}

				}
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

}
