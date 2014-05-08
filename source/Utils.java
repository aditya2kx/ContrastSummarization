package source;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

public class Utils {
	private static Set<String> stopWordsSet;
	
	private static Set<String> skipWordsSet;
	
	private static Levenshtein levenshtein;

	static {
		try {
			//Read the stop words
			readStopWords();
			
			//Read the skip words
			readSkipWords();
			
			//Levenshtein
			levenshtein = new Levenshtein();
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void readStopWords() throws FileNotFoundException, IOException{
		stopWordsSet = new HashSet<>();
		
		try(BufferedReader bufReader = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("stop-words.txt")))){
			String readLine;
			while((readLine = bufReader.readLine()) != null){
				stopWordsSet.add(readLine.toLowerCase());
			}
		}
	}
	
	private static void readSkipWords() throws FileNotFoundException, IOException{
		skipWordsSet = new HashSet<>();
		try(BufferedReader bufReader = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("skip-words.txt")))){
			String readLine;
			while((readLine = bufReader.readLine()) != null){
				skipWordsSet.add(readLine.toLowerCase());
			}
		}
	}

	public static Set<String> getStopWords(){
		return stopWordsSet;
	}
	
	public static boolean isSimilar(Set<String> keywordSet, String term){
		for(String keyword : keywordSet){
			String lowerCase = term.toLowerCase();
			if(Character.toLowerCase(term.charAt(0)) == Character.toLowerCase(keyword.charAt(0)) 
					&& !skipWordsSet.contains(lowerCase)){
				float score = levenshtein.getSimilarity(keyword, term);
				if(score >= 0.8){
					return true;
				}
			}
		}
		
		return false;
	}
	
	public static String splitSentenceHavingPattern(String text)
	{
		boolean patternExists = true;
		
		while(patternExists)
		{
			Pattern p = Pattern.compile("(.)*([a-z]{2})(\\.)([a-zA-Z]{2})(.)*");
			Matcher m = p.matcher(text);
			patternExists = m.matches();
			if(patternExists)
			{
				int endIndex = m.start(3);
				text = text.substring(0, endIndex+1) + " " + text.substring(endIndex+1);
			}
		}
		return text;
	}
	
	public static String getSimilarKeyword(Set<String> keywordSet, String term){
		for(String keyword : keywordSet){
			String lowerCase = term.toLowerCase();
			if(Character.toLowerCase(term.charAt(0)) == Character.toLowerCase(keyword.charAt(0)) 
					&& !skipWordsSet.contains(lowerCase)){
				float score = levenshtein.getSimilarity(keyword, term);
				if(score >= 0.8){
					return keyword;
				}
			}
		}
		
		return null;
	}
	
	public static void main(String[] args) {
		System.out.println(getStopWords());
	}
}
