package edu.ucla.nesl.rulemanager.db.model;

public class LocationLabel extends Label {
	
	private static final int EARTH_METERS = 6367000;
	
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
	
	public boolean isOverlap(LocationLabel anotherLocation) {
		double r1 = this.getRadius();
		double r2 = anotherLocation.getRadius();
		double dist = getDistance(anotherLocation);
		
		if (dist < r1 + r2) {
			return true;
		}
		return false;
	}
	
	public double getDistance(LocationLabel anotherLocation) {
		return getDistance(this.getLongitude(), anotherLocation.getLongitude(), this.getLatitude(), anotherLocation.getLatitude()); 
	}
	
	// Great circle distance in meters
	public static double getDistance(double x1, double x2, double y1, double y2) {
		x1 = x1 * (Math.PI / 180);
		x2 = x2 * (Math.PI / 180);
		y1 = y1 * (Math.PI / 180);
		y2 = y2 * (Math.PI / 180);
		double dlong = x1 - x2;
		double dlat = y1 - y2;
		double a = Math.pow(Math.sin(dlat / 2), 2) + Math.cos(y1) *
				Math.cos(y2) * Math.pow(Math.sin(dlong / 2), 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return EARTH_METERS * c;
	}
}
