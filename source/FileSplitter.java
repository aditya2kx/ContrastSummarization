package source;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileSplitter {
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException {
		if(args.length < 3){
			System.err.println("FileSplitter: <sentence-input-file> <total-lines> <# of lines per file> <output-dir>");
			System.exit(0);
		}

		String inputfile = args[0];
		long totalLines = Long.parseLong(args[1]);
		long lineCount = Long.parseLong(args[2]);
		Path outputdirPath = Paths.get(args[0]+"splitter_out");
		long fileCount = (long) Math.ceil((double) totalLines/lineCount);
		int count;
		String readLine;
		
		//Create the directory, if it does not exist
		if(!Files.exists(outputdirPath)){
			Files.createDirectories(outputdirPath);
		}
		
		//Read the input file
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputfile), "UTF-8"))){
			for(int fileIndex = 0; fileIndex < fileCount; fileIndex++){
				count = 0;
				try(BufferedWriter writer = new BufferedWriter(new FileWriter(outputdirPath.resolve("review-sentences-file-" + fileIndex).toFile()))){
					while((readLine = reader.readLine()) != null){
						writer.write(readLine);
						writer.write("\n");
						count++;
						
						//break the loop, if it exceeds the sentence count
						if(count >= lineCount){
							break;
						}
					}
				}
			}
		}
	}

}
