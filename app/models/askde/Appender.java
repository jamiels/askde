package models.askde;

import java.util.List;

import javax.persistence.Entity;

import io.ebean.Finder;

@Entity
public class Appender extends BaseWord {
	public static Finder<Long, Appender> find = new Finder<>(Appender.class);
	
	public static List<Appender> getAllCurrent() {
		return find.query().where().eq("current", true).findList();
	}
	
	public static Appender findByUUID(String uuid) {
		return find.query().where().eq("uuid", uuid).findUnique();
	}

}
