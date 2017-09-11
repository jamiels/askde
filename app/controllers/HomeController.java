package controllers;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import bindings.askde.listings.Listing;
import models.askde.OpenHouse;
import play.mvc.*;
import services.askde.ListingsService;
import views.html.*;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
	@Inject ListingsService ls;
	
    public Result index() {
        return ok(index.render(ls.getOpenHouses()));
    }
    
    public Result loadFeed() {
    	String apiKey = request().getQueryString("apiKey");
    	if(apiKey==null || apiKey.isEmpty())
    		return ok("");
    	if(apiKey.equalsIgnoreCase("df39e0ee-01f1-46b3-a0dd-4c639c6a7655")) {
    		ls.loadOpenHouses();
    		return ok(index.render(ls.getOpenHouses()));
    	}
    	return ok("");
    }
    
    public Result getRandomOpenHouseByZipCode(Integer zipCode) {
    	OpenHouse oh = ls.getRandomizedOpenHouseByZipCode(zipCode);
    	List<OpenHouse> ohs = new ArrayList<OpenHouse>(1);
    	if(oh!=null)
    		ohs.add(oh);
    	return ok(index.render(ohs));
    }
    
  

}
