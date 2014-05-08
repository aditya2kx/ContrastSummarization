package source;

import java.util.ArrayList;
import java.util.List;

public class SummaryBean {
	private List<String> SentenceList;
	
	public SummaryBean(){
		SentenceList = new ArrayList<String>();
	}
	
	public void addSentence(String sentence){
		SentenceList.add(sentence);
	}
	
	public List<String> getSentenceList(){
		return SentenceList;
	}
}
