package source;

import java.util.Map;

public class HTMLBuilder {

	public static void main(String[] args) {
		CategorySummaryBean categorySummaryBean = new CategorySummaryBean();
		SummaryBean summaryBean = new SummaryBean();
		summaryBean.addSentence("abcdefg");
		summaryBean.addSentence("hsfhksafkjsfkjs");

		categorySummaryBean.addCategorySummary(SentimentClass.Positive, "Food", summaryBean);
		categorySummaryBean.addCategorySummary(SentimentClass.Negative, "Food", summaryBean);


		System.out.println(generateHTMLContent(categorySummaryBean ));
	}

	public static String generateHTMLContent(CategorySummaryBean categorySummaryBean){
		/*String var = "<div id=\"positive-summary\"><h2>Positive Summary Reviews</h2>" 
				+ "<h3>"
				+ "<bullet></bullet>&nbsp;&nbsp;&nbsp;"
				+ "Food"
				+ "</h3>" 
				+ "<div>" 
				+ "<ul><bullet></bullet>"
				+ " The Pizza and beef ribs are amazing. "
				+ "</ul>"
				+ "<ul><bullet></bullet>"
				+ "The habanero wings have just the right amount of heat."
				+ "</ul>"
				+ "</div>"
				+ "</div>"
				+ "<div id=\"negative-summary\"><h2>Negative Summary Reviews</h2><h3>"
				+ "<bullet></bullet>&nbsp;&nbsp;&nbsp;Food</h3>"
				+ "<div><ul><bullet></bullet>"
				+ " The cole slaw was bland and flavorless."
				+ "</ul><ul><bullet></bullet>"
				+ " My biggest criticism is the dough."
				+ "</ul></div></div>";*/

		String contentHTMLString = "";
		Map<SentimentClass, Map<String, SummaryBean>> categorySummaryMap = categorySummaryBean.getCategorySummaryMap();
		contentHTMLString += getSentimentHTMLString(SentimentClass.Positive, categorySummaryMap.get(SentimentClass.Positive));
		contentHTMLString += getSentimentHTMLString(SentimentClass.Negative, categorySummaryMap.get(SentimentClass.Negative));

		return contentHTMLString;
	}

	private static String getSentimentHTMLString(SentimentClass sentimentClass, Map<String, SummaryBean> summaryBeanMap){
		String sentimentHTML = "<div id=\"" + sentimentClass.toString().toLowerCase() + "-summary\"><h2>" 
				+ sentimentClass.toString() + " Summary Reviews</h2>";

		for(String category : summaryBeanMap.keySet()){
			sentimentHTML += getCategoryHTMLString(category, summaryBeanMap.get(category));
		}

		sentimentHTML += "</div>";
		return sentimentHTML;
	}

	private static String getCategoryHTMLString(String category, SummaryBean summaryBean){
		String categoryHTML = "<h3>"
				+ "<bullet></bullet>&nbsp;&nbsp;&nbsp;"
				+  category
				+ "</h3>" 
				+ "<div>"; 

		for(String sentence : summaryBean.getSentenceList()){
			categoryHTML += getSummaryHTMLString(sentence);
		}

		categoryHTML += "</div>";
		return categoryHTML;
	}

	private static String getSummaryHTMLString(String summarySentence){
		return "<ul><bullet></bullet>"
				+ summarySentence
				+ "</ul>";
	}

}
