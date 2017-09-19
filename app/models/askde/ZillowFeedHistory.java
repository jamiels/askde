package models.askde;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.avaje.ebean.Model.Finder;

import models.raven.BaseModel;
import play.data.format.Formats;

@Entity
public class ZillowFeedHistory extends BaseModel {

	public static Finder<Long, ZillowFeedHistory> find = new Finder<>(ZillowFeedHistory.class);
	
	@Column(columnDefinition = "DATETIME")
	@Formats.DateTime(pattern="yyyy-MM-dd hh:mm")
	private Date feedLoadStart;
	
	@Column(columnDefinition = "DATETIME")
	@Formats.DateTime(pattern="yyyy-MM-dd hh:mm")
	private Date feedLoadComplete;	
	
	private int totalListings;
	private int discardedListings;
	private int relevantListings;
	private int relevantRentals;
	private int relevantSales;
	private int neighborhoods;
	
	
	private boolean failed = true;
	
	@Column(columnDefinition = "TEXT") 
	private String failureCause;
	
	public Date getFeedLoadStart() {
		return feedLoadStart;
	}
	public void setFeedLoadStart(Date feedLoadStart) {
		this.feedLoadStart = feedLoadStart;
	}
	public Date getFeedLoadComplete() {
		return feedLoadComplete;
	}
	public void setFeedLoadComplete(Date feedLoadComplete) {
		this.feedLoadComplete = feedLoadComplete;
	}
	public int getTotalListings() {
		return totalListings;
	}
	public void setTotalListings(int totalListings) {
		this.totalListings = totalListings;
	}
	public int getDiscardedListings() {
		return discardedListings;
	}
	public void setDiscardedListings(int discardedListings) {
		this.discardedListings = discardedListings;
	}
	public int getRelevantListings() {
		return relevantListings;
	}
	public void setRelevantListings(int relevantListings) {
		this.relevantListings = relevantListings;
	}
	public boolean isFailed() {
		return failed;
	}
	public void setFailed(boolean failed) {
		this.failed = failed;
	}
	public String getFailureCause() {
		return failureCause;
	}
	public void setFailureCause(String failureCause) {
		this.failureCause = failureCause;
	}
	public int getRelevantRentals() {
		return relevantRentals;
	}
	public void setRelevantRentals(int relevantRentals) {
		this.relevantRentals = relevantRentals;
	}
	public int getRelevantSales() {
		return relevantSales;
	}
	public void setRelevantSales(int relevantSales) {
		this.relevantSales = relevantSales;
	}
	public int getNeighborhoods() {
		return neighborhoods;
	}
	public void setNeighborhoods(int neighborhoods) {
		this.neighborhoods = neighborhoods;
	}
	
	
	

}
