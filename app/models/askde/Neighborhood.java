package models.askde;

import java.util.List;

import javax.persistence.Entity;

import com.avaje.ebean.Model.Finder;

import models.raven.BaseModel;

@Entity
public class Neighborhood extends BaseModel {

	public static Finder<Long, Neighborhood> find = new Finder<>(Neighborhood.class);
	
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public static List<Neighborhood> getAllCurrent() {
		return find.all();
	}
}
