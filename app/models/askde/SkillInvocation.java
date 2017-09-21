package models.askde;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.avaje.ebean.Model.Finder;

import models.raven.BaseModel;

@Entity
public class SkillInvocation extends BaseModel {
	public static Finder<Long, SkillInvocation> find = new Finder<>(SkillInvocation.class);
	
	private String skill;
	@Column(columnDefinition = "TEXT")
	private String request;
	@Column(columnDefinition = "TEXT")
	private String response;
	private String deviceID;
	
	public static List<SkillInvocation> getLastestHundred() {
		return find.orderBy("createdAt desc").setMaxRows(100).findList();
	}
	
	public String getSkill() {
		return skill;
	}
	public void setSkill(String skill) {
		this.skill = skill;
	}
	public static Finder<Long, SkillInvocation> getFind() {
		return find;
	}
	public static void setFind(Finder<Long, SkillInvocation> find) {
		SkillInvocation.find = find;
	}
	public String getRequest() {
		return request;
	}
	public void setRequest(String request) {
		this.request = request;
	}
	public String getResponse() {
		return response;
	}
	public void setResponse(String response) {
		this.response = response;
	}
	public String getDeviceID() {
		return deviceID;
	}
	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}

}
