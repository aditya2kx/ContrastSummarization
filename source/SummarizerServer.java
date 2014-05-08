package source;

import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;

import javax.ws.rs.core.UriBuilder;

import org.glassfish.grizzly.http.server.HttpServer;

import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.json.JSONConfiguration;

public class SummarizerServer {
	
	private static URI getBaseURI() {
		return UriBuilder.fromUri("http://localhost/").port(9998).build();
	}

	public static final URI BASE_URI = getBaseURI();

	protected static HttpServer startServer() throws IOException, ClassNotFoundException {
		System.out.println("Starting grizzly...");
		ResourceConfig rc = new PackagesResourceConfig("edu.usc.review.summarization.resources");
		rc.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
		
		return GrizzlyServerFactory.createHttpServer(BASE_URI, rc);
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		final HttpServer httpServer = startServer();
		System.out.println(String.format("Jersey app started.. "
				+ "\nHit enter to stop it...",
				BASE_URI, BASE_URI));
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("Shutting down the server.");
				
				//Save the cache
				try {
					SummaryCache.getInstance().releaseResources();
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException e) {
					throw new RuntimeException(e);
				}
				
				httpServer.shutdown();
			}
		});
		System.in.read();
		
	}
}
