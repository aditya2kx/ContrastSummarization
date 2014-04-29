package source;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class MMR_MD_Utility 
{
	double weights[];
	double lambda;
	Properties props;
	StanfordCoreNLP pipeline;
	Set<String> keywords;
	public MMR_MD_Utility(double weights[], double lambda, String filename)
	{
		this.weights = weights;
		this.lambda = lambda;
		props = new Properties();
		props.put("annotators", "tokenize, ssplit");
		pipeline = new StanfordCoreNLP(props);
		keywords = KeywordsFetcher.getInstance(filename).keywords;
	}
	
	public double similarityPassageAndCategory(String passage)
	{
		double score = 0;
		
		Annotation document = new Annotation(passage);
		pipeline.annotate(document);
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		if(sentences.size()>1)
		{
			System.out.println("more than 1 sentence... not possible.. bye..");
			System.exit(1);
		}
		
		for(CoreMap sentence: sentences)
		{
			List<CoreLabel> tokens = sentence.get(TokensAnnotation.class);
			for (CoreLabel token : tokens) 
			{
				String word = token.get(TextAnnotation.class);
				if(Utils.isSimilar(keywords, word))
				{
					score++;
				}
			}
			score = Math.log10(score)/Math.log10(tokens.size());
		}
		return score;
	}
	
	public double Similarity_1(String passage, int clusterSize)
	{
		double score = 0;		
		score = weights[0] * similarityPassageAndCategory(passage);
		score += weights[1] * Math.log10((double)(1.0/clusterSize));
		return score;
	}
	
	public double similarityPassageAndSelectedPassages(double datasetForAPassage[],
													 	double datasetForSelectedPassages[][])
	{
		double score = 0;
		for ( int sentenceIndex = 0; sentenceIndex < datasetForSelectedPassages.length;
					sentenceIndex++)
		{
			double distance = 0;
			double selectedSentenceMod = 0;
			double vectorMod = 0;
			for(int featureIndex = 0; featureIndex < datasetForSelectedPassages[sentenceIndex].length;
								featureIndex++)
			{
				distance += 
						(datasetForAPassage[featureIndex] * 
								datasetForSelectedPassages[sentenceIndex][featureIndex]);
				
				selectedSentenceMod += datasetForAPassage[featureIndex] *
						datasetForAPassage[featureIndex];
				
				vectorMod += datasetForSelectedPassages[sentenceIndex][featureIndex] *
						datasetForSelectedPassages[sentenceIndex][featureIndex];
			}
			selectedSentenceMod = Math.sqrt(selectedSentenceMod);
			vectorMod = Math.sqrt(vectorMod);
			distance /= (selectedSentenceMod * vectorMod);
			score += distance;
		}    
		score = Math.log10(score)/Math.log10(datasetForSelectedPassages.length);
		
		return score;
	}
	
	public double Similarity_2(double datasetForAPassage[], double datasetForSelectedPassages[][], 
								boolean belongsToSelectedCluster, int selectedClusterSize)
	{
		double score = 0;
		
		score = weights[2] * similarityPassageAndSelectedPassages(datasetForAPassage,
									datasetForSelectedPassages);
		if(belongsToSelectedCluster)
		{
			score += weights[3] * Math.log10((double)(1.0/selectedClusterSize));
		}
		return score;
	}
}
