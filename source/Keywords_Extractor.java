package source;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

class ScoreComparator<String, Integer extends Comparable<Integer>>
					implements Comparator<Map.Entry<String, Integer>> 
{
	@Override
	public int compare(Entry<String, Integer> arg0, Entry<String, Integer> arg1)
	{
		Integer count1 =  arg0.getValue();
		Integer count2 = arg1.getValue();
		//java.lang.String str1 = (" " + count1 + arg0.getKey());
		//java.lang.String str2 = (" " + count2 + arg1.getKey());
		return -1 * (count1.compareTo(count2));
	}	
}

public class Keywords_Extractor 
{
	public static void main(String[] args) throws IOException, IOException 
	{
		String filename = args[0];
		FileInputStream fis = null;
		BufferedReader br = null;
		FileOutputStream fos = null;
		HashMap<String, Integer> unigrams = new HashMap<String,Integer>();
		HashMap<String, Integer> bigrams = new HashMap<String,Integer>();
		HashMap<String, Integer> trigrams = new HashMap<String,Integer>();
		
		ScoreComparator<String, Integer> myComp = 
				new ScoreComparator<String, Integer>();
		TreeMap<Map.Entry<String, Integer>, Integer> unigrams_sorted = 
				new TreeMap<Map.Entry<String, Integer>, Integer>(myComp);
		TreeMap<Map.Entry<String, Integer>, Integer> bigrams_sorted = 
				new TreeMap<Map.Entry<String, Integer>, Integer>(myComp);
		TreeMap<Map.Entry<String, Integer>, Integer> trigrams_sorted = 
				new TreeMap<Map.Entry<String, Integer>, Integer>(myComp);
	    Properties props = new Properties();
	    props.put("annotators", "tokenize, ssplit");
	    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

	    
		try
		{
			fis = new FileInputStream(filename);
			br = new BufferedReader(new InputStreamReader(fis));
			String currentLine = null;
			
			while((currentLine = br.readLine())!=null)
			{
				String previousWord = null, previousPreviousWord = null,
						currentWord = null;
				//String tokens[] = currentLine.split(" ");
				Annotation document = new Annotation(currentLine);
			    pipeline.annotate(document);
			    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
			    
			    for(CoreMap sentence: sentences)
			    {
			      for (CoreLabel word: sentence.get(TokensAnnotation.class)) 
			      {
			    	  	String token = word.get(TextAnnotation.class);
			    	  	currentWord = token.toLowerCase();
						if(unigrams.containsKey(currentWord))
						{
							unigrams.put(currentWord, unigrams.get(currentWord)+1);
						}
						else
						{
							unigrams.put(currentWord, 1);
						}
						
						if(previousWord!=null && bigrams.containsKey(previousWord+" "+currentWord))
						{
							bigrams.put(previousWord+" "+currentWord, bigrams.get(previousWord+" "+currentWord)+1);
						}
						else if(previousWord!=null)
						{
							bigrams.put(previousWord+" "+currentWord, 1);
						}
						if(previousPreviousWord!=null && previousWord!=null &&
								trigrams.containsKey(previousPreviousWord+" "+previousWord+" "+currentWord))
						{
							trigrams.put(previousPreviousWord+" "+previousWord+" "+currentWord,
									trigrams.get(previousPreviousWord+" "+previousWord+" "+currentWord)+1);
						}
						else if(previousPreviousWord!=null && previousWord!=null)
						{
							trigrams.put(previousPreviousWord+" "+previousWord+" "+currentWord, 1);
						}
						previousPreviousWord = previousWord;
						previousWord = currentWord;

			      }
			    }
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
		for(Map.Entry<String, Integer> entry : unigrams.entrySet())
		{
			unigrams_sorted.put(entry, new Integer(0));
		}
		fos = new FileOutputStream(filename+".uni.out");
		for(Map.Entry<Map.Entry<String, Integer>, Integer> entry : unigrams_sorted.entrySet())
		{
			fos.write((entry.getKey().getKey() +" Count: "+entry.getKey().getValue()+"\n").getBytes("UTF8"));
		}
		fos.close();
		
		for(Map.Entry<String, Integer> entry : bigrams.entrySet())
		{
			bigrams_sorted.put(entry, new Integer(0));
		}
		fos = new FileOutputStream(filename+".bi.out");
		for(Map.Entry<Map.Entry<String, Integer>, Integer> entry : bigrams_sorted.entrySet())
		{
			fos.write((entry.getKey().getKey() +" Count: "+entry.getKey().getValue()+"\n").getBytes("UTF8"));
		}
		fos.close();
		
		for(Map.Entry<String, Integer> entry : trigrams.entrySet())
		{
			trigrams_sorted.put(entry, new Integer(0));
		}
		fos = new FileOutputStream(filename+".tri.out");
		for(Map.Entry<Map.Entry<String, Integer>, Integer> entry : trigrams_sorted.entrySet())
		{
			fos.write((entry.getKey().getKey() +" Count: "+entry.getKey().getValue()+"\n").getBytes("UTF8"));
		}
		fos.close();
		System.out.println();
	}

}
