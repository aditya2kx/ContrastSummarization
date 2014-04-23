/*
 Example code based on code from Nicholas Smith at http://imnes.blogspot.com/2011/01/how-to-use-yelp-v2-from-java-including.html
 For a more complete example (how to integrate with GSON, etc) see the blog post above.
 */
package yelp.api;

import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

/**
 * Example for accessing the Yelp API.
 */
public class Yelp {

  OAuthService service;
  Token accessToken;

  /**
   * Setup the Yelp API OAuth credentials.
   *
   * OAuth credentials are available from the developer site, under Manage API access (version 2 API).
   *
   * @param consumerKey Consumer key
   * @param consumerSecret Consumer secret
   * @param token Token
   * @param tokenSecret Token secret
   */
  public Yelp(String consumerKey, String consumerSecret, String token, String tokenSecret) {
    this.service = new ServiceBuilder().provider(YelpApi2.class).apiKey(consumerKey).apiSecret(consumerSecret).build();
    this.accessToken = new Token(token, tokenSecret);
  }

  /**
   * Search with term and location.
   *
   * @param term Search term
   * @param latitude Latitude
   * @param longitude Longitude
   * @return JSON string response
   */
  public String search(String terms, String location, int offset) {
    OAuthRequest request = new OAuthRequest(Verb.GET, "http://api.yelp.com/v2/search");
    request.addQuerystringParameter("term", terms);
    request.addQuerystringParameter("location", location);
    request.addQuerystringParameter("limit", "20");
    request.addQuerystringParameter("offset", String.valueOf(offset));
    //request.addQuerystringParameter("ll", latitude + "," + longitude);
    this.service.signRequest(this.accessToken, request);
    Response response = request.send();
    return response.getBody();
  }
  
  /**
   * Get business with their id
   *
   * @param businessId 
   * @return JSON string response
   */
  public String business(String businessId) {
    OAuthRequest request = new OAuthRequest(Verb.GET, "http://api.yelp.com/v2/business/" + businessId);
    this.service.signRequest(this.accessToken, request);
    Response response = request.send();
    return response.getBody();
  }

  // CLI
  public static void main(String[] args) {
    // Update tokens here from Yelp developers site, Manage API access.
    String consumerKey = "o4OtK-AAqYB5ICDoZPZSOw";
    String consumerSecret = "taqaSqwAYvGUvDDYdkUude1yZcg";
    String token = "_8CSk1zzsOQgXBCkBcFWAyHImaJhJtRY";
    String tokenSecret = "biSu65CThUhxtkE5R7skM6wYPcg";

    Yelp yelp = new Yelp(consumerKey, consumerSecret, token, tokenSecret);
   // String response = yelp.search("food", "Los+Angeles", 0);
    String response = yelp.business("papa-cristos-los-angeles");

    System.out.println(response);
  }
}
