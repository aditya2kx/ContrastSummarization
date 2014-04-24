package source;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;


public class Phrase_Extractor 
{
	private static Set<String> keywordSet;
	private static Levenshtein levenshtein;
	public static void phraseExtract(Tree t,OutputStream out) throws UnsupportedEncodingException, IOException
	{
		List<Tree> leaves = t.getLeaves();
		ArrayList<String> nounWordTags = new ArrayList<>();
		ArrayList<String> nounPhraseTags = new ArrayList<>();
		ArrayList<String> rule1PhraseTags = new ArrayList<>();
		nounWordTags.add("NN");
		nounWordTags.add("NNS");
		nounWordTags.add("NNP");
		nounWordTags.add("NNPS");		
		nounPhraseTags.add("NP");
		rule1PhraseTags.add("ADJP");
		rule1PhraseTags.add("VP");
		Tree parent = null;
		for(Tree leaf : leaves)
		{
			parent = leaf.parent(t);
			String term = leaf.label().toString();
			boolean containsInList = false;
			
			for(String keyword : keywordSet)
			{
				if(Character.toLowerCase(term.charAt(0)) == Character.toLowerCase(keyword.charAt(0)))
				{
					float score = levenshtein.getSimilarity(keyword, term);
					if(score >= 0.8)
					{
						containsInList = true;
						break;
					}
				}				
			}
			if(containsInList && 
					nounWordTags.contains(parent.label().toString()))
			{
				parent = parent.parent(t);
				if(nounPhraseTags.contains(parent.label().toString()))
				{
					List<Tree> siblings = parent.siblings(t);
					for(Tree sibling : siblings)
					{
						if(rule1PhraseTags.contains(sibling.label().toString()))
						{
							parent = parent.parent(t);
							//String check = parent.label().toString();
							//System.out.println(check);
							List<Tree> l = parent.getLeaves();
							out.write((l.toString()+"\n").getBytes("UTF8"));
							break;
						}
					}
				}
			}
			//System.out.println(parent.label().toString());
		}
	}
	
	public static void main(String[] args)
	{
		FileInputStream fis = null;
		FileOutputStream fos = null;
		BufferedReader br = null;
		Properties props;
		//LexicalizedParserQuery myParserQuery;
		StanfordCoreNLP pipeline;
		Annotation document;
		props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, parse");
		//myParserQuery = LexicalizedParser.loadModel().lexicalizedParserQuery();
		pipeline = new StanfordCoreNLP(props);
		levenshtein = new Levenshtein();
		//System.out.println(LexicalizedParser.DEFAULT_PARSER_LOC);
		
		try
		{
			fis = new FileInputStream(args[0]);
			fos = new FileOutputStream(args[0]+".phrases");
			String readLine;
			keywordSet = new HashSet<>();
			//keywords file
			try(BufferedReader reader = new BufferedReader(new FileReader(new File(args[1])))){
				while((readLine = reader.readLine()) != null){
					keywordSet.add(readLine);
				}
			}
			String currentLine = null;
			br = new BufferedReader(new InputStreamReader(fis));
			JSONObject json = null;
			while( (currentLine=br.readLine())!=null)
			{
				try
				{
					json = new JSONObject(currentLine);
				}
				catch(JSONException e)
				{
					System.out.println("Line: "+currentLine+" not a json object");
					System.exit(1);
				}
				String text = json.getString("text");
				document = new Annotation(text);
				pipeline.annotate(document);
				List<CoreMap> sentences = document.get(SentencesAnnotation.class);
				CoreMap sentence = sentences.get(0);
				Tree tree = sentence.get(TreeAnnotation.class);
				//List<Tree> leaves = tree.getLeaves();
				//tree.indentedListPrint();
				phraseExtract(tree, fos);
			}
		}
		catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}
