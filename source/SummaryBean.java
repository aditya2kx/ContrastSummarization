package source;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SummaryBean implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
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
