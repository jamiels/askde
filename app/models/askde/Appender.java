package models.askde;

import java.util.List;

import javax.persistence.Entity;

import com.avaje.ebean.Model.Finder;

@Entity
public class Appender extends BaseWord {
	public static Finder<Long, Appender> find = new Finder<>(Appender.class);
	
	public static List<Appender> getAllCurrent() {
		return find.where().eq("current", true).findList();
	}

}
