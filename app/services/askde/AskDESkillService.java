package services.askde;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

import javax.inject.Inject;

import org.apache.commons.lang3.math.NumberUtils;

import io.ebean.Ebean;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.twilio.sdk.resource.instance.Address;

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
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.amazon.speech.ui.SsmlOutputSpeech;
import com.amazon.speech.ui.AskForPermissionsConsentCard;
import com.amazon.speech.speechlet.interfaces.system.SystemInterface;
import com.amazon.speech.speechlet.interfaces.system.SystemState;



public class AskDESkillService extends BaseAlexaService {
	

	private ListingsService ts;


	@Inject
	public AskDESkillService(Environment env, Config conf, ApplicationLifecycle al, ListingsService ts, WSClient ws) {
		super(env, conf, al, ws);
		this.ts = ts;
		
	}
	
    private SpeechletResponse getPermissionsResponse() {
        String speechText = "You have not given this skill permissions to access your address. " +
            "Please give this skill permissions to access your address.";

        // Create the permission card content.
        // The differences between a permissions card and a simple card is that the
        // permissions card includes additional indicators for a user to enable permissions if needed.
        AskForPermissionsConsentCard card = new AskForPermissionsConsentCard();
        card.setTitle("Ask Douglas Elliman");

        Set<String> permissions = new HashSet<>();
        permissions.add("read::alexa:device:all:country_and_postal_code");
        card.setPermissions(permissions);

        PlainTextOutputSpeech speech = getPlainTextOutputSpeech(speechText);

        return SpeechletResponse.newTellResponse(speech, card);
    }
	
    private SsmlOutputSpeech getSsmlOutputSpeech(String speechText) {
    	SsmlOutputSpeech speech = new SsmlOutputSpeech();
    	speech.setSsml("<speak>"+speechText+"</speak>");
        return speech;
    }
    
    private PlainTextOutputSpeech getPlainTextOutputSpeech(String speechText) {
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        return speech;
    }

    
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
	
	public SpeechletResponse invoke(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
		if(ts == null || ts.getOpenHouses()==null || requestEnvelope==null) {
	        SimpleCard card = new SimpleCard();
	        card.setTitle("Ask Douglas Elliman");
	        card.setContent(generateErrorListingsDown());
	        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
	        speech.setText(generateErrorListingsDown());
			return SpeechletResponse.newTellResponse(speech,card);		
		}
		
		String intent = requestEnvelope.getRequest().getIntent().getName().toLowerCase();
		SpeechletResponse responseMessage = null;
		switch(intent) {
			case "getnextopenhousebyzipcode":
				responseMessage = intentOpenHouseByZipCode(requestEnvelope);
				break;
/*			case "getnextopenhousebyneighborhood":
				responseMessage = intentOpenHouseByNeighborhood(sr);
				break;
			case "getnextopenhousenearme":

				responseMessage = intentOpenHouseNearMe(sr);
				break;
			default:
				responseMessage = defaultResponse(); // TODO: Change to a better message
*/		}
		
		SkillInvocation si = new SkillInvocation();
		si.setSkill(requestEnvelope.getRequest().getIntent().getName());
		if(requestEnvelope.getRequest().getIntent().getSlot("ZipCode")!=null)
			si.setSourceZipCode(requestEnvelope.getRequest().getIntent().getSlot("ZipCode").getValue());
		
		si.setRequest(requestEnvelope.getRequest().toString());
		si.setResponse(responseMessage.toString());
		si.setDeviceID("Fix this");
		Ebean.save(si);
		
		return responseMessage;
		
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
	
	public String convertPropertyDescriptionToSpeech(OpenHouse oh, boolean sayNeighborhood) {
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
		if(sayNeighborhood=true)
			description+= "located in " + oh.getNeighborhood() + " ";
		else
			description+= "located in the <say-as interpret-as='spell-out'>" + oh.getZipCode() + "</say-as> zip code ";
		
		description+= " and a current ask of $" + oh.getPrice();
		
		description+= " <break time='1s'/>The listing ID is <say-as interpret-as='spell-out'>" + oh.getListingID().replace("*", "") + "</say-as>.";		
		return description;
		
	}
	
	public String intentOpenHouseByZipCode (String zipCode) {
		OpenHouse oh = ts.getRandomizedOpenHouseByZipCode(Integer.valueOf(zipCode));
		String message= null;
		if(oh==null)
			message="There are no open houses in the  <say-as interpret-as='spell-out'>" + zipCode + "</say-as> zip code";
		else {
			message = "The next open house in <say-as interpret-as='spell-out'>" + zipCode+ "</say-as> is at <say-as interpret-as='address'>" + oh.getAddress() + "</say-as> starting " + convertDateTimeToSpeech(oh.getStartDateTime()) + " until " + convertTimeToSpeech(oh.getEndDateTime()) + ". ";			
			message += convertPropertyDescriptionToSpeech(oh, true);
		}
		Logger.info("Response: " + message);
		Logger.info("Packaged response: " + packageResponse(message));
		return message;
	}

	public SpeechletResponse intentOpenHouseByZipCode(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {	
		String zipCode = requestEnvelope.getRequest().getIntent().getSlot("ZipCode").getValue();
		if(!NumberUtils.isCreatable(zipCode)) {
	        SimpleCard card = new SimpleCard();
	        card.setTitle("Ask Douglas Elliman");
	        card.setContent("The zip code was not found or came across as incomplete, please try again");
	        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
	        speech.setText("The zip code was not found or came across as incomplete, please try again");
			return SpeechletResponse.newTellResponse(speech,card);
		}
		
		String speechText = intentOpenHouseByZipCode(zipCode);
        SimpleCard card = new SimpleCard();
        card.setTitle("Ask Douglas Elliman");
        card.setContent(speechText);
        SsmlOutputSpeech speech = getSsmlOutputSpeech(speechText); 
		return SpeechletResponse.newTellResponse(speech,card);
	}	
	
	
	public SpeechletResponse intentOpenHouseNearMe(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
/*		String consentToken = session.getUser().getPermissions().getConsentToken();
		if(consentToken==null)
			return getPermissionResponse();
		SystemState systemState = getSystemState(speechletRequestEnvelope.getContext());
		String deviceID = systemState.getDevice().getDeviceId();
		String apiEndpoint = systemState.getApiEndpoint();
		
		
		 AlexaDeviceAddressClient alexaDeviceAddressClient = new AlexaDeviceAddressClient(
                 deviceId, consentToken, apiEndpoint);
		 
		 Address addressObject = alexaDeviceAddressClient.getFullAddress();
		 String postalCode = addressObject.getPostalCode();
		 
		//requestEnvelope.getRequest().getIntent().
		if(sr.getUserZipCode()==null || sr.getDeviceId()==null)
			return "{  \"version\": \"1.0\",  \"response\": {    \"card\": {      \"type\": \"AskForPermissionsConsent\",      \"permissions\": [        \"read::alexa:device:all:country_and_postal_code\" ]}}}";
		
		String zipCode = sr.getUserZipCode();
		if(zipCode==null)
			return defaultResponse();
		
		Logger.info("Consent token: " + consentToken);
		Logger.info("Pulling up an open house listing");
		return packageResponse(addMarketing(intentOpenHouseByZipCode(postalCode)));*/
		return null;
	}

	public String intentOpenHouseByZipCode(SkillRequest sr) {	
		String zipCode = sr.getJson().findPath("ZipCode").findPath("value").asText();
		if(!NumberUtils.isCreatable(zipCode)) {
			return packageResponse("The zip code was not found or came across as incomplete, please try again");
		}
		
		return packageResponse(addMarketing(intentOpenHouseByZipCode(zipCode)));
	}
	
	
	
	public String intentOpenHouseNearMe(SkillRequest sr) {

		if(sr.getUserZipCode()==null || sr.getDeviceId()==null)
			return "{  \"version\": \"1.0\",  \"response\": {    \"card\": {      \"type\": \"AskForPermissionsConsent\",      \"permissions\": [        \"read::alexa:device:all:country_and_postal_code\" ]}}}";
		
		String zipCode = sr.getUserZipCode();
		if(zipCode==null)
			return defaultResponse();
		
		Logger.info("Consent token: " + sr.getConsentToken());
		Logger.info("Pulling up an open house listing");
		return packageResponse(addMarketing(intentOpenHouseByZipCode(sr.getUserZipCode())));
	}
	

	public String intentOpenHouseByNeighborhood(SkillRequest sr) {	
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
	
	public String invoke(JsonNode incomingJsonRequest) {
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
	}
	
	public String defaultResponse() {
		String responseMessage = conf.getString("askde.messageIfListingsDown");
		if(responseMessage==null) 
			responseMessage="Hi, I couldn't get what you said, please repeat that!";
		return packageResponse(addMarketing(responseMessage));
	}
	


}
