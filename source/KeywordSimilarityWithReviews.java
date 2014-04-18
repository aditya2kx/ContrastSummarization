package source;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import uk.ac.shef.wit.simmetrics.similaritymetrics.JaroWinkler;
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
	//private static JaccardSimilarity jc;

	public static void main(String[] args) throws FileNotFoundException, IOException {
		if(args.length < 2){
			System.err.println("Usage: KeywordSimilarityWithReviews <keywords-file> <sentences-input-file>");
			System.exit(0);
		}
		
	    Properties props = new Properties();
	    props.put("annotators", "tokenize, ssplit");
	    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

	    String keywordsFile = args[0];//"aggregate_keywords_file.txt";
		//String sentencesFile = "yelp_phoenix_academic_dataset.rest_reviews_split";
		String sentencesFile = args[1];//"TestFile";
		String outputFile = args[1]+".out";

		String readLine;
		keywordSet = new HashSet<>();
		//keywords file
		try(BufferedReader reader = new BufferedReader(new FileReader(new File(keywordsFile)))){
			while((readLine = reader.readLine()) != null){
				keywordSet.add(readLine);
			}
		}

		//Create the jaro winkler instance
		jaroWinkler = new JaroWinkler();

		//sentences file
		//"keyword_training_sentences_file_test"
		try(BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outputFile)));
				BufferedReader reader = new BufferedReader(new FileReader(new File(sentencesFile)))){
			while((readLine = reader.readLine()) != null){
				//String[] wordsArray = readLine.split("\\s+");
			    Annotation document = new Annotation(readLine);
			    pipeline.annotate(document);
			    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
			    
			    for(CoreMap sentence: sentences)
			    {
			      for (CoreLabel token: sentence.get(TokensAnnotation.class)) 
			      {
			        String word = token.get(TextAnnotation.class);
			        if(wordMatches(word.toLowerCase())){
						writer.write(readLine);
						writer.write("\n");
						break;
					}
			      }
			    }
			}
		}
	}

	public static boolean wordMatches(String word){
		for(String keyword : keywordSet){
			float score = jaroWinkler.getSimilarity(keyword, word);
			if(score > 0.96){
				return true;
			}
		}
		return false;
	}

	public static float wordMatchesWithScore(String word){
		for(String keyword : keywordSet){
			float score = jaroWinkler.getSimilarity(keyword, word);
			if(score > 0.96){
				return score;
			}
		}
		return Float.MIN_VALUE;
	}

}
