package controllers.askde;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import controllers.raven.BaseController;
import models.askde.Neighborhood;
import models.askde.OpenHouse;
import play.Logger;
import play.mvc.Result;
import services.askde.ListingsService;
import views.html.*;

public class AdminPanelController extends BaseController {

	@Inject ListingsService ls;
	
    public Result index() {
        return ok(index.render());
    }
    
    public Result viewOpenHouses() {
    	return ok(openhouses.render(OpenHouse.find.all()));
    }
    
  
    public Result loadFeed() {
    	String apiKey = request().getQueryString("apiKey");
    	if(apiKey==null || apiKey.isEmpty())
    		return ok("");
    	if(apiKey.equalsIgnoreCase("df39e0ee-01f1-46b3-a0dd-4c639c6a7655")) {
    		ls.loadOpenHouses();
    		return ok(index.render());
    	}
    	return ok("");
    }
    
    public Result viewZipCodes() {
    	return ok(zipcodes.render());
    }
    
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
    
    public Result viewFeedHistory() {
    	return ok(feedhistory.render());
    }
 
/*    public Result getRandomOpenHouseByZipCode(Integer zipCode) {
    	OpenHouse oh = ls.getRandomizedOpenHouseByZipCode(zipCode);
    	List<OpenHouse> ohs = new ArrayList<OpenHouse>(1);
    	if(oh!=null)
    		ohs.add(oh);
    	return ok(index.render());
    }*/
}
