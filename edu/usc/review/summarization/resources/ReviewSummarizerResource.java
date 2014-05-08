package edu.usc.review.summarization.resources;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

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

@Path("/review/summarization")
public class ReviewSummarizerResource {

	@Path("/generate")
	@GET
	@Produces("plain/txt")
	public Response createSummary(@QueryParam("name") String businessName) throws FileNotFoundException, IOException{
		String businessReviewPath = BusinessNameMapping.getBusinessReviewFilePath(businessName);
		System.out.println(businessName);
		System.out.println(businessReviewPath);

		ReviewsCategoryPhraseExtractor.generatePhrase(businessReviewPath);
		CategorySummaryBean categorySummaryBean = Summarizer.generateSummary(businessReviewPath + ".phrase.out");

		return Response.status(Status.OK).entity(HTMLBuilder.generateHTMLContent(categorySummaryBean)).build();
	}

}
