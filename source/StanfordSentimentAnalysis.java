package source;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.ejml.simple.SimpleMatrix;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

public class StanfordSentimentAnalysis 
{
	private static final NumberFormat NF = new DecimalFormat("0.0000");
	Properties props;
	StanfordCoreNLP pipeline;
	  
	public StanfordSentimentAnalysis()
	{
		props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
		pipeline = new StanfordCoreNLP(props);
	}
	  
	/* Usage
	 * StanfordSentimentAnalysis sent = new StanfordSentimentAnalysis();
		Map<String, Object> stats = sent.getScore(text);
	 */
	public Map<String, Object> getScore(String text) 
	{
	    Map<String, Object> stats = new HashMap<String, Object>();
		Annotation document = new Annotation(text);
	    pipeline.annotate(document);
	    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
	    if(sentences.size()!=1)
	    {
	    	System.out.println("Input text is not 1 sentence");
	    	System.exit(1);
	    }
	    CoreMap sentence = sentences.get(0); 
	    Tree tree = sentence.get(SentimentCoreAnnotations.AnnotatedTree.class);
	    if (tree.isLeaf()) 
	    {
	    	System.out.println("Input text has improper format");
	    	System.exit(1);
	    }
	    SimpleMatrix vector = RNNCoreAnnotations.getPredictions(tree);
	    double posScore = 0;
	    double negScore = 0;
	    posScore += Double.parseDouble(NF.format(vector.get(3)));
	    posScore += Double.parseDouble(NF.format(vector.get(4)));
	    negScore += Double.parseDouble(NF.format(vector.get(0)));
	    negScore += Double.parseDouble(NF.format(vector.get(1)));
	    if(posScore >= negScore)
	    {
	    	stats.put("class", SentimentClass.Positive);
	    	stats.put("score", posScore);
	    }
	    else
	    {
	    	stats.put("class", SentimentClass.Negative);
	    	stats.put("score", negScore);
	    }
	    return stats;
	}
}
