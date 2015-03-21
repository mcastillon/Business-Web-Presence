import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class YelpAPI {

	private static final String API_HOST = "api.yelp.com";
	private static final String DEFAULT_TERM = "gym";
	private static final String DEFAULT_LOCATION = "1001 Brannan St San Francisco";
	private static final String SEARCH_PATH = "/v2/search";
		
	private static final String CONSUMER_KEY = "nBvd1HZmNDZofO13xr172g";
	private static final String CONSUMER_SECRET = "nQsSHTo7WHgnuHPZJke8gNxY6DE";
	private static final String TOKEN = "DeMxjCtRfXJhEPmCm39eVlNB8T62ks9r";
	private static final String TOKEN_SECRET = "h77GbnxEQ92YLpP17vfuDkR-ctE";
	
	OAuthService service;
	Token accessToken;
	
	/*
	Parameters for Yelp API call
	
	@param consumerKey CONSUMER_KEY;
	@param consumerSecret CONSUMER_SECRET;
	@param token TOKEN;
	@param tokenSecret TOKEN_SECRET;
	*/
	
	public YelpAPI(String consumerKey, String consumerSecret,
				   String token, String tokenSecret) {
		this.service = new ServiceBuilder()
							.provider(TwoStepOAuth.class)
							.apiKey(consumerKey)
							.apiSecret(consumerSecret)
							.build();
		this.accessToken = new Token(token, tokenSecret);
	}

	public String searchByLocation(String term, String location) {
		OAuthRequest request = createOAuthRequest(SEARCH_PATH);
		request.addQuerystringParameter("term", term);
		request.addQuerystringParameter("location", location);
		return sendRequestAndGetResponse(request);
	}
	
	private OAuthRequest createOAuthRequest(String path) {
		OAuthRequest request = new OAuthRequest(Verb.GET,
												"http://" + API_HOST + path);
		return request;
	}
	
	private String sendRequestAndGetResponse(OAuthRequest request) {
		System.out.println("Querying " + request.getCompleteUrl() + "");
		this.service.signRequest(this.accessToken, request);
		Response response = request.send();
		return response.getBody();
	}
	
	private static void queryAPI(YelpAPI yelpApi, YelpAPICLI yelpApiCli) {
		String searchResponseJSON  = yelpApi.searchByLocation(yelpApiCli.term, yelpApiCli.location);
		
		JSONParser parser = new JSONParser();
		JSONObject response = null;
		try {
			response = (JSONObject) parser.parse(searchResponseJSON);
		} catch (ParseException pe) {
			System.out.println("Error: could not parse JSON response:");
			System.out.println(searchResponseJSON);
			System.exit(1);
		}
		
		JSONArray businesses = (JSONArray) response.get("businesses");
	    JSONObject firstBusiness = (JSONObject) businesses.get(0);
	    String firstBusinessID = firstBusiness.get("name").toString();
	    System.out.println(String.format(
	        "%s businesses found, querying business info for the top result \"%s\" ...",
	        businesses.size(), firstBusinessID));
	}
	
	private static class YelpAPICLI {
	    @Parameter(names = {"-q", "--term"}, description = "Search Query Term")
	    public String term = DEFAULT_TERM;
		@Parameter(names = {"-l", "--location"}, description = "Location to be Queried")
		public String location = DEFAULT_LOCATION;
	}
	
	public static void main(String[] args) {
		YelpAPICLI yelpApiCli = new YelpAPICLI();
		new JCommander(yelpApiCli, args);
		
		YelpAPI yelpApi = new YelpAPI(CONSUMER_KEY, CONSUMER_SECRET, TOKEN, TOKEN_SECRET);
		queryAPI(yelpApi, yelpApiCli);
	}

}
