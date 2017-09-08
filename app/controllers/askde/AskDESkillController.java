package controllers.askde;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;

import controllers.raven.BaseController;
import play.Logger;
import play.mvc.Result;
import services.askde.AskDESkillService;

public class AskDESkillController extends BaseController {

	@Inject AskDESkillService dess;
	
	public Result invoke() {
		Logger.info("Ask DE skill request received");
		JsonNode json = request().body().asJson();
		
		return ok(dess.invoke(json)).as("application/json");
		
	}
}
