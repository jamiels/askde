import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import bindings.askde.listings.Listing;
import bindings.askde.listings.Listings;
import models.raven.AuthenticatedUser;
import play.Application;
import play.Configuration;
import play.Environment;
import play.Logger;
import play.inject.ApplicationLifecycle;
import raven.BaseLifecycle;
import services.askde.ListingsService;

@Singleton
public class Lifecycle extends BaseLifecycle {
	
	
	@Inject
	public Lifecycle(Environment env, Configuration conf, Application app, ApplicationLifecycle al, ListingsService ls) {
		super(env, conf, app, al);
	
		//ls.loadOpenHouses();
		
		al.addStopHook(() -> {
			return CompletableFuture.completedFuture(null);
		});

	}

}
