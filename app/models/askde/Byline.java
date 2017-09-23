package models.askde;

import java.util.List;

import javax.persistence.Entity;

import io.ebean.Finder;

@Entity
public class Byline extends BaseWord {
	public static Finder<Long, Byline> find = new Finder<>(Byline.class);
	
	public static List<Byline> getAllCurrent() {
		return find.query().where().eq("current", true).findList();
	}
	
	public static Byline findByUUID(String uuid) {
		return find.query().where().eq("uuid", uuid).findUnique();
	}
}
