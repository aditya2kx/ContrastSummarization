package source;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import static source.SentimentClass.Positive;
import static source.SentimentClass.Negative;
import static source.SentimentClass.Neutral;

import org.json.JSONObject;

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

	public static SentimentClass getSentimentClass(String reviewJsonString) throws UnsupportedEncodingException{
		JSONObject jsonObj = new JSONObject(reviewJsonString);
		String review = jsonObj.getString("text");
		double ratingScore = ratingScoreUtil[jsonObj.getInt("stars")];
		
		//Splat Service
		Map<SentimentClass, Double> splatSentiMap = SplatService.getSentimentValues(review);
		double splatPosScore = splatSentiMap.get(Positive);
		double splatNegScore = splatSentiMap.get(Negative);
		double splatFinalScore = splatPosScore - splatNegScore;
		
		//Stanford Sentiment Analyzer
		Map<SentimentClass, Double> stanfordSentiMap = stanfordSentiAnalyzer.getScore(review);
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
	public static void main(String[] args) throws UnsupportedEncodingException
	{
		System.out.println(EnsembledSentimentAnalyzer.getSentimentClass("{\"stars\":5,\"text\":\"In fact, I've been now 3 times before writing this review as I wanted to make sure my 'best pizza' claim was true.\"}")
				+" "+"In fact, I've been now 3 times before writing this review as I wanted to make sure my 'best pizza' claim was true.");
	}
}
