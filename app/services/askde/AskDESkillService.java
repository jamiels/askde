package services.askde;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

import javax.inject.Inject;

import org.h2.util.StringUtils;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;

import models.askde.Adjective;
import models.askde.Appender;
import models.askde.Byline;
import models.askde.OpenHouse;
import models.askde.SkillInvocation;
import play.Configuration;
import play.Environment;
import play.Logger;
import play.inject.ApplicationLifecycle;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import raven.services.BaseAlexaService;

public class AskDESkillService extends BaseAlexaService {
	

	private ListingsService ts;
	private WSClient ws;


	@Inject
	public AskDESkillService(Environment env, Configuration conf, ApplicationLifecycle al, ListingsService ts, WSClient ws) {
		super(env, conf, al);
		this.ts = ts;
		this.ws = ws;
		
	}
	
	public String addMarketing(String message) {
		return message + "<break time='2s'/>" + generateMarketing();
	}
	
	public String generateMarketing() {
		List<Appender> appenders = Appender.getAllCurrent();
		List<Byline> bylines = Byline.getAllCurrent();
		if(bylines.size()>0 && appenders.size()>0) {
			Appender appender = appenders.get(ThreadLocalRandom.current().nextInt(0, appenders.size()));
			Byline byline = bylines.get(ThreadLocalRandom.current().nextInt(0, bylines.size()));
			String message = appender.getMessage() + " " + byline.getMessage() + "!";
			return message;
		} else
			return "";
	}
	
	public String getAdjective() {
		List<Adjective> adjectives = Adjective.getAllCurrent();
		if(adjectives.size()>0)
			return adjectives.get(ThreadLocalRandom.current().nextInt(0, adjectives.size())).getMessage();
		else
			return "";
	}	
	
	public String convertPropertyDescriptionToSpeech(OpenHouse oh) {
		String adjective = getAdjective();
		String description = "This " + adjective + " ";
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
		description+= "<break time='1s'/>The listing ID is <say-as interpret-as='spell-out'>" + oh.getListingID().replace("*", "") + "</say-as>.";		
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
		return message;		
	}
	
	public String intentOpenHouseByZipCode(JsonNode incomingJsonRequest) {	
		String zipCode = incomingJsonRequest.findPath("ZipCode").findPath("value").asText();
		if(!StringUtils.isNumber(zipCode)) {
			return packageResponse("The zip code was not found or came across as incomplete, please try again");
		}
		return packageResponse(addMarketing(intentOpenHouseByZipCode(zipCode)));
	}
	
	
	public String intentOpenHouseNearMe(JsonNode incomingJsonRequest) {
		String consentToken = getConsentToken(incomingJsonRequest);
		String deviceId = getDeviceID(incomingJsonRequest);
		if(consentToken==null || deviceId==null)
			return "{  \"version\": \"1.0\",  \"response\": {    \"card\": {      \"type\": \"AskForPermissionsConsent\",      \"permissions\": [        \"read::alexa:device:all:country_and_postal_code\" ]}}}";
		
		 
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
			return packageResponse(addMarketing(intentOpenHouseByZipCode(zipCode)));
			
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
		return packageResponse(addMarketing(message));
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
		SkillInvocation si = new SkillInvocation();
		switch(intent) {
			case "getnextopenhousebyzipcode":
				si.setSkill("GetNextOpenHouseByZipCode");
				responseMessage = intentOpenHouseByZipCode(incomingJsonRequest);
				break;
			case "getnextopenhousebyneighborhood":
				si.setSkill("GetNextOpenHouseByNeighborhood");
				responseMessage = intentOpenHouseByNeighborhood(incomingJsonRequest);
				break;
			case "getnextopenhousenearme":
				si.setSkill("GetNextOpenHouseNearMe");
				responseMessage = intentOpenHouseNearMe(incomingJsonRequest);
				break;
			default:
				si.setSkill("Default");
				responseMessage = defaultResponse(); // TODO: Change to a better message
		}
		si.setRequest(incomingJsonRequest.toString());
		si.setResponse(responseMessage);
		si.setDeviceID(getDeviceID(incomingJsonRequest));
		Ebean.save(si);
		return responseMessage;		
	}
	
	public String defaultResponse() {
		String responseMessage = conf.getString("askde.messageIfListingsDown");
		if(responseMessage==null) 
			responseMessage="Hi, I couldn't get what you said, please repeat that!";
		return packageResponse(addMarketing(responseMessage));
	}
		


}
