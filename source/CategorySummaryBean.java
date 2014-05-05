package source;

import java.util.HashMap;
import java.util.Map;

public class CategorySummaryBean {
	private Map<String, Map<SentimentClass, SummaryBean>> categorySummaryMap;

	public CategorySummaryBean(){
		categorySummaryMap = new HashMap<String, Map<SentimentClass, SummaryBean>>();
	}

	public void addCategorySummary(String category, SentimentClass sentimentClass, SummaryBean summaryBean){
		Map<SentimentClass, SummaryBean> summaryMap = categorySummaryMap.get(category);
		if(summaryMap == null){
			summaryMap = new HashMap<>();
		}

		summaryMap.put(sentimentClass, summaryBean);
		categorySummaryMap.put(category, summaryMap);
	}

	public SummaryBean getSentimentSummary(String category, SentimentClass sentimentClass){
		if(categorySummaryMap.containsKey(category)){
			return categorySummaryMap.get(category).get(sentimentClass);
		}

		return null;
	}

	public Map<SentimentClass, SummaryBean> getSentimentSummaryMap(String category){
		return categorySummaryMap.get(category);
	}

	public Map<String, Map<SentimentClass, SummaryBean>> getCategorySummaryMap(){
		return categorySummaryMap;
	}
}
