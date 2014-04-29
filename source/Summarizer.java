package source;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

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
			System.out.println(Arrays.toString(ltc.getTermSet()));
			for(int i = 0; i < dataset.length; i++){
				System.out.println(Arrays.toString(dataset[i]));
			}
			km = new KMean(3, dataset.length, dataset[0].length, dataset);
			
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
					for(int i=0;i<km.K;i++)
					{
						for(int j=0;j<km.clusterCount[i];j++)
						{
							km.previousClusters[i][j] = km.clusters[i][j]; 
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
