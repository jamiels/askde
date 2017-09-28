package controllers.askde;

import javax.inject.Inject;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.fasterxml.jackson.databind.JsonNode;

import play.Logger;
import play.mvc.Result;
import services.askde.AskDESkillService;

public class AskDESkillController extends BaseAlexaController {

	public AskDESkillController() {
		super();
	}


	@Inject AskDESkillService dess;
	
/*	public Result invoke() {
		Logger.info("Ask DE skill request received");
		JsonNode json = request().body().asJson();
		
		return ok(dess.invoke(json)).as("application/json");
		
	}*/
	
	
    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
        Logger.info("onIntent requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(),
                requestEnvelope.getSession().getSessionId());

        Intent intent = requestEnvelope.getRequest().getIntent();
        String intentName = (intent != null) ? intent.getName() : null;
        Logger.info("Fired onIntent");
        return dess.invoke(requestEnvelope);
/*        if ("HelloWorldIntent".equals(intentName)) {
            return alexaService.getHelloResponse();
        } else if ("AMAZON.HelpIntent".equals(intentName)) {
            return alexaService.getHelpResponse();
*/        //}
        //return null;
    }


	@Override
	public SpeechletResponse onLaunch(SpeechletRequestEnvelope<LaunchRequest> requestEnvelope) {
		// TODO Auto-generated method stub

		Logger.info("Fired onLaunch");
		return dess.getWelcomeMessage(requestEnvelope);
	}


	@Override
	public void onSessionEnded(SpeechletRequestEnvelope<SessionEndedRequest> arg0) {
		// TODO Auto-generated method stub
		Logger.info("Fired onSessionEnded");
		
	}


	@Override
	public void onSessionStarted(SpeechletRequestEnvelope<SessionStartedRequest> arg0) {
		Logger.info("Fired onSessionStarted");
		
		
	}
}
