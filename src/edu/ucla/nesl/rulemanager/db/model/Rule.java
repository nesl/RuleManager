package edu.ucla.nesl.rulemanager.db.model;

public class Rule {
	protected int id;
	protected String action;
	protected String data;
	protected String consumer;
	protected String timeLabel;
	protected String locationLabel;
	
	public Rule() {}
	
	public Rule(int id, String action, String data, String consumer, String timeLabel, String locationLabel) {
		this.id = id;
		this.action = action;
		this.data = data;
		this.consumer = consumer;
		this.timeLabel = timeLabel;
		this.locationLabel = locationLabel;
	}

	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getAction() {
		return action;
	}
	
	public void setAction(String action) {
		this.action = action;
	}
	
	public String getData() {
		return data;
	}
	
	public void setData(String data) {
		this.data = data;
	}
	
	public String getConsumer() {
		return consumer;
	}
	
	public void setConsumer(String consumer) {
		this.consumer = consumer;
	}
	
	public String getTimeLabel() {
		return timeLabel;
	}
	
	public void setTimeLabel(String timeLabel) {
		this.timeLabel = timeLabel;
	}
	
	public String getLocationLabel() {
		return locationLabel;
	}
	
	public void setLocationLabel(String locationLabel) {
		this.locationLabel = locationLabel;
	}

	public String getSummaryText() {
		String text = action + " " + data + " data with " + consumer;
		String timeCond = null;
		if (timeLabel != null) {
			timeCond = "time is " + timeLabel;
		}
		String locationCond = null;
		if (locationLabel != null) {
			locationCond = "location is " + locationLabel;
		}
		if (timeCond != null && locationCond != null) {
			text += " when " + timeCond + " and " + locationCond + ".";
		} else if (timeCond != null && locationCond == null) {
			text += " when " + timeCond + ".";
		} else if (timeCond == null && locationCond != null) {
			text += " when " + locationCond + ".";
		} else if (timeCond == null && locationCond == null) {
			text += ".";
		}
		return text;
	}
}
