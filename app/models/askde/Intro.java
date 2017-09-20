package models.askde;

import javax.persistence.Entity;

import com.avaje.ebean.Model.Finder;

@Entity
public class Intro extends BaseWord {
	public static Finder<Long, Intro> find = new Finder<>(Intro.class);
	

}
