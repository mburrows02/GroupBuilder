package ca.carleton.michelleburrows.groupbuilder;

import org.json.simple.JSONObject;

public class Answer {
	private String text;
	private long minSelect;
	private long maxSelect;
	
	public void parseJSON(JSONObject jAnswer) {
		text = (String)jAnswer.get("text");
		minSelect = (long)jAnswer.get("minSelect");
		maxSelect = (long)jAnswer.get("maxSelect");
	}
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public long getMinSelect() {
		return minSelect;
	}
	public void setMinSelect(long minSelect) {
		this.minSelect = minSelect;
	}
	public long getMaxSelect() {
		return maxSelect;
	}
	public void setMaxSelect(long maxSelect) {
		this.maxSelect = maxSelect;
	}
}
