package source;

import java.util.Properties;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class StanfordLoadAnnotaters {

	private static StanfordLoadAnnotaters annotaters;
	
	private StanfordCoreNLP stanfordPipeLine;
	
	private StanfordLoadAnnotaters(){
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, parse, sentiment");
		stanfordPipeLine = new StanfordCoreNLP(props);
	}
	
	public static StanfordLoadAnnotaters getInstance(){
		if(annotaters == null){
			annotaters = new StanfordLoadAnnotaters();
		}
		
		return annotaters;
	}
	
	public StanfordCoreNLP getPipeLine(){
		return stanfordPipeLine;
	}
}
