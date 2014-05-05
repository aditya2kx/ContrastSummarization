package source;

import java.util.Map;

import org.json.JSONObject;

import source.SummaryBean.SupportingSentenceBean;

public class SummaryJsonBuilder {

	public static JSONObject getSummaryJson(CategorySummaryBean summaryJsonBean){
		JSONObject jsonObject = new JSONObject();
		Map<String, Map<SentimentClass, SummaryBean>> categorySummaryMap = summaryJsonBean.getCategorySummaryMap();
		Map<SentimentClass, SummaryBean> sentimentSummaryMap;
		for(String category : categorySummaryMap.keySet()){
			sentimentSummaryMap = categorySummaryMap.get(category);
			for(SentimentClass sentimentClass : sentimentSummaryMap.keySet()){
				jsonObject.append(category, getSentimentSummaryObject(sentimentClass, sentimentSummaryMap.get(sentimentClass)));
			}
		}

		return jsonObject;
	}

	private static JSONObject getSentimentSummaryObject(SentimentClass sentimentClass, SummaryBean summaryBean){
		JSONObject sentimentSummaryObj = new JSONObject();
		sentimentSummaryObj.put(sentimentClass.toString(), getSummaryObject(summaryBean));
		return sentimentSummaryObj;
	}

	private static JSONObject getSummaryObject(SummaryBean summaryBean){
		JSONObject summaryObject = new JSONObject();
		summaryObject.put("summary", summaryBean.getSummary());
		for(SupportingSentenceBean sentenceBean : summaryBean.getSupportingSentenceList()){
			summaryObject.append("support_sentences", getSupportingSentenceObject(sentenceBean));
		}

		return summaryObject;
	}

	private static JSONObject getSupportingSentenceObject(SupportingSentenceBean supportingSentenceBean){
		JSONObject sentenceObject = new JSONObject();
		sentenceObject.put("review_id", supportingSentenceBean.getReviewId());
		sentenceObject.put("sentence", supportingSentenceBean.getSupportingSentence());
		return sentenceObject;
	}

	public static void main(String[] args) {
		CategorySummaryBean sumBean = new CategorySummaryBean();
		SummaryBean summaryBean = new SummaryBean("test1 test2");
		summaryBean.addSupportingSentence(0, "test1");
		summaryBean.addSupportingSentence(1, "test2");
		sumBean.addCategorySummary("food", SentimentClass.Positive, summaryBean);
		
		SummaryBean summaryBean1 = new SummaryBean("test3 test4");
		summaryBean1.addSupportingSentence(0, "test3");
		summaryBean1.addSupportingSentence(0, "test4");
		sumBean.addCategorySummary("food", SentimentClass.Negative, summaryBean1);
		
		JSONObject json = getSummaryJson(sumBean);
		System.out.println(json.toString());
	}
}
