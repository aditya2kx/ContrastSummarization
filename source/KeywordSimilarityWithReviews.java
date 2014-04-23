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

import org.json.JSONObject;

import uk.ac.shef.wit.simmetrics.similaritymetrics.JaroWinkler;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class KeywordSimilarityWithReviews {

	private static Set<String> keywordSet;

	private static JaroWinkler jaroWinkler;

	private static Levenshtein levenshtein;
	
	private static Set<String> skipWordsSet;
	//private static JaccardSimilarity jc;

	public static void main(String[] args) throws FileNotFoundException, IOException {
		if(args.length < 3){
			System.err.println("Usage: KeywordSimilarityWithReviews <keywords-file> <sentences-input-file> <skip-words-list>");
			System.exit(0);
		}

		//"C:\Users\Santosh\google drive\Natural Language Processing Project\Dataset"
		//"C:\\Users\\Santosh\\google drive\\Natural Language Processing Project\\Dataset\\\yelp_phoenix_academic_dataset.rest_reviews_splitsplitter_out\\review-sentences-file-15"
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

		String keywordsFile = args[0];//"aggregate_keywords_file.txt";
		//String sentencesFile = "yelp_phoenix_academic_dataset.rest_reviews_split";
		String sentencesFile = args[1];//"TestFile";
		String skipWordsFile = args[2];
		String outputFile = args[1]+".out";

		String readLine;
		keywordSet = new HashSet<>();
		//keywords file
		try(BufferedReader reader = new BufferedReader(new FileReader(new File(keywordsFile)))){
			while((readLine = reader.readLine()) != null){
				keywordSet.add(readLine);
			}
		}
		
		//Read the skip words
		skipWordsSet = new HashSet<>();
		try(BufferedReader reader = new BufferedReader(new FileReader(new File(skipWordsFile)))){
			while((readLine = reader.readLine()) != null){
				skipWordsSet.add(readLine);
			}
		}

		//Create the jaro winkler instance
		jaroWinkler = new JaroWinkler();
		levenshtein = new Levenshtein();

		//sentences file
		//"keyword_training_sentences_file_test"
		int totalReviews = 0;
		int categoryReviews = 0;
		JSONObject jsonObject;
		String reviewText;
		int rating;
		try(BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outputFile)));
				BufferedReader reader = new BufferedReader(new FileReader(new File(sentencesFile)))){
			while((readLine = reader.readLine()) != null){
				System.out.println(readLine);
				jsonObject = new JSONObject(readLine);
				reviewText = jsonObject.getString("text");
				rating = jsonObject.getInt("stars");
				
				Annotation document = new Annotation(reviewText);
				pipeline.annotate(document);
				List<CoreMap> sentences = document.get(SentencesAnnotation.class);
				totalReviews += sentences.size();
				for(CoreMap sentence: sentences)
				{
					String outputReview = getReviewSentence(sentence);
					if(outputReview != null){
						writer.write(getJsonString(rating, outputReview));
						writer.write("\n");
						categoryReviews++;
					}
				}
			}
		}
		
		System.out.println("Total Reviews: " + totalReviews);
		System.out.println("Category Wise Reviews: " + categoryReviews);
		System.out.println("Percentage of Category Wise Reviews: " + (categoryReviews * 100.0/totalReviews)+"%");
	}
	
	private static String getJsonString(int stars, String reviewText){
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("stars", stars);
		jsonObj.put("text", reviewText);
		
		return jsonObj.toString();
	}

	private static String getReviewSentence(CoreMap sentence){
		Map<Integer, List<String>> ngramMap = new HashMap<>();
		List<CoreLabel> coreLabelList = sentence.get(TokensAnnotation.class);
		ngramMap.put(1, getUnigrams(coreLabelList));
		ngramMap.put(2, getBigrams(coreLabelList));
		ngramMap.put(3, getTrigrams(coreLabelList));

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
						return sentence.toString();// + "->->" + keyword + "->->" + score;
					}
				}
			}
		}

		return null;
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

	/*private static boolean wordMatches(String word, String keyword){
		return jaroWinkler.getSimilarity(keyword, word) > 0.96;
	}

	private static boolean wordMatches(String word){
		for(String keyword : keywordSet){
			float score = jaroWinkler.getSimilarity(keyword, word);
			if(score > 0.96){
				return true;
			}
		}
		return false;
	}

	private static float wordMatchesWithScore(String word){
		for(String keyword : keywordSet){
			float score = jaroWinkler.getSimilarity(keyword, word);
			if(score > 0.96){
				return score;
			}
		}
		return Float.MIN_VALUE;
	}*/

}
