package edu.ucla.nesl.rulemanager.db.model;

public class Label {
	protected String labelName;
	protected int uploadCount;
	protected int serverId;
	
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

	public String getLabelName() {
		return labelName;
	}		
	
	public void setLabelName(String labelName) {
		this.labelName = labelName;
	}
}
