package source;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;

public class PhraseExtractor {

	private static PhraseExtractor phraseExtractor;

	private StanfordCoreNLP pipeline;

	private PhraseExtractor(){
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, parse");
		pipeline = new StanfordCoreNLP(props);
	}

	public static PhraseExtractor getInstance(){
		if(phraseExtractor == null){
			phraseExtractor = new PhraseExtractor();
		}

		return phraseExtractor;
	}

	public List<String> getExtractedPhrases(String reviewPassage) throws IOException{
		Annotation reviewTextDocument = new Annotation(reviewPassage);
		pipeline.annotate(reviewTextDocument);
		List<CoreMap> reviewSentencesMap = reviewTextDocument.get(SentencesAnnotation.class);

		Annotation phraseTextDocument;
		List<CoreMap> phraseSentencesList;
		Tree phraseTreeModel;
		List<String> phrasesList = new LinkedList<>();
		for(CoreMap reviewSentence : reviewSentencesMap){
			phraseTextDocument = new Annotation(reviewSentence.toString());
			pipeline.annotate(phraseTextDocument);
			phraseSentencesList = phraseTextDocument.get(SentencesAnnotation.class);

			for(CoreMap phraseSentenceMap : phraseSentencesList){
				phraseTreeModel = phraseSentenceMap.get(TreeAnnotation.class);
				phrasesList.addAll(phraseExtract(phraseTreeModel));
			}
		}

		return phrasesList;
	}

	public List<String> phraseExtract(Tree tree) throws IOException{
		List<String> phrasesList = new ArrayList<String>();
		List<Tree> subTrees = tree.getChildrenAsList();
		List<Tree> childLeaves;
		String leavesAsString;
		for(Tree parentTree : subTrees){
			if(parentTree.label().toString().equals("S")){
				List<Tree> childrenTrees = parentTree.getChildrenAsList();
				if(containsSubjectChildren(childrenTrees)){
					for(Tree childTree : childrenTrees){
						childLeaves = childTree.getLeaves();
						leavesAsString = getLeavesAsString(childLeaves);
						phrasesList.add(leavesAsString);
					}
				}else{
					childLeaves = parentTree.getLeaves();
					leavesAsString = getLeavesAsString(childLeaves);
					phrasesList.add(leavesAsString);
				}
			}else{
				childLeaves = parentTree.getLeaves();
				leavesAsString = getLeavesAsString(childLeaves);
				phrasesList.add(leavesAsString);
			}
		}

		return phrasesList;
	}

	private static boolean containsSubjectChildren(List<Tree> childrenTrees){
		for(Tree child : childrenTrees){
			if(child.label().toString().equals("S")){
				return true;
			}
		}

		return false;
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

}
