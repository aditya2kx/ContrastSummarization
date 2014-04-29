package source;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import static source.Utils.isSimilar;
import static source.Utils.getSimilarKeyword;

public class LTCGenerator {

	private Map<String, Integer> termToIndexMap;

	private double[][] featureVector;

	private Set<String> stopWordsSet;

	private List<Map<String, Integer>> documentTermsMap;

	private Map<String, Integer> inverseDocumentFreqMap;

	private String[] featureWords;

	private Map<String, Integer> sentencesToIndexMap;

	private Map<String, Integer> keywordsToIndexMap;

	private List<String> sentencesList;

	private static int KEYWORD_FEATURE_WEIGHT = 2;

	public LTCGenerator(List<String> sentencesList, String keywordsCategoryPath) throws FileNotFoundException, IOException{
		documentTermsMap = new ArrayList<>();
		termToIndexMap = new HashMap<>();
		keywordsToIndexMap = new HashMap<>();
		inverseDocumentFreqMap = new HashMap<>();
		sentencesToIndexMap = new HashMap<>();
		stopWordsSet = Utils.getStopWords();
		sentencesList = new ArrayList<>();

		//Sentence Indexing
		int sentenceIndex = 0;
		for(String sentence : sentencesList){
			if(!sentencesToIndexMap.containsKey(sentence)){
				sentencesToIndexMap.put(sentence, sentenceIndex);
				sentencesList.add(sentence);
			}
		}

		sentencesList = new ArrayList<>(sentencesToIndexMap.keySet());
		Set<String> termsList = getUniqueTerms(sentencesList);
		int indexCount = 0;
		for(String term : termsList){
			termToIndexMap.put(term, indexCount++);
		}

		String[] wordSplit;
		Set<String> keywordsSet = KeywordsFetcher.getInstance(keywordsCategoryPath).getKeywords();
		for(String keyword : keywordsSet){
			wordSplit = keyword.split("\\s+");
			if(wordSplit.length <= 1){
				keywordsToIndexMap.put(keyword, indexCount++);
			}
		}

		featureWords = new String[indexCount];
		featureVector = new double[sentencesToIndexMap.size()][indexCount];
	}

	private Set<String> getUniqueTerms(List<String> sentencesList){
		Set<String> wordSet = new HashSet<>();
		Map<String, Integer> termsMap;
		int val;

		//load the stanford tokenizer model
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

		List<CoreMap> sentences;
		List<CoreLabel> coreLabelList;
		Annotation document;
		for(String sentence : sentencesList){
			termsMap = new HashMap<>();
			document = new Annotation(sentence);
			pipeline.annotate(document);
			sentences = document.get(SentencesAnnotation.class);
			for(CoreMap sentenceMap : sentences){
				coreLabelList = sentenceMap.get(TokensAnnotation.class);
				for(CoreLabel coreLabel : coreLabelList){
					String term = coreLabel.get(TextAnnotation.class).toLowerCase();
					if(!stopWordsSet.contains(term) && !term.matches("[^a-zA-Z0-9]+")){
						val = 0;
						if(termsMap.containsKey(term)){
							val = termsMap.get(term);
						}

						termsMap.put(term, ++val);
						wordSet.add(term);
					}
				}
			}

			System.out.println(termsMap);
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
		Double ltcnum = 0.0, ltcdenom;
		int totalDocs = documentTermsMap.size();
		Map<String, Double> ltcMap;
		String keyword;
		Set<String> documentTermSet;
		Set<String> termSet = termToIndexMap.keySet();

		//Update the feature words vector
		for(String term : termSet){
			termIndex = termToIndexMap.get(term);
			featureWords[termIndex] = term;
		}

		Set<String> keywordSet = keywordsToIndexMap.keySet();
		for(String term : keywordSet){
			termIndex = keywordsToIndexMap.get(term);
			featureWords[termIndex] = term;
		}

		//Calculate the LTC weights
		for(Map<String, Integer> termMap : documentTermsMap){
			ltcdenom = 0.0;
			//Set<String> termSet = termMap.keySet();
			ltcMap = new HashMap<>();
			documentTermSet = termMap.keySet();
			for(String term : documentTermSet){
				tf = termMap.get(term);
				isf = inverseDocumentFreqMap.get(term);

				ltcnum = ((1.0 + Math.log10(tf)) * Math.log10((double)totalDocs/isf));
				ltcdenom += Math.pow(ltcnum, 2.0);

				ltcMap.put(term, ltcnum);

				//Category keywords in the document
				keyword = getSimilarKeyword(keywordSet, term);
				if(keyword != null){
					termIndex = keywordsToIndexMap.get(keyword);
					featureVector[docIter][termIndex] += KEYWORD_FEATURE_WEIGHT;
				}
			}

			//Calculate the feature vector
			ltcdenom = Math.sqrt(ltcdenom);
			for(String term : termSet){
				//Terms in the document
				termIndex = termToIndexMap.get(term);
				ltcnum = ltcMap.get(term);
				ltcnum = (ltcnum == null)  ? 0 : ltcnum;
				featureVector[docIter][termIndex] = (double) ltcnum/ltcdenom;
			}

			docIter++;
		}

		return featureVector;
	}

	public String[] getTermSet(){
		return featureWords;
	}

	public Map<String, Integer> getSentencesToIndexMap(){
		return sentencesToIndexMap;
	}

	public Map<String, Integer> getKeywordsMap(){
		return keywordsToIndexMap;
	}
	
	public List<String> getSentencesList(){
		return sentencesList;
	}
}
