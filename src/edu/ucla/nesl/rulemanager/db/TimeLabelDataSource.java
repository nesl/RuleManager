package edu.ucla.nesl.rulemanager.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import edu.ucla.nesl.rulemanager.Const;
import edu.ucla.nesl.rulemanager.db.model.TimeLabel;

public class TimeLabelDataSource extends DataSource {

	public TimeLabelDataSource(Context context) {
		super(context);
	}

	public void insertTimeRange(String labelName, String fromDate, String fromTime, String toDate, String toTime) throws SQLException {
		ContentValues values = new ContentValues();
		values.put(SQLiteHelper.COL_LABEL_NAME, labelName);
		values.put(SQLiteHelper.COL_FROM_DATE, fromDate);
		values.put(SQLiteHelper.COL_FROM_TIME, fromTime);
		values.put(SQLiteHelper.COL_TO_DATE, toDate);
		values.put(SQLiteHelper.COL_TO_TIME, toTime);
		values.put(SQLiteHelper.COL_IS_ALL_DAY, false);
		values.put(SQLiteHelper.COL_IS_REPEAT, false);
		values.put(SQLiteHelper.COL_REPEAT_DAY, 0);
		values.put(SQLiteHelper.COL_IS_REPEAT_MON, false);
		values.put(SQLiteHelper.COL_IS_REPEAT_TUE, false);
		values.put(SQLiteHelper.COL_IS_REPEAT_WED, false);
		values.put(SQLiteHelper.COL_IS_REPEAT_THU, false);
		values.put(SQLiteHelper.COL_IS_REPEAT_FRI, false);
		values.put(SQLiteHelper.COL_IS_REPEAT_SAT, false);
		values.put(SQLiteHelper.COL_IS_REPEAT_SUN, false);
		database.insertOrThrow(SQLiteHelper.TABLE_TIME_LABELS, null, values);
	}
	
	public void insertRepeatWeekly(String labelName, String fromTime, String toTime
			, boolean isMon
			, boolean isTue
			, boolean isWed
			, boolean isThu
			, boolean isFri
			, boolean isSat
			, boolean isSun
			) throws SQLException {
		ContentValues values = new ContentValues();
		values.put(SQLiteHelper.COL_LABEL_NAME, labelName);
		values.put(SQLiteHelper.COL_FROM_TIME, fromTime);
		values.put(SQLiteHelper.COL_TO_TIME, toTime);
		values.put(SQLiteHelper.COL_IS_ALL_DAY, false);
		values.put(SQLiteHelper.COL_IS_REPEAT, true);
		values.put(SQLiteHelper.COL_REPEAT_TYPE, TimeLabel.REPEAT_TYPE_WEEKLY);
		values.put(SQLiteHelper.COL_REPEAT_DAY, 0);
		values.put(SQLiteHelper.COL_IS_REPEAT_MON, isMon);
		values.put(SQLiteHelper.COL_IS_REPEAT_TUE, isTue);
		values.put(SQLiteHelper.COL_IS_REPEAT_WED, isWed);
		values.put(SQLiteHelper.COL_IS_REPEAT_THU, isThu);
		values.put(SQLiteHelper.COL_IS_REPEAT_FRI, isFri);
		values.put(SQLiteHelper.COL_IS_REPEAT_SAT, isSat);
		values.put(SQLiteHelper.COL_IS_REPEAT_SUN, isSun);
		database.insertOrThrow(SQLiteHelper.TABLE_TIME_LABELS, null, values);
	}
	
	public void insertRepeatMonthly(String labelName, String fromTime, String toTime, int repeatDay) throws SQLException {
		ContentValues values = new ContentValues();
		values.put(SQLiteHelper.COL_LABEL_NAME, labelName);
		values.put(SQLiteHelper.COL_FROM_TIME, fromTime);
		values.put(SQLiteHelper.COL_TO_TIME, toTime);
		values.put(SQLiteHelper.COL_IS_ALL_DAY, false);
		values.put(SQLiteHelper.COL_IS_REPEAT, true);
		values.put(SQLiteHelper.COL_REPEAT_TYPE, TimeLabel.REPEAT_TYPE_MONTHLY);
		values.put(SQLiteHelper.COL_REPEAT_DAY, repeatDay);
		values.put(SQLiteHelper.COL_IS_REPEAT_MON, false);
		values.put(SQLiteHelper.COL_IS_REPEAT_TUE, false);
		values.put(SQLiteHelper.COL_IS_REPEAT_WED, false);
		values.put(SQLiteHelper.COL_IS_REPEAT_THU, false);
		values.put(SQLiteHelper.COL_IS_REPEAT_FRI, false);
		values.put(SQLiteHelper.COL_IS_REPEAT_SAT, false);
		values.put(SQLiteHelper.COL_IS_REPEAT_SUN, false);
		database.insertOrThrow(SQLiteHelper.TABLE_TIME_LABELS, null, values);
	}
	
	public void insertAllDay(String labelName, String fromDate, String toDate) throws SQLException {
		ContentValues values = new ContentValues();
		values.put(SQLiteHelper.COL_LABEL_NAME, labelName);
		values.put(SQLiteHelper.COL_FROM_DATE, fromDate);
		values.put(SQLiteHelper.COL_TO_DATE, toDate);
		values.put(SQLiteHelper.COL_IS_ALL_DAY, true);
		values.put(SQLiteHelper.COL_IS_REPEAT, false);
		values.put(SQLiteHelper.COL_REPEAT_DAY, 0);
		values.put(SQLiteHelper.COL_IS_REPEAT_MON, false);
		values.put(SQLiteHelper.COL_IS_REPEAT_TUE, false);
		values.put(SQLiteHelper.COL_IS_REPEAT_WED, false);
		values.put(SQLiteHelper.COL_IS_REPEAT_THU, false);
		values.put(SQLiteHelper.COL_IS_REPEAT_FRI, false);
		values.put(SQLiteHelper.COL_IS_REPEAT_SAT, false);
		values.put(SQLiteHelper.COL_IS_REPEAT_SUN, false);
		database.insertOrThrow(SQLiteHelper.TABLE_TIME_LABELS, null, values);
	}

	public void insertAllDayRepeatWeekly(String labelName
			, boolean isMon
			, boolean isTue
			, boolean isWed
			, boolean isThu
			, boolean isFri
			, boolean isSat
			, boolean isSun
			) throws SQLException {
		ContentValues values = new ContentValues();
		values.put(SQLiteHelper.COL_LABEL_NAME, labelName);
		values.put(SQLiteHelper.COL_IS_ALL_DAY, true);
		values.put(SQLiteHelper.COL_IS_REPEAT, true);
		values.put(SQLiteHelper.COL_REPEAT_TYPE, TimeLabel.REPEAT_TYPE_WEEKLY);
		values.put(SQLiteHelper.COL_REPEAT_DAY, 0);
		values.put(SQLiteHelper.COL_IS_REPEAT_MON, isMon);
		values.put(SQLiteHelper.COL_IS_REPEAT_TUE, isTue);
		values.put(SQLiteHelper.COL_IS_REPEAT_WED, isWed);
		values.put(SQLiteHelper.COL_IS_REPEAT_THU, isThu);
		values.put(SQLiteHelper.COL_IS_REPEAT_FRI, isFri);
		values.put(SQLiteHelper.COL_IS_REPEAT_SAT, isSat);
		values.put(SQLiteHelper.COL_IS_REPEAT_SUN, isSun);
		database.insertOrThrow(SQLiteHelper.TABLE_TIME_LABELS, null, values);
	}

	public void insertAllDayRepeatMonthly(String labelName, int repeatDay) throws SQLException {
		ContentValues values = new ContentValues();
		values.put(SQLiteHelper.COL_LABEL_NAME, labelName);
		values.put(SQLiteHelper.COL_IS_ALL_DAY, true);
		values.put(SQLiteHelper.COL_IS_REPEAT, true);
		values.put(SQLiteHelper.COL_REPEAT_TYPE, TimeLabel.REPEAT_TYPE_MONTHLY);
		values.put(SQLiteHelper.COL_REPEAT_DAY, repeatDay);
		values.put(SQLiteHelper.COL_IS_REPEAT_MON, false);
		values.put(SQLiteHelper.COL_IS_REPEAT_TUE, false);
		values.put(SQLiteHelper.COL_IS_REPEAT_WED, false);
		values.put(SQLiteHelper.COL_IS_REPEAT_THU, false);
		values.put(SQLiteHelper.COL_IS_REPEAT_FRI, false);
		values.put(SQLiteHelper.COL_IS_REPEAT_SAT, false);
		values.put(SQLiteHelper.COL_IS_REPEAT_SUN, false);
		database.insertOrThrow(SQLiteHelper.TABLE_TIME_LABELS, null, values);
	}

	public List<TimeLabel> getTimeLabels() {
		List<TimeLabel> labels = new ArrayList<TimeLabel>();
		Cursor c = database.query(SQLiteHelper.TABLE_TIME_LABELS, null, null, null, null, null, null);
		c.moveToFirst();
		while (!c.isAfterLast()) {
			TimeLabel label = new TimeLabel();
			label.setLabelName(c.getString(0));
			label.setFromDate(c.getString(1));
			label.setFromTime(c.getString(2));
			label.setToDate(c.getString(3));
			label.setToTime(c.getString(4));
			label.setAllDay(c.getInt(5) == 0 ? false : true);
			label.setRepeat(c.getInt(6) == 0 ? false : true);
			label.setRepeatType(c.getString(7));
			label.setRepeatDay(c.getInt(8));
			label.setRepeatMon(c.getInt(9) == 0 ? false : true);
			label.setRepeatTue(c.getInt(10) == 0 ? false : true);
			label.setRepeatWed(c.getInt(11) == 0 ? false : true);
			label.setRepeatThu(c.getInt(12) == 0 ? false : true);
			label.setRepeatFri(c.getInt(13) == 0 ? false : true);
			label.setRepeatSat(c.getInt(14) == 0 ? false : true);
			label.setRepeatSun(c.getInt(15) == 0 ? false : true);
			labels.add(label);
			c.moveToNext();
		}
		return labels;
	}

	public int deleteTimeLabel(String labelName) {
		return database.delete(SQLiteHelper.TABLE_TIME_LABELS, SQLiteHelper.COL_LABEL_NAME + " = ?", new String[] { labelName });
	}

	public int updateTimeLabel(String prevLabelName, String newLabelName
			, String fromDate, String fromTime, String toDate, String toTime
			, boolean isAllDay, boolean isRepeat, String repeatType
			, int repeatDay
			, boolean isMon, boolean isTue, boolean isWed, boolean isThu, boolean isFri, boolean isSat, boolean isSun
			) {
		ContentValues values = new ContentValues();
		values.put(SQLiteHelper.COL_LABEL_NAME, newLabelName);
		values.put(SQLiteHelper.COL_FROM_DATE, fromDate);
		values.put(SQLiteHelper.COL_FROM_TIME, fromTime);
		values.put(SQLiteHelper.COL_TO_DATE, toDate);
		values.put(SQLiteHelper.COL_TO_TIME, toTime);
		values.put(SQLiteHelper.COL_IS_ALL_DAY, isAllDay);
		values.put(SQLiteHelper.COL_IS_REPEAT, isRepeat);
		values.put(SQLiteHelper.COL_REPEAT_TYPE, repeatType);
		values.put(SQLiteHelper.COL_REPEAT_DAY, repeatDay);
		values.put(SQLiteHelper.COL_IS_REPEAT_MON, isMon);
		values.put(SQLiteHelper.COL_IS_REPEAT_TUE, isTue);
		values.put(SQLiteHelper.COL_IS_REPEAT_WED, isWed);
		values.put(SQLiteHelper.COL_IS_REPEAT_THU, isThu);
		values.put(SQLiteHelper.COL_IS_REPEAT_FRI, isFri);
		values.put(SQLiteHelper.COL_IS_REPEAT_SAT, isSat);
		values.put(SQLiteHelper.COL_IS_REPEAT_SUN, isSun);
		return database.update(SQLiteHelper.TABLE_TIME_LABELS, values, SQLiteHelper.COL_LABEL_NAME + " = ?", new String[] { prevLabelName });
	}

	public List<String> getLabelNames() {
		List<String> labels = new ArrayList<String>();
		Cursor c = database.query(SQLiteHelper.TABLE_TIME_LABELS, new String[] { SQLiteHelper.COL_LABEL_NAME }, null, null, null, null, null);
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
			labels.add(Const.ALL_TIME);
		} else {
			labels.add(Const.OTHER_TIME);
		}
		return labels;
	}
}
