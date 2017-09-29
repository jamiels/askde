package forms.askde;

import play.data.validation.Constraints.Required;

public class NewPartOfSpeechForm {
	
	@Required
	private String textContent;
	private String posType;

	public String getTextContent() {
		return textContent;
	}
	public void setTextContent(String textContent) {
		this.textContent = textContent;
	}
	public String getPosType() {
		return posType;
	}
	public void setPosType(String posType) {
		this.posType = posType;
	}
	

}
