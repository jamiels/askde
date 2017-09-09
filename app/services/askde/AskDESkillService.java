package services.askde;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

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
	private List<String> adjectives = new ArrayList<String>(10);
	private List<String> marketings = new ArrayList<String>(10);
	private List<String> appenders = new ArrayList<String>(10);

	@Inject
	public AskDESkillService(Environment env, Configuration conf, ApplicationLifecycle al, ListingsService ts) {
		super(env, conf, al);
		this.ts = ts;
		adjectives.add("Beautiful");
		adjectives.add("Gorgeous");
		adjectives.add("Stunning");
		adjectives.add("Exquisite");
		adjectives.add("Spacious");
		adjectives.add("Lovely");
		adjectives.add("Fantastic");
		
		appenders.add("Oh yes, ");
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
		String marketing = marketings.get(ThreadLocalRandom.current().nextInt(0, appenders.size()));
		String message = appender + " " + marketing + "!";
		
		return message;
	}
	
	
	public String convertPropertyDescriptionToSpeech(OpenHouse oh) {
		String description = "This " + adjectives.get(ThreadLocalRandom.current().nextInt(0, adjectives.size())) + " ";
		if(oh.isRental())
			description += "rental is a ";
		else
			description += oh.getPropertyType() + " for sale is a ";
		
		description+= oh.getBeds() + " bedroom " + oh.getBaths() + " bathroom ";
		description+= "located in " + oh.getNeighborhood() + ".";
		description+= "The listing ID is " + oh.getListingID().replace("*", "") + ".";
		
		
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
		OpenHouse oh = ts.getRandomizedOpenHouseByZipCode(Integer.valueOf(zipCode));
		String message= null;
		if(oh==null)
			message="There are no open houses in the  <say-as interpret-as='spell-out'>" + zipCode + "</say-as> zip code";
		else
			message = "The next open house is at <say-as interpret-as='address'>" + oh.getAddress() + "</say-as> starting at " + convertDateTimeToSpeech(oh.getStartDateTime()) + " until " + convertTimeToSpeech(oh.getEndDateTime());
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
