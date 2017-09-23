package models.askde;

import java.util.List;
import javax.persistence.Entity;
import io.ebean.Finder;


@Entity
public class Adjective extends BaseWord {
	
	public static Finder<Long, Adjective> find = new Finder<>(Adjective.class);
	public static List<Adjective> getAllCurrent() {
		return find.query().where().eq("current", true).findList();
	}
	
	public static Adjective findByUUID(String uuid) {
		return find.query().where().eq("uuid", uuid).findUnique();
	}
}
