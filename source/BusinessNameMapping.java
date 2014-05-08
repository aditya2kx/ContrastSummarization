package source;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class BusinessNameMapping {

	private static final String REVIEW_FILE_START_PORTION="yelp_phoenix_academic_dataset.rest_business_";
	
	private static String reviewFilePath;
	
	private static Map<String, String> businessNameToIdMapping;
	
	static {
		//Load the config file to read the file path
		Properties properties = new Properties();
		try {
			properties.load(ClassLoader.getSystemResourceAsStream("config"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		//Get the business reviews directory
		reviewFilePath = properties.getProperty("REVIEWS_PATH");
		
		//Load the business name to id mapping
		try {
			loadBusinessIdMapping();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static void loadBusinessIdMapping() throws IOException{
		businessNameToIdMapping = new HashMap<>();
		try(BufferedReader reader = new BufferedReader(
				new InputStreamReader(ClassLoader.getSystemResourceAsStream("business_name_id_mapping.txt")))){
			String readline;
			String[] splitArr;
			while((readline = reader.readLine()) != null){
				splitArr = readline.split("-->");
				businessNameToIdMapping.put(splitArr[0], splitArr[1]);
			}
		}
	}
	
	public static Map<String, String> getBusinessIdMapping(){
		return businessNameToIdMapping;
	}
	
	public static String getBusinessReviewFilePath(String businessName){
		String businessPath = businessNameToIdMapping.get(businessName);
		if(businessPath == null){
			return null;
		}
		
		return reviewFilePath + File.separator + REVIEW_FILE_START_PORTION + businessPath;
	}
	
	public static void main(String[] args) {
		String businessReviewFilePath = getBusinessReviewFilePath("North Mountain Brewing Co.");
		System.out.println(businessReviewFilePath);
		System.out.println(Files.exists(Paths.get(businessReviewFilePath)));
	}
}
