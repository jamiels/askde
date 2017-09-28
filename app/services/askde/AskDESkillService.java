package services.askde;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.*;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

import javax.inject.Inject;

import org.apache.commons.lang3.math.NumberUtils;

import io.ebean.Ebean;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Permissions;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.speechlet.User;
import com.fasterxml.jackson.databind.JsonNode;
import models.askde.Adjective;
import models.askde.Appender;
import models.askde.Byline;
import models.askde.OpenHouse;
import models.askde.SkillInvocation;
import com.typesafe.config.Config;
import play.Environment;
import play.Logger;
import play.inject.ApplicationLifecycle;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import raven.services.BaseAlexaService;
import raven.exceptions.alexa.*;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.amazon.speech.ui.SsmlOutputSpeech;
import com.amazon.speech.ui.AskForPermissionsConsentCard;

import com.amazon.speech.ui.OutputSpeech;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.Context;
import controllers.askde.AlexaDeviceAddressClient;
import controllers.askde.Address;
import com.amazon.speech.speechlet.interfaces.system.SystemInterface;
import com.amazon.speech.speechlet.interfaces.system.SystemState;

public class AskDESkillService extends BaseAlexaService {
	
	private ListingsService ts;
	
	@Inject
	public AskDESkillService(Environment env, Config conf, ApplicationLifecycle al, ListingsService ts, WSClient ws) {
		super(env, conf, al, ws);
		this.ts = ts;
		this.CARD_TITLE = "Ask Douglas Elliman";
	}
	
	public SpeechletResponse invoke(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
		if(ts == null || ts.getOpenHouses()==null || requestEnvelope==null)
			return errorResponse(generateErrorListingsDown());	
		
		if(requestEnvelope.getRequest().getIntent() ==  null)
			return errorResponse(generateErrorIntentBlank());
		
		String intent = requestEnvelope.getRequest().getIntent().getName();
		SpeechletResponse responseMessage = null;
		switch(intent.toLowerCase()) {
			case "getnextopenhousebyzipcode":
				responseMessage = intentOpenHouseByZipCode(requestEnvelope);
				break;
			case "getnextopenhousebyneighborhood":
				responseMessage = intentOpenHouseByNeighborhood(requestEnvelope);
				break;
			case "getnextopenhousenearme":
				responseMessage = intentOpenHouseNearMe(requestEnvelope);
				break;
			default:
				responseMessage = errorResponse(generateErrorIntentBlank());
		}
		
		SkillInvocation si = new SkillInvocation();
		si.setSkill(intent);
		if(requestEnvelope.getRequest().getIntent().getSlot("ZipCode")!=null)
			si.setSourceZipCode(requestEnvelope.getRequest().getIntent().getSlot("ZipCode").getValue());
		si.setRequest(requestEnvelope.toString());
		si.setResponse(responseMessage.getOutputSpeech().toString());
		SystemState systemState = getSystemState(requestEnvelope.getContext());
		String deviceID = systemState.getDevice().getDeviceId();
		si.setDeviceID(deviceID);
		Ebean.save(si);
		
		return responseMessage;
		
	}
	
	// Intents
	public SpeechletResponse intentOpenHouseNearMe(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
		Session session = requestEnvelope.getSession();
		if(session==null) return errorResponse();
		
		User u = session.getUser();
		if(u==null) return errorResponse();
		
		Permissions ps = u.getPermissions();
		if(ps==null) return getPermissionsResponse();
		
		String consentToken = ps.getConsentToken();
		if(consentToken==null || consentToken.isEmpty()) return getPermissionsResponse();

		String postalCode = null;
		try {
			
			SystemState systemState = getSystemState(requestEnvelope.getContext());
			String deviceID = systemState.getDevice().getDeviceId();
			String apiEndpoint = systemState.getApiEndpoint();
			
			AlexaDeviceAddressClient alexaDeviceAddressClient = new AlexaDeviceAddressClient(deviceID, consentToken, apiEndpoint);
			Address addressObject = alexaDeviceAddressClient.getFullAddress();
	        
			if (addressObject == null) 
	             return getAskResponse("Ask Douglas Elliman", "Address information missing, please try again");	       
			 postalCode = addressObject.getPostalCode();
			 if(postalCode==null)
				 return getAskResponse("Ask Douglas Elliman", "Address information missing, please try again");
        } catch (UnauthorizedException e) {
        	Logger.error("Unauthorized exception",e);
            return getPermissionsResponse();
        } catch (DeviceAddressClientException e) {
            Logger.error("Device Address Client failed to successfully return the address.", e);
            return errorResponse();
        }
		
		Map<String,String> speechText = intentOpenHouseByZipCode(postalCode);
		Map<String,String> marketingText = addMarketing();
        SimpleCard card = getSimpleCard(speechText.get("plainText") + marketingText.get("plainText"));
        SsmlOutputSpeech speech = getSsmlOutputSpeech(speechText.get("ssmlText") + marketingText.get("ssmlText")); 
		return SpeechletResponse.newTellResponse(speech,card);
	}
	

	
	public SpeechletResponse intentOpenHouseByZipCode(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {			
		Slot zipCodeSlot = requestEnvelope.getRequest().getIntent().getSlot("ZipCode");
		if(zipCodeSlot==null)
			return errorResponse();
		String zipCode = zipCodeSlot.getValue();
		if(zipCode==null || zipCode.isEmpty() || !NumberUtils.isCreatable(zipCode)) 
			return errorResponse();
		Map<String,String> speechText = intentOpenHouseByZipCode(zipCode);
		Map<String,String> marketingText = addMarketing();
        SimpleCard card = getSimpleCard(speechText.get("plainText") + marketingText.get("plainText"));
        SsmlOutputSpeech speech = getSsmlOutputSpeech(speechText.get("plainText") + marketingText.get("ssmlText")); 
		return SpeechletResponse.newTellResponse(speech,card);
	}
	
	public SpeechletResponse intentOpenHouseByNeighborhood(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {	
		
		Slot neighborhoodSlot = requestEnvelope.getRequest().getIntent().getSlot("Neighborhood");
		if(neighborhoodSlot==null)
			return errorResponse();
		String neighborhood = neighborhoodSlot.getValue();
		if(neighborhood==null || neighborhood.isEmpty())
			return  errorResponse();
		Logger.info("Neighborhood retrieved is " + neighborhood);
		OpenHouse oh = ts.getRandomizedOpenHouseByNeighborhood(neighborhood);
		String plainText = null;
		if(oh==null) {
			plainText = "There are currently no open houses in the " + neighborhood + " neigbhorhood";
	        SimpleCard card = getSimpleCard(plainText);
	        PlainTextOutputSpeech speech = getPlainTextOutputSpeech(plainText); 
			return SpeechletResponse.newTellResponse(speech,card);
		}
		
		plainText = "The next open house in " + neighborhood + " is at " + oh.getAddress() + " starting " + convertDateTimeToSpeech(oh.getStartDateTime()) + " until " + convertTimeToSpeech(oh.getEndDateTime()) + ". ";
		String ssmlText = "The next open house in " + neighborhood + " is at <say-as interpret-as='address'>" + oh.getAddress() + "</say-as> starting " + convertDateTimeToSpeech(oh.getStartDateTime()) + " until " + convertTimeToSpeech(oh.getEndDateTime()) + ". ";			
		
		Map<String,String> propertyDescriptionText = convertPropertyDescriptionToSpeech(oh, false);
		plainText += propertyDescriptionText.get("plainText");
		ssmlText += propertyDescriptionText.get("ssmlText");
		
		Map<String,String> marketingText = addMarketing();
		
        SimpleCard card = getSimpleCard(plainText + marketingText.get("plainText"));
        SsmlOutputSpeech speech = getSsmlOutputSpeech(ssmlText + marketingText.get("ssmlText")); 
        return SpeechletResponse.newTellResponse(speech, card);
	}
	
	private SimpleCard getSimpleCard(String speechText) {
		return super.getSimpleCard(CARD_TITLE, speechText);
	}
	
    private SpeechletResponse getPermissionsResponse() {
        String speechText = "Ask Douglas Elliman does not have permission to access your zip code " +
                "Please give us permission by going into your Alexa app and following the instructions on the card we just sent you. See you soon.";
        return super.getPermissionCountryAndPostalCodeResponse(speechText);
    }
	
	public Map<String,String> addMarketing() {
		String plainText = generateMarketing();
		String ssmlText =  " <break time='2s'/>" + plainText;
		Map<String,String> marketingText = putIntoMap(plainText,ssmlText);
		return marketingText;
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
	
	public Map<String,String> convertPropertyDescriptionToSpeech(OpenHouse oh, boolean sayNeighborhood) {
		String adjective = getAdjective();
		String plainText = "This " + adjective + " ";
		String ssmlText = null;
		if(oh.isRental())
			plainText += "rental is a ";
		else
			plainText += oh.getPropertyType() + " for sale is a ";
		
		String bedrooms = null;
		if(oh.getBaths().intValue()>0)
			bedrooms = oh.getBeds() + " bedroom ";
		else
			bedrooms = " studio ";
		
		plainText+= bedrooms + " and " + oh.getBaths() + " bathroom ";
		ssmlText = plainText.toString();
		if(sayNeighborhood=true)
			plainText+= "located in " + oh.getNeighborhood() + " ";
		else {
			plainText += "located in the " + oh.getZipCode() + " zip code ";
			ssmlText+= "located in the <say-as interpret-as='spell-out'>" + oh.getZipCode() + "</say-as> zip code ";
		}
		plainText+= " and a current ask of $" + oh.getPrice();
		ssmlText+= " and a current ask of $" + oh.getPrice();
				
		ssmlText+= " <break time='1s'/>The listing ID is <say-as interpret-as='spell-out'>" + oh.getListingID().replace("*", "") + "</say-as>.";
		plainText+= "The listing ID is " + oh.getListingID().replace("*", "") + ".";
		Map<String,String> speechText = putIntoMap(plainText,ssmlText);
		return speechText;
		
	}
	
	public Map<String,String> intentOpenHouseByZipCode (String zipCode) {
		OpenHouse oh = ts.getRandomizedOpenHouseByZipCode(Integer.valueOf(zipCode));
		Map<String,String> speechText = new HashMap<String,String>(2);
		String plainText = null;
		String ssmlText = null;
		if(oh==null) {
			plainText ="There are no open houses in the " + zipCode + " zip code";
			ssmlText="There are no open houses in the  <say-as interpret-as='spell-out'>" + zipCode + "</say-as> zip code";
		} else {
			ssmlText = "The next open house in <say-as interpret-as='spell-out'>" + zipCode+ "</say-as> is at <say-as interpret-as='address'>" + oh.getAddress() + "</say-as> starting " + convertDateTimeToSpeech(oh.getStartDateTime()) + " until " + convertTimeToSpeech(oh.getEndDateTime()) + ". ";
			plainText = "The next open house in " + zipCode+ " is at " + oh.getAddress() + " starting " + convertDateTimeToSpeech(oh.getStartDateTime()) + " until " + convertTimeToSpeech(oh.getEndDateTime()) + ". ";			
			Map<String,String> propertyDescriptionText = convertPropertyDescriptionToSpeech(oh, true);
			ssmlText += propertyDescriptionText.get("ssmlText");
			plainText += propertyDescriptionText.get("plainText");
		}
		
		speechText = putIntoMap(plainText,ssmlText);
		Logger.info("Response: " + plainText);
		return speechText;
	}
	
	private Map<String,String> putIntoMap(String plainText, String ssmlText) {
		Map<String,String> map = new HashMap<String,String>(2);
		map.put("plainText",plainText);
		map.put("ssmlText",ssmlText);
		return map;
	}
	
	

    private SpeechletResponse errorResponse() {    	
    	return errorResponse("Looks like there's a problem with the request, please try again");
    }
    
    private SpeechletResponse errorResponse(String errorMessage) {
    	Logger.info("default error called");
    	SimpleCard card = getSimpleCard(errorMessage);
    	PlainTextOutputSpeech speech = getPlainTextOutputSpeech("Looks like there's a problem, please try again");  
    	return SpeechletResponse.newTellResponse(speech,card);
    }
    
	public String generateErrorListingsDown() {
		String messageIfListingsDown = conf.getString("askde.messageIfListingsDown");
		Logger.info("Listings unavailable - Response is: " + messageIfListingsDown);
		return messageIfListingsDown;
	}

	public String generateErrorIntentBlank() {
		String messageIfListingsDown = conf.getString("askde.messageIfListingsDown");
		Logger.info("Listings unavailable - Response is: " + messageIfListingsDown);
		return messageIfListingsDown;
	}
	

/*	
	public String defaultResponse() {
		String responseMessage = conf.getString("askde.messageIfListingsDown");
		if(responseMessage==null) 
			responseMessage="Hi, I couldn't get what you said, please repeat that!";
		return packageResponse(addMarketing(responseMessage));
	}*/
	
	
	/*	public String invoke(JsonNode incomingJsonRequest) {
	Logger.info("Invoked");
	if(ts == null || ts.getOpenHouses()==null || incomingJsonRequest==null) {
		return packageResponse(generateErrorListingsDown());
	}
	
	SkillRequest sr = new SkillRequest(incomingJsonRequest);
	String intent = sr.getIntent();
	if(intent==null || intent.isEmpty()) 
		return packageResponse(generateErrorIntentBlank());			

	Logger.info("Intent invoked: " + intent);
	String responseMessage = null;
	
	switch(sr.getIntent().toLowerCase()) {
		case "getnextopenhousebyzipcode":
			responseMessage = intentOpenHouseByZipCode(sr);
			break;
		case "getnextopenhousebyneighborhood":
			sr.setIntent("GetNextOpenHouseByNeighborhood");
			responseMessage = intentOpenHouseByNeighborhood(sr);
			break;
		case "getnextopenhousenearme":
			sr.setIntent("GetNextOpenHouseNearMe");
			responseMessage = intentOpenHouseNearMe(sr);
			break;
		default:
			sr.setIntent("Default");
			responseMessage = defaultResponse(); // TODO: Change to a better message
	}
	
	
	
	SkillInvocation si = new SkillInvocation();
	si.setSkill(sr.getIntent());
	si.setSourceZipCode(sr.getUserZipCode());
	si.setRequest(sr.getJson().toString());
	si.setResponse(responseMessage);
	si.setDeviceID(sr.getDeviceId());
	Ebean.save(si);
	return responseMessage;		
}*/	
	/*	public String intentOpenHouseByZipCode(SkillRequest sr) {	
	String zipCode = sr.getJson().findPath("ZipCode").findPath("value").asText();
	if(!NumberUtils.isCreatable(zipCode)) {
		return packageResponse("The zip code was not found or came across as incomplete, please try again");
	}
	
	return packageResponse(addMarketing(intentOpenHouseByZipCode(zipCode)));
}*/



/*	public String intentOpenHouseNearMe(SkillRequest sr) {

	if(sr.getUserZipCode()==null || sr.getDeviceId()==null)
		return "{  \"version\": \"1.0\",  \"response\": {    \"card\": {      \"type\": \"AskForPermissionsConsent\",      \"permissions\": [        \"read::alexa:device:all:country_and_postal_code\" ]}}}";
	
	String zipCode = sr.getUserZipCode();
	if(zipCode==null)
		return defaultResponse();
	
	Logger.info("Consent token: " + sr.getConsentToken());
	Logger.info("Pulling up an open house listing");
	return packageResponse(addMarketing(intentOpenHouseByZipCode(sr.getUserZipCode())));
}*/


/*	public String intentOpenHouseByNeighborhood(SkillRequest sr) {	
	String neighborhood = null;
	
	JsonNode i = sr.getJson().findPath("Neighborhood");
	if(i==null)
		return packageResponse(generateErrorIntentBlank());
	
	JsonNode n = i.findPath("value");
	if(n==null)
		return packageResponse(generateErrorIntentBlank());
	
	neighborhood = n.textValue();
	if(neighborhood==null)
		return packageResponse(generateErrorIntentBlank());
	
	Logger.info("Neighborhood retrieved is " + neighborhood);
	OpenHouse oh = ts.getRandomizedOpenHouseByNeighborhood(neighborhood);
	if(oh==null)
		return packageResponse ("There are no open houses in the " + neighborhood + " neigbhorhood");
	
	String message = "The next open house in " + neighborhood + " is at <say-as interpret-as='address'>" + oh.getAddress() + "</say-as> starting " + convertDateTimeToSpeech(oh.getStartDateTime()) + " until " + convertTimeToSpeech(oh.getEndDateTime()) + ". ";			
	message += convertPropertyDescriptionToSpeech(oh,false);
	return packageResponse(addMarketing(message));
}*/
	
	/*	public String invoke(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
	if(ts == null || ts.getOpenHouses()==null || requestEnvelope==null) {
		return packageResponse(generateErrorListingsDown());
	}
	
	String intent = requestEnvelope.getRequest().getIntent().getName().toLowerCase();
	String responseMessage = null;
	switch(intent) {
		case "getnextopenhousebyzipcode":
			responseMessage = intentOpenHouseByZipCode(requestEnvelope);
			break;
		case "getnextopenhousebyneighborhood":
			responseMessage = intentOpenHouseByNeighborhood(sr);
			break;
		case "getnextopenhousenearme":

			responseMessage = intentOpenHouseNearMe(sr);
			break;
		default:
			responseMessage = defaultResponse(); // TODO: Change to a better message
	}
	
	SkillInvocation si = new SkillInvocation();
	si.setSkill(requestEnvelope.getRequest().getIntent().getName());
	if(requestEnvelope.getRequest().getIntent().getSlot("ZipCode")!=null)
		si.setSourceZipCode(requestEnvelope.getRequest().getIntent().getSlot("ZipCode").getValue());
	
	si.setRequest(requestEnvelope.getRequest().toString());
	si.setResponse(responseMessage);
	si.setDeviceID("Fix this");
	Ebean.save(si);
	
}*/

}
