package models.askde;

import java.util.List;

import javax.persistence.Entity;

import io.ebean.Finder;

import models.raven.BaseModel;

@Entity
public class ZipCode extends BaseModel {
	
	public static Finder<Long, ZipCode> find = new Finder<>(ZipCode.class);
	
	private Integer zipCode;

	public Integer getZipCode() {
		return zipCode;
	}

	public void setZipCode(Integer zipCode) {
		this.zipCode = zipCode;
	}
	
	public static List<ZipCode> getAllCurrent() {
		return find.all();
	}
	
	

}
