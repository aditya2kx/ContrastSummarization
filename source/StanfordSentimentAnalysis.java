package source;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.ejml.simple.SimpleMatrix;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentUtils;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Generics;

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
	  
	  static enum Output {
	    PENNTREES, VECTORS, ROOT, PROBABILITIES
	  }

	  static enum Input {
	    TEXT, TREES
	  }

	  /**
	   * Sets the labels on the tree (except the leaves) to be the integer
	   * value of the sentiment prediction.  Makes it easy to print out
	   * with Tree.toString()
	   */
	  static void setSentimentLabels(Tree tree) {
	    if (tree.isLeaf()) {
	      return;
	    }

	    for (Tree child : tree.children()) {
	      setSentimentLabels(child);
	    }

	    Label label = tree.label();
	    if (!(label instanceof CoreLabel)) {
	      throw new IllegalArgumentException("Required a tree with CoreLabels");
	    }
	    CoreLabel cl = (CoreLabel) label;
	    cl.setValue(Integer.toString(RNNCoreAnnotations.getPredictedClass(tree)));
	  }

	  /**
	   * Sets the labels on the tree to be the indices of the nodes.
	   * Starts counting at the root and does a postorder traversal.
	   */
	  static int setIndexLabels(Tree tree, int index) {
	    if (tree.isLeaf()) {
	      return index;
	    }

	    tree.label().setValue(Integer.toString(index));
	    index++;
	    for (Tree child : tree.children()) {
	      index = setIndexLabels(child, index);
	    }
	    return index;
	  }

	  /**
	   * Outputs the vectors from the tree.  Counts the tree nodes the
	   * same as setIndexLabels.
	   */
	  static int outputTreeVectors(PrintStream out, Tree tree, int index) {
	    if (tree.isLeaf()) {
	      return index;
	    }

	    out.print("  " + index + ":");
	    SimpleMatrix vector = RNNCoreAnnotations.getNodeVector(tree);
	    for (int i = 0; i < vector.getNumElements(); ++i) {
	      out.print("  " + NF.format(vector.get(i)));
	    }
	    out.println();
	    index++;
	    for (Tree child : tree.children()) {
	      index = outputTreeVectors(out, child, index);
	    }
	    return index;
	  }

	  /**
	   * Outputs the scores from the tree.  Counts the tree nodes the
	   * same as setIndexLabels.
	   */
	  static int outputTreeScores(PrintStream out, Tree tree, int index) {
	    if (tree.isLeaf()) {
	      return index;
	    }

	    out.print("  " + index + ":");
	    SimpleMatrix vector = RNNCoreAnnotations.getPredictions(tree);
	    for (int i = 0; i < vector.getNumElements(); ++i) {
	      out.print("  " + NF.format(vector.get(i)));
	    }
	    out.println();
	    /*index++;
	    for (Tree child : tree.children()) {
	      index = outputTreeScores(out, child, index);
	    }*/
	    return index;
	  }

	  /**
	   * Outputs a tree using the output style requested
	   */
	  static void outputTree(PrintStream out, CoreMap sentence, List<Output> outputFormats) {
	    Tree tree = sentence.get(SentimentCoreAnnotations.AnnotatedTree.class);
	    for (Output output : outputFormats) {
	      switch (output) {
	      case PENNTREES: {
	        Tree copy = tree.deepCopy();
	        setSentimentLabels(copy);
	        out.println(copy);
	        break;
	      }
	      case VECTORS: {
	        Tree copy = tree.deepCopy();
	        setIndexLabels(copy, 0);
	        out.println(copy);
	        outputTreeVectors(out, tree, 0);
	        break;
	      }
	      case ROOT: {
	        out.println("  " + sentence.get(SentimentCoreAnnotations.ClassName.class));
	        break;
	      }
	      case PROBABILITIES: {
	        //Tree copy = tree.deepCopy();
	        //setIndexLabels(copy, 0);
	        //out.println(copy);
	        outputTreeScores(out, tree, 0);
	        break;
	      }
	      default:
	        throw new IllegalArgumentException("Unknown output format " + output);
	      }
	    }
	  }

	  public static void help() {
	    System.err.println("Known command line arguments:");
	    System.err.println("  -sentimentModel <model>: Which model to use");
	    System.err.println("  -parserModel <model>: Which parser to use");
	    System.err.println("  -file <filename>: Which file to process");
	    System.err.println("  -fileList <file>,<file>,...: Comma separated list of files to process.  Output goes to file.out");
	    System.err.println("  -stdin: Process stdin instead of a file");
	    System.err.println("  -input <format>: Which format to input, TEXT or TREES.  Will not process stdin as trees.  Trees need to be binarized");
	    System.err.println("  -output <format>: Which format to output, PENNTREES, VECTOR, PROBABILITIES, or ROOT.  Multiple formats can be specified as a comma separated list.");
	    System.err.println("  -filterUnknown: remove neutral and unknown trees from the input.  Only applies to TREES input");
	  }

	  /**
	   * Reads an annotation from the given filename using the requested input.
	   */
	  public static Annotation getAnnotation(Input inputFormat, String filename, boolean filterUnknown) {
	    switch (inputFormat) {
	    case TEXT: {
	      String text = IOUtils.slurpFileNoExceptions(filename);
	      Annotation annotation = new Annotation(text);
	      return annotation;
	    }
	    case TREES: {
	      List<Tree> trees = SentimentUtils.readTreesWithGoldLabels(filename);
	      if (filterUnknown) {
	        trees = SentimentUtils.filterUnknownRoots(trees);
	      }
	      List<CoreMap> sentences = Generics.newArrayList();
	      
	      for (Tree tree : trees) {
	        CoreMap sentence = new Annotation(Sentence.listToString(tree.yield()));
	        sentence.set(TreeCoreAnnotations.BinarizedTreeAnnotation.class, tree);
	        sentences.add(sentence);
	      }
	      Annotation annotation = new Annotation("");
	      annotation.set(CoreAnnotations.SentencesAnnotation.class, sentences);
	      return annotation;
	    }
	    default:
	      throw new IllegalArgumentException("Unknown format " + inputFormat);
	    }
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
	    posScore *= 100;
	    negScore *= 100;
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
