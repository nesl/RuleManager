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
			rules.add(rule);
			c.moveToNext();
		}
		return rules;
	}

	public int deleteRule(int id) {
		return database.delete(SQLiteHelper.TABLE_RULES, SQLiteHelper.COL_ID + " = ?", new String[] { Integer.toString(id) });
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
			c.moveToNext();
		}
		return rule;
	}
}
