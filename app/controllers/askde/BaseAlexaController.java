package controllers.askde;

import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.Sdk;
import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.SpeechletRequestHandlerException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.speechlet.SpeechletV2;
import com.amazon.speech.speechlet.authentication.SpeechletRequestSignatureVerifier;
import com.amazon.speech.speechlet.servlet.ServletSpeechletRequestHandler;
import com.amazon.speech.speechlet.servlet.SpeechletServlet;
import com.google.inject.Inject;

import controllers.raven.BaseController;
import play.mvc.Result;
import raven.services.AlexaService;
import util.App;


public abstract class BaseAlexaController extends BaseController implements SpeechletV2 {


	/**
	 * Controller For Handling Alexa Functionality.
	 */


    private static final Logger log = LoggerFactory.getLogger(BaseAlexaController.class);

    private final boolean disableRequestSignatureCheck;

    @Inject
    private transient ServletSpeechletRequestHandler speechletRequestHandler;

    @Inject
    private SpeechletServlet speechletServlet;


    public BaseAlexaController() {
        App.prepareSystemProperties();

        // An invalid value or null will turn signature checking on.
        disableRequestSignatureCheck =
                Boolean.parseBoolean(System.getProperty(Sdk.DISABLE_REQUEST_SIGNATURE_CHECK_SYSTEM_PROPERTY));
    }

    public Result invoke() {
        Result result;
        try {
            byte[] serializedSpeechletRequest = request().body().asJson().toString().getBytes();
            if (disableRequestSignatureCheck) {
                log.warn("Warning: Speechlet request signature verification has been disabled!");
            } else {
                // Verify the authenticity of the request by checking the provided signature &
                // certificate.
            	log.info("Checking authenticity");
                Optional<String> signatureRequest = request().header(Sdk.SIGNATURE_REQUEST_HEADER);
                Optional<String> signatureCertificateRequest = request().header(Sdk.SIGNATURE_CERTIFICATE_CHAIN_URL_REQUEST_HEADER);

                SpeechletRequestSignatureVerifier.checkRequestSignature(serializedSpeechletRequest,
                        signatureRequest.isPresent() ? signatureRequest.get() : null,
                        signatureCertificateRequest.isPresent() ? signatureCertificateRequest.get() : null);
            }

            // Generate JSON and send back the response
            byte[] outputBytes = speechletRequestHandler.handleSpeechletCall(this, serializedSpeechletRequest);
            String outputResult = new String(outputBytes, "UTF-8");
            log.warn("Output result is: " + outputResult);

            result = ok(outputResult).as("application/json");
        } catch (SpeechletRequestHandlerException | SecurityException ex) {
            log.error("Exception occurred in handlePostResponse method, returning status code {}", HttpServletResponse.SC_BAD_REQUEST, ex);
            result = badRequest(ex.getMessage());
        } catch (Exception ex) {
            log.error("Exception occurred in handlePostResponse method, returning status code {}", HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex);
            result = internalServerError(ex.getMessage());
        }
        return result;
    }
/*
    @Override
    public void onSessionStarted(SpeechletRequestEnvelope<SessionStartedRequest> requestEnvelope) {
        log.info("onSessionStarted requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(),
                requestEnvelope.getSession().getSessionId());
        // any initialization logic goes here
    }

    @Override
    public SpeechletResponse onLaunch(SpeechletRequestEnvelope<LaunchRequest> requestEnvelope) {
        log.info("onLaunch requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(),
                requestEnvelope.getSession().getSessionId());
        return alexaService.getWelcomeResponse();
    }



    @Override
    public void onSessionEnded(SpeechletRequestEnvelope<SessionEndedRequest> requestEnvelope) {
        log.info("onSessionEnded requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(),
                requestEnvelope.getSession().getSessionId());
        // any cleanup logic goes here
    }*/
}
