package models.askde;

import javax.persistence.Entity;

import com.avaje.ebean.Model.Finder;

@Entity
public class Appenders extends BaseWord {
	public static Finder<Long, Appenders> find = new Finder<>(Appenders.class);

}
