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
		pipeline = StanfordLoadAnnotaters.getInstance().getPipeLine();
	}
	  
	/* Usage
	 * StanfordSentimentAnalysis sent = new StanfordSentimentAnalysis();
		Map<String, Object> stats = sent.getScore(text);
	 */
	public Map<SentimentClass, Double> getScore(String text) 
	{
		Map<SentimentClass, Double> stats = new HashMap<SentimentClass, Double>();
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
	    stats.put(SentimentClass.Positive, posScore);
    	stats.put(SentimentClass.Negative, negScore);
	    return stats;
	}
}
