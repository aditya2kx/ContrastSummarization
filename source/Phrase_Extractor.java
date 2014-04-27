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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
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

	private static List<String> nounWordTags = new ArrayList<>();
	private static List<String> nounPhraseTags = new ArrayList<>();
	private static List<String> rule1PhraseTags = new ArrayList<>();
	private static List<String> conjunctionPhraseTags = new ArrayList<>();

	static {
		nounWordTags.add("NN");
		nounWordTags.add("NNS");
		nounWordTags.add("NNP");
		nounWordTags.add("NNPS");		
		nounPhraseTags.add("NP");
		rule1PhraseTags.add("ADJP");
		rule1PhraseTags.add("VP");
		conjunctionPhraseTags.add("CC");
		conjunctionPhraseTags.add("CONJP");
	}

	private static Set<String> keywordSet;
	private static Levenshtein levenshtein;
	public static void phraseExtract_approach1(Tree rootNode, OutputStream out1) throws UnsupportedEncodingException, IOException
	{
		List<Tree> termLeaves = rootNode.getLeaves();

		Tree parentNode = null, currentLeafNode;
		boolean matchFound = false, containsInList = false;
		String term, leavesAsString;
		List<Tree> childLeavesSubList;
		StringBuffer buffer = new StringBuffer();
		for(int index = 0; index < termLeaves.size(); index++)
		{
			currentLeafNode = termLeaves.get(index);
			parentNode = currentLeafNode.parent(rootNode);
			term = currentLeafNode.label().toString();
			containsInList = false;
			matchFound = false;
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
					nounWordTags.contains(parentNode.label().toString()))
			{
				parentNode = parentNode.parent(rootNode);
				if(nounPhraseTags.contains(parentNode.label().toString()))
				{
					List<Tree> siblings = parentNode.siblings(rootNode);
					for(Tree sibling : siblings)
					{
						if(rule1PhraseTags.contains(sibling.label().toString()))
						{
							//Set the ADJ/VP flag as found
							matchFound = true;
							break;
						}
					}
				}

				//Generate the sentence
				if(matchFound){
					parentNode = parentNode.parent(rootNode);
					Iterator<Tree> matchSiblingsIter = parentNode.getChildrenAsList().iterator();
					boolean foundConjuction = false;
					if(matchSiblingsIter.hasNext() && !foundConjuction){
						Tree child = matchSiblingsIter.next();
						if(conjunctionPhraseTags.contains(child.label().toString()))
						{
							foundConjuction = true;
						}
						else{
							childLeavesSubList = child.getLeaves();
							index = getNewIndex(termLeaves, childLeavesSubList);
							buffer.append(getLeavesAsString(childLeavesSubList));
						}
					}

					while(matchSiblingsIter.hasNext() && !foundConjuction){
						Tree child = matchSiblingsIter.next();
						if(conjunctionPhraseTags.contains(child.label().toString()))
						{
							foundConjuction = true;
						}
						else{
							childLeavesSubList = child.getLeaves();
							index = getNewIndex(termLeaves, childLeavesSubList);
							buffer.append(" ").append(getLeavesAsString(childLeavesSubList));
						}
					}

					//out1.write(("---> " + buffer.toString()).getBytes());
					//out1.write("\n".getBytes());
				}else{
					//Rule 4. Look for ADJ/VP
					//childLeavesSubList = parentNode.getLeaves();
					//index = getNewIndex(termLeaves, childLeavesSubList);
					//buffer.append(getLeavesAsString(childLeavesSubList));
					parentNode = parentNode.parent(rootNode);
					boolean foundRBSibling = false;
					while(parentNode != null){
						if(rule1PhraseTags.contains(parentNode.label().toString())){

							List<Tree> siblings = parentNode.siblings(rootNode);
							for(Tree sibling : siblings){
								if(sibling.label().toString().equals("RB")){
									foundRBSibling = true;
									break;
								}
							}

							if(foundRBSibling){
								childLeavesSubList = parentNode.parent(rootNode).getLeaves();
								index = getNewIndex(termLeaves, childLeavesSubList);
								buffer.append(getLeavesAsString(childLeavesSubList));
							}else{
								childLeavesSubList = parentNode.getLeaves();
								leavesAsString = getLeavesAsString(childLeavesSubList);
								index = getNewIndex(termLeaves, childLeavesSubList);
								//out1.write(("---> " + leavesAsString).getBytes());
								//out1.write("\n".getBytes());;
								buffer.append(leavesAsString);
							}

							break;
						}

						parentNode = parentNode.parent(rootNode);
					}
				}

				if(buffer.length() > 0){
					out1.write(("---> " + buffer.toString()).getBytes());
					out1.write("\n".getBytes());
				}
				buffer.setLength(0);
			}

		}
	}

	private static int getNewIndex(List<Tree> termLeaves,
			List<Tree> childLeavesSubList) {
		int subListIndex = Collections.indexOfSubList(termLeaves, childLeavesSubList);
		return subListIndex + childLeavesSubList.size() - 1;
	}

	/**
	 * Return the sentence for the leaves
	 * 
	 * @param leaves
	 * @return
	 */
	private static String getLeavesAsString(List<Tree> leaves){
		Iterator<Tree> iter = leaves.iterator();
		StringBuffer buffer = new StringBuffer();
		if(iter.hasNext()){
			buffer.append(iter.next().label().toString());
		}

		while(iter.hasNext()){
			buffer.append(" ").append(iter.next().label().toString());
		}

		return buffer.toString();
	}

	public static void main(String[] args)
	{
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
			String readLine;
			keywordSet = new HashSet<>();
			//keywords file
			try(BufferedReader reader = new BufferedReader(new FileReader(new File(args[1])))){
				while((readLine = reader.readLine()) != null){
					keywordSet.add(readLine);
				}
			}
			String currentLine = null;

			try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(args[0]))));
					FileOutputStream fos = new FileOutputStream(args[0]+".phrases");
					FileOutputStream fos_rule1 = new FileOutputStream(args[0]+".phrases.rule1");
					FileOutputStream fos_rule2 = new FileOutputStream(args[0]+".phrases.rule2")){
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
					//phraseExtract(tree, fos, fos_rule1, fos_rule2);
					fos.write(text.getBytes());
					fos.write("\n".getBytes());
					phraseExtract_approach1(tree, fos);
					//phraseExtract(tree, fos);
				}
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

	public static void phraseExtract(Tree tree, OutputStream fos) throws IOException{
		List<Tree> subTrees = tree.getChildrenAsList();
		List<Tree> childLeaves;
		String leavesAsString;
		for(Tree parentTree : subTrees){
			if(parentTree.label().toString().equals("S")){
				List<Tree> childrenTrees = parentTree.getChildrenAsList();
				if(containsSubjectChildren(childrenTrees)){
					for(Tree childTree : childrenTrees){
						if(containsKeywordMatch(childTree)){
							childLeaves = childTree.getLeaves();
							leavesAsString = getLeavesAsString(childLeaves);
							fos.write(("---> " + leavesAsString).getBytes());
							fos.write("\n".getBytes());
						}
					}
				}else{
					if(containsKeywordMatch(parentTree)){
						childLeaves = parentTree.getLeaves();
						leavesAsString = getLeavesAsString(childLeaves);
						fos.write(("---> " + leavesAsString).getBytes());
						fos.write("\n".getBytes());
					}
				}
			}else{
				if(containsKeywordMatch(parentTree)){
					childLeaves = parentTree.getLeaves();
					leavesAsString = getLeavesAsString(childLeaves);
					fos.write(("---> " + leavesAsString).getBytes());
					fos.write("\n".getBytes());
				}
			}
		}
	}

	private static boolean containsSubjectChildren(List<Tree> childrenTrees){
		for(Tree child : childrenTrees){
			if(child.label().toString().equals("S")){
				return true;
			}
		}

		return false;
	}

	private static boolean containsKeywordMatch(Tree childTree) {
		List<Tree> childLeaves = childTree.getLeaves();
		String term ;
		for(Tree leafNodes : childLeaves){
			term = leafNodes.label().toString();
			for(String keyword : keywordSet)
			{
				if(Character.toLowerCase(term.charAt(0)) == Character.toLowerCase(keyword.charAt(0)))
				{
					float score = levenshtein.getSimilarity(keyword, term);
					if(score >= 0.8)
					{
						return true;
					}
				}				
			}
		}

		return false;
	}
}
