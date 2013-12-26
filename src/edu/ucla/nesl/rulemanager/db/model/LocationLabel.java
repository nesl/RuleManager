package edu.ucla.nesl.rulemanager.db.model;

public class LocationLabel extends Label {
	protected double latitude;
	protected double longitude;
	protected double radius;
	
	public double getLatitude() {
		return latitude;
	}
	
	public double getLongitude() {
		return longitude;
	}
	
	public double getRadius() {
		return radius;
	}
	
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	public void setRadius(double radius) {
		this.radius = radius;
	}
	
	@Override
	public String toString() {
		return labelName;
	}

	public String getSummaryText() {
		return "lat: " + latitude + ", lon: " + longitude + ", radius: " + String.format("%.1f m", radius);
	}
}
