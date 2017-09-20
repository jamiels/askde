package models.askde;

import javax.persistence.Entity;

import com.avaje.ebean.Model.Finder;

import models.raven.BaseModel;

@Entity
public class Adjectives extends BaseWord {
	
	public static Finder<Long, Adjectives> find = new Finder<>(Adjectives.class);

}
