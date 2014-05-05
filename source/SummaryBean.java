package source;

import java.util.LinkedList;
import java.util.List;

public class SummaryBean {
	private String summary;
	
	private List<SupportingSentenceBean> supportingSentenceBeanList;
	
	public SummaryBean(String summary){
		this.summary = summary;
		supportingSentenceBeanList = new LinkedList<>();
	}
	
	public void addSupportingSentence(int reviewId, String supportingSentence){
		supportingSentenceBeanList.add(new SupportingSentenceBean(reviewId, supportingSentence));
	}
	
	public List<SupportingSentenceBean> getSupportingSentenceList(){
		return supportingSentenceBeanList;
	}
	
	public String getSummary(){
		return summary;
	}
	
	public static class SupportingSentenceBean{
		private int review_id;
		
		private String supportingSentence;
		
		
		public SupportingSentenceBean(int review_id, String supportingSentence){
			this.review_id = review_id;
			this.supportingSentence = supportingSentence;
		}
		
		public int getReviewId(){
			return review_id;
		}
		
		public String getSupportingSentence(){
			return supportingSentence;
		}
	}
}
