package services.askde;

import javax.inject.Inject;

import play.Application;
import play.Configuration;
import play.Environment;
import play.Logger;
import play.inject.ApplicationLifecycle;
import raven.services.BaseAlexaService;
import com.fasterxml.jackson.databind.JsonNode;

import models.askde.OpenHouse;

public class AskDESkillService extends BaseAlexaService {
	

	private ListingsService ts;

	@Inject
	public AskDESkillService(Environment env, Configuration conf, ApplicationLifecycle al, ListingsService ts) {
		super(env, conf, al);
		this.ts = ts;
	}
	
	public String intentOpenHouseByZipCode(JsonNode incomingJsonRequest) {	
		String zipCode = incomingJsonRequest.findPath("ZipCode").findPath("value").asText();
		OpenHouse oh = ts.getRandomizedOpenHouseByZipCode(Integer.valueOf(zipCode));
		String message= null;
		if(oh==null)
			message="There are no open houses in the  <say-as interpret-as='spell-out'>" + zipCode + "</say-as> zip code";
		else
			message = "The next open house is at <say-as interpret-as='address'>" + oh.getAddress() + "</say-as> starting at " + convertDateToSpeech(oh.getStartDateTime());
		Logger.info("Response: " + message);
		Logger.info("Packaged response: " + packageResponse(message));
		return packageResponse(message);
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
		
		String intent = getIntent(incomingJsonRequest);
		if(intent==null || intent.isEmpty()) {
			String messageIfIntentBlank = conf.getString("askde.messageIfIntentBlank");
			Logger.info("Intent is blank or null - Response is: " + messageIfIntentBlank);
			return packageResponse(messageIfIntentBlank);			
		}
		
		intent = intent.toLowerCase();
		String responseMessage = null;
		switch(intent) {
			case "nextopenhousebyzip":
				responseMessage = intentOpenHouseByZipCode(incomingJsonRequest);
				break;
			case "getopenhousebyneighborhood":
				responseMessage = intentOpenHouseByNeighborhood(incomingJsonRequest);
				break;
			default:
				responseMessage = conf.getString("askde.messageIfListingsDown"); // TODO: Change to a better message
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
