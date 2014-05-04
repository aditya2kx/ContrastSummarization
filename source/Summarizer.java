package source;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Summarizer 
{
	public static void main(String[] args) 
	{
		FileInputStream fis = null;
		BufferedReader br = null;
		String currentLine = null;
		String JSONFileContent = "";
		ArrayList<String> lines = new ArrayList<String>();
		LTCGenerator ltc;
		KMean km;
		
		try
		{
			fis = new FileInputStream(args[0]);
			br = new BufferedReader(new InputStreamReader(fis));
			while((currentLine = br.readLine()) !=null)
			{
				JSONFileContent += currentLine;
				//lines.add(currentLine);
			}
			
			try
			{
				JSONObject reviewJSON = new JSONObject(JSONFileContent);
				JSONArray outputArray = reviewJSON.getJSONArray("output");
				for(int outputindex=0; outputindex<outputArray.length(); outputindex++)
				{
					JSONObject outputJSONObj = outputArray.getJSONObject(outputindex);
					JSONArray foodArray = outputJSONObj.getJSONObject("categories").getJSONArray("food");
					for(int foodindex=0; foodindex<foodArray.length(); foodindex++)
					{
						String temp = (String) foodArray.get(foodindex);
						JSONObject foodJSONObj = new JSONObject(temp);//foodArray.get(0)//.getJSONObject(foodindex);
						lines.add(foodJSONObj.getString("text"));
					}
				}
			}
			catch(JSONException e)
			{
				e.printStackTrace();
				System.exit(0);
			}


			Set<String> keywordsSet = KeywordsFetcher.getInstance(args[1]).getCategoryKeywords("food");
			ltc = new LTCGenerator(lines, keywordsSet);
			double dataset[][] = ltc.calculateTLC();
			System.out.println(Arrays.toString(ltc.getTermSet()));
			for(int i = 0; i < dataset.length; i++){
				System.out.println(Arrays.toString(dataset[i]));
			}
			km = new KMean(5, dataset.length, dataset[0].length, dataset, ltc);
			
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
			
			List<String> sentencesList = ltc.getSentencesList();
			HashMap<Integer, Integer> sentenceIndexToClusterCenterSentenceIndexMap = 
								new HashMap<Integer, Integer>();
			for(int i=0;i<km.K;i++)
			{
				int[] supportingNodes = km.clusters[i];
				int clustersize = km.clusterCount[i];
				int centroidSentenceIndex = km.getCentroidSentence(i);
				System.out.println("Cluster Index"+i+" Sentence: "+ sentencesList.get(centroidSentenceIndex));
				for(int index = 0; index < clustersize; index++){
					int supportingSentence = supportingNodes[index];
					sentenceIndexToClusterCenterSentenceIndexMap.put(supportingSentence, centroidSentenceIndex);
					if(centroidSentenceIndex != supportingSentence){
						System.out.println("--> Supporting Sentence: " + sentencesList.get(supportingSentence));
					}
				}
			}
			
			double lambda = 0.8;
			double weights[] = {0.2, 0.6, 0.2, 0.6, 0.4};
			MMR_MD_Utility relevanceRanker = new MMR_MD_Utility(weights, keywordsSet);
			HashMap<String, Double> sim1Scores = new HashMap<String, Double>();
			Map<String, Integer> sentencesToClusterCenterMap = new HashMap<String, Integer>();
			Map<String, Integer> sentencesToIndexMap = ltc.getSentencesToIndexMap();
			for(int i=0;i<km.K;i++)
			{
				int[] supportingNodes = km.clusters[i];
				int clustersize = km.clusterCount[i];
				for(int index = 0; index < clustersize; index++)
				{
					int supportingSentence = supportingNodes[index];
					String currentSentence = sentencesList.get(supportingSentence);
					int centroidSentenceIndex = 
							sentenceIndexToClusterCenterSentenceIndexMap.get(supportingSentence);
					String centroidSentence = sentencesList.get(centroidSentenceIndex);
					double distance = km.getDistance(dataset[sentencesToIndexMap.get(currentSentence)],
							dataset[sentencesToIndexMap.get(centroidSentence)]);					
					
					double score = lambda * 
									relevanceRanker.Similarity_1(currentSentence, distance, clustersize);
					sim1Scores.put(currentSentence, score);
					sentencesToClusterCenterMap.put(currentSentence, i);
				}
			}
			DecimalFormat myFormat = new DecimalFormat("##.##");
			SortComparator sortComparator = new SortComparator();
			List<Map.Entry<String, Double>> sortedSim1List = new ArrayList<>(sim1Scores.entrySet());
			Collections.sort(sortedSim1List, sortComparator);
			
			Map<String, Double> mmrmdScoresMap = new HashMap<>();
			System.out.println("Sim 1 sentences:");
			for(Map.Entry<String, Double> printCandidate : sortedSim1List)
			{
				System.out.println("Score: "+myFormat.format(printCandidate.getValue())
									+"Sentence: "+printCandidate.getKey());
				mmrmdScoresMap.put(printCandidate.getKey(), printCandidate.getValue());
			}
			
			//Similarity 2 scores
			Map<String, Double> sim2ScoresMap = new HashMap<>();
			double[][] selectedPassages = new double[dataset.length][dataset[0].length];
			int noOfSelectedPassages = 0;
			double[] featureVector;
			double sim2Score;
			Set<Integer> selectedCluster = new HashSet<Integer>();
			for(Map.Entry<String, Double> candidates : sortedSim1List)
			{
				featureVector = dataset[sentencesToIndexMap.get(candidates.getKey())];
				boolean belongsToSelectedCluster = 
						selectedCluster.contains(sentencesToClusterCenterMap.get(candidates.getKey()));
				int selectedClusterSize = 0;
				if(belongsToSelectedCluster)
				{
					selectedClusterSize = 
							km.clusterCount[sentencesToClusterCenterMap.get(candidates.getKey())];
				}
				sim2Score = (1 - lambda) * relevanceRanker.Similarity_2(featureVector, selectedPassages, 
											noOfSelectedPassages, belongsToSelectedCluster, 
											selectedClusterSize);
				selectedPassages[noOfSelectedPassages++] = featureVector;
				selectedCluster.add(sentencesToClusterCenterMap.get(candidates.getKey()));
				sim2ScoresMap.put(candidates.getKey(), sim2Score);
			}
			
			List<Map.Entry<String, Double>> sortedSim2List = new ArrayList<>(sim2ScoresMap.entrySet());
			Collections.sort(sortedSim2List, sortComparator);
			
			System.out.println("Sim 2 sentences:");
			for(Map.Entry<String, Double> printCandidate : sortedSim2List)
			{
				System.out.println("Score: "+myFormat.format(printCandidate.getValue())
						+"Sentence: "+printCandidate.getKey());
				mmrmdScoresMap.put(printCandidate.getKey(), 
						mmrmdScoresMap.get(printCandidate.getKey()) - printCandidate.getValue());
			}
			
			List<Map.Entry<String, Double>> sortedMMRList = new ArrayList<>(mmrmdScoresMap.entrySet());
			Collections.sort(sortedMMRList, sortComparator);
			System.out.println("MMR MD sentences:");
			for(Map.Entry<String, Double> printCandidate : sortedMMRList)
			{
				System.out.println("In Cluster: "+sentencesToClusterCenterMap.get(printCandidate.getKey())
						+" Relevance Score: "+myFormat.format(sim1Scores.get(printCandidate.getKey()))
						+" Anti-Redundancy Score: "+myFormat.format(sim2ScoresMap.get(printCandidate.getKey()))
						+" MMR Score: "+myFormat.format(printCandidate.getValue())
						+" Sentence: "+printCandidate.getKey());			
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
	private static class SortComparator implements Comparator<Map.Entry<String, Double>>
	{
		@Override
		public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) 
		{
			// TODO Auto-generated method stub
			return o2.getValue().compareTo(o1.getValue());
		}
	}

}
