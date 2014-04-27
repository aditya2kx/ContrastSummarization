package source;

public class KMean
{
	int K;
	float distanceFromClusterCenter[];
	int numberOfSentences;
	int numberOfFeatures;
	float vectorList[][];  //i = sentence, j = vector set for sentence i 
	int clusters[][];   //i = cluster no.,j = sentence index
	float clusterCenters[][]; //i = sentence, j = vector set for sentence i
	int clusterCount[]; // # of sentences in cluster i
	int previousClusters[][]; //previous clusters;i = cluster no.,j = sentence index
	int previousClusterCount[];// # of sentences in previous cluster i
	
	public KMean(int numberOfClusters, int numberOfSentences, int numberOfFeatures, float dataset[][])         
	{
		this.K = numberOfClusters; // making cluster size as k-1 DONT KNOW WHY. I guess because we will index from 0.
		this.numberOfFeatures = numberOfFeatures;
		this.numberOfSentences = numberOfSentences;
		distanceFromClusterCenter = new float[numberOfClusters];
		vectorList = dataset;
		
		clusterCount = new int[numberOfSentences]; //initializing each cluster size as 0
		previousClusterCount = new int[numberOfSentences]; //initializing each cluster size as 0
		clusterCenters = new float[K][numberOfFeatures];
		clusters = new int[K][numberOfSentences];
		previousClusters = new int[K][numberOfSentences];
		
		//initializing K clusters centroids
		for(int clusterCenterIndex = 0; clusterCenterIndex < K; clusterCenterIndex++) //DOUBT 
		{
			for(int featureIndex = 0; featureIndex < numberOfFeatures; featureIndex++)
			{
				clusterCenters[clusterCenterIndex][featureIndex] = vectorList[clusterCenterIndex][featureIndex];
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

	boolean hasClustersChanged()
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
	
	void displayClusterCenterValues()
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

	void refactorClusterStructure()
	{
		clusterCount = new int[numberOfSentences]; //initializing each cluster size as 0 

		for ( int sentenceIndex = 0; sentenceIndex < numberOfSentences; sentenceIndex++)
		{
			// find for a particular sentence, distance from all cluster centers
			for ( int clusterCenterIndex = 0; clusterCenterIndex < K; clusterCenterIndex++)
			{              
				distanceFromClusterCenter[clusterCenterIndex] = 0;
				float clusterCenterMod = 0;
				float vectorMod = 0;
				for(int featureIndex = 0; featureIndex < numberOfFeatures; featureIndex++)
				{
					distanceFromClusterCenter[clusterCenterIndex] += 
							(clusterCenters[clusterCenterIndex][featureIndex] * 
									vectorList[sentenceIndex][featureIndex]);
					
					clusterCenterMod += clusterCenters[clusterCenterIndex][featureIndex] *
											clusterCenters[clusterCenterIndex][featureIndex];
					
					vectorMod += vectorList[sentenceIndex][featureIndex] *
										vectorList[sentenceIndex][featureIndex];
				}
				clusterCenterMod = (float) Math.sqrt(clusterCenterMod);
				vectorMod = (float) Math.sqrt(vectorMod);
				distanceFromClusterCenter[clusterCenterIndex] /= (clusterCenterMod * vectorMod);
			}
			// find minimum distance index cluster no 't'
			int maximumDistanceIndex = 0;
			float maximumDistance = distanceFromClusterCenter[0];   
			for ( int clusterCenterIndex = 0; clusterCenterIndex < K; clusterCenterIndex++)                  
			{
				if(maximumDistance < distanceFromClusterCenter[clusterCenterIndex])
				{
					maximumDistanceIndex = clusterCenterIndex;
					maximumDistance = distanceFromClusterCenter[clusterCenterIndex];
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
				int totalSum = 0;
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

	void displayClusterContents()
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
	
	void displayClusterIndices()
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

}