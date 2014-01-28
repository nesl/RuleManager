package edu.ucla.nesl.rulemanager.db.model;

import org.json.JSONException;
import org.json.JSONObject;

import edu.ucla.nesl.rulemanager.Const;
import edu.ucla.nesl.rulemanager.Tools;


public class LocationLabel extends Label {
	
	private static final int EARTH_METERS = 6367000;

	public static final int INVALID = -1;
	public static final int NON_OVERLAP = 0;
	public static final int EXACTLY_SAME = 1;
	public static final int PARTIAL_OVERLAP = 2;
	public static final int SUBSET = 3;
	public static final int SUPERSET = 4;

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
	
	public int checkOverlap(LocationLabel anotherLocation) {
		double r1 = this.getRadius();
		double r2 = anotherLocation.getRadius();
		double dist = getDistance(anotherLocation);
		
		if (this.getLatitude() == anotherLocation.getLatitude() 
				&& this.getLongitude() == anotherLocation.getLongitude() 
				&& this.getRadius() == anotherLocation.getRadius()) {
			return EXACTLY_SAME;
		} else if (dist > (r1 + r2)) {
			return NON_OVERLAP;
		} else if (dist <= Math.abs(r1 - r2)) {
			if (r1 > r2) {
				return SUPERSET;
			} else if (r1 < r2){
				return SUBSET;
			} else {
				assert false;
				return INVALID;
			}
		} else {
			return PARTIAL_OVERLAP;
		}
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
	
	public JSONObject toJson() {
		String name = Tools.makeValidMacroName(labelName);
		String value = Const.GPS_DISTANCE_FUNCTION_NAME + "(" 
				+ Const.LOCATION_STREAM_NAME + "." + Const.LATITUDE_CHANNEL_NAME + ", "
				+ Const.LOCATION_STREAM_NAME + "." + Const.LONGITUDE_CHANNEL_NAME + ", "
				+ getLatitude() + ", " + getLongitude() + ") <= " + getRadius();

		JSONObject json = new JSONObject();
		try {
			json.put("name", Const.LOCATION_LABEL_PREFIX + name);
			json.put("value", value);
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
