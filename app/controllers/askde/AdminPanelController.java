package controllers.askde;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import io.ebean.Ebean;

import be.objectify.deadbolt.java.actions.SubjectPresent;
import controllers.routes;
import controllers.raven.BaseController;
import forms.askde.NewPartOfSpeechForm;
import models.askde.Adjective;
import models.askde.Appender;
import models.askde.Byline;
import models.askde.Neighborhood;
import models.askde.OpenHouse;
import models.askde.BaseWord;
import play.Logger;
import play.data.Form;
import play.mvc.Result;
import services.askde.ListingsService;
import views.html.*;

public class AdminPanelController extends BaseController {

	@Inject ListingsService ls;
	
	
	
	@SubjectPresent
    public Result index() {
        return ok(index.render());
    }
    
	@SubjectPresent
    public Result viewOpenHouses() {
    	return ok(openhouses.render(OpenHouse.find.all()));
    }
    
  
    public Result loadFeed() {
		String apiKey =  conf.getString("askde.feedAPIKey");
		String apiKeySubmitted = request().getQueryString("apiKey");
		
    	if(apiKey==null || apiKey.isEmpty() || apiKeySubmitted==null || apiKeySubmitted.isEmpty()) {
    		notification("Unauthorized access to load feed");
    		return redirect(controllers.askde.routes.AdminPanelController.viewFeedHistory());
    	}
    	if(apiKey.equalsIgnoreCase(apiKeySubmitted)) {
    		ls.loadOpenHouses();
    		return redirect(controllers.askde.routes.AdminPanelController.viewFeedHistory());
    	}
    	return redirect(controllers.askde.routes.AdminPanelController.viewFeedHistory());
    }
    
	@SubjectPresent
    public Result viewZipCodes() {
    	return ok(zipcodes.render());
    }
    
	@SubjectPresent
    public Result viewNeighborhoods() {
    	
    	List<OpenHouse> ohs = OpenHouse.find.all();
    	Logger.info("Open houses: " + ohs.size());
    	Set<String> listingsNeighborhoods = new HashSet<>();
    	for (OpenHouse oh : ohs)
    		listingsNeighborhoods.add(oh.getNeighborhood().toLowerCase().trim());
    	
    	List<Neighborhood> ns = Neighborhood.find.all();
    	Set<String> canonicalNeighborhoods = new HashSet<>();
    	for (Neighborhood n : ns)
    		canonicalNeighborhoods.add(n.getName().toLowerCase().trim());
    	
    	Logger.info("Listings neighborhood set size: " + listingsNeighborhoods.size());
    	Logger.info("Canonical neighborhood set size: " + canonicalNeighborhoods.size());
    	
    	
    	listingsNeighborhoods.removeAll(canonicalNeighborhoods);
    	
    	Logger.info("Listings neighborhood set size shrunk to: " + listingsNeighborhoods.size());
    	Logger.info("Canonical neighborhood set size: " + canonicalNeighborhoods.size());
    	
    	return ok(neighborhoods.render(canonicalNeighborhoods,listingsNeighborhoods));
    }
    
	@SubjectPresent
    public Result viewFeedHistory() {
    	return ok(feedhistory.render());
    }
    
	@SubjectPresent
    public Result deactivatePartsOfSpeech() {
    	String type = request().getQueryString("type");
    	if(type==null || type.isEmpty())
    		return redirect(controllers.askde.routes.AdminPanelController.index());
    	
    	String uuid = request().getQueryString("uuid");
    	if(uuid==null || uuid.isEmpty())
			return redirect(controllers.askde.routes.AdminPanelController.index());
    	
    	switch(type.toLowerCase().trim()) {
    		case "appender":
    			Appender a = Appender.findByUUID(uuid);
    			if(a==null)
    				return redirect(controllers.askde.routes.AdminPanelController.index());
    			a.setActive(false);
    			Ebean.update(a);
    			break;
    		case "byline":
    			Byline b = Byline.findByUUID(uuid);
    			if(b==null)
    				return redirect(controllers.askde.routes.AdminPanelController.index());
    			b.setActive(false);
    			Ebean.update(b);
    			break;
    		case "adjective":
    			Adjective adj = Adjective.findByUUID(uuid);
    			if(adj==null)
    				return redirect(controllers.askde.routes.AdminPanelController.index());
    			adj.setActive(false);
    			Ebean.update(adj);
    			break;
    		default:
    			return redirect(controllers.askde.routes.AdminPanelController.index());
    	}
    	
    	return redirect(controllers.askde.routes.AdminPanelController.index());
    			
    }
    
	@SubjectPresent
    public Result activatePartsOfSpeech() {
    	String type = request().getQueryString("type");
    	if(type==null || type.isEmpty())
    		return redirect(controllers.askde.routes.AdminPanelController.index());
    	
    	String uuid = request().getQueryString("uuid");
    	if(uuid==null || uuid.isEmpty())
			return redirect(controllers.askde.routes.AdminPanelController.index());
    	
    	switch(type.toLowerCase().trim()) {
    		case "appender":
    			Appender a = Appender.findByUUID(uuid);
    			if(a==null)
    				return redirect(controllers.askde.routes.AdminPanelController.index());
    			a.setActive(true);
    			Ebean.update(a);
    			break;
    		case "byline":
    			Byline b = Byline.findByUUID(uuid);
    			if(b==null)
    				return redirect(controllers.askde.routes.AdminPanelController.index());
    			b.setActive(true);
    			Ebean.update(b);
    			break;
    		case "adjective":
    			Adjective adj = Adjective.findByUUID(uuid);
    			if(adj==null)
    				return redirect(controllers.askde.routes.AdminPanelController.index());
    			adj.setActive(true);
    			Ebean.update(adj);
    			break;
    		default:
    			return redirect(controllers.askde.routes.AdminPanelController.index());
    	}
    	
    	return redirect(controllers.askde.routes.AdminPanelController.index());
    			
    }    

	@SubjectPresent
    public Result deletePartsOfSpeech() {
    	String type = request().getQueryString("type");
    	if(type==null || type.isEmpty())
    		return redirect(controllers.askde.routes.AdminPanelController.index());
    	
    	String uuid = request().getQueryString("uuid");
    	if(uuid==null || uuid.isEmpty())
			return redirect(controllers.askde.routes.AdminPanelController.index());
    	
    	switch(type.toLowerCase().trim()) {
    		case "appender":
    			Appender a = Appender.findByUUID(uuid);
    			if(a==null)
    				return redirect(controllers.askde.routes.AdminPanelController.index());
    			a.setCurrent(false);
    			Ebean.update(a);
    			break;
    		case "byline":
    			Byline b = Byline.findByUUID(uuid);
    			if(b==null)
    				return redirect(controllers.askde.routes.AdminPanelController.index());
    			b.setCurrent(false);
    			Ebean.update(b);
    			break;
    		case "adjective":
    			Adjective adj = Adjective.findByUUID(uuid);
    			if(adj==null)
    				return redirect(controllers.askde.routes.AdminPanelController.index());
    			adj.setCurrent(false);
    			Ebean.update(adj);
    			break;
    		default:
    			return redirect(controllers.askde.routes.AdminPanelController.index());
    	}
    	
    	return redirect(controllers.askde.routes.AdminPanelController.index());

    }
    
	@SubjectPresent
    public Result viewSkillInvocationHistory() {
    	return ok(skillinvocationhistory.render());
    }
    
	@SubjectPresent
    public Result submitNewPartOfSpeech() {
		Form<NewPartOfSpeechForm> request = ff.form(NewPartOfSpeechForm.class).bindFromRequest();
    	if(request.hasErrors()) {
    		showFormBindingErrors(request);
    		return badRequest(request.errorsAsJson());
    	}
    	
    	NewPartOfSpeechForm form = request.get();
    	String posType = (form.getPosType()!=null) ? form.getPosType().toLowerCase() : "";
    	BaseWord a = null;
    	if(posType.equals("adjective")) 
    		a = new Adjective();
    	else
    		if(posType.equals("appender")) 
    			a = new Appender();
    		else
    			if(posType.equals("byline"))
    				a = new Byline();
    	if(a!=null) {
    		a.setMessage(form.getTextContent());
    		a.setActive(true);
    		a.setCurrent(true);
        	notification("New " + posType + " added");
        	Ebean.save(a);
    	} else 
	    	notification("There's a problem with the submission, please try again");
    	return redirect(controllers.askde.routes.AdminPanelController.index());
	}
	
	

}
