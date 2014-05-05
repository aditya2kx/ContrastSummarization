package edu.usc.review.summarization.resources;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import source.BusinessNameMapping;

@Path("/review/summarization")
public class ReviewSummarizerResource {

	@Path("/generate")
	@GET
	@Produces("plain/txt")
	public Response createSummary(@QueryParam("name") String businessName) throws UnsupportedEncodingException{
		String businessReviewPath = BusinessNameMapping.getBusinessReviewFilePath(businessName);
		System.out.println(businessReviewPath);
		return Response.status(Status.OK).entity(URLEncoder.encode(businessReviewPath, "UTF-8")).build();
	}

}
