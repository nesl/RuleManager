package edu.ucla.nesl.rulemanager.db.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;

import edu.ucla.nesl.rulemanager.Const;
import edu.ucla.nesl.rulemanager.tools.Tools;

public class TimeLabel extends Label {

	public static final String REPEAT_TYPE_WEEKLY = "weekly";
	public static final String REPEAT_TYPE_MONTHLY = "monthly";
	
	public static final int INVALID = -1;
	public static final int NON_OVERLAP = 0;
	public static final int EXACTLY_SAME = 1;
	public static final int PARTIAL_OVERLAP = 2;
	public static final int SUBSET = 3;
	public static final int SUPERSET = 4;
	
	protected String fromDate;
	protected String fromTime;
	protected String toDate;
	protected String toTime;
	protected boolean isAllDay;
	protected boolean isRepeat;
	protected String repeatType;
	protected int repeatDay;
	protected boolean isRepeatMon;
	protected boolean isRepeatTue;
	protected boolean isRepeatWed;
	protected boolean isRepeatThu;
	protected boolean isRepeatFri;
	protected boolean isRepeatSat;
	protected boolean isRepeatSun;

	public String getFromDate() {
		return fromDate;
	}

	public void setFromDate(String fromDate) {
		this.fromDate = fromDate;
	}

	public String getFromTime() {
		return fromTime;
	}

	public boolean isAllDay() {
		return isAllDay;
	}

	public void setAllDay(boolean isAllDay) {
		this.isAllDay = isAllDay;
	}

	public boolean isRepeat() {
		return isRepeat;
	}

	public void setRepeat(boolean isRepeat) {
		this.isRepeat = isRepeat;
	}

	public String getRepeatType() {
		return repeatType;
	}

	public void setRepeatType(String repeatType) {
		this.repeatType = repeatType;
	}

	public int getRepeatDay() {
		return repeatDay;
	}

	public void setRepeatDay(int repeatDay) {
		this.repeatDay = repeatDay;
	}

	public boolean isRepeatMon() {
		return isRepeatMon;
	}

	public void setRepeatMon(boolean isRepeatMon) {
		this.isRepeatMon = isRepeatMon;
	}

	public boolean isRepeatTue() {
		return isRepeatTue;
	}

	public void setRepeatTue(boolean isRepeatTue) {
		this.isRepeatTue = isRepeatTue;
	}

	public boolean isRepeatWed() {
		return isRepeatWed;
	}

	public void setRepeatWed(boolean isRepeatWed) {
		this.isRepeatWed = isRepeatWed;
	}

	public boolean isRepeatThu() {
		return isRepeatThu;
	}

	public void setRepeatThu(boolean isRepeatThu) {
		this.isRepeatThu = isRepeatThu;
	}

	public boolean isRepeatFri() {
		return isRepeatFri;
	}

	public void setRepeatFri(boolean isRepeatFri) {
		this.isRepeatFri = isRepeatFri;
	}

	public boolean isRepeatSat() {
		return isRepeatSat;
	}

	public void setRepeatSat(boolean isRepeatSat) {
		this.isRepeatSat = isRepeatSat;
	}

	public boolean isRepeatSun() {
		return isRepeatSun;
	}

	public void setRepeatSun(boolean isRepeatSun) {
		this.isRepeatSun = isRepeatSun;
	}

	public void setFromTime(String fromTime) {
		this.fromTime = fromTime;
	}

	public String getToDate() {
		return toDate;
	}

	public void setToDate(String toDate) {
		this.toDate = toDate;
	}

	public String getToTime() {
		return toTime;
	}

	public void setToTime(String toTime) {
		this.toTime = toTime;
	}

	@Override
	public String toString() {
		return labelName;
	}

	private String getWeekdayString() {
		String ret = "";
		if (isRepeatMon) ret += "Mon, ";
		if (isRepeatTue) ret += "Tue, ";
		if (isRepeatWed) ret += "Wed, ";
		if (isRepeatThu) ret += "Thu, ";
		if (isRepeatFri) ret += "Fri, ";
		if (isRepeatSat) ret += "Sat, ";
		if (isRepeatSun) ret += "Sun, ";
		if (ret.length() > 2) {
			ret = ret.substring(0, ret.length() - 2);
		}
		return ret;
	}

	public String getSummaryText() {
		if (!isAllDay && !isRepeat) {
			return "From " + fromTime + " " + fromDate + " to " + toTime + " " + toDate + "."; 
		} else if (!isAllDay && isRepeat) {
			if (repeatType.equalsIgnoreCase(REPEAT_TYPE_WEEKLY)) {
				return "From " + fromTime + " to " + toTime + " weekly on " + getWeekdayString() + "."; 
			} else if (repeatType.equalsIgnoreCase(REPEAT_TYPE_MONTHLY)) {
				return "From " + fromTime + " to " + toTime + " monthly on day " + repeatDay + ".";
			} else {
				return "Invalid repeatType: " + repeatType;
			}
		} else if (isAllDay && !isRepeat) {
			return "From " + fromDate + " to " + toDate + ".";
		} else if (isAllDay && isRepeat) {
			if (repeatType.equalsIgnoreCase(REPEAT_TYPE_WEEKLY)) {
				return "Weekly on " + getWeekdayString() + "."; 
			} else if (repeatType.equalsIgnoreCase(REPEAT_TYPE_MONTHLY)) {
				return "Monthly on day " + repeatDay + ".";
			} else {
				return "Invalid repeatType: " + repeatType;
			}
		}
		return null;
	}

	/* 0: non-overlap
	 * 1: exactly same
	 * 2: partial overlap
	 * 3: this is included in anotherTime
	 * 4: this includes anotherTime
	 */
	public int checkOverlap(TimeLabel anotherTime) {
		if (isBothRepeat(this, anotherTime)) {
			if (isAtLeastOneAllDay(this, anotherTime)) {
				return checkRepeatOverlaps(this, anotherTime);
			} else { // both not all day
				if (isTimeOverlaps(this.getFromTime(), this.getToTime(), anotherTime.getFromTime(), anotherTime.getToTime())) {
					return checkRepeatOverlaps(this, anotherTime);
				} else {
					return NON_OVERLAP;
				}
			}
		} else if (isOnlyOneRepeat(this, anotherTime)) {
			TimeLabel repeat, range;
			if (this.isRepeat()) {
				repeat = this;
				range = anotherTime;
			} else {
				repeat = anotherTime;
				range = this;
			}
			if (isRepeatOverlapsRange(repeat, range)) {
				return PARTIAL_OVERLAP;
			} else {
				return NON_OVERLAP;
			}
		} else if (!isBothRepeat(this, anotherTime)) {
			return checkRangeOverlaps(this, anotherTime);
		} else {
			assert false;
		}
		return INVALID;
	}

	private int checkRangeOverlaps(TimeLabel t1, TimeLabel t2) {
		DateTimeFormatter fmt = DateTimeFormat.forPattern("MM/dd/yyyy hh:mm aa");
		
		DateTime t1StartDateTime;
		DateTime t1EndDateTime;
		if (t1.isAllDay()) {
			t1StartDateTime = fmt.parseDateTime(t1.getFromDate() + " 12:00 AM");
			t1EndDateTime = fmt.parseDateTime(t1.getToDate() + " 12:00 AM");
			t1EndDateTime = t1EndDateTime.plusDays(1);
		} else {
			t1StartDateTime = fmt.parseDateTime(t1.getFromDate() + " " + t1.getFromTime());
			t1EndDateTime = fmt.parseDateTime(t1.getToDate() + " " + t1.getToTime());
			t1EndDateTime = t1EndDateTime.plusMillis(1);
		}
		
		DateTime t2StartDateTime;
		DateTime t2EndDateTime;
		if (t2.isAllDay()) {
			t2StartDateTime = fmt.parseDateTime(t2.getFromDate() + " 12:00 AM");
			t2EndDateTime = fmt.parseDateTime(t2.getToDate() + " 12:00 AM");
			t2EndDateTime = t2EndDateTime.plusDays(1);
		} else {
			t2StartDateTime = fmt.parseDateTime(t2.getFromDate() + " " + t2.getFromTime());
			t2EndDateTime = fmt.parseDateTime(t2.getToDate() + " " + t2.getToTime());
			t2EndDateTime = t2EndDateTime.plusMillis(1);
		}
		
		/*Interval t1Interval = new Interval(t1StartDateTime, t1EndDateTime);
		Interval t2Interval = new Interval(t2StartDateTime, t2EndDateTime);
		
		return t1Interval.overlaps(t2Interval);*/
		
		long t1StartMs = t1StartDateTime.getMillis();
		long t2StartMs = t2StartDateTime.getMillis();
		long t1EndMs = t1EndDateTime.getMillis();
		long t2EndMs = t2EndDateTime.getMillis();
		
		return checkTimeOverlap(t1StartMs, t1EndMs, t2StartMs, t2EndMs);
	}

	private boolean isRepeatOverlapsRange(TimeLabel repeat, TimeLabel range) {

		if (repeat.getRepeatType().equalsIgnoreCase(REPEAT_TYPE_MONTHLY)) {
			return isRangeOverlapsDayOfMonth(repeat, range); 
		} else if (repeat.getRepeatType().equalsIgnoreCase(REPEAT_TYPE_WEEKLY)) {
			return isRangeOverlapsDayOfWeek(repeat, range);
		} else {
			assert false;
		}
		return false;
	}

	private boolean isRangeOverlapsDayOfWeek(TimeLabel repeat, TimeLabel range) {
		boolean [] weekdays = getWeekDaysArray(repeat);

		String startDate = range.getFromDate();
		String endDate = range.getToDate();
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		Date sDate = null;
		Date eDate = null;
		try {
			sDate = df.parse(startDate);
			eDate = df.parse(endDate);
		} catch (ParseException e) {
			e.printStackTrace();
			assert false;
		}
		int startYear = sDate.getYear();
		int endYear = eDate.getYear();
		int startMonth = sDate.getMonth();
		int endMonth = eDate.getMonth();
		int startDay = sDate.getDate();
		int endDay = eDate.getDate();

		DateTime startDateTime = new DateTime(startYear + 1900, startMonth + 1, startDay, 0, 0, 0, 0);
		DateTime endDateTime = new DateTime(endYear + 1900, endMonth + 1, endDay, 0, 0, 0, 0);
		DateTime curDateTime = new DateTime(startDateTime);
		while (curDateTime.isBefore(endDateTime) || curDateTime.isEqual(endDateTime)) {
			int dayOfWeek = curDateTime.getDayOfWeek() - 1;
			if (weekdays[dayOfWeek]) {
				if (repeat.isAllDay() || range.isAllDay()) {
					return true;
				} else {
					if (curDateTime.equals(startDateTime) && curDateTime.equals(endDateTime)) {
						if (isTimeOverlaps(repeat.getFromTime(), repeat.getToTime(), range.getFromTime(), range.getToTime())) {
							return true;
						}
					} else if (curDateTime.equals(startDateTime)) {
						if (isTimeOverlaps(repeat.getFromTime(), repeat.getToTime(), range.getFromTime(), "00:00 PM")) {
							return true;
						}
					} else if (curDateTime.equals(endDateTime)) {
						if (isTimeOverlaps(repeat.getFromTime(), repeat.getToTime(), "00:00 AM", range.getToTime())) {
							return true;
						}
					} else {
						return true;
					}
				}
			}
			curDateTime = curDateTime.plusDays(1);
		}
		return false;
	}

	private boolean isRangeOverlapsDayOfMonth(TimeLabel repeat, TimeLabel range) {
		String startDate = range.getFromDate();
		String endDate = range.getToDate();
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		Date sDate = null;
		Date eDate = null;
		try {
			sDate = df.parse(startDate);
			eDate = df.parse(endDate);
		} catch (ParseException e) {
			e.printStackTrace();
			assert false;
		}
		int startYear = sDate.getYear();
		int endYear = eDate.getYear();
		int startMonth = sDate.getMonth();
		int endMonth = eDate.getMonth();
		int startDay = sDate.getDate();
		int endDay = eDate.getDate();

		DateTime startDateTime = new DateTime(startYear + 1900, startMonth + 1, startDay, 0, 0, 0, 0);
		DateTime endDateTime = new DateTime(endYear + 1900, endMonth + 1, endDay, 0, 0, 0, 0);
		DateTime curDateTime = new DateTime(startDateTime);
		while (curDateTime.isBefore(endDateTime) || curDateTime.isEqual(endDateTime)) {
			if (curDateTime.getDayOfMonth() == repeat.getRepeatDay()) {
				if (repeat.isAllDay() || range.isAllDay()) {
					return true;
				} else {
					if (curDateTime.equals(startDateTime) && curDateTime.equals(endDateTime)) {
						if (isTimeOverlaps(repeat.getFromTime(), repeat.getToTime(), range.getFromTime(), range.getToTime())) {
							return true;
						}
					} else if (curDateTime.equals(startDateTime)) {
						if (isTimeOverlaps(repeat.getFromTime(), repeat.getToTime(), range.getFromTime(), "00:00 PM")) {
							return true;
						}
					} else if (curDateTime.equals(endDateTime)) {
						if (isTimeOverlaps(repeat.getFromTime(), repeat.getToTime(), "00:00 AM", range.getToTime())) {
							return true;
						}
					} else {
						return true;
					}
				}
			}
			curDateTime = curDateTime.plusDays(1);
		}

		return false;
	}

	private static boolean isTimeOverlaps(String t1start, String t1end, String t2start, String t2end) {

		long t1StartMs = convertToMillis(t1start);
		long t1EndMs = convertToMillis(t1end);
		long t2StartMs = convertToMillis(t2start);
		long t2EndMs = convertToMillis(t2end);

		if (t1StartMs <= t2EndMs && t1EndMs >= t2StartMs) {
			return true;
		}

		return false;
	}

	private static long convertToMillis(String time) {
		long ms = 0;
		String[] timeSplit = time.split(":");
		String[] minAmPm = timeSplit[1].split(" ");
		int hour = Integer.valueOf(timeSplit[0]);
		int min = Integer.valueOf(minAmPm[0]);
		boolean isAm;
		if (minAmPm[1].equalsIgnoreCase("AM")) {
			isAm = true;
		} else {
			isAm = false;
		}
		if (!isAm && hour == 0 && min == 0) {
			hour = 24;
		} else if (!isAm && hour != 12) {
			hour += 12;
		}

		ms += hour * 3600 * 1000 + min * 60 * 1000;

		return ms;
	}

	private static int getLastDayOfMonth(int month, int year) {
		Calendar mycal = new GregorianCalendar(year + 1900, month, 1);
		int daysInMonth = mycal.getActualMaximum(Calendar.DAY_OF_MONTH);
		return daysInMonth;
	}

	private boolean isOnlyOneRepeat(TimeLabel timeLabel, TimeLabel anotherTime) {
		if ( (timeLabel.isRepeat() && !anotherTime.isRepeat()) 
				|| (!timeLabel.isRepeat() && anotherTime.isRepeat()) ) {
			return true;
		}
		return false;
	}

	private boolean isBothRepeat(TimeLabel timeLabel, TimeLabel anotherTime) {
		if (timeLabel.isRepeat() && anotherTime.isRepeat()) {
			return true;
		}
		return false;
	}

	private int checkRepeatOverlaps(TimeLabel timeLabel, TimeLabel anotherTime) {
		if (isRepeatMonthlyAndRepeatWeekly(timeLabel, anotherTime)) {
			return PARTIAL_OVERLAP;
		} else if (isBothRepeatMonthly(timeLabel, anotherTime)) {
			if (timeLabel.getRepeatDay() == anotherTime.getRepeatDay()) {
				return checkBasedOnTimeOnly(timeLabel, anotherTime);
			} else {
				return NON_OVERLAP;
			}
		} else if (isBothRepeatWeekly(timeLabel, anotherTime)) {
			return isRepeatWeekdayOverlap(timeLabel, anotherTime);
		} else {
			assert false;
			return INVALID;
		}		
	}

	private boolean isAtLeastOneAllDay(TimeLabel timeLabel, TimeLabel anotherTime) {
		if (timeLabel.isAllDay() || anotherTime.isAllDay()) {
			return true;
		}
		return false;
	}

	private int isRepeatWeekdayOverlap(TimeLabel timeLabel, TimeLabel anotherTime) {
		boolean[] t1WeekDays = getWeekDaysArray(timeLabel);
		boolean[] t2WeekDays = getWeekDaysArray(anotherTime);
		
		// if weekdays are same
		boolean isSame = true;
		for (int i = 0; i < 7; i++) {
			if (t1WeekDays[i] != t2WeekDays[i]) {
				isSame = false;
				break;
			}
		}
		if (isSame) {
			return checkBasedOnTimeOnly(timeLabel, anotherTime);
		}
		
		// check if overlap
		boolean isOverlap = false;
		for (int i = 0; i < 7; i++) {
			if (t1WeekDays[i] && t2WeekDays[i]) {
				isOverlap = true;
			}
		}

		if (isOverlap) {
			// if superset
			boolean isSuperset = true;
			for (int i = 0; i < 7; i++) {
				if (!t1WeekDays[i] && t2WeekDays[i]) {
					isSuperset = false;
					break;
				}
			}
			// if subset
			boolean isSubset = true;
			for (int i = 0; i < 7; i++) {
				if (t1WeekDays[i] && !t2WeekDays[i]) {
					isSubset = false;
				}
			}
			
			if (isSuperset && !isSubset) {
				if (timeLabel.isAllDay()) {
					return SUPERSET;
				} else if (!timeLabel.isAllDay() && !anotherTime.isAllDay()) {
					int result = checkTimeOverlap(timeLabel, anotherTime);
					if (result == EXACTLY_SAME || result == SUPERSET) {
						return SUPERSET;
					} else {
						return PARTIAL_OVERLAP;
					}
				} else {
					return PARTIAL_OVERLAP;
				}
			} else if (!isSuperset && isSubset) {
				if (anotherTime.isAllDay()) {
					return SUBSET;
				} else if (!timeLabel.isAllDay() && !anotherTime.isAllDay()) {
					int result = checkTimeOverlap(timeLabel, anotherTime);
					if (result == EXACTLY_SAME || result == SUBSET) {
						return SUBSET;
					} else {
						return PARTIAL_OVERLAP;
					}
				} else {
					return PARTIAL_OVERLAP;
				}
			} else if (!isSuperset && !isSubset) {
				// if weekdays are partially overlap
				return PARTIAL_OVERLAP;
			} else {
				assert false;
				return INVALID;
			}	
		} else {
			// weekdays are exclusive
			return NON_OVERLAP;
		}
	}

	private int checkTimeOverlap(TimeLabel timeLabel, TimeLabel anotherTime) {
		String t1start = timeLabel.getFromTime();
		String t1end = timeLabel.getToTime();
		String t2start = anotherTime.getFromTime();
		String t2end = anotherTime.getToTime();
		
		long t1StartMs = convertToMillis(t1start);
		long t1EndMs = convertToMillis(t1end);
		long t2StartMs = convertToMillis(t2start);
		long t2EndMs = convertToMillis(t2end);

		return checkTimeOverlap(t1StartMs, t1EndMs, t2StartMs, t2EndMs);
	}

	private int checkTimeOverlap(long t1StartMs, long t1EndMs, long t2StartMs, long t2EndMs) {
		if (t1StartMs == t2StartMs && t1EndMs == t2EndMs) {
			return EXACTLY_SAME;
		} else if (t1StartMs < t2StartMs && t1EndMs > t2EndMs) {
			return SUPERSET;
		} else if (t1StartMs > t2StartMs && t1EndMs < t2EndMs) {
			return SUBSET;
		} else if ((t1StartMs < t2StartMs && t1EndMs < t2StartMs)
				|| (t1StartMs > t2EndMs && t1EndMs > t2EndMs) ) {
			return NON_OVERLAP;
		} else {
			return PARTIAL_OVERLAP;
		}
	}

	private int checkBasedOnTimeOnly(TimeLabel timeLabel, TimeLabel anotherTime) {
		if (timeLabel.isAllDay() && anotherTime.isAllDay()) {
			return EXACTLY_SAME;
		} else if (timeLabel.isAllDay() && !anotherTime.isAllDay()) {
			return SUPERSET;
		} else if (!timeLabel.isAllDay() && anotherTime.isAllDay()) {
			return SUBSET;
		} else if (!timeLabel.isAllDay() && !anotherTime.isAllDay()) {
			return checkTimeOverlap(timeLabel, anotherTime);
		} else {
			assert false;
		}
		return INVALID;
	}

	private boolean[] getWeekDaysArray(TimeLabel timeLabel) {
		boolean [] weekdays = new boolean[7];
		if (timeLabel.isRepeatMon()) {
			weekdays[0] = true;
		}
		if (timeLabel.isRepeatTue()) {
			weekdays[1] = true;
		}
		if (timeLabel.isRepeatWed()) {
			weekdays[2] = true;
		}
		if (timeLabel.isRepeatThu()) {
			weekdays[3] = true;
		}
		if (timeLabel.isRepeatFri()) {
			weekdays[4] = true;
		}
		if (timeLabel.isRepeatSat()) {
			weekdays[5] = true;
		}
		if (timeLabel.isRepeatSun()) {
			weekdays[6] = true;
		}
		return weekdays;
	}

	private boolean isBothRepeatWeekly(TimeLabel timeLabel, TimeLabel anotherTime) {
		if (timeLabel.getRepeatType().equalsIgnoreCase(REPEAT_TYPE_WEEKLY) && anotherTime.getRepeatType().equalsIgnoreCase(REPEAT_TYPE_WEEKLY)) {
			return true;
		}
		return false;
	}

	private boolean isBothRepeatMonthly(TimeLabel timeLabel, TimeLabel anotherTime) {
		if (timeLabel.getRepeatType().equalsIgnoreCase(REPEAT_TYPE_MONTHLY) && anotherTime.getRepeatType().equalsIgnoreCase(REPEAT_TYPE_MONTHLY)) {
			return true;
		}
		return false;
	}

	private boolean isRepeatMonthlyAndRepeatWeekly(TimeLabel timeLabel, TimeLabel anotherTime) {
		if ( (timeLabel.getRepeatType().equalsIgnoreCase(REPEAT_TYPE_MONTHLY) && anotherTime.getRepeatType().equalsIgnoreCase(REPEAT_TYPE_WEEKLY)) 
				|| (timeLabel.getRepeatType().equalsIgnoreCase(REPEAT_TYPE_WEEKLY) && anotherTime.getRepeatType().equalsIgnoreCase(REPEAT_TYPE_MONTHLY)) ) {
			return true;
		}
		return false;
	}

	private String convertTimeTo24(String time) {
		if (!time.contains("AM") && !time.contains("am") && !time.contains("PM") && !time.contains("pm")) {
			return time;
		}
		
		String[] splitTime = time.split(" ");
		String[] timeonly = splitTime[0].split(":");
		String ampm = splitTime[1];
		int hour = Integer.parseInt(timeonly[0]);
		int min = Integer.parseInt(timeonly[1]);
		
		if (ampm.equalsIgnoreCase("AM")) {
			if (hour == 0 || hour == 12) {
				hour = 0;
			}
		} else if (ampm.equalsIgnoreCase("PM")) {
			if (hour != 12) {
				hour += 12;
			}
		} else {
			assert false;
			return time;
		}
				
		return String.format("%02d:%02d", hour, min);
	}

	private String getTimeRangeCondition() {
		String value = "";
		String[] fromTime24 = convertTimeTo24(fromTime).split(":");
		String[] toTime24 = convertTimeTo24(toTime).split(":");
		int fromHour = Integer.parseInt(fromTime24[0]);
		int fromMin = Integer.parseInt(fromTime24[1]);
		int toHour = Integer.parseInt(toTime24[0]);
		int toMin = Integer.parseInt(toTime24[1]);
		
		if (toHour == fromHour) {
			value += "(HOUR(timestamp) = " + toHour + " and MINUTE(timestamp) BETWEEN " + fromMin + " AND " + toMin +")"; 
		} else if (toHour - fromHour == 1) {
			value += "(";
			value += "(HOUR(timestamp) = " + fromHour + " and MINUTE(timestamp) >= " + fromMin + ")";
			value += " OR ";
			value += "(HOUR(timestamp) = " + toHour + " and MINUTE(timestamp) <= " + toMin + ")";
			value += ")";
		} else {
			value += "(";
			value += "(HOUR(timestamp) = " + fromHour + " and MINUTE(timestamp) >= " + fromMin + ")";
			value += " OR ";
			value += "(HOUR(timestamp) = " + toHour + " and MINUTE(timestamp) <= " + toMin + ")";
			value += " OR ";
			value += "(HOUR(timestamp) BETWEEN " + (fromHour+1) + " AND " + (toHour-1) + ")";
			value += ")";
		}
		return value;
	}
	
	public JSONObject toJson() {
		String name = Tools.makeValidMacroName(labelName);
		String value = null;
		if (isRepeat) {
			if (repeatType.equalsIgnoreCase(REPEAT_TYPE_MONTHLY)) {
				value = "[ * * * " + repeatDay + " * * ]";
				if (!isAllDay) {
					value += " AND " + getTimeRangeCondition();
				} 
			} else if (repeatType.equalsIgnoreCase(REPEAT_TYPE_WEEKLY)) {
				List<String> days = new ArrayList<String>();
				if (isRepeatSun) {
					days.add("0");
				}
				if (isRepeatMon) {
					days.add("1");
				}
				if (isRepeatTue) {
					days.add("2");
				}
				if (isRepeatWed) {
					days.add("3");
				}
				if (isRepeatThu) {
					days.add("4");
				}
				if (isRepeatFri) {
					days.add("5");
				}
				if (isRepeatSat) {
					days.add("6");
				}
				value = "[ * * * * * " + Tools.joinString(days, ",") + " ]";
				if (!isAllDay) {
					value += " AND " + getTimeRangeCondition();
				} 
			} else {
				assert false;
				return null;
			}
		} else {
			if (isAllDay) {
				DateTimeFormatter fmt = DateTimeFormat.forPattern("MM/dd/yyyy");
				DateTime toDateTime = fmt.parseDateTime(toDate);
				toDateTime = toDateTime.plusDays(1);
				String newToDate = toDateTime.toString(fmt);
				value = "(timestamp >= '" + convertToValidSqlDate(fromDate) + " 00:00:00'"
						+ " AND timestamp < '" + convertToValidSqlDate(newToDate) + " 00:00:00')"; 
			} else {
				String format = "timestamp BETWEEN '%s' AND '%s'";
				String from = convertToValidSqlDate(fromDate) + " " + convertTimeTo24(fromTime) + ":00";
				String to = convertToValidSqlDate(toDate) + " " + convertTimeTo24(toTime) + ":00";
				value = String.format(format, from, to);
			}
		}
		
		JSONObject json = new JSONObject();
		try {
			json.put("name", Const.TIME_LABEL_PREFIX + name);
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
	
	private String convertToValidSqlDate(String date) {
		String[] split = date.split("/");
		return split[2] + "-" + split[0] + "-" + split[1];
	}
}
