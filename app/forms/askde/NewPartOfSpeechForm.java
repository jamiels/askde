package forms.askde;

import play.data.validation.Constraints.Required;

public class NewPartOfSpeechForm {
	
	@Required
	private String textContent;
	
	private boolean optionAppender;
	private boolean optionByline;
	private boolean optionAdjective;
	public String getTextContent() {
		return textContent;
	}
	public void setTextContent(String textContent) {
		this.textContent = textContent;
	}
	public boolean isOptionAppender() {
		return optionAppender;
	}
	public void setOptionAppender(boolean optionAppender) {
		this.optionAppender = optionAppender;
	}
	public boolean isOptionByline() {
		return optionByline;
	}
	public void setOptionByline(boolean optionByline) {
		this.optionByline = optionByline;
	}
	public boolean isOptionAdjective() {
		return optionAdjective;
	}
	public void setOptionAdjective(boolean optionAdjective) {
		this.optionAdjective = optionAdjective;
	}
	
	
	

}
