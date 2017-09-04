package controllers.askde;

import javax.inject.Inject;

import controllers.raven.BaseController;
import play.mvc.Result;
import services.askde.ListingsService;

public class AdminController extends BaseController {

	@Inject ListingsService ls;
	
	
	public Result refreshListingsDatabase() {
		
		
		return ok("");
	}
}
