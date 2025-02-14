package services.askde;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang3.StringUtils;

import io.ebean.Ebean;

import bindings.askde.listings.Listing;
import bindings.askde.listings.Listings;
import bindings.askde.listings.OpenHouses;
import bindings.askde.listings.OpenHouse;
import exceptions.askde.ListingsLoadException;
import models.askde.Neighborhood;
import models.askde.ZillowFeedHistory;
import models.askde.ZipCode;
import com.typesafe.config.Config;
import play.Environment;
import play.Logger;
import play.inject.ApplicationLifecycle;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import raven.services.AWS;
import raven.services.BaseService;

@Singleton
public class ListingsService extends BaseService {
	@Inject WSClient ws;
	
	@Inject
	public ListingsService (Environment env, Config conf, ApplicationLifecycle al) {
		super(env,conf,al);
	}
	
	public List<models.askde.OpenHouse> getOpenHouses() {
		List<models.askde.OpenHouse> openHouses = models.askde.OpenHouse.find.all();
		return openHouses;
	}
	
	public models.askde.OpenHouse getRandomizedOpenHouseByNeighborhood(String neighborhood) {
		Logger.info("## Search request for random open house by neighborhood " + neighborhood);
		Date currentDateTime = new Date();
		List<models.askde.OpenHouse> results =  models.askde.OpenHouse.find.query().where().eq("neighborhood", neighborhood.toLowerCase()).and().gt("startDateTime", currentDateTime).order("startDateTime asc").findList();
		Logger.info("Matches with same neighborhood " + neighborhood + " found: " + results.size());
		
		if(results.size()>1) {
			Date nextOpenHouse = results.get(0).getStartDateTime();
			List<models.askde.OpenHouse> openHousesAtSameTime = new ArrayList<models.askde.OpenHouse>();
			for (models.askde.OpenHouse oh : results) {
				if(oh.getStartDateTime().equals(nextOpenHouse))
					openHousesAtSameTime.add(oh);
			}
			
			Logger.info("Multiple open houses at the earliest time: " + openHousesAtSameTime.size() + " for neighborhood " + neighborhood);
			if(openHousesAtSameTime.size()>1) {
				int randomItem = ThreadLocalRandom.current().nextInt(0, openHousesAtSameTime.size());
				return openHousesAtSameTime.get(randomItem);
			}
		}
		
		if(results.size()==1) {
			Logger.info("Only one open house in neighborhood " + neighborhood);
			return results.get(0);
		}
		

		if(results.size()>0) {
			Logger.info("Going to randomize from " + results.size() + " open houses in neighborhood "  + neighborhood);
			return results.get(ThreadLocalRandom.current().nextInt(0, results.size()));	
		}
		
		Logger.info("No open houses found for neighborhood " + neighborhood);
		return null;
	}
	
	public models.askde.OpenHouse getRandomizedOpenHouseByZipCode (Integer zipCode) {
		
		// Business rule
		// The current EST date/time is retrieved
		// The next available (earliest) open house's date/time after the current EST date/time is identified
		// If there are more than one open houses at the same earliest date/time, one is selected randomly
		// If there are no additional open houses at the same date/time, one is selected randomly from all open houses in that zip code
		Logger.info("## Search request for random open house by zip code " + zipCode);
		Date currentDateTime = new Date();
		List<models.askde.OpenHouse> results =  models.askde.OpenHouse.find.query().where().eq("zipCode", zipCode).and().gt("startDateTime", currentDateTime).order("startDateTime asc").findList();
		Logger.info("Matches with same zip code " + zipCode + " found: " + results.size());
		
		if(results.size()>1) {
			Date nextOpenHouse = results.get(0).getStartDateTime();
			List<models.askde.OpenHouse> openHousesAtSameTime = new ArrayList<models.askde.OpenHouse>();
			for (models.askde.OpenHouse oh : results) {
				if(oh.getStartDateTime().equals(nextOpenHouse))
					openHousesAtSameTime.add(oh);
			}
			
			Logger.info("Multiple open houses at the earliest time: " + openHousesAtSameTime.size() + " for zip code " + zipCode);
			if(openHousesAtSameTime.size()>1) {
				int randomItem = ThreadLocalRandom.current().nextInt(0, openHousesAtSameTime.size());
				return openHousesAtSameTime.get(randomItem);
			}
		}
		
		if(results.size()==1) {
			Logger.info("Only one open house in zip code " + zipCode);
			return results.get(0);
		}
		

		if(results.size()>0) {
			Logger.info("Going to randomize from " + results.size() + " open houses in zip code "  + zipCode);
			return results.get(ThreadLocalRandom.current().nextInt(0, results.size()));	
		}
		
		Logger.info("No open houses found for zip code " + zipCode);
		return null;
	}
	
/*	public models.askde.OpenHouse getRandomizedOpenHouseByNeighborhood(String neighborhood) {
		Date currentDateTime = new Date();
		List<models.askde.OpenHouse> results =  models.askde.OpenHouse.find.query().where().eq("neighborhood", neighborhood).and().gt("startDateTime", currentDateTime).order("startDateTime asc").findList();
		if(results.size()>0)
			retu
	}*/
	
	public void loadCanonicalNeighborhoods() {
		Logger.info("Loading neighborhoods from data/CanonicalNeighborhoods.csv");
		BufferedReader br = null;
        String line = "";
        try {
			br = new BufferedReader(new FileReader("data/CanonicalNeighborhoods.csv"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        List<Neighborhood> ns = new ArrayList<Neighborhood>(600);
        Neighborhood n = null;
        try {
			while ((line = br.readLine()) != null) {	
				String[] arrayLine = line.split(",");
				n = new Neighborhood();
				n.setName(expandNeighborhood(arrayLine[1]).toLowerCase().trim());
				ns.add(n);
			}
			Ebean.saveAll(ns);
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        Logger.info("Canonical neighborhoods loaded: " + Neighborhood.find.query().findCount());
	}
	
	public void loadZipCodes() {
		Logger.info("Loading zip codes from data/ZipCodeWhiteList.csv");
		BufferedReader br = null;
        String line = "";
        try {
			br = new BufferedReader(new FileReader("data/ZipCodeWhiteList.csv"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        Integer zip = null;
        ZipCode zipCode =  null;
        List<ZipCode> zs = new ArrayList<ZipCode>(400);
        try {
			while ((line = br.readLine()) != null) {	
				String[] arrayLine = line.split(",");
				zip = Integer.valueOf(arrayLine[0]);
				zipCode = new ZipCode();
				zipCode.setZipCode(zip);
				zs.add(zipCode);
			}
			Ebean.saveAll(zs);
		} catch (IOException e) {
			e.printStackTrace();
		}
        Logger.info("Zip codes loaded: " + zs.size());
   
	}
	
	private boolean checkListingIsClean(Listing l) {
		if(l.getLocation().getZip()!=null && 
				!l.getLocation().getZip().isEmpty() && 
				StringUtils.isNumeric(l.getLocation().getZip()) &&
				l.getLocation().getStreetAddress()!=null &&
				!l.getLocation().getStreetAddress().isEmpty() &&
				l.getListingDetails().getMlsId()!=null &&
				!l.getListingDetails().getMlsId().isEmpty())
			return true;
		return false;
	}
	
	private boolean checkOpenHouseIsClean(OpenHouse o) {
		if(o.getDate()!=null && 
				!o.getDate().isEmpty() && 
				o.getStartTime()!=null && 
				!o.getStartTime().isEmpty() &&
				o.getEndTime()!=null && 
				!o.getEndTime().isEmpty() && checkOpenHouseIsAtOddHours(o))
			return true;
		return false;
	}
	
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	
	private boolean checkOpenHouseIsInPastAlready(Date now, OpenHouse o) {
		try {
			Date startDateTime =  formatter.parse(o.getDate() + " " + o.getStartTime());
			if(startDateTime.before(now))
				return true; // open house is in the past
		} catch (ParseException e) {
			return true;
		}
		return false;
	}
	
	private boolean checkIsRental(Listing l) {
		if(l.getListingDetails().getStatus().equalsIgnoreCase("for rent"))
			return true;
		return false;
	}
	
	private boolean checkOpenHouseIsAtOddHours(OpenHouse o) {
	    try {
		    Date morningBoundary = new SimpleDateFormat("HH:mm:ss").parse(conf.getString("askde.openHouseMorningBoundary"));
		    Calendar calendarMorningBoundary = Calendar.getInstance();
		    calendarMorningBoundary.setTime(morningBoundary);
	
	
		    Date nightBoundary = new SimpleDateFormat("HH:mm:ss").parse(conf.getString("askde.openHouseNightBoundary"));
		    Calendar calendaryNightBoundary = Calendar.getInstance();
		    calendaryNightBoundary.setTime(nightBoundary);
		    calendaryNightBoundary.add(Calendar.DATE, 1);
		    
		    Date current = new SimpleDateFormat("HH:mm").parse(o.getStartTime());
		    Calendar currentCalendar = Calendar.getInstance();
		    currentCalendar.setTime(current);
		    Date x = currentCalendar.getTime();
		    
		    if(x.after(calendarMorningBoundary.getTime()) && x.before(calendaryNightBoundary.getTime()))
		    	return true;
		    else {
		    	Logger.info("Open house is at odd hour, discarded - From " + o.getStartTime() + " till " + o.getEndTime());
		    	return false;
		    }
	    } catch (Exception e) {
	    	Logger.error("checkOpenHouseIsAtOddHours error",e);
	    }
	    
	    return false;
	}
	
	private String expandNeighborhood(String neighborhood) {
		neighborhood = neighborhood.replace("n. ", "north ");
		neighborhood = neighborhood.replace("s. ", "south ");
		neighborhood = neighborhood.replace("e. ", "east ");
		neighborhood = neighborhood.replace("w. ", "west ");
		neighborhood = neighborhood.replace("st. ", "saint ");
		neighborhood = neighborhood.replace("mt. ", "mount ");
		neighborhood = neighborhood.replace("west village - meatpacking district", "west village");
		neighborhood = neighborhood.replace("little italy - chinatown", "little italy");
		neighborhood = neighborhood.replace("east hampton village fringe", "east hampton village");
		neighborhood = neighborhood.replace("bedford - stuyvesant", "bed stuy");
		neighborhood = neighborhood.replace("prospect-lefferts gardens", "prospect lefferts gardens");
		neighborhood = neighborhood.replace("prospect-lefferts garden", "prospect lefferts gardens");
		neighborhood = neighborhood.replace("hastings-on-hudson", "hastings on hudson");
		neighborhood = neighborhood.replace("soho - nolita", "soho");
		neighborhood = neighborhood.replace("garden city s.", "garden city");
		neighborhood = neighborhood.replace("east elmhurst", "elmhurst");
		
		neighborhood = neighborhood.replace("huntington sta", "huntington station");
		neighborhood = neighborhood.replace("pt.jefferson sta", "port jefferson station");
		neighborhood = neighborhood.replace("pt.jefferson vil", "port jefferson village"); 
		neighborhood = neighborhood.replace("quiogue","quogue");
		neighborhood = neighborhood.replace("quogue north","quogue");
		neighborhood = neighborhood.replace("quogue south","quogue");
		neighborhood = neighborhood.replace("rochaway beach", "rockaway beach");
		neighborhood = neighborhood.replace("rochaway park", "rockaway park");
		neighborhood = neighborhood.replace("sagaponack north", "sagaponack");
		neighborhood = neighborhood.replace("sagaponack south", "sagaponack");
		neighborhood = neighborhood.replace("shelter island h", "shelter island heights");
		neighborhood = neighborhood.replace("richmond hill south", "richmond hill");
		neighborhood = neighborhood.replace("richmond hill north", "richmond hill");
		neighborhood = neighborhood.replace("queens village south", "queens village");
		neighborhood = neighborhood.replace("queens village north", "queens village");		
		neighborhood = neighborhood.replace("amagansett north", "amagansett");
		neighborhood = neighborhood.replace("amagansett south", "amagansett");
		neighborhood = neighborhood.replace("amagansett dunes", "amagansett");
		neighborhood = neighborhood.replace("bellerose terr ", "bellerose terrace");
		neighborhood = neighborhood.replace("cold spring hrbr", "cold spring harbor");
		neighborhood = neighborhood.replace("e atlantic beach", "east atlantic beach");
		neighborhood = neighborhood.replace("east hampton northwest", "east hampton");
		neighborhood = neighborhood.replace("east hampton south", "east hampton");
		neighborhood = neighborhood.replace("east hampton nw", "east hampton");
		neighborhood = neighborhood.replace("eh north", "east hampton");
		neighborhood = neighborhood.replace("flatiron district", "flatiron");
		neighborhood = neighborhood.replace("fresh meadow", "fresh meadows");
		neighborhood = neighborhood.replace("great neck est", "great neck");
		neighborhood = neighborhood.replace("great neck east", "great neck");
		neighborhood = neighborhood.replace("bed stuy", "bedford stuyvesant");
		neighborhood = neighborhood.replace("gramercy - union square", "gramercy");
		
		
		neighborhood = neighborhood.replace("jamaica north", "jamaica");
		neighborhood = neighborhood.replace("jamaica south", "jamaica");
		neighborhood = neighborhood.replace("westhampton beach north", "west hampton beach");
		neighborhood = neighborhood.replace("westhampton beach south", "west hampton beach");
		
		neighborhood = neighborhood.replace("westhampton dunes", "west hampton");
		neighborhood = neighborhood.replace("westhampton north", "west hampton");
		neighborhood = neighborhood.replace("westhampton south", "west hampton");
		
		
		
		
		return neighborhood;
	}
	
	private String transformPropertyType(String propertyType) {
		switch(propertyType.toLowerCase()) {
			case "unit":
				return "";
			case "coop":
				return "co-op";
		}
		
		return propertyType;
	}
	
	public void loadOpenHouses() throws ListingsLoadException {
		ZillowFeedHistory zfh = new ZillowFeedHistory();
		zfh.setFeedLoadStart(new Date());
		Ebean.save(zfh);
		List<ZipCode> zips = ZipCode.find.all();
		Set<Integer> zipCodeWhiteList = new HashSet<Integer>();
		for(ZipCode z : zips)
			zipCodeWhiteList.add(z.getZipCode());
		
		JAXBContext ctx;
		Unmarshaller u;
		boolean isLoadFromURLSuccessful = false;
		Logger.info("Retrieving Zillow feed URL from config file");
		String url = conf.getString("askde.listingsFeedURL");
		if(url==null || url.isEmpty()) {
			zfh.setFailed(true);
			zfh.setFailureCause("Feed URL not in config file or is blank");
			Ebean.update(zfh);
			throw new ListingsLoadException("Feed URL not in config file or is blank");
		}
		Logger.info("Retrieving listings feed from Zillow at URL " + url);
		CompletionStage<WSResponse> resp =  ws.url(url)
				.setRequestTimeout(Duration.ofMillis(60000))
				.get();
		CompletionStage<InputStream> feed = resp.thenApply(WSResponse::getBodyAsStream);
		try {
			ctx = JAXBContext.newInstance(Listings.class);
		} catch (JAXBException e) {
			zfh.setFailed(true);
			zfh.setFailureCause(e.getMessage());
			Ebean.update(zfh);
			e.printStackTrace();
			Logger.error("JAXBContext creation failed",e);
			
			throw new ListingsLoadException("JAXBContext creation failed. Message: " + e.getMessage());
		}
		try {
			u = ctx.createUnmarshaller();
		} catch (JAXBException e) {
			e.printStackTrace();
			zfh.setFailed(true);
			zfh.setFailureCause(e.getMessage());
			Ebean.update(zfh);
			Logger.error("JAXBContext unmarshaller creation failed",e);
			throw new ListingsLoadException("JAXBContext unmarshaller creation failed. Message: " + e.getMessage());			
		}
		Listings allListings = null;
		Set<String> neighborhoods = new HashSet<String>(20);
		
		
		try {
			allListings = (Listings) u.unmarshal(feed.toCompletableFuture().get());
			isLoadFromURLSuccessful = true;
		} catch (JAXBException | InterruptedException | ExecutionException e) {
			e.printStackTrace();
			zfh.setFailed(true);
			zfh.setFailureCause(e.getMessage());
			Ebean.update(zfh);
			Logger.error("DE Zillow feed load from Zillow failed",e);
			Logger.info("Attempting to load from local file");
				File f = new File("conf/DEZillowFeed.xml");
				try {
					allListings = (Listings) u.unmarshal(f);
					Logger.info("Feed loaded from local file conf/DEZillowFeed.xml");
				} catch (JAXBException e1) {
					e.printStackTrace();
					zfh.setFailed(true);
					zfh.setFailureCause(e.getMessage());
					Ebean.update(zfh);
					Logger.error("DE Zillow feed load from local file failed",e1);
					Logger.info("Attempting to load from S3");
					try {
						allListings = (Listings) u.unmarshal(AWS.S3.get("askde", "DEZillowFeed.xml"));
					} catch (JAXBException e2) {
						e2.printStackTrace();
						zfh.setFailed(true);
						zfh.setFailureCause(e.getMessage());
						Ebean.update(zfh);
						Logger.error("Failed to load from S3",e1);
						Logger.info("Ask DE will operate in maintenance mode. Restart when connectivity / feeds are available");
						throw new ListingsLoadException("All alternatives to loading feed have failed. Message: " + e.getMessage());
					}
					
				}			
		}
		
		Logger.info("Feed loaded into memory");	
		Marshaller m = null;
		File f = null;
		
		if(isLoadFromURLSuccessful) {
			Logger.info("Since feed is loaded from URL, we will persist it to local and S3");
			Logger.info("Storing file to local for backup under data/ as filename DEZillowFeed.xml");
			try {
				m = ctx.createMarshaller();
				f = new File("data/DEZillowFeed.xml");
				m.marshal(allListings, f);
			} catch (JAXBException e) {
				zfh.setFailed(true);
				zfh.setFailureCause("Failed to marshal feed to local file");
				Ebean.update(zfh);
				Logger.error("Failed to marshal feed to local file",e);
				e.printStackTrace();
			}	
			
			Logger.info("Storing file to S3 for backup under data/ as filename DEZillowFeed.xml");
			//Logger.info("Storing back to up S3");
			//AWS.S3.upload("askde", "DEZillowFeed.xml", f, true);
		}
		
		Logger.info("Processing and flattening listings...");
		int listingsCount = 1;
		int rentalsCount = 0;
		models.askde.OpenHouse oh = null;
		List<models.askde.OpenHouse> listOfOpenHouses = new ArrayList<models.askde.OpenHouse>();
				
		Listings discardedListings = new Listings();
		Listings listingsWithOpenHouses = new Listings();
		String neighborhood = null;
		boolean isRental = false;
		for(Listing l : allListings.getListing()) {	
			isRental = false;
			if(listingsCount++%1000==0) Logger.info("... " + listingsCount + " listings processed");
			if(checkListingIsClean(l)) { // Check if it is clean before doing any work
				Integer zip = Integer.valueOf(l.getLocation().getZip().trim());
				OpenHouses openHouses = l.getOpenHouses();
				if(zipCodeWhiteList.contains(zip)) {
					neighborhood = expandNeighborhood(l.getNeighborhood().getName().toLowerCase());
					neighborhoods.add(neighborhood);
					if(openHouses.getOpenHouse().size()>0) { // Match up with zip whitelist and make sure there are open houses in this listing
						if(checkIsRental(l)) { // toggle rental flag
							isRental = true;
							rentalsCount++;
						}
					Date now = new Date();
					listingsWithOpenHouses.getListing().add(l); // need this to output a file 
					String phoneNumber;
					for(OpenHouse o : openHouses.getOpenHouse()) {
						 // need to check if oh is in the past
							if(!checkOpenHouseIsInPastAlready(now,o) && checkOpenHouseIsClean(o)) {	// check open house has relevant pieces of data before processing							
								oh = new models.askde.OpenHouse();
								oh.setStartDateTime(o.getDate(),o.getStartTime());
								oh.setAddress(l.getLocation().getStreetAddress());
								oh.setUnitNumber(l.getLocation().getUnitNumber());
								oh.setDate(o.getDate());
								oh.setStatus(l.getListingDetails().getStatus());
								oh.setStartTime(o.getStartTime());
								oh.setEndDateTime(o.getDate(),o.getEndTime());
								oh.setEndTime(o.getEndTime());
								oh.setUnitNumber(l.getLocation().getUnitNumber());
								oh.setListingID(l.getListingDetails().getMlsId());
								oh.setZipCode(Integer.valueOf(l.getLocation().getZip()));
								oh.setNeighborhood(neighborhood);
								oh.setCity(l.getLocation().getCity());
								oh.setState(l.getLocation().getState());
								oh.setDescription(l.getBasicDetails().getDescription());
								oh.setPrice(l.getListingDetails().getPrice());
								oh.setStatus(l.getListingDetails().getStatus());
								oh.setRental(isRental);
								oh.setBeds(l.getBasicDetails().getBedrooms());
								oh.setBaths(l.getBasicDetails().getBathrooms());
								oh.setPropertyType(transformPropertyType(l.getBasicDetails().getPropertyType()));
								oh.setAgentName(l.getAgent().getFirstName() + " " +l.getAgent().getLastName());
								phoneNumber = l.getAgent().getMobileLineNumber();
								if(phoneNumber==null || phoneNumber.isEmpty()) {
									phoneNumber = l.getAgent().getOfficeLineNumber();
									if(phoneNumber==null || phoneNumber.isEmpty())
										phoneNumber = "(212) 891-7000";
								}
								oh.setAgentPhoneNumber(phoneNumber);
								oh.setAgentEmail(l.getAgent().getEmailAddress());
								oh.setCurrent(true);
								listOfOpenHouses.add(oh);
							} else {
								Logger.info("Open house in past or missing or malformed date/time data, listing ID: " + l.getListingDetails().getMlsId());
							}
						}				
					} else discardedListings.getListing().add(l);
				} else
					discardedListings.getListing().add(l);
			} else
				discardedListings.getListing().add(l);
		}
		Logger.info("....done.");
		Logger.info("Preparing open houses for database");
		
		// Separate process to make the db changes within the shortest window possible
		Logger.info("Preparation complete");
		if(models.askde.OpenHouse.find.query().findCount()>0) {
			Logger.info("Purging old data and inserting new");
			Date now = new Date();
			List<models.askde.OpenHouse> delete = models.askde.OpenHouse.find.query().where().lt("createdAt", now).findList();
			Logger.info("Will purge " + delete.size() + " rows and insert " + listOfOpenHouses.size() + " rows");
			try {
				Logger.info("Starting transaction..");
				Ebean.beginTransaction();
				Ebean.saveAll(listOfOpenHouses);
				Ebean.deleteAll(delete);
				Ebean.commitTransaction();
				Logger.info("Transaction committed");
			} finally {
				Ebean.endTransaction();
			}
		} else {
			Logger.info("Database empty, creating intial load");
			Logger.info("Persisting..");
			Ebean.saveAll(listOfOpenHouses);
			Logger.info("..open houses saved to DB");
		}
		
		Logger.info("Number of US listings: " + allListings.getListing().size());
		Logger.info("Number of NYC, Nassau, Suffolk & Westchester listings with open houses: " + listOfOpenHouses.size());
		Logger.info("..Of which, # of rentals are: " + rentalsCount);
		Logger.info("Number of listings discarded: " + discardedListings.getListing().size());
		Logger.info("Rows stored in Open House DB: " + models.askde.OpenHouse.find.query().findCount());
		Logger.info("Storing discarded listings to data/DiscardedListings.xml");
		try {
			m = ctx.createMarshaller();
			f = new File("data/DiscardedListings.xml");
			m.marshal(discardedListings, f);
			Logger.info("Discarded listings stored successfully to data/DiscardedListings.xml");
		} catch (JAXBException e) {
			Logger.error("Failed to marshal feed to local file for discarded listings",e);
		}

		try {
			m = ctx.createMarshaller();
			f = new File("data/ListingsWithOpenHouses.xml");
			m.marshal(listingsWithOpenHouses, f);
			Logger.info("ListingsWithOpenHouses matching zip code whitelist stored successfully to data/ListingsWithOpenHouses.xml");
		} catch (JAXBException e) {
			Logger.error("Failed to marshal feed to local file for ListingsWithOpenHouses ",e);
		}
		
		Logger.info("Persisting list of neighborhoods, needed for training Alexa");
		f = new File ("data/Neighborhoods.csv");
		PrintWriter pw;
		try {
			pw = new PrintWriter(f);
			for(String n : neighborhoods)
				pw.println(n);	
			pw.flush();
			pw.close();
		} catch (FileNotFoundException e) {
			Logger.error("Failed to write neighborhoods",e);
		}

		zfh.setFailed(false);
		zfh.setFeedLoadComplete(new Date());
		zfh.setTotalListings(allListings.getListing().size());
		zfh.setRelevantListings(listOfOpenHouses.size());
		zfh.setDiscardedListings(discardedListings.getListing().size());
		zfh.setRelevantRentals(rentalsCount);
		zfh.setRelevantSales(listOfOpenHouses.size() - rentalsCount);
		zfh.setNeighborhoods(neighborhoods.size());
		Ebean.update(zfh);
		
		
		Logger.info("Load completed");
		// Signal to gc
		neighborhoods = null;
		listingsWithOpenHouses = null;
		listOfOpenHouses = null;
		allListings = null;
		discardedListings = null;
		
	}
	
}
