package edu.ucla.nesl.rulemanager.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import edu.ucla.nesl.rulemanager.db.model.Rule;


public class RuleDataSource extends DataSource {

	public RuleDataSource(Context context) {
		super(context);
	}

	public void insert(String action, String data, String consumer, String timeLabel, String locationLabel) throws SQLException {
		ContentValues values = new ContentValues();
		values.put(SQLiteHelper.COL_ACTION, action);
		values.put(SQLiteHelper.COL_DATA, data);
		values.put(SQLiteHelper.COL_CONSUMER, consumer);
		if (timeLabel != null) 
			values.put(SQLiteHelper.COL_TIME_LABEL, timeLabel);
		if (locationLabel != null) 
			values.put(SQLiteHelper.COL_LOCATION_LABEL, locationLabel);
		values.put(SQLiteHelper.COL_UPLOAD_COUNT, 1);
		database.insertOrThrow(SQLiteHelper.TABLE_RULES, null, values);
	}

	public List<Rule> getRules() {
		List<Rule> rules = new ArrayList<Rule>();
		Cursor c = database.query(SQLiteHelper.TABLE_RULES, null, null, null, null, null, null);
		c.moveToFirst();
		while (!c.isAfterLast()) {
			Rule rule = new Rule();
			rule.setId(c.getInt(0));
			rule.setAction(c.getString(1));
			rule.setData(c.getString(2));
			rule.setConsumer(c.getString(3));
			rule.setTimeLabel(c.getString(4).equals("") ? null : c.getString(4));
			rule.setLocationLabel(c.getString(5).equals("") ? null : c.getString(5));
			rule.setServerId(c.getInt(6));
			rule.setUploadCount(c.getInt(7));
			rules.add(rule);
			c.moveToNext();
		}
		return rules;
	}

	public int deleteRule(int id) {
		return database.delete(SQLiteHelper.TABLE_RULES, SQLiteHelper.COL_ID + " = ?", new String[] { Integer.toString(id) });
	}
	
	public int deleteRuleWithTimeLabel(String label) {
		return database.delete(SQLiteHelper.TABLE_RULES, SQLiteHelper.COL_TIME_LABEL + " = ?", new String[] { label });
	}

	public int deleteRuleWithLocationLabel(String label) {
		return database.delete(SQLiteHelper.TABLE_RULES, SQLiteHelper.COL_LOCATION_LABEL + " = ?", new String[] { label });
	}

	public int update(int prevRuleId, String action, String data, String consumer, String timeLabel, String locationLabel) {
		ContentValues values = new ContentValues();
		values.put(SQLiteHelper.COL_ACTION, action);
		values.put(SQLiteHelper.COL_DATA, data);
		values.put(SQLiteHelper.COL_CONSUMER, consumer);
		if (timeLabel != null) {
			values.put(SQLiteHelper.COL_TIME_LABEL, timeLabel);
		} else {
			values.put(SQLiteHelper.COL_TIME_LABEL, "");
		}
		if (locationLabel != null) {
			values.put(SQLiteHelper.COL_LOCATION_LABEL, locationLabel);
		} else {
			values.put(SQLiteHelper.COL_LOCATION_LABEL, "");
		}
		return database.update(SQLiteHelper.TABLE_RULES, values, SQLiteHelper.COL_ID + " = ?", new String[] { Integer.toString(prevRuleId) });
	}

	public Rule getARule(int id) {
		Cursor c = database.query(SQLiteHelper.TABLE_RULES, null, SQLiteHelper.COL_ID + " = ?", new String[] { Integer.toString(id) }, null, null, null);
		Rule rule = null;
		c.moveToFirst();
		while (!c.isAfterLast()) {
			rule = new Rule();
			rule.setId(c.getInt(0));
			rule.setAction(c.getString(1));
			rule.setData(c.getString(2));
			rule.setConsumer(c.getString(3));
			rule.setTimeLabel(c.getString(4).equals("") ? null : c.getString(4));
			rule.setLocationLabel(c.getString(5).equals("") ? null : c.getString(5));
			rule.setServerId(c.getInt(6));
			rule.setUploadCount(c.getInt(7));
			c.moveToNext();
		}
		return rule;
	}

	public int updateTimeLabelName(String prevLabel, String newLabel) {
		if (prevLabel == null || newLabel == null) {
			return 0;
		}
		
		ContentValues values = new ContentValues();
		values.put(SQLiteHelper.COL_TIME_LABEL, newLabel);
		
		return database.update(SQLiteHelper.TABLE_RULES, values, SQLiteHelper.COL_TIME_LABEL + " = ?", new String[] { prevLabel });
	}

	public int updateLocationLabelName(String prevLabel, String newLabel) {
		if (prevLabel == null || newLabel == null) {
			return 0;
		}
		
		ContentValues values = new ContentValues();
		values.put(SQLiteHelper.COL_LOCATION_LABEL, newLabel);
		
		return database.update(SQLiteHelper.TABLE_RULES, values, SQLiteHelper.COL_LOCATION_LABEL + " = ?", new String[] { prevLabel });
	}

	public List<Rule> getRulesWithTimeLabel(String timeLabel) {
		Cursor c = database.query(SQLiteHelper.TABLE_RULES, null, SQLiteHelper.COL_TIME_LABEL + " = ?", new String[] { timeLabel }, null, null, null);
		List<Rule> rules = new ArrayList<Rule>();
		c.moveToFirst();
		while (!c.isAfterLast()) {
			Rule rule = new Rule();
			rule.setId(c.getInt(0));
			rule.setAction(c.getString(1));
			rule.setData(c.getString(2));
			rule.setConsumer(c.getString(3));
			rule.setTimeLabel(c.getString(4).equals("") ? null : c.getString(4));
			rule.setLocationLabel(c.getString(5).equals("") ? null : c.getString(5));
			rule.setServerId(c.getInt(6));
			rule.setUploadCount(c.getInt(7));
			rules.add(rule);
			c.moveToNext();
		}
		return rules;
	}
	
	public List<Rule> getRulesWithLocationLabel(String locationLabel) {
		Cursor c = database.query(SQLiteHelper.TABLE_RULES, null, SQLiteHelper.COL_LOCATION_LABEL + " = ?", new String[] { locationLabel }, null, null, null);
		List<Rule> rules = new ArrayList<Rule>();
		c.moveToFirst();
		while (!c.isAfterLast()) {
			Rule rule = new Rule();
			rule.setId(c.getInt(0));
			rule.setAction(c.getString(1));
			rule.setData(c.getString(2));
			rule.setConsumer(c.getString(3));
			rule.setTimeLabel(c.getString(4).equals("") ? null : c.getString(4));
			rule.setLocationLabel(c.getString(5).equals("") ? null : c.getString(5));
			rule.setServerId(c.getInt(6));
			rule.setUploadCount(c.getInt(7));
			rules.add(rule);
			c.moveToNext();
		}
		return rules;
	}

}
