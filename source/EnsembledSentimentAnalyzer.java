package source;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import static source.SentimentClass.Positive;
import static source.SentimentClass.Negative;
import static source.SentimentClass.Neutral;

import org.json.JSONObject;

public class EnsembledSentimentAnalyzer {
	
	private static StanfordSentimentAnalysis stanfordSentiAnalyzer;
	
	static{
		stanfordSentiAnalyzer = new StanfordSentimentAnalysis();
	}

	public static SentimentClass getSentimentClass(String reviewJsonString) throws UnsupportedEncodingException{
		JSONObject jsonObj = new JSONObject(reviewJsonString);
		String review = jsonObj.getString("text");
		double ratingScore =  (double) ((((2 * jsonObj.getInt("stars") - 5 )
				+ 80) * (double) (10/9)) - 100)/100;
		
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
		
		
		double ensembledScore = 0.4 * splatFinalScore + 0.3 * stanfordFinalScore + 0.3 * ratingScore;
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
