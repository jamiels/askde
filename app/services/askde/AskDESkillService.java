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

import models.askde.Adjective;
import models.askde.Appender;
import models.askde.Byline;
import models.askde.OpenHouse;

public class AskDESkillService extends BaseAlexaService {
	

	private ListingsService ts;
	private WSClient ws;


	@Inject
	public AskDESkillService(Environment env, Configuration conf, ApplicationLifecycle al, ListingsService ts, WSClient ws) {
		super(env, conf, al);
		this.ts = ts;
		this.ws = ws;
		
	}
	
	public String getMarketing() {
		List<Appender> appenders = Appender.getAllCurrent();
		List<Byline> bylines = Byline.getAllCurrent();
		Appender appender = appenders.get(ThreadLocalRandom.current().nextInt(0, appenders.size()));
		Byline byline = bylines.get(ThreadLocalRandom.current().nextInt(0, bylines.size()));
		String message = appender.getMessage() + " " + byline.getMessage() + "!";
		
		return message;
	}
	
	
	public String convertPropertyDescriptionToSpeech(OpenHouse oh) {
		List<Adjective> adjectives = Adjective.getAllCurrent();
		String description = "This " + adjectives.get(ThreadLocalRandom.current().nextInt(0, adjectives.size())).getMessage() + " ";
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
	
	public String intentOpenHouseByZipCode(String zipCode) {

		OpenHouse oh = ts.getRandomizedOpenHouseByZipCode(Integer.valueOf(zipCode));
		String message= null;
		if(oh==null)
			message="There are no open houses in the  <say-as interpret-as='spell-out'>" + zipCode + "</say-as> zip code";
		else {
			message = "The next open house in <say-as interpret-as='spell-out'>" + zipCode+ "</say-as> is at <say-as interpret-as='address'>" + oh.getAddress() + "</say-as> starting " + convertDateTimeToSpeech(oh.getStartDateTime()) + " until " + convertTimeToSpeech(oh.getEndDateTime()) + ". ";			
			message += convertPropertyDescriptionToSpeech(oh);
		}
		Logger.info("Response: " + message);
		Logger.info("Packaged response: " + packageResponse(message));
		return packageResponse(message);		
	}
	
	public String intentOpenHouseByZipCode(JsonNode incomingJsonRequest) {	
		String zipCode = incomingJsonRequest.findPath("ZipCode").findPath("value").asText();
		if(!StringUtils.isNumber(zipCode)) {
			return packageResponse("The zip code was not found or came across as incomplete, please try again");
		}
		return intentOpenHouseByZipCode(zipCode);
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
		CompletionStage<WSResponse> resp =  ws.url(endpoint).setHeader("Authorization","Bearer " + consentToken)
				.setRequestTimeout(50000)
				.get();
		CompletionStage<JsonNode> feed = resp.thenApply(WSResponse::asJson);
		try {
			JsonNode response = feed.toCompletableFuture().get();
			
			Logger.info("Check if response is null");
			if (response==null)
				return defaultResponse();
			Logger.info("Response: " + response.toString());
			
			Logger.info("CHecking if postalCode exists");
			JsonNode p = response.findPath("postalCode");
			if(p==null)
				return defaultResponse();
			
			Logger.info("Checking if zipcode is value is null or empty");
			String zipCode = p.textValue();
			Logger.info("Text value of postal field: " + p.textValue());
			if(zipCode==null || zipCode.isEmpty())
				return defaultResponse();
			
			Logger.info("Making sure its a number");
			if(!StringUtils.isNumber(zipCode))
				return defaultResponse();
			
			Logger.info("Pulling up an open house listing");
			return intentOpenHouseByZipCode(zipCode);
			
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			return defaultResponse();

		}
	}
	


	public String intentOpenHouseByNeighborhood(JsonNode incomingJsonRequest) {	
		String intent = getIntent(incomingJsonRequest);
		if(intent==null || intent.isEmpty()) {
			String messageIfIntentBlank = conf.getString("askde.messageIfIntentBlank");
			Logger.info("Intent is blank or null - Response is: " + messageIfIntentBlank);
			return packageResponse(messageIfIntentBlank);			
		}
		
		String neighborhood = null;
		JsonNode i = incomingJsonRequest.findPath("Neighborhood");
		if(i==null)
			return null;
		JsonNode n = i.findPath("value");
		if(n==null)
			return null;
		neighborhood = n.textValue();
		//return intent;
		if(neighborhood==null)
			return packageResponse("I couldn't grab what neighborhood you mentioned");
		Logger.info("Neighborhood retrieved is " + neighborhood);
		OpenHouse oh = ts.getRandomizedOpenHouseByNeighborhood(neighborhood);
		if(oh==null)
			return packageResponse ("There are no open houses in the " + neighborhood + " neigbhorhood");
		
		String message = "The next open house in " + neighborhood + " is at <say-as interpret-as='address'>" + oh.getAddress() + "</say-as> starting " + convertDateTimeToSpeech(oh.getStartDateTime()) + " until " + convertTimeToSpeech(oh.getEndDateTime()) + ". ";			
		message += convertPropertyDescriptionToSpeech(oh);
		return packageResponse(message);
	}
	
	public String invoke(JsonNode incomingJsonRequest) {
		Logger.info("Invoked");
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
			case "getnextopenhousebyzipcode":
				responseMessage = intentOpenHouseByZipCode(incomingJsonRequest);
				break;
			case "getnextopenhousebyneighborhood":
				responseMessage = intentOpenHouseByNeighborhood(incomingJsonRequest);
				break;
			case "getnextopenhousenearme":
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
