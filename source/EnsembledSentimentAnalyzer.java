package source;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Properties;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import static source.SentimentClass.Positive;
import static source.SentimentClass.Negative;
import static source.SentimentClass.Neutral;

public class EnsembledSentimentAnalyzer {
	
	private static StanfordSentimentAnalysis stanfordSentiAnalyzer;
	private static double ratingScoreUtil[];
	
	static{
		stanfordSentiAnalyzer = new StanfordSentimentAnalysis();
		ratingScoreUtil = new double[6];
		ratingScoreUtil[0] = -500;
		ratingScoreUtil[1] = -1;
		ratingScoreUtil[2] = -0.5;
		ratingScoreUtil[3] = 0;
		ratingScoreUtil[4] = 0.5;
		ratingScoreUtil[5] = 1;
	}

	public static SentimentMeta getSentimentClass(int rating, String reviewText) throws UnsupportedEncodingException{
		double ratingScore = ratingScoreUtil[rating];
		
		//Splat Service
		Map<SentimentClass, Double> splatSentiMap = SplatService.getSentimentValues(reviewText);
		double splatPosScore = splatSentiMap.get(Positive);
		double splatNegScore = splatSentiMap.get(Negative);
		double splatFinalScore = splatPosScore - splatNegScore;
		
		//Stanford Sentiment Analyzer
		Map<SentimentClass, Double> stanfordSentiMap = stanfordSentiAnalyzer.getScore(reviewText);
		double stanfordPosScore = stanfordSentiMap.get(Positive);
		double stanfordNegScore = stanfordSentiMap.get(Negative);
		double stanfordFinalScore = stanfordPosScore - stanfordNegScore;
		
		
		double ensembledScore = 0.25 * splatFinalScore + 0.25 * stanfordFinalScore + 0.5 * ratingScore;

		if(ensembledScore >= -0.05 && ensembledScore <= 0.05){
			return new SentimentMeta(Neutral, ensembledScore);
		}else if(ensembledScore > 0.05){
			return new SentimentMeta(Positive, ensembledScore);
		}else if(ensembledScore < -0.05){
			return new SentimentMeta(Negative, ensembledScore);
		}
		
		return null;
	}
	
	public static class SentimentMeta {
		private SentimentClass sentimentClass;
		private double ensembledScore;
		
		public SentimentMeta(SentimentClass sentimentClass, double ensembledScore){
			this.sentimentClass = sentimentClass;
			this.ensembledScore = ensembledScore;
		}

		public SentimentClass getSentimentClass() {
			return sentimentClass;
		}

		public double getEnsembledScore() {
			return ensembledScore;
		}
	}
	
	public static void main(String[] args) throws UnsupportedEncodingException {
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, parse");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		
		System.out.println(EnsembledSentimentAnalyzer.getSentimentClass(5, "i am feeling awesome").getSentimentClass());
	}
}
