import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import bindings.askde.listings.Listing;
import bindings.askde.listings.Listings;
import models.askde.Neighborhood;
import models.askde.ZipCode;
import models.raven.AuthenticatedUser;
import play.Application;
import com.typesafe.config.Config;
import play.Environment;
import play.Logger;
import play.inject.ApplicationLifecycle;
import raven.BaseLifecycle;
import services.askde.ListingsService;

@Singleton
public class Lifecycle extends BaseLifecycle {
	
	
	@Inject
	public Lifecycle(Environment env, Config conf, Application app, ApplicationLifecycle al, ListingsService ls) {
		super(env, conf, app, al);
	
		if(Neighborhood.find.query().findCount() < 1) {
			Logger.info("Loading neighborhoods");
			ls.loadCanonicalNeighborhoods();
		}
			
		
		if(ZipCode.find.query().findCount() < 1) {
			Logger.info("Loading zip codes");
			ls.loadZipCodes();
		}
		
		al.addStopHook(() -> {
			return CompletableFuture.completedFuture(null);
		});

	}

}
