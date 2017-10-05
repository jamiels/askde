package controllers.askde;

import javax.inject.Inject;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.SpeechletResponse;

import controllers.raven.alexa.BaseAlexaController;
import play.Logger;
import services.askde.AskDESkillService;
import util.App;
import util.App;

public class AskDESkillController extends BaseAlexaController {

	@Inject AskDESkillService dess;
	
	public AskDESkillController() {
		super();
		App.prepareSystemProperties();
	}
	
    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
        Logger.info("onIntent requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(),
                requestEnvelope.getSession().getSessionId());
        Logger.info("Fired onIntent");
        return dess.invoke(requestEnvelope);
    }


	@Override
	public SpeechletResponse onLaunch(SpeechletRequestEnvelope<LaunchRequest> requestEnvelope) {
		Logger.info("Fired onLaunch");
		return dess.getWelcomeMessage(requestEnvelope);
	}


	@Override
	public void onSessionEnded(SpeechletRequestEnvelope<SessionEndedRequest> arg0) {
		Logger.info("Fired onSessionEnded");
	}


	@Override
	public void onSessionStarted(SpeechletRequestEnvelope<SessionStartedRequest> arg0) {
		Logger.info("Fired onSessionStarted");
	}
}
