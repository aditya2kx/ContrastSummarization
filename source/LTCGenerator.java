package source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LTCGenerator {

	private Map<String, Integer> termToIndexMap;

	private double[][] featureVector;

	private Set<String> stopWordsSet;

	private List<Map<String, Integer>> documentTermsMap;
	
	private Map<String, Integer> inverseDocumentFreqMap;
	
	public LTCGenerator(List<String> sentencesList){
		documentTermsMap = new ArrayList<>();
		termToIndexMap = new HashMap<>();
		inverseDocumentFreqMap = new HashMap<>();
		
		Set<String> termsList = getUniqueTerms(sentencesList);
		int count = 0;
		for(String term : termsList){
			termToIndexMap.put(term, count);
		}
		
		featureVector = new double[termsList.size()][sentencesList.size()];
		
		stopWordsSet = Utils.getStopWords();
	}

	private Set<String> getUniqueTerms(List<String> sentencesList){
		Set<String> wordSet = new HashSet<>();
		String[] split;
		Map<String, Integer> termsMap;
		int val;
		for(String sentence : sentencesList){
			termsMap = new HashMap<>();
			split = sentence.split("\\s+");
			for(String term : split){
				if(!stopWordsSet.contains(term) && !term.matches("[^a-zA-Z0-9]+")){
					val = 0;
					if(termsMap.containsKey(term)){
						val = termsMap.get(term);
					}
					
					termsMap.put(term, ++val);
					wordSet.add(term);
				}
			}
			
			for(String term : termsMap.keySet()){
				val = 0;
				if(inverseDocumentFreqMap.containsKey(term)){
					val = inverseDocumentFreqMap.get(term);
				}
				
				inverseDocumentFreqMap.put(term, ++val);
			}
			
			documentTermsMap.add(termsMap);
		}
		
		return wordSet;
	}
	
	public double[][] calculateTLC(){
		int tf, isf, termIndex, docIter = 0;
		double ltcnum = 0.0, ltcdenom;
		int totalDocs = documentTermsMap.size();
		Map<String, Double> ltcMap;
		for(Map<String, Integer> termMap : documentTermsMap){
			ltcdenom = 0.0;
			Set<String> termSet = termMap.keySet();
			ltcMap = new HashMap<>();
			
			for(String term : termSet){
				tf = termMap.get(term);
				isf = inverseDocumentFreqMap.get(term);
				
				ltcnum = (1.0 + Math.log(tf) * Math.log(totalDocs/isf));
				ltcdenom += Math.pow(ltcnum, 2.0);
				
				ltcMap.put(term, ltcnum);
			}
			
			//Calculate the feature vector
			for(String term : termSet){
				termIndex = termToIndexMap.get(term);
				featureVector[docIter][termIndex] = (double) ltcnum/ltcdenom;
			}
			
			docIter++;
		}
		
		return featureVector;
	}
}
