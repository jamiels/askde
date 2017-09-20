package models.askde;

import javax.persistence.Entity;

import com.avaje.ebean.Model.Finder;

@Entity
public class Byline extends BaseWord {
	public static Finder<Long, Byline> find = new Finder<>(Byline.class);
	

}
