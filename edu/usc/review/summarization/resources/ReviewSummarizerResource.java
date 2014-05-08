package edu.usc.review.summarization.resources;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import source.BusinessNameMapping;
import source.CategorySummaryBean;
import source.HTMLBuilder;
import source.ReviewsCategoryPhraseExtractor;
import source.Summarizer;
import source.SummaryCache;

@Path("/review/summarization")
public class ReviewSummarizerResource {

	@Path("/generate")
	@GET
	@Produces("plain/txt")
	public Response createSummary(@QueryParam("name") String businessName) throws FileNotFoundException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException{
		String businessReviewPath = BusinessNameMapping.getBusinessReviewFilePath(businessName);
		if(businessReviewPath == null){
			return Response.status(Status.NOT_FOUND).entity("Reviews cannot be found for the business.").build();
		}
		
		System.out.println(businessName);
		System.out.println(businessReviewPath);
		SummaryCache cacheInstance = SummaryCache.getInstance();
		CategorySummaryBean categorySummaryBean = 
				cacheInstance.fetchSummaryBean(businessName);

		if(categorySummaryBean == null){
			ReviewsCategoryPhraseExtractor.generatePhrase(businessReviewPath);
			categorySummaryBean = Summarizer.generateSummary(businessReviewPath + ".phrase.out");
			cacheInstance.saveSummaryBean(businessName, categorySummaryBean);
		}

		return Response.status(Status.OK).entity(HTMLBuilder.generateHTMLContent(categorySummaryBean)).build();
	}

}
