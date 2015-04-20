package ca.carleton.michelleburrows.groupbuilder;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Question {
	private String text;
	private String type;
	private long minBestAgreementCount;
	private Answer[] answers;
	
	public void parseJSON(JSONObject jQuestion) {
		setText((String)jQuestion.get("text"));
		setType((String)jQuestion.get("type"));
		setMinBestAgreementCount((long)jQuestion.get("minBestAgreementCount"));
		JSONArray jAnswers = (JSONArray)jQuestion.get("answers");
		answers = new Answer[jAnswers.size()];
		for (int i = 0; i < jAnswers.size(); ++i) {
			JSONObject jAnswer = (JSONObject) jAnswers.get(i);
			Answer answer = new Answer();
			answer.parseJSON(jAnswer);
			answers[i] = answer;
		}
	}
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public long getMinBestAgreementCount() {
		return minBestAgreementCount;
	}
	public void setMinBestAgreementCount(long minBestAgreementCount) {
		this.minBestAgreementCount = minBestAgreementCount;
	}
	public Answer[] getAnswers() {
		return answers;
	}
	public void setAnswers(Answer[] answers) {
		this.answers = answers;
	}
}
