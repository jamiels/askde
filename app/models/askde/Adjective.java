package models.askde;

import java.util.List;
import javax.persistence.Entity;
import com.avaje.ebean.Model.Finder;


@Entity
public class Adjective extends BaseWord {
	
	public static Finder<Long, Adjective> find = new Finder<>(Adjective.class);
	public static List<Adjective> getAllCurrent() {
		return find.where().eq("current", true).findList();
	}
}
