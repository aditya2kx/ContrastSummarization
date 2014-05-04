package source;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class ReviewsCategoryPhraseExtractor {

	private static Levenshtein levenshtein;

	private static Set<String> skipWordsSet;
	//private static JaccardSimilarity jc;

	public static void main(String[] args) throws FileNotFoundException, IOException {
		if(args.length < 3){
			System.err.println("Usage: KeywordSimilarityWithReviews <keywords-file> <sentences-input-file> <skip-words-list>");
			System.exit(0);
		}

		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

		String keywordsFile = args[0];//"aggregate_keywords_file.txt";
		//String sentencesFile = "yelp_phoenix_academic_dataset.rest_reviews_split";
		String sentencesFile = args[1];//"TestFile";
		String skipWordsFile = args[2];
		String outputFile = args[1]+".out";

		String readLine;
		Set<String> keywordsSet;
		Map<String, Set<String>> categoryKeywordsMap = KeywordsFetcher.getInstance(keywordsFile).getCategoryKeywordsMap();

		//Read the skip words
		skipWordsSet = new HashSet<>();
		try(BufferedReader reader = new BufferedReader(new FileReader(new File(skipWordsFile)))){
			while((readLine = reader.readLine()) != null){
				skipWordsSet.add(readLine);
			}
		}

		//Create the jaro winkler instance
		levenshtein = new Levenshtein();

		//sentences file
		JSONObject jsonObject;
		String reviewText;
		int rating, reviewId = 0;
		List<String> phrasesList;
		Annotation phraseSentenceDocument;
		List<CoreLabel> tokensList; boolean containsMatch;
		Map<Integer, List<String>> ngramMap;
		PhraseExtractor phraseExtractor = PhraseExtractor.getInstance();
		
		/*JsonFactory jsonFactory = new JsonFactory();
		JsonGenerator jsonGenerator = jsonFactory.createGenerator(Files.newOutputStream(Paths.get(outputFile)));
		jsonGenerator.useDefaultPrettyPrinter();
		ObjectMapper objectMapper = new ObjectMapper();*/
		
		JSONArray inputObject = new JSONArray();
		JSONArray outputObject = new JSONArray();
		JSONObject reviewPhraseObject, categoryObject;
		try(BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outputFile)));
				BufferedReader reader = new BufferedReader(new FileReader(new File(sentencesFile)))){
			while((readLine = reader.readLine()) != null){
				jsonObject = new JSONObject(readLine);
				reviewText = jsonObject.getString("text");
				rating = jsonObject.getInt("stars");
				reviewId++;

				categoryObject = new JSONObject();
				phrasesList = phraseExtractor.getExtractedPhrases(reviewText);
				for(String phraseSentence : phrasesList){
					phraseSentenceDocument = new Annotation(phraseSentence);
					pipeline.annotate(phraseSentenceDocument);
					tokensList = phraseSentenceDocument.get(TokensAnnotation.class);
					ngramMap = getNgramsMap(tokensList);

					for(String category : categoryKeywordsMap.keySet()){
						keywordsSet = categoryKeywordsMap.get(category);
						containsMatch = getReviewSentence(tokensList, ngramMap, keywordsSet);
						if(containsMatch){
							categoryObject.append(category, getJsonStringForPhrase(rating, phraseSentence.toString()));
						}
					}
				}
				
				//Append to the existing input object
				inputObject.put(getJsonStringForReview(reviewId, reviewText));
				
				//Each review phrase object
				reviewPhraseObject = new JSONObject();
				reviewPhraseObject.put("review_id", reviewId);
				reviewPhraseObject.put("categories", categoryObject);
				
				//Append to the existing output object
				outputObject.put(reviewPhraseObject);
			}
		}

		try(BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))){
			writer.append("{\n");
			writer.append("\"input\":");
			writer.append(inputObject.toString(1));
			writer.append(",\"output\":");
			writer.append(outputObject.toString(1));
			writer.append("\n}");
		}
	}

	private static String getJsonStringForPhrase(int stars, String categoryPhrase){
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("stars", stars);
		jsonObj.put("text", categoryPhrase);

		return jsonObj.toString();
	}
	
	private static String getJsonStringForReview(int reviewId, String reviewText){
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("review_id", reviewId);
		jsonObj.put("text", reviewText);

		return jsonObj.toString();
	}

	private static boolean getReviewSentence(List<CoreLabel> coreLabelList, Map<Integer, List<String>> ngramMap, Set<String> keywordSet){
		for(String keyword : keywordSet){
			int wordLen = keyword.split("\\s+").length;
			List<String> ngramsList = ngramMap.get(wordLen);

			if(ngramsList == null){
				ngramsList = getNgrams(coreLabelList, wordLen);
			}

			for(String term : ngramsList){
				if(Character.toLowerCase(term.charAt(0)) == Character.toLowerCase(keyword.charAt(0)) 
						&& !skipWordsSet.contains(term.toLowerCase())){
					float score = levenshtein.getSimilarity(keyword, term);
					if(score >= 0.8){
						return true;// + "->->" + keyword + "->->" + score;
					}
				}
			}
		}

		return false;
	}

	private static Map<Integer, List<String>> getNgramsMap(
			List<CoreLabel> coreLabelList) {
		Map<Integer, List<String>> ngramMap = new HashMap<>();
		ngramMap.put(1, getUnigrams(coreLabelList));
		ngramMap.put(2, getBigrams(coreLabelList));
		ngramMap.put(3, getTrigrams(coreLabelList));
		return ngramMap;
	}

	private static List<String> getNgrams(List<CoreLabel> coreLabelList, int ngram){
		List<String> ngramSet = new ArrayList<>();
		StringBuffer buffer = new StringBuffer();
		int count;
		for(int index = 0; index < coreLabelList.size(); index++){
			if(index + ngram < coreLabelList.size()){
				count = 0;
				while(count < ngram){
					buffer.append(coreLabelList.get(count + index).get(TextAnnotation.class)).append(" ");
					count++;
				}

				ngramSet.add(buffer.toString().trim());
				buffer.setLength(0);
			}
		}

		return ngramSet;
	}

	private static List<String> getTrigrams(List<CoreLabel> coreLabelList){
		List<String> bigramSet = new ArrayList<>();
		for(int index = 0; index < coreLabelList.size(); index++){
			if(index + 2 < coreLabelList.size()){
				bigramSet.add(coreLabelList.get(index).get(TextAnnotation.class) + " " + coreLabelList.get(index + 1).get(TextAnnotation.class) + " " + coreLabelList.get(index + 2).get(TextAnnotation.class));
			}
		}

		return bigramSet;
	}

	private static List<String> getBigrams(List<CoreLabel> coreLabelList){
		List<String> bigramSet = new ArrayList<>();
		for(int index = 0; index < coreLabelList.size(); index++){
			if(index + 1 < coreLabelList.size()){
				bigramSet.add(coreLabelList.get(index).get(TextAnnotation.class) + " " + coreLabelList.get(index + 1).get(TextAnnotation.class));
			}
		}

		return bigramSet;
	}

	private static List<String> getUnigrams(List<CoreLabel> coreLabelList){
		List<String> unigramSet = new ArrayList<>();
		for(int index = 0; index < coreLabelList.size(); index++){
			unigramSet.add(coreLabelList.get(index).get(TextAnnotation.class));
		}

		return unigramSet;
	}

}
