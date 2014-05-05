package edu.usc.review.summarization.resources;

import java.io.UnsupportedEncodingException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import source.BusinessNameMapping;

@Path("/review/summarization")
public class ReviewSummarizerResource {

	@Path("/generate")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response createSummary(@QueryParam("name") String businessName) throws UnsupportedEncodingException{
		String businessReviewPath = BusinessNameMapping.getBusinessReviewFilePath(businessName);
		
		//TODO insert the summarization code for the retrieved business review path. Create
		//Json object from JsonBuilder
		
		return Response.status(Status.OK).entity("").build();
	}

}
