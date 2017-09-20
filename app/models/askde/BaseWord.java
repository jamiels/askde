package models.askde;

import javax.persistence.MappedSuperclass;

import models.raven.BaseModel;

@MappedSuperclass
public abstract class BaseWord extends BaseModel {
	
	private boolean active = true;
	private String message;
	
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	


}
