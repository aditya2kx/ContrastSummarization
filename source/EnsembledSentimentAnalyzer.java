package source;

import java.io.UnsupportedEncodingException;
import java.util.Map;

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

	public static SentimentClass getSentimentClass(int rating, String reviewText) throws UnsupportedEncodingException{
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
			return Neutral;
		}else if(ensembledScore > 0.05){
			return Positive;
		}else if(ensembledScore < -0.05){
			return Negative;
		}
		
		return null;
	}
}
