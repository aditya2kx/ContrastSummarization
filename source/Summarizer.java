package source;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Summarizer 
{
	static int NoOfClusters = 5;
	
	private static int minimumIndex(double array[])
	{
		double minimumValue = Double.MAX_VALUE;
		int minimumIndex = -1;
		for(int index=0; index<array.length; index++)
		{
			if(minimumValue > array[index])
			{
				minimumValue = array[index];
				minimumIndex = index;
			}
		}
		return minimumIndex;
	}
	
	private static void setSentencesLengthForEachCluster(int noOfSentencesPerCluster[], 
			int sizeOfEachCluster[], int noOfSentencesInSummary, int totalSentences)
	{
		//Stride Algorithm
		if(noOfSentencesInSummary < NoOfClusters)
		{
			System.out.println("Warning: To get a good summary have alteast sentences equal to number of clusters");
			System.exit(1);
		}
		System.out.println("");
		double stridesPerCluster[] = new double[NoOfClusters];
		for(int strideIndex=0; strideIndex < NoOfClusters; strideIndex++)
		{
			stridesPerCluster[strideIndex] = (totalSentences * 1.0) / sizeOfEachCluster[strideIndex];
		}
		double passValuesPerCluster[] = Arrays.copyOf(stridesPerCluster, stridesPerCluster.length);
		for(int sizeIndex=0; sizeIndex<noOfSentencesInSummary; sizeIndex++)
		{
			int minIndex = minimumIndex(passValuesPerCluster);
			noOfSentencesPerCluster[minIndex]++;
			passValuesPerCluster[minIndex] += stridesPerCluster[minIndex];
		}
	}
	
	public static CategorySummaryBean generateSummary(String args) 
	{
		FileInputStream fis = null;
		BufferedReader br = null;
		String currentLine = null;
		String JSONFileContent = "";
		ArrayList<ArrayList<String>> linesPerSentiment = new ArrayList<ArrayList<String>>();
		HashMap<String, Double> sentencesSentimentScoreMap = new HashMap<String, Double>();
		LTCGenerator ltc;
		ArrayList<String> sentimentCategories = new ArrayList<String>();
		KMean km;
		CategorySummaryBean catSumBean = new CategorySummaryBean();
		List<SummaryBean> summaryBeanList = new ArrayList<SummaryBean>();
		InputStream categoryFileStream = ClassLoader.getSystemResourceAsStream("aggregated_keywords_file.json");
		
		
		sentimentCategories.add("positive");
		sentimentCategories.add("negative");
		for(int sentimentCatIndex=0; sentimentCatIndex<sentimentCategories.size(); sentimentCatIndex++)
		{
			linesPerSentiment.add(new ArrayList<String>());
			SummaryBean sumBean = new SummaryBean(); 
			catSumBean.addCategorySummary(SentimentClass.values()[sentimentCatIndex], "Food", sumBean);
			summaryBeanList.add(sumBean);
		}
		
		try
		{
			fis = new FileInputStream(args);
			br = new BufferedReader(new InputStreamReader(fis));
			while((currentLine = br.readLine()) !=null)
			{
				JSONFileContent += currentLine;
				//lines.add(currentLine);
			}
			fis.close();
			try
			{
				JSONObject reviewJSON = new JSONObject(JSONFileContent);
				JSONArray outputArray = reviewJSON.getJSONArray("output");
				for(int outputindex=0; outputindex<outputArray.length(); outputindex++)
				{
					JSONObject outputJSONObj = outputArray.getJSONObject(outputindex);
					//System.out.println(outputJSONObj.toString());
					if(outputJSONObj.getJSONObject("categories").has("food"))
					{	
						JSONArray foodArray = outputJSONObj.getJSONObject("categories").getJSONArray("food");
						for(int foodindex=0; foodindex<foodArray.length(); foodindex++)
						{
							String currentReviewItem = (String) foodArray.get(foodindex);
							JSONObject foodJSONObj = new JSONObject(currentReviewItem);
							if(foodJSONObj.has("senti_label"))
							{
								int sentimentCatIndex = 
										sentimentCategories.indexOf(foodJSONObj.getString("senti_label").toLowerCase());
								if(sentimentCatIndex == -1)
								{
									continue;
								}
								String sentence = foodJSONObj.getString("text"); 
								linesPerSentiment.get(sentimentCatIndex).add(sentence);
								double sentimentScore = Math.abs(foodJSONObj.getDouble("senti_score"));
								sentencesSentimentScoreMap.put(sentence, sentimentScore);
							}
						}
					}
				}
			}
			catch(JSONException e)
			{
				e.printStackTrace();
				System.exit(0);
			}


			Set<String> keywordsSet = KeywordsFetcher.getInstance(categoryFileStream).getCategoryKeywords("food");
			for(int sentimentCatIndex=0; sentimentCatIndex<sentimentCategories.size(); sentimentCatIndex++)
			{
				ltc = new LTCGenerator(linesPerSentiment.get(sentimentCatIndex), keywordsSet);
				double dataset[][] = ltc.calculateTLC();
				/*System.out.println(Arrays.toString(ltc.getTermSet()));
				for(int i = 0; i < dataset.length; i++){
					System.out.println(Arrays.toString(dataset[i]));
				}*/
				km = new KMean(NoOfClusters, dataset.length, dataset[0].length, dataset, ltc);
				
				System.out.println("\n\n");
				boolean t=true;
				int iterations = 0;
				while(t)
				{
					//km.displayClusterCenterValues();
					//System.out.println("\n\n");
					km.refactorClusterStructure();
					//km.displayClusterIndices();
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
					iterations++;
					System.out.println("Iterations: "+iterations);
				}
				
				int noOfSentencesFromEachCluster[] = new int[NoOfClusters];
				Arrays.fill(noOfSentencesFromEachCluster, 1);
				int noOfSentencesInSummary = 10;
				noOfSentencesInSummary -= NoOfClusters;
				setSentencesLengthForEachCluster(noOfSentencesFromEachCluster, km.clusterCount, 
						noOfSentencesInSummary, dataset.length);
				
				
				List<String> sentencesList = ltc.getSentencesList();
				HashMap<Integer, Integer> sentenceIndexToClusterCenterSentenceIndexMap = 
									new HashMap<Integer, Integer>();
				ArrayList<Map.Entry<Integer, Integer>> sortedClusterToClusterSizeList;
				HashMap<Integer, Integer> clusterToClusterSizMap = 
						new HashMap<Integer, Integer>();
				
				for(int i=0;i<km.K;i++)
				{
					clusterToClusterSizMap.put(i, km.clusterCount[i]);
				}
				
				sortedClusterToClusterSizeList = 
						new ArrayList<Map.Entry<Integer, Integer>>(clusterToClusterSizMap.entrySet());
				
				Collections.sort(sortedClusterToClusterSizeList, new SortComparatorInteger());
				
				
				FileOutputStream fos = null;
				for(int i=0;i<km.K;i++)
				{
					int[] supportingNodes = km.clusters[i];
					int clustersize = km.clusterCount[i];
					int centroidSentenceIndex = km.getCentroidSentence(i);
					fos = new FileOutputStream(args+sentimentCategories.get(sentimentCatIndex)+".cluster"+i);
					
					fos.write(("Cluster Index"+i+" Sentence: "+ sentencesList.get(centroidSentenceIndex) +"\n")
							.getBytes("UTF8"));
					
					for(int index = 0; index < clustersize; index++)
					{
						int supportingSentence = supportingNodes[index];
						sentenceIndexToClusterCenterSentenceIndexMap.put(supportingSentence, centroidSentenceIndex);
						if(centroidSentenceIndex != supportingSentence)
						{
							fos.write(("--> Supporting Sentence: " + sentencesList.get(supportingSentence) +"\n")
									.getBytes("UTF8"));
						}
					}
					fos.close();
				}
				
				double lambda = 0.5;
				double weights[] = {0.3, 0.4, 0.3, 1.0, 0.0};
				MMR_MD_Utility relevanceRanker = new MMR_MD_Utility(weights, keywordsSet);
				List<HashMap<String, Double>> sim1ScoresList = new ArrayList<HashMap<String, Double>>();
				Map<String, Integer> sentencesToClusterCenterMap = new HashMap<String, Integer>();
				Map<String, Integer> sentencesToIndexMap = ltc.getSentencesToIndexMap();
				double maxSim1Score[] = new double[NoOfClusters];
				Arrays.fill(maxSim1Score, -1);
				for(int i=0;i<km.K;i++)
				{
					HashMap<String, Double> sim1ScoresCurrentCuster =  new HashMap<String, Double>();
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
						double sentimentScore = sentencesSentimentScoreMap.get(currentSentence);
						
						double score = lambda * 
										relevanceRanker.Similarity_1(currentSentence, distance, sentimentScore);//, clustersize);
						if(maxSim1Score[i] < score)
						{
							maxSim1Score[i] = score;
						}
						sim1ScoresCurrentCuster.put(currentSentence, score);
						sentencesToClusterCenterMap.put(currentSentence, i);
					}
					sim1ScoresList.add(sim1ScoresCurrentCuster);
				}
				DecimalFormat myFormat = new DecimalFormat("##.##");
				SortComparator sortComparator = new SortComparator();
				ArrayList<ArrayList<Map.Entry<String, Double>>> sortedSim1ListPerCluster = 
						new ArrayList<ArrayList<Map.Entry<String, Double>>>();
				
				for(int clusterIndex=0; clusterIndex<sim1ScoresList.size(); clusterIndex++)
				{
					ArrayList<Map.Entry<String, Double>> sortedSim1List = 
							new ArrayList<Map.Entry<String, Double>>(sim1ScoresList.get(clusterIndex).entrySet());
					Collections.sort(sortedSim1List, sortComparator);
					sortedSim1ListPerCluster.add(sortedSim1List);
				}
				
				List<HashMap<String, Double>> mmrmdScoresMapList = new ArrayList<HashMap<String, Double>>();
				System.out.println("Sim 1 sentences:");
				for(int clusterIndex=0; clusterIndex<sortedSim1ListPerCluster.size(); clusterIndex++)
				{
					ArrayList<Map.Entry<String, Double>> sortedSim1List = sortedSim1ListPerCluster.get(clusterIndex);
					HashMap<String, Double> mmrmdScoresMap = new HashMap<String, Double>();
					for(Map.Entry<String, Double> printCandidate : sortedSim1List)
					{
						//can normalize before adding score
						printCandidate.setValue(printCandidate.getValue()/maxSim1Score[clusterIndex]);
						mmrmdScoresMap.put(printCandidate.getKey(), printCandidate.getValue());
					}
					mmrmdScoresMapList.add(mmrmdScoresMap);
				}
				
				//Similarity 2 scores
				List<HashMap<String, Double>> sim2ScoresList = new ArrayList<HashMap<String, Double>>();
				double maxSim2Score[] = new double[NoOfClusters];
				Arrays.fill(maxSim2Score, -1);
				for(int clusterIndex=0; clusterIndex<sortedSim1ListPerCluster.size(); clusterIndex++)
				{
					double[][] selectedPassages = new double[dataset.length][dataset[0].length];
					int noOfSelectedPassages = 0;
					double[] featureVector;
					double sim2Score;
					//Set<Integer> selectedCluster = new HashSet<Integer>();
					int sim2Index = 0;
					ArrayList<Map.Entry<String, Double>> sortedSim1List = sortedSim1ListPerCluster.get(clusterIndex);
					HashMap<String, Double> sim2ScoresMap = new HashMap<String, Double>();
					for(Map.Entry<String, Double> candidates : sortedSim1List)
					{
						featureVector = dataset[sentencesToIndexMap.get(candidates.getKey())];
						/*boolean belongsToSelectedCluster = 
								selectedCluster.contains(sentencesToClusterCenterMap.get(candidates.getKey()));*/
						//int selectedClusterSize = 0;
						/*if(belongsToSelectedCluster)
						{
							selectedClusterSize = 
									km.clusterCount[sentencesToClusterCenterMap.get(candidates.getKey())];
						}*/
						sim2Score = (1 - lambda) * relevanceRanker.Similarity_2(featureVector, selectedPassages, 
													noOfSelectedPassages);/*, belongsToSelectedCluster, 
													selectedClusterSize);*/
						selectedPassages[noOfSelectedPassages++] = featureVector;
						//selectedCluster.add(sentencesToClusterCenterMap.get(candidates.getKey()));
						if(maxSim2Score[clusterIndex] < sim2Score)
						{
							maxSim2Score[clusterIndex] = sim2Score;
						}
						sim2ScoresMap.put(candidates.getKey(), sim2Score);
						sim2Index++;
						if(sim2Index % 200 == 0)
						{
							System.out.println(sim2Index+" sentence processed for sim 2");
						}
					}
					sim2ScoresList.add(sim2ScoresMap);
				}
				ArrayList<ArrayList<Map.Entry<String, Double>>> sortedSim2ListPerCluster = 
						new ArrayList<ArrayList<Map.Entry<String, Double>>>();
				for(int clusterIndex=0; clusterIndex<sim2ScoresList.size(); clusterIndex++)
				{
					ArrayList<Map.Entry<String, Double>> sortedSim2List = 
							new ArrayList<Map.Entry<String, Double>>(sim2ScoresList.get(clusterIndex).entrySet());
					Collections.sort(sortedSim2List, sortComparator);
					sortedSim2ListPerCluster.add(sortedSim2List);
				}
				
				System.out.println("Sim 2 sentences:");
				for(int clusterIndex=0; clusterIndex<sortedSim2ListPerCluster.size(); clusterIndex++)
				{
					ArrayList<Map.Entry<String, Double>> sortedSim2List = sortedSim2ListPerCluster.get(clusterIndex);
					HashMap<String, Double> mmrmdScoresMap = mmrmdScoresMapList.get(clusterIndex);
					for(Map.Entry<String, Double> printCandidate : sortedSim2List)
					{
						printCandidate.setValue(printCandidate.getValue()/maxSim2Score[clusterIndex]);
						//can normalize before subtracting
						mmrmdScoresMap.put(printCandidate.getKey(), 
								mmrmdScoresMap.get(printCandidate.getKey()) - printCandidate.getValue());
					}
				}
				
				ArrayList<ArrayList<Map.Entry<String, Double>>> sortedMMRListPerCluster = 
						new ArrayList<ArrayList<Map.Entry<String, Double>>>();
				for(int clusterIndex=0; clusterIndex<mmrmdScoresMapList.size(); clusterIndex++)
				{
					ArrayList<Map.Entry<String, Double>> sortedMMRList = 
							new ArrayList<Map.Entry<String, Double>>(mmrmdScoresMapList.get(clusterIndex).entrySet());
					Collections.sort(sortedMMRList, sortComparator);
					sortedMMRListPerCluster.add(sortedMMRList);
				}
				
				for(int clusterIndex=0; clusterIndex<sortedMMRListPerCluster.size(); clusterIndex++)
				{
					fos = new FileOutputStream(args+sentimentCategories.get(sentimentCatIndex)+".cluster"+clusterIndex+".mmrmd");
					System.out.println("MMR MD sentences: ");
					ArrayList<Map.Entry<String, Double>> sortedMMRList = sortedMMRListPerCluster.get(clusterIndex);
					for(Map.Entry<String, Double> printCandidate : sortedMMRList)
					{
						fos.write(("In Cluster: "+sentencesToClusterCenterMap.get(printCandidate.getKey())
						+" Relevance Score: "+myFormat.format(sim1ScoresList.get(clusterIndex).get(printCandidate.getKey()))
						+" Anti-Redundancy Score: "+myFormat.format(sim2ScoresList.get(clusterIndex).get(printCandidate.getKey()))
						+" MMR Score: "+myFormat.format(printCandidate.getValue())
						+" Sentence: "+printCandidate.getKey()+"\n").getBytes("UTF8"));			
					}
				}
				
				for(Map.Entry<Integer, Integer> clusterIndex : sortedClusterToClusterSizeList)
				{
					ArrayList<Map.Entry<String, Double>> sortedMMRList =
							sortedMMRListPerCluster.get(clusterIndex.getKey());
					int noOfSentencesToPick = 0;
					for(Map.Entry<String, Double> printCandidate : sortedMMRList)
					{
						if(noOfSentencesToPick < noOfSentencesFromEachCluster[clusterIndex.getKey()])
						{
							summaryBeanList.get(sentimentCatIndex).addSentence(printCandidate.getKey());
						}
						else
						{
							break;
						}
						noOfSentencesToPick++;
					}
				}
				System.out.println();
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
		return catSumBean;
	}
	private static class SortComparator implements Comparator<Map.Entry<String, Double>>
	{
		@Override
		public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) 
		{
			return o2.getValue().compareTo(o1.getValue());
		}
	}
	private static class SortComparatorInteger implements Comparator<Map.Entry<Integer, Integer>>
	{
		@Override
		public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) 
		{
			return o2.getValue().compareTo(o1.getValue());
		}
	}
}
