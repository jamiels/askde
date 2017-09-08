package services.askde;

import javax.inject.Inject;

import play.Application;
import play.Configuration;
import play.Environment;
import play.Logger;
import play.inject.ApplicationLifecycle;
import raven.services.BaseAlexaService;
import com.fasterxml.jackson.databind.JsonNode;

public class AskDESkillService extends BaseAlexaService {
	
	private ListingsService ts;

	@Inject
	public AskDESkillService(Environment env, Configuration conf, ApplicationLifecycle al, ListingsService ts) {
		super(env, conf, al);
	}
	
	public String intentOpenHouseByZipCode(JsonNode incomingJsonRequest) {	
		return "intentOpenHouseByZipCode!";
	}

	public String intentOpenHouseByNeighborhood(JsonNode incomingJsonRequest) {	
		return "intentOpenHouseByNeighborhood!";
	}
	
	public String invoke(JsonNode incomingJsonRequest) {
		Logger.info("Invoking");
		Logger.info("incomingJsonRequest is " + incomingJsonRequest);
		Logger.info("open houses is " + ts.getOpenHouses());
		if(ts.getOpenHouses()==null || incomingJsonRequest==null) {
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
			case "getopenhousebyzipcode":
				responseMessage = intentOpenHouseByZipCode(incomingJsonRequest);
				break;
			case "getopenhousebyneighborhood":
				responseMessage = intentOpenHouseByNeighborhood(incomingJsonRequest);
				break;
			default:
				responseMessage = conf.getString("askde.messageIfListingsDown");
		}
		
		return packageResponse(responseMessage);		
	}
		


}
