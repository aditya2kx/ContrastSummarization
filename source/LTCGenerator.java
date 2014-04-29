package source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class LTCGenerator {

	private Map<String, Integer> termToIndexMap;

	private double[][] featureVector;

	private Set<String> stopWordsSet;

	private List<Map<String, Integer>> documentTermsMap;

	private Map<String, Integer> inverseDocumentFreqMap;
	
	private String[] featureWords;

	public LTCGenerator(List<String> sentencesList){
		documentTermsMap = new ArrayList<>();
		termToIndexMap = new HashMap<>();
		inverseDocumentFreqMap = new HashMap<>();
		stopWordsSet = Utils.getStopWords();

		Set<String> termsList = getUniqueTerms(sentencesList);
		int count = 0;
		for(String term : termsList){
			termToIndexMap.put(term, count++);
		}

		featureWords = new String[termsList.size()];
		featureVector = new double[sentencesList.size()][termsList.size()];
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
		Set<String> documentTermSet;
		Set<String> termSet = termToIndexMap.keySet();
		
		//Update the feature words vector
		for(String term : termSet){
			termIndex = termToIndexMap.get(term);
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
			}
			
			//Calculate the feature vector
			ltcdenom = Math.sqrt(ltcdenom);
			for(String term : termSet){
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
}
