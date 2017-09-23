package models.askde;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;

import io.ebean.Finder;

import models.raven.BaseModel;
import play.Logger;
import play.data.format.Formats;

@Entity
public class OpenHouse extends BaseModel {
	
	public static Finder<Long, OpenHouse> find = new Finder<>(OpenHouse.class);
	private String address;
	private String unitNumber;
	private String listingID;
	private Integer zipCode;
	private String neighborhood;
	private String city;
	private String state;
	private String status;
	private int price;
	private boolean rental; // sale if true;
	
	
	@Column(columnDefinition = "TEXT") 
	private String description;
	
	@Column(columnDefinition = "DATETIME")
	@Formats.DateTime(pattern="yyyy-MM-dd hh:mm")
    public Date startDateTime = new Date();

	@Column(columnDefinition = "DATETIME")
	@Formats.DateTime(pattern="yyyy-MM-dd hh:mm")
    public Date endDateTime = new Date();
	
	private int hits;
	
	private String date;
	private String startTime;
	private String endTime;
	private BigDecimal beds;
	private BigDecimal baths;
	private String propertyType;
	
	private Date convertToDate(String date, String time) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		try {
			return formatter.parse(date + " " + time);
		} catch (ParseException e) {
			formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm");
			try {
				return formatter.parse(date + " " + time);
			} catch (ParseException e1) {
				Logger.error("Cannot parse open house date/time date: " + date + " time: " + time);
			}
		}
		
		return null;
	}
	
	public void setEndDateTime(String date, String time) {
		this.endDateTime = convertToDate(date,time);
	}
	
	public void setStartDateTime(String date, String time) {
		this.startDateTime = convertToDate(date,time);
	}
	

	public String getListingID() {
		return listingID;
	}
	public void setListingID(String listingID) {
		this.listingID = listingID;
	}
	public Integer getZipCode() {
		return zipCode;
	}
	public void setZipCode(Integer zipCode) {
		this.zipCode = zipCode;
	}
	public String getNeighborhood() {
		return neighborhood;
	}
	public void setNeighborhood(String neighborhood) {
		this.neighborhood = neighborhood;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Date getStartDateTime() {
		return startDateTime;
	}
	public void setStartDateTime(Date startDateTime) {
		this.startDateTime = startDateTime;
	}
	public Date getEndDateTime() {
		return endDateTime;
	}
	public void setEndDateTime(Date endDateTime) {
		this.endDateTime = endDateTime;
	}
	public int getHits() {
		return hits;
	}
	public void setHits(int hits) {
		this.hits = hits;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getUnitNumber() {
		return unitNumber;
	}
	public void setUnitNumber(String unitNumber) {
		this.unitNumber = unitNumber;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}


	public boolean isRental() {
		return rental;
	}

	public void setRental(boolean rental) {
		this.rental = rental;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public static Finder<Long, OpenHouse> getFind() {
		return find;
	}

	public static void setFind(Finder<Long, OpenHouse> find) {
		OpenHouse.find = find;
	}

	public BigDecimal getBeds() {
		return beds;
	}

	public void setBeds(BigDecimal beds) {
		this.beds = beds;
	}

	public BigDecimal getBaths() {
		return baths;
	}

	public void setBaths(BigDecimal baths) {
		this.baths = baths;
	}

	public String getPropertyType() {
		return propertyType;
	}

	public void setPropertyType(String propertyType) {
		this.propertyType = propertyType;
	}

	
	
	

}
