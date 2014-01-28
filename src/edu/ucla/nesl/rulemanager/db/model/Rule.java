package edu.ucla.nesl.rulemanager.db.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.ucla.nesl.rulemanager.Const;
import edu.ucla.nesl.rulemanager.Tools;

public class Rule {
	protected int id;
	protected String action;
	protected String data;
	protected String consumer;
	protected String timeLabel;
	protected String locationLabel;
	protected int uploadCount;
	protected int serverId;

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

	public int getUploadCount() {
		return uploadCount;
	}

	public void setUploadCount(int uploadCount) {
		this.uploadCount = uploadCount;
	}

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
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

	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		try {
			json.put("id", id);
			if (action.equalsIgnoreCase(Const.SHARE)) {
				json.put("action", Const.ACTION_ALLOW);
			} else if (action.equalsIgnoreCase(Const.NOT_SHARE)) {
				json.put("action", Const.ACTION_DENY);
			} else {
				assert false;
				return null;
			}
			if (data != null && !data.equalsIgnoreCase(Const.ALL)) {
				JSONArray arr = new JSONArray();
				arr.put(data);
				json.put("target_streams", arr);
			}
			if (consumer != null && !consumer.equalsIgnoreCase(Const.EVERYONE)) {
				JSONArray arr = new JSONArray();
				arr.put(consumer);
				json.put("target_users", arr);
			}
			if (timeLabel != null && locationLabel != null) {
				json.put("condition", Tools.makeMacroRefer(Const.TIME_LABEL_PREFIX + timeLabel) + " AND " + Tools.makeMacroRefer(Const.LOCATION_LABEL_PREFIX + locationLabel));
			} else if (timeLabel == null && locationLabel != null) {
				json.put("condition", Tools.makeMacroRefer(Const.LOCATION_LABEL_PREFIX + locationLabel));
			} else if (timeLabel != null && locationLabel == null) {
				json.put("condition", Tools.makeMacroRefer(Const.TIME_LABEL_PREFIX + timeLabel));
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}	
		return json;
	}
	
	public String toJsonString() {
		return toJson().toString();
	}
}
