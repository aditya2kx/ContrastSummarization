package source;

import static source.SentimentClass.Positive;
import static source.SentimentClass.Negative;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class SplatService {
	public static final String PROBABILITY = "Probability";
	public static final String CLASSIFICATION = "Classification";

	public static void main(String[] args) throws UnsupportedEncodingException {
		Map<SentimentClass, Double> sentimentValues = getSentimentValues("I hate this place!!!");
		System.out.println(sentimentValues.get(Positive));
		System.out.println(sentimentValues.get(Negative));
		
	}
	
	public static Map<SentimentClass, Double> getSentimentValues(String reviewText) throws UnsupportedEncodingException{
		String sentimentResponse = getSentimentResponse(reviewText);
		JSONArray jsonArr = new JSONArray(sentimentResponse);
		JSONObject jsonObj  = (JSONObject) jsonArr.get(0);
		JSONObject valObj =  jsonObj.getJSONObject("Value");
		
		Map<SentimentClass, Double> sentimentValuesMap = new HashMap<>();
		String splatClassification = String.valueOf(valObj.get(CLASSIFICATION));
		Double splatSentiScore = Double.valueOf(valObj.get(PROBABILITY).toString());
		double splatPosScore, splatNegScore;
		if(splatClassification.equals("pos")){
			splatPosScore = splatSentiScore;
			splatNegScore = 1 - splatSentiScore;
		}else{
			splatNegScore = splatSentiScore;
			splatPosScore = 1 - splatSentiScore;
		}
			
		sentimentValuesMap.put(Positive, splatPosScore);
		sentimentValuesMap.put(Negative, splatNegScore);
		return sentimentValuesMap;
	}

	private static String getSentimentResponse(String reviewText) throws UnsupportedEncodingException{
		Client client = Client.create();

		reviewText = URLEncoder.encode(reviewText, "UTF-8");
		WebResource webResource = client
				.resource("http://msrsplat.cloudapp.net/SplatServiceJson.svc/Analyze?language=en&analyzers=Sentiment&appId=DBBD2339-C4D5-472B-873A-7A5E731B2355&json=x&input=" + reviewText);

		ClientResponse response = webResource.accept("application/json")
				.get(ClientResponse.class);

		if (response.getStatus() != 200) {
			throw new RuntimeException("Failed : HTTP error code : "
					+ response.getStatus());
		}

		return response.getEntity(String.class);
	}
}
