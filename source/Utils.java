package source;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Utils {
	private static Set<String> stopWordsSet;

	static {
		try {
			readStopWords();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void readStopWords() throws FileNotFoundException, IOException{
		stopWordsSet = new HashSet<>();
		try(BufferedReader bufReader = new BufferedReader(new FileReader("stop-words.txt"))){
			String readLine;
			while((readLine = bufReader.readLine()) != null){
				stopWordsSet.add(readLine);
			}
		}
	}

	public static Set<String> getStopWords(){
		return stopWordsSet;
	}
}
