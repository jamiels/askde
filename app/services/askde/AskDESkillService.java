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
import com.amazon.speech.speechlet.interfaces.system.SystemInterface;
import com.amazon.speech.speechlet.interfaces.system.SystemState;
import com.amazon.speech.ui.OutputSpeech;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.Context;
import controllers.askde.AlexaDeviceAddressClient;
import controllers.askde.Address;

public class AskDESkillService extends BaseAlexaService {
	

	private ListingsService ts;
	


	@Inject
	public AskDESkillService(Environment env, Config conf, ApplicationLifecycle al, ListingsService ts, WSClient ws) {
		super(env, conf, al, ws);
		this.ts = ts;
		
	}
	
	public SpeechletResponse intentOpenHouseByNeighborhood(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {	
		
		
		String neighborhood = requestEnvelope.getRequest().getIntent().getSlot("Neighborhood").getValue();
		if(neighborhood==null)
			return  defaultError();
					
					//packageResponse(generateErrorIntentBlank());
		
		Logger.info("Neighborhood retrieved is " + neighborhood);
		OpenHouse oh = ts.getRandomizedOpenHouseByNeighborhood(neighborhood);
		if(oh==null) {
			String speechText = intentOpenHouseByZipCode("There are currently no open houses in the " + neighborhood + " neigbhorhood");
	        SimpleCard card = getSimpleCard("Ask Douglas Elliman",speechText);
	        card.setTitle("Ask Douglas Elliman");
	        card.setContent(speechText);
	        PlainTextOutputSpeech speech = getPlainTextOutputSpeech(speechText); 
			return SpeechletResponse.newTellResponse(speech,card);
		}
	
		
		String speechText = "The next open house in " + neighborhood + " is at <say-as interpret-as='address'>" + oh.getAddress() + "</say-as> starting " + convertDateTimeToSpeech(oh.getStartDateTime()) + " until " + convertTimeToSpeech(oh.getEndDateTime()) + ". ";			
		speechText += convertPropertyDescriptionToSpeech(oh,false);
		
        SimpleCard card = getSimpleCard("Ask Douglas Elliman",speechText);
        card.setTitle("Ask Douglas Elliman");
        card.setContent(addMarketing(speechText));
        SsmlOutputSpeech speech = getSsmlOutputSpeech(speechText); 
        return SpeechletResponse.newTellResponse(speech, card);
	}
	
    private SpeechletResponse getPermissionsResponse() {
        String speechText = "Ask Douglas Elliman does not have permission to access your zip code " +
            "Please give us permission by going into your Alexa app and following the instructions on the card we just sent you. See you soon.";
        AskForPermissionsConsentCard card = new AskForPermissionsConsentCard();
        card.setTitle("Ask Douglas Elliman");

        Set<String> permissions = new HashSet<>();
        permissions.add("read::alexa:device:all:address:country_and_postal_code");
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
			case "getnextopenhousebyneighborhood":
				responseMessage = intentOpenHouseByNeighborhood(requestEnvelope);
				break;
			case "getnextopenhousenearme":
				responseMessage = intentOpenHouseNearMe(requestEnvelope);
				break;
			default:
				responseMessage = defaultError();
		}
		
/*		SkillInvocation si = new SkillInvocation();
		si.setSkill(requestEnvelope.getRequest().getIntent().getName());
		if(requestEnvelope.getRequest().getIntent().getSlot("ZipCode")!=null)
			si.setSourceZipCode(requestEnvelope.getRequest().getIntent().getSlot("ZipCode").getValue());
		
		si.setRequest(requestEnvelope.getRequest().toString());
		si.setResponse(responseMessage.toString());
		si.setDeviceID("Fix this");
		Ebean.save(si);*/
		
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
	
    private SimpleCard getSimpleCard(String title, String content) {
        SimpleCard card = new SimpleCard();
        card.setTitle(title);
        card.setContent(content);

        return card;
    }
    
    private Reprompt getReprompt(OutputSpeech outputSpeech) {
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(outputSpeech);

        return reprompt;
    }
    
    private SpeechletResponse getAskResponse(String cardTitle, String speechText) {
        SimpleCard card = getSimpleCard(cardTitle, speechText);
        PlainTextOutputSpeech speech = getPlainTextOutputSpeech(speechText);
        Reprompt reprompt = getReprompt(speech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }
    
    private SystemState getSystemState(Context context) {
        return context.getState(SystemInterface.class, SystemState.class);
    }
    
    private SpeechletResponse defaultError() {
    	Logger.info("default error called");
    	SimpleCard card = getSimpleCard("Ask Douglas Elliman","Looks like there's a problem, please try again");
    	PlainTextOutputSpeech speech = getPlainTextOutputSpeech("Looks like there's a problem, please try again");
    	return SpeechletResponse.newTellResponse(speech, card);
    }
    
	public SpeechletResponse intentOpenHouseNearMe(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
		Logger.info("Getting session");
		Session session = requestEnvelope.getSession();
		Logger.info("Got session");
		if(session==null)
			return defaultError();
		Logger.info("Getting user");
		User u = session.getUser();
		if(u==null)
			return defaultError();
		Logger.info("Getting permissions");
		Permissions ps = u.getPermissions();
		if(ps==null)
			return getPermissionsResponse();
		Logger.info("Getting consentToken");
		String consentToken = ps.getConsentToken();

		if(consentToken==null) {
			Logger.info("Consent token empty");
			return getPermissionsResponse();
		}
		
		String postalCode = null;
		try {
			SystemState systemState = getSystemState(requestEnvelope.getContext());
			String deviceID = systemState.getDevice().getDeviceId();
			String apiEndpoint = systemState.getApiEndpoint();
			
			
			 AlexaDeviceAddressClient alexaDeviceAddressClient = new AlexaDeviceAddressClient(
					 deviceID, consentToken, apiEndpoint);
			 
			 
			 Address addressObject = alexaDeviceAddressClient.getFullAddress();
	         if (addressObject == null) {
	             return getAskResponse("Ask Douglas Elliman", "Address information missing, please try again");
	         }
			 
			 
			 postalCode = addressObject.getPostalCode();
			 if(postalCode==null)
				 return getAskResponse("Ask Douglas Elliman", "Address information missing, please try again");
		 
		 
        } catch (UnauthorizedException e) {
        	e.printStackTrace();
            return getPermissionsResponse();
        } catch (DeviceAddressClientException e) {
        	e.printStackTrace();
            Logger.error("Device Address Client failed to successfully return the address.", e);
            return getAskResponse("Ask Douglas Elliman", "There was an error, please try again later");
        }
		 
/*		//requestEnvelope.getRequest().getIntent().
		if(sr.getUserZipCode()==null || sr.getDeviceId()==null)
			return "{  \"version\": \"1.0\",  \"response\": {    \"card\": {      \"type\": \"AskForPermissionsConsent\",      \"permissions\": [        \"read::alexa:device:all:country_and_postal_code\" ]}}}";
		
		String zipCode = sr.getUserZipCode();
		if(zipCode==null)
			return defaultResponse();*/
		
		Logger.info("Consent token: " + consentToken);
		Logger.info("Pulling up an open house listing");
		
		String speechText = addMarketing(intentOpenHouseByZipCode(postalCode));
        SimpleCard card = new SimpleCard();
        card.setTitle("Ask Douglas Elliman");
        card.setContent(speechText);
        SsmlOutputSpeech speech = getSsmlOutputSpeech(speechText); 
		return SpeechletResponse.newTellResponse(speech,card);
	}

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
	
	public String defaultResponse() {
		String responseMessage = conf.getString("askde.messageIfListingsDown");
		if(responseMessage==null) 
			responseMessage="Hi, I couldn't get what you said, please repeat that!";
		return packageResponse(addMarketing(responseMessage));
	}
	


}
