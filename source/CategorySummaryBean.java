package source;

import java.util.HashMap;
import java.util.Map;

public class CategorySummaryBean {
	private Map<SentimentClass, Map<String, SummaryBean>> categorySummaryMap;

	public CategorySummaryBean(){
		categorySummaryMap = new HashMap<SentimentClass, Map<String, SummaryBean>>();
	}

	public void addCategorySummary(SentimentClass sentimentClass, String category, SummaryBean summaryBean){
		Map<String, SummaryBean> summaryMap = categorySummaryMap.get(sentimentClass);
		if(summaryMap == null){
			summaryMap = new HashMap<>();
		}

		summaryMap.put(category, summaryBean);
		categorySummaryMap.put(sentimentClass, summaryMap);
	}

	public SummaryBean getSentimentSummary(String category, SentimentClass sentimentClass){
		if(categorySummaryMap.containsKey(category)){
			return categorySummaryMap.get(category).get(sentimentClass);
		}

		return null;
	}

	public Map<String, SummaryBean> getSentimentSummaryMap(String category){
		return categorySummaryMap.get(category);
	}

	public Map<SentimentClass, Map<String, SummaryBean>> getCategorySummaryMap(){
		return categorySummaryMap;
	}
}
