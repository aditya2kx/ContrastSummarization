package source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KMean
{
	public int K;
	public double distanceFromNodeZero[];
	public int numberOfSentences;
	public int numberOfFeatures;
	public double vectorList[][];  //i = sentence, j = vector set for sentence i 
	public int clusters[][];   //i = cluster no.,j = sentence index
	public double clusterCenters[][]; //i = sentence, j = vector set for sentence i
	public int clusterCount[]; // # of sentences in cluster i
	public int previousClusters[][]; //previous clusters;i = cluster no.,j = sentence index
	public int previousClusterCount[];// # of sentences in previous cluster i
	
	public KMean(int numberOfClusters, int numberOfSentences, int numberOfFeatures, double dataset[][])         
	{
		this.K = numberOfClusters; // making cluster size as k-1 DONT KNOW WHY. I guess because we will index from 0.
		this.numberOfFeatures = numberOfFeatures;
		this.numberOfSentences = numberOfSentences;
		distanceFromNodeZero = new double[numberOfClusters];
		vectorList = dataset;
		
		clusterCount = new int[K]; //initializing each cluster size as 0
		previousClusterCount = new int[K]; //initializing each cluster size as 0
		clusterCenters = new double[K][numberOfFeatures];
		clusters = new int[K][numberOfSentences];
		previousClusters = new int[K][numberOfSentences];
		
		int initialClusterIndices[] = getInitialClusterCenterIndices();
		
		//initializing K clusters centroids
		for(int clusterCenterIndex = 0; clusterCenterIndex < K; clusterCenterIndex++) //DOUBT 
		{
			for(int featureIndex = 0; featureIndex < numberOfFeatures; featureIndex++)
			{
				clusterCenters[clusterCenterIndex][featureIndex] = 
						vectorList[initialClusterIndices[clusterCenterIndex]][featureIndex];
			}
		}
		
		////initializing K clusters to store no sentence indices
		for(int clusterIndex = 0; clusterIndex<K; clusterIndex++) //DOUBT
		{
			for(int sentenceIndex = 0; sentenceIndex < numberOfSentences; sentenceIndex++)
			{
					previousClusters[clusterIndex][sentenceIndex] = -1;
					clusters[clusterIndex][sentenceIndex] = -2;
			}
		}
	}

	public boolean hasClustersChanged()
	{
		for(int clusterIndex = 0; clusterIndex < K; clusterIndex++)
		{
			for(int clusterSizeIndex=0;clusterSizeIndex<clusterCount[clusterIndex];clusterSizeIndex++)
			{
				if(clusters[clusterIndex][clusterSizeIndex] != 
							previousClusters[clusterIndex][clusterSizeIndex])
				{
					return true;
				}
			}
		}
		return false;
	}
	
	public int[] getInitialClusterCenterIndices()
	{
		if(K<=0)
		{
			return null;
		}
		Map<Integer, Double> distanceMap =	new HashMap<>();
		//distanceMapFromNodeZero.put(1.0, 0);
		
		for ( int datasetIndex = 1; datasetIndex < numberOfSentences; datasetIndex++)
		{
			// find for a particular sentence, distance from all cluster centers
				double distanceFromNodeZero = 0;
				double NodeZero = 0;
				double vectorNode = 0;
				for(int featureIndex = 0; featureIndex < numberOfFeatures; featureIndex++)
				{
					distanceFromNodeZero += 
							(vectorList[0][featureIndex] * 
									vectorList[datasetIndex][featureIndex]);
					
					NodeZero += vectorList[0][featureIndex] *
							vectorList[0][featureIndex];
					
					vectorNode += vectorList[datasetIndex][featureIndex] *
										vectorList[datasetIndex][featureIndex];
				}
				NodeZero = Math.sqrt(NodeZero);
				vectorNode = Math.sqrt(vectorNode);
				distanceFromNodeZero /= (NodeZero * vectorNode);
				distanceMap.put(datasetIndex, distanceFromNodeZero);
		}

		SortComparator sortComparator = new SortComparator();
		List<Map.Entry<Integer, Double>> sortedDistList = new ArrayList<>(distanceMap.entrySet());
		Collections.sort(sortedDistList, sortComparator);
		
		int initialClusterIndices[] = new int[K];
		initialClusterIndices[0] = 0;
		for(int clusterIndex = 1; clusterIndex < K; clusterIndex++)
		{
			initialClusterIndices[clusterIndex] = sortedDistList.get(clusterIndex - 1).getKey();
		}
		return initialClusterIndices;
		
	}
	
	public int getCentroidSentence(int clusterIndex)
	{
		double maximumDistance = -1;
		int sentenceIndex = -1;
		for ( int clusterSetIndex = 0; clusterSetIndex < clusterCount[clusterIndex]; clusterSetIndex++)
		{
			// find for a particular sentence, distance from all cluster centers
			double distanceFromCentroid = 0;
			double nodeCentroid = 0;
			double vectorNode = 0;
			for(int featureIndex = 0; featureIndex < numberOfFeatures; featureIndex++)
			{
				distanceFromCentroid += 
						(vectorList[clusters[clusterIndex][clusterSetIndex]][featureIndex] * 
								clusterCenters[clusterIndex][featureIndex]);
				
				nodeCentroid += clusterCenters[clusterIndex][featureIndex] *
						clusterCenters[clusterIndex][featureIndex];
				
				vectorNode += vectorList[clusters[clusterIndex][clusterSetIndex]][featureIndex] *
						vectorList[clusters[clusterIndex][clusterSetIndex]][featureIndex];
			}
			nodeCentroid = Math.sqrt(nodeCentroid);
			vectorNode = Math.sqrt(vectorNode);
			distanceFromCentroid /= (nodeCentroid * vectorNode);
			if(distanceFromCentroid > maximumDistance)
			{
				maximumDistance = distanceFromCentroid;
				sentenceIndex = clusters[clusterIndex][clusterSetIndex];
			}
		}
		return sentenceIndex;
	}
	
	public void displayClusterCenterValues()
	{
		for(int clusterCenterIndex = 0; clusterCenterIndex < K; clusterCenterIndex++)
		{   
			System.out.print("\n");
			for(int featureIndex = 0; featureIndex < numberOfFeatures; featureIndex++)
			{
				System.out.print(" "+clusterCenters[clusterCenterIndex][featureIndex]);
			}
			System.out.print("\n");
		}
	}

	public void refactorClusterStructure()
	{
		clusterCount = new int[K]; //initializing each cluster size as 0 

		for ( int sentenceIndex = 0; sentenceIndex < numberOfSentences; sentenceIndex++)
		{
			// find for a particular sentence, distance from all cluster centers
			for ( int clusterCenterIndex = 0; clusterCenterIndex < K; clusterCenterIndex++)
			{              
				distanceFromNodeZero[clusterCenterIndex] = 0;
				double clusterCenterMod = 0;
				double vectorMod = 0;
				for(int featureIndex = 0; featureIndex < numberOfFeatures; featureIndex++)
				{
					distanceFromNodeZero[clusterCenterIndex] += 
							(clusterCenters[clusterCenterIndex][featureIndex] * 
									vectorList[sentenceIndex][featureIndex]);
					
					clusterCenterMod += clusterCenters[clusterCenterIndex][featureIndex] *
											clusterCenters[clusterCenterIndex][featureIndex];
					
					vectorMod += vectorList[sentenceIndex][featureIndex] *
										vectorList[sentenceIndex][featureIndex];
				}
				clusterCenterMod = Math.sqrt(clusterCenterMod);
				vectorMod = Math.sqrt(vectorMod);
				distanceFromNodeZero[clusterCenterIndex] /= (clusterCenterMod * vectorMod);
			}
			// find minimum distance index cluster no 't'
			int maximumDistanceIndex = 0;
			double maximumDistance = distanceFromNodeZero[0];   
			for ( int clusterCenterIndex = 0; clusterCenterIndex < K; clusterCenterIndex++)                  
			{
				if(maximumDistance < distanceFromNodeZero[clusterCenterIndex])
				{
					maximumDistanceIndex = clusterCenterIndex;
					maximumDistance = distanceFromNodeZero[clusterCenterIndex];
				}
			}
			
			//store sentence in proper cluster and increment cluster size
			clusters[maximumDistanceIndex][clusterCount[maximumDistanceIndex]] = sentenceIndex;
			clusterCount[maximumDistanceIndex]++;
		}    

		for(int clusterCenterIndex = 0; clusterCenterIndex < K; clusterCenterIndex++)
		{
			for(int featureIndex = 0; featureIndex < numberOfFeatures; featureIndex++)
			{ 
				double totalSum = 0;
				for(int clusterSizeIndex = 0; clusterSizeIndex < clusterCount[clusterCenterIndex]; 
						clusterSizeIndex++)
				{
					totalSum += vectorList[clusters[clusterCenterIndex][clusterSizeIndex]][featureIndex]; 
				}
				totalSum = totalSum / clusterCount[clusterCenterIndex];
				clusterCenters[clusterCenterIndex][featureIndex] = totalSum;
			}
		}
	}

	public void displayClusterContents()
	{
		for(int clusterCenterIndex = 0; clusterCenterIndex < K; clusterCenterIndex++)
		{
			for(int clusterSizeIndex = 0; clusterSizeIndex < clusterCount[clusterCenterIndex];
					clusterSizeIndex++)
			{ 
				System.out.println("\n Cluster no:" +clusterCenterIndex + "  Object No: " + clusterSizeIndex);
				for(int featureIndex = 0; featureIndex < numberOfFeatures; featureIndex++)
				{
					System.out.print(" Feature Value are :" + 
										vectorList[clusters[clusterCenterIndex][clusterSizeIndex]][featureIndex]);
				}
			}
			System.out.println("\n\n");
		}
	}
	
	public void displayClusterIndices()
	{
		for(int clusterCenterIndex = 0; clusterCenterIndex < K; clusterCenterIndex++)
		{
			System.out.println("\n Cluster no:" +clusterCenterIndex+" Sentence Index are :" );
			for(int clusterSizeIndex = 0; clusterSizeIndex < clusterCount[clusterCenterIndex];
					clusterSizeIndex++)
			{ 
				System.out.print(" " + 
									clusters[clusterCenterIndex][clusterSizeIndex]);
			}
			System.out.println("\n\n");
		}
	}	

	private static class SortComparator implements Comparator<Map.Entry<Integer, Double>>{
		@Override
		public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
			// TODO Auto-generated method stub
			return o1.getValue().compareTo(o2.getValue());
		}
		
	}
}