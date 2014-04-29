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
				if(term.equalsIgnoreCase("sausage")){
					System.out.println("test!");
				}
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
		Set<String> termSet = termToIndexMap.keySet();
		for(Map<String, Integer> termMap : documentTermsMap){
			ltcdenom = 0.0;
			//Set<String> termSet = termMap.keySet();
			ltcMap = new HashMap<>();

			for(String term : termSet){
				Integer tfWrapper = termMap.get(term);
				tf = tfWrapper == null ? 1 : tfWrapper;
				isf = inverseDocumentFreqMap.get(term);

				ltcnum = (1.0 + Math.log(tf) * Math.log(totalDocs/isf));
				ltcdenom += Math.pow(ltcnum, 2.0);

				ltcMap.put(term, ltcnum);
			}

			//Calculate the feature vector
			for(String term : termSet){
				termIndex = termToIndexMap.get(term);
				ltcnum = ltcMap.get(term);
				featureVector[docIter][termIndex] = (double) ltcnum/Math.sqrt(ltcdenom);
			}

			docIter++;
		}

		return featureVector;
	}
}
