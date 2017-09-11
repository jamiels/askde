package services.askde;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

import javax.inject.Inject;

import org.h2.util.StringUtils;

import play.Application;
import play.Configuration;
import play.Environment;
import play.Logger;
import play.inject.ApplicationLifecycle;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import raven.services.BaseAlexaService;
import com.fasterxml.jackson.databind.JsonNode;

import models.askde.OpenHouse;

public class AskDESkillService extends BaseAlexaService {
	

	private ListingsService ts;
	private WSClient ws;
	private List<String> adjectives = new ArrayList<String>(10);
	private List<String> marketings = new ArrayList<String>(10);
	private List<String> appenders = new ArrayList<String>(10);

	@Inject
	public AskDESkillService(Environment env, Configuration conf, ApplicationLifecycle al, ListingsService ts, WSClient ws) {
		super(env, conf, al);
		this.ts = ts;
		this.ws = ws;
		adjectives.add("Beautiful");
		adjectives.add("Gorgeous");
		adjectives.add("Stunning");
		adjectives.add("Exquisite");
		adjectives.add("Spacious");
		adjectives.add("Lovely");
		adjectives.add("Fantastic");
		
		appenders.add("Oh! yes, ");
		appenders.add("Also");
		appenders.add("and do me a favor and");
		appenders.add("and remember to");
		appenders.add("don't forget to");
		appenders.add("before I forget");
		appenders.add("one more thing");
		
		marketings.add("visit elliman.com for the most current listings");
		marketings.add("see this month's issue of the Elliman magazine");
		marketings.add("Pick up a copy of our latest market valuation report");
		
		
	}
	
	public String getMarketing() {
		String appender = appenders.get(ThreadLocalRandom.current().nextInt(0, appenders.size()));
		String marketing = marketings.get(ThreadLocalRandom.current().nextInt(0, marketings.size()));
		String message = appender + " " + marketing + "!";
		
		return message;
	}
	
	
	public String convertPropertyDescriptionToSpeech(OpenHouse oh) {
		String description = "This " + adjectives.get(ThreadLocalRandom.current().nextInt(0, adjectives.size())) + " ";
		if(oh.isRental())
			description += "rental is a ";
		else
			description += oh.getPropertyType() + " for sale is a ";
		
		String bedrooms = null;
		if(oh.getBaths().intValue()>0)
			bedrooms = oh.getBeds() + " bedroom ";
		else
			bedrooms = " studio ";
		
		description+= bedrooms + " and " + oh.getBaths() + " bathroom ";
		description+= "located in " + oh.getNeighborhood() + ". ";
		description+= "<break time='1s'/>The listing ID is <say-as interpret-as='spell-out'>" + oh.getListingID().replace("*", "") + "</say-as>. <break time='1s'/>";
		
		
		description+= getMarketing();
/*		String agentsDescription = oh.getDescription();
		
		int iend = agentsDescription.indexOf("."); 
		

		if (iend != -1) 
			agentsDescription = agentsDescription.substring(0 , iend);
		
		description += ". " + agentsDescription;*/
		
		
		return description;
		
	}
	
	
	
	public String intentOpenHouseByZipCode(JsonNode incomingJsonRequest) {	
		String zipCode = incomingJsonRequest.findPath("ZipCode").findPath("value").asText();
		if(!StringUtils.isNumber(zipCode)) {
			return packageResponse("The zip code was not found or came across as incomplete, please try again");
		}
		OpenHouse oh = ts.getRandomizedOpenHouseByZipCode(Integer.valueOf(zipCode));
		String message= null;
		if(oh==null)
			message="There are no open houses in the  <say-as interpret-as='spell-out'>" + zipCode + "</say-as> zip code";
		else {
			message = "The next open house is at <say-as interpret-as='address'>" + oh.getAddress() + "</say-as> starting " + convertDateTimeToSpeech(oh.getStartDateTime()) + " until " + convertTimeToSpeech(oh.getEndDateTime()) + ". ";			
			message += convertPropertyDescriptionToSpeech(oh);
		}
		Logger.info("Response: " + message);
		Logger.info("Packaged response: " + packageResponse(message));
		return packageResponse(message);
	}
	
	public String intentOpenHouseNearMe(JsonNode incomingJsonRequest) {
		JsonNode c = incomingJsonRequest.findPath("consentToken");
		JsonNode d = incomingJsonRequest.findPath("deviceId");
		if(c==null || d==null)
			return "{  \"version\": \"1.0\",  \"response\": {    \"card\": {      \"type\": \"AskForPermissionsConsent\",      \"permissions\": [        \"read::alexa:device:all:country_and_postal_code\" ]}}}";
		String consentToken = c.textValue();
		String deviceId = d.textValue();
		Logger.info("Consent token: " + consentToken);
		
		String endpoint = "https://api.amazonalexa.com//v1/devices/"+deviceId+"/settings/address/countryAndPostalCode";
		CompletionStage<WSResponse> resp =  ws.url(endpoint)
				.setRequestTimeout(50000)
				.get();
		CompletionStage<JsonNode> feed = resp.thenApply(WSResponse::asJson);
		try {
			JsonNode response = feed.toCompletableFuture().get();
			Logger.info(response.asText());
			Logger.info(response.asText());
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		return packageResponse("You got it");
	}
	


	public String intentOpenHouseByNeighborhood(JsonNode incomingJsonRequest) {	
		return "intentOpenHouseByNeighborhood!";
	}
	
	public String invoke(JsonNode incomingJsonRequest) {
		if(ts == null || ts.getOpenHouses()==null || incomingJsonRequest==null) {
			String messageIfListingsDown = conf.getString("askde.messageIfListingsDown");
			Logger.info("Listings unavailable - Response is: " + messageIfListingsDown);
			return packageResponse(messageIfListingsDown);
		}
		Logger.info("Request:");
		Logger.info(incomingJsonRequest.toString());
		
		String intent = getIntent(incomingJsonRequest);
		if(intent==null || intent.isEmpty()) {
			String messageIfIntentBlank = conf.getString("askde.messageIfIntentBlank");
			Logger.info("Intent is blank or null - Response is: " + messageIfIntentBlank);
			return packageResponse(messageIfIntentBlank);			
		}
		
		intent = intent.toLowerCase();
		Logger.info("Intent invoked: " + intent);
		String responseMessage = null;
		switch(intent) {
			case "nextopenhousebyzip":
				responseMessage = intentOpenHouseByZipCode(incomingJsonRequest);
				break;
			case "getopenhousebyneighborhood":
				responseMessage = intentOpenHouseByNeighborhood(incomingJsonRequest);
				break;
			case "openhousenearme":
				responseMessage = intentOpenHouseNearMe(incomingJsonRequest);
				break;
			default:
				responseMessage = defaultResponse(); // TODO: Change to a better message
		}
		
		return responseMessage;		
	}
	
	public String defaultResponse() {
		String responseMessage = conf.getString("askde.messageIfListingsDown");
		if(responseMessage==null) 
			responseMessage="Hi, I couldn't get what you said, please repeat that!";
		
		return packageResponse(responseMessage);
		
		
	}
		


}
