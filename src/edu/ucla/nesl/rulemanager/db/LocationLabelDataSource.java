package edu.ucla.nesl.rulemanager.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import edu.ucla.nesl.rulemanager.Const;
import edu.ucla.nesl.rulemanager.db.model.LocationLabel;

public class LocationLabelDataSource extends DataSource {

	public LocationLabelDataSource(Context context) {
		super(context);
	}

	public void insert(String labelName, double latitude, double longitude, double radius) throws SQLException {
		ContentValues values = new ContentValues();
		values.put(SQLiteHelper.COL_LABEL_NAME, labelName);
		values.put(SQLiteHelper.COL_LATITUDE, latitude);
		values.put(SQLiteHelper.COL_LONGITUDE, longitude);
		values.put(SQLiteHelper.COL_RADIUS, radius);
		database.insertOrThrow(SQLiteHelper.TABLE_LOCATION_LABELS, null, values);
	}

	public List<LocationLabel> getLocationLabels() {
		List<LocationLabel> labels = new ArrayList<LocationLabel>();
		Cursor c = database.query(SQLiteHelper.TABLE_LOCATION_LABELS, null, null, null, null, null, null);
		c.moveToFirst();
		while (!c.isAfterLast()) {
			LocationLabel label = new LocationLabel();
			label.setLabelName(c.getString(0));
			label.setLatitude(c.getDouble(1));
			label.setLongitude(c.getDouble(2));
			label.setRadius(c.getDouble(3));
			labels.add(label);
			c.moveToNext();
		}
		return labels;
	}

	public int deleteLocationLabel(String labelName) {
		return database.delete(SQLiteHelper.TABLE_LOCATION_LABELS, SQLiteHelper.COL_LABEL_NAME + " = ?", new String[] { labelName });
	}

	public int updateLocationLabel(String prevLabelName, String newLabelName, double latitude, double longitude, double radius) {
		
		ContentValues values = new ContentValues();
		values.put(SQLiteHelper.COL_LABEL_NAME, newLabelName);
		values.put(SQLiteHelper.COL_LATITUDE, latitude);
		values.put(SQLiteHelper.COL_LONGITUDE, longitude);
		values.put(SQLiteHelper.COL_RADIUS, radius);
		
		return database.update(SQLiteHelper.TABLE_LOCATION_LABELS, values, SQLiteHelper.COL_LABEL_NAME + " = ?", new String[] { prevLabelName });
	}

	public List<String> getLabelNames() {
		List<String> labels = new ArrayList<String>();
		Cursor c = database.query(SQLiteHelper.TABLE_LOCATION_LABELS, new String[] { SQLiteHelper.COL_LABEL_NAME }, null, null, null, null, null);
		c.moveToFirst();
		while (!c.isAfterLast()) {
			labels.add(c.getString(0));
			c.moveToNext();
		}
		return labels;	
	}
	
	public List<String> getLabelNamesWithOther() {
		List<String> labels = getLabelNames();
		if (labels.size() <= 0) {
			labels.add(Const.ALL_LOCATIONS);
		} else {
			labels.add(Const.OTHER_LOCATIONS);
		}
		return labels;
	}
}
