package edu.ucla.nesl.rulemanager.activity;

import java.util.Calendar;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import edu.ucla.nesl.rulemanager.Const;
import edu.ucla.nesl.rulemanager.R;
import edu.ucla.nesl.rulemanager.Tools;
import edu.ucla.nesl.rulemanager.SyncService;
import edu.ucla.nesl.rulemanager.db.RuleDataSource;
import edu.ucla.nesl.rulemanager.db.TimeLabelDataSource;
import edu.ucla.nesl.rulemanager.db.model.TimeLabel;
import edu.ucla.nesl.rulemanager.uielement.TimeRepeatDialog;
import edu.ucla.nesl.rulemanager.uielement.TimeRepeatDialog.TimeRepeatDialogListener;

public class TimeLabelActivity extends Activity {

	private TimeLabelDataSource timeLabelDataSource;
	private RuleDataSource ruleDataSource;

	private static final int FROM_TIME_DIALOG_ID = 1;
	private static final int FROM_DATE_DIALOG_ID = 2;
	private static final int TO_TIME_DIALOG_ID = 3;
	private static final int TO_DATE_DIALOG_ID = 4;
	private static final int REPEAT_DIALOG = 5;

	private EditText labelEditText;
	private EditText toDate;
	private EditText toTime;
	private EditText fromDate;
	private EditText fromTime;

	private TimeRepeatDialog repeatDialog;

	private CheckBox repeatCheckBox;
	private CheckBox alldayCheckBox;

	private String repeatType;
	private int repeatDay;
	private boolean isMon;
	private boolean isTue;
	private boolean isWed;
	private boolean isThu;
	private boolean isFri;
	private boolean isSat;
	private boolean isSun;

	private String prevLabelName;
	private boolean isSetupLabel = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_new_time_label);

		timeLabelDataSource = new TimeLabelDataSource(this);
		ruleDataSource = new RuleDataSource(this);

		labelEditText = (EditText) findViewById(R.id.time_label);
		repeatCheckBox = (CheckBox)findViewById(R.id.checkbox_repeat);
		alldayCheckBox = (CheckBox)findViewById(R.id.checkbox_allday);

		toDate = (EditText) findViewById(R.id.to_date);
		toTime = (EditText) findViewById(R.id.to_time);
		fromDate = (EditText) findViewById(R.id.from_date);
		fromTime = (EditText) findViewById(R.id.from_time);

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			labelEditText.setText(bundle.getString(Const.BUNDLE_KEY_LABEL_NAME));
			isSetupLabel = bundle.getBoolean(Const.BUNDLE_KEY_IS_SETUP_LABEL); 
			if (isSetupLabel) {
				labelEditText.setEnabled(false);
				setCurrentDateTimeOnView();
			} else {
				setCurrentDateTimeOnView();
				prevLabelName = bundle.getString(Const.BUNDLE_KEY_LABEL_NAME);
				String fromDateStr = bundle.getString(Const.BUNDLE_KEY_FROM_DATE);
				if (fromDateStr != null && fromDateStr.length() > 0) {
					fromDate.setText(fromDateStr);	
				}
				String fromTimeStr = bundle.getString(Const.BUNDLE_KEY_FROM_TIME);
				if (fromTimeStr != null && fromTimeStr.length() > 0) {
					fromTime.setText(fromTimeStr);	
				}
				String toDateStr = bundle.getString(Const.BUNDLE_KEY_TO_DATE);
				if (toDateStr != null && toDateStr.length() > 0) {
					toDate.setText(toDateStr);	
				}
				String toTimeStr = bundle.getString(Const.BUNDLE_KEY_TO_TIME);
				if (toTimeStr != null && toTimeStr.length() > 0) {
					toTime.setText(toTimeStr);
				}
				alldayCheckBox.setChecked(bundle.getBoolean(Const.BUNDLE_KEY_IS_ALL_DAY));
				repeatCheckBox.setChecked(bundle.getBoolean(Const.BUNDLE_KEY_IS_REPEAT));
				repeatType = bundle.getString(Const.BUNDLE_KEY_REPEAT_TYPE);
				repeatDay = bundle.getInt(Const.BUNDLE_KEY_REPEAT_DAY);
				isMon = bundle.getBoolean(Const.BUNDLE_KEY_IS_REPEAT_MON);
				isTue = bundle.getBoolean(Const.BUNDLE_KEY_IS_REPEAT_TUE);
				isWed = bundle.getBoolean(Const.BUNDLE_KEY_IS_REPEAT_WED);
				isThu = bundle.getBoolean(Const.BUNDLE_KEY_IS_REPEAT_THU);
				isFri = bundle.getBoolean(Const.BUNDLE_KEY_IS_REPEAT_FRI);
				isSat = bundle.getBoolean(Const.BUNDLE_KEY_IS_REPEAT_SAT);
				isSun = bundle.getBoolean(Const.BUNDLE_KEY_IS_REPEAT_SUN);
				if (alldayCheckBox.isChecked()) {
					fromTime.setEnabled(false);
					toTime.setEnabled(false);
				}
				if (repeatCheckBox.isChecked()) {
					fromDate.setEnabled(false);
					toDate.setEnabled(false);
				}
			}
		} else {
			setCurrentDateTimeOnView();
		}
	}

	@Override
	protected void onResume() {
		timeLabelDataSource.open();
		ruleDataSource.open();
		super.onResume();
	}

	@Override
	protected void onPause() {
		timeLabelDataSource.close();
		ruleDataSource.close();
		super.onPause();
	}

	private void setCurrentDateTimeOnView() {
		final Calendar c = Calendar.getInstance();
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);
		int year = c.get(Calendar.YEAR);
		int monthOfYear = c.get(Calendar.MONTH);
		int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);

		setFromDate(monthOfYear, dayOfMonth, year);
		setToDate(monthOfYear, dayOfMonth, year);
		setFromTime(hour, minute);
		setToTime(hour + 1, minute);
	}

	private void setToTime(int hour, int minute) {
		setTime(toTime, hour, minute);
	}

	private void setFromTime(int hour, int minute) {
		setTime(fromTime, hour, minute);
	}

	private void setTime(TextView timeTextView, int hour, int minute) {
		String ampm = "AM";
		if (hour > 12) {
			ampm = "PM";
			hour -= 12;
		} else if (hour == 12) {
			ampm = "PM";
		} else if (hour == 0) {
			hour = 12;
		}
		timeTextView.setText(pad(hour) + ":" + pad(minute) + " " + ampm);
	}

	private void setToDate(int monthOfYear, int dayOfMonth, int year) {
		toDate.setText(pad(monthOfYear + 1) + "/" + pad(dayOfMonth) + "/" + pad(year));
	}

	private void setFromDate(int monthOfYear, int dayOfMonth, int year) {
		fromDate.setText(pad(monthOfYear + 1) + "/" + pad(dayOfMonth) + "/" + pad(year));
	}

	private static String pad(int c) {
		if (c >= 10)
			return String.valueOf(c);
		else
			return "0" + String.valueOf(c);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case FROM_TIME_DIALOG_ID:
			return new TimePickerDialog(this, fromTimePickerListener, getFromHour(), getFromMinute(), false);
		case FROM_DATE_DIALOG_ID:
			return new DatePickerDialog(this, fromDatePickerListener, getFromYear(), getFromMonth(), getFromDayOfMonth());
		case TO_TIME_DIALOG_ID:
			return new TimePickerDialog(this, toTimePickerListener, getToHour(), getToMinute(), false);
		case TO_DATE_DIALOG_ID:
			return new DatePickerDialog(this, toDatePickerListener, getToYear(), getToMonth(), getToDayOfMonth());
		case REPEAT_DIALOG:
			repeatDialog = new TimeRepeatDialog(this
					, repeatType, repeatDay, isMon, isTue, isWed, isThu, isFri, isSat, isSun
					, new TimeRepeatDialogListener() {
				@Override
				public void uncheckRepeatCheckbox() {
					fromDate.setEnabled(true);
					toDate.setEnabled(true);
					repeatCheckBox.setChecked(false);
				}

				@Override 
				public void setDialogData(String _repeatType, int _repeatDay 
						, boolean _isMon, boolean _isTue, boolean _isWed, boolean _isThu
						, boolean _isFri, boolean _isSat, boolean _isSun) {
					repeatType = _repeatType;
					repeatDay = _repeatDay;
					isMon = _isMon;
					isTue = _isTue;
					isWed = _isWed;
					isThu = _isThu;
					isFri = _isFri;
					isSat = _isSat;
					isSun = _isSun;
				}
			}); 
			return repeatDialog;
		}
		return null;
	}

	private int getFromHour() {
		String[] strArr = fromTime.getText().toString().split("[: ]");
		return get24Hour(Integer.valueOf(strArr[0]), strArr[2]);
	}

	private int getFromMinute() {
		String str = fromTime.getText().toString();
		return Integer.valueOf(str.split("[: ]")[1]);
	}

	private int getFromYear() {
		String str = fromDate.getText().toString();
		return Integer.valueOf(str.split("/")[2]);
	}

	private int getFromMonth() {
		String str = fromDate.getText().toString();
		return Integer.valueOf(str.split("/")[0]) - 1;
	}

	private int getFromDayOfMonth() {
		String str = fromDate.getText().toString();
		return Integer.valueOf(str.split("/")[1]);
	}

	private int getToHour() {
		String[] strArr = toTime.getText().toString().split("[: ]");
		return get24Hour(Integer.valueOf(strArr[0]), strArr[2]);
	}

	private int getToMinute() {
		String str = toTime.getText().toString();
		return Integer.valueOf(str.split("[: ]")[1]);
	}

	private int getToYear() {
		String str = toDate.getText().toString();
		return Integer.valueOf(str.split("/")[2]);
	}

	private int getToMonth() {
		String str = toDate.getText().toString();
		return Integer.valueOf(str.split("/")[0]) - 1;
	}

	private int getToDayOfMonth() {
		String str = toDate.getText().toString();
		return Integer.valueOf(str.split("/")[1]);
	}

	private int get24Hour(int hour, String ampm) {
		if (hour == 12 && ampm.equalsIgnoreCase("AM")) {
			return 0;
		} else if (hour == 12 && ampm.equalsIgnoreCase("PM")) {
			return hour;
		} else if (ampm.equalsIgnoreCase("PM")) {
			return hour + 12;
		} else {
			return hour;
		}
	}

	private TimePickerDialog.OnTimeSetListener fromTimePickerListener = new TimePickerDialog.OnTimeSetListener() {
		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			setFromTime(hourOfDay, minute);
		}
	};

	private TimePickerDialog.OnTimeSetListener toTimePickerListener = new TimePickerDialog.OnTimeSetListener() {
		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			setToTime(hourOfDay, minute);
		}
	};

	private DatePickerDialog.OnDateSetListener fromDatePickerListener = new DatePickerDialog.OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			setFromDate(monthOfYear, dayOfMonth, year);
		}
	};

	private DatePickerDialog.OnDateSetListener toDatePickerListener = new DatePickerDialog.OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			setToDate(monthOfYear, dayOfMonth, year);
		}
	};

	public void onAllDayCheckboxClicked(View view) {
		boolean checked = ((CheckBox) view).isChecked();
		if (checked) {
			fromTime.setEnabled(false);
			toTime.setEnabled(false);
		} else {
			fromTime.setEnabled(true);
			toTime.setEnabled(true);
		}
	}

	public void onRepeatCheckboxClicked(View view) {
		boolean checked = ((CheckBox) view).isChecked();
		if (checked) {
			fromDate.setEnabled(false);
			toDate.setEnabled(false);
			showDialog(REPEAT_DIALOG);
		} else {
			fromDate.setEnabled(true);
			toDate.setEnabled(true);
		}
	}

	public void onClickDoneButton(View view) {
		String labelName = labelEditText.getText().toString();
		boolean isAllDay = alldayCheckBox.isChecked();
		boolean isRepeat = repeatCheckBox.isChecked();
		String fromDate = this.fromDate.getText().toString();
		String fromTime = this.fromTime.getText().toString();
		String toDate = this.toDate.getText().toString();
		String toTime = this.toTime.getText().toString();

		String message = null;
		try {
			if (labelName == null || labelName.length() <= 0) {
				Tools.showAlertDialog(this, "Error", "Please enter label name.");
				return;
			} else {
				if (!isAllDay && !isRepeat) {
					if (fromDate == null || fromDate.length() <= 0 || toDate == null || toDate.length() <= 0
							|| fromTime == null || fromTime.length() <= 0 || toTime == null || toTime.length() <= 0) {
						Tools.showAlertDialog(this, "Error", "Please enter date and time.");
						return;
					} else if (!isValidTimeRange()) {
						Tools.showAlertDialog(this, "Error", "From date/time must be before To date/time.");
						return;
					} else {
						if (prevLabelName != null) {
							if (!prevLabelName.equals(labelName)) {
								ruleDataSource.updateTimeLabelName(prevLabelName, labelName);
							}
							int result = timeLabelDataSource.updateTimeLabel(prevLabelName, labelName, fromDate, fromTime, toDate, toTime, isAllDay, isRepeat, repeatType
									, repeatDay, isMon, isTue, isWed, isThu, isFri, isSat, isSun);
							if (result != 1) {
								Tools.showAlertDialog(this, "Error", "Error code = " + result);
								return;
							}
							message = "Time label updated.";
						} else {
							timeLabelDataSource.insertTimeRange(labelName, fromDate, fromTime, toDate, toTime);
							message = "Time label created.";
						}
					}
				} else if (isAllDay && !isRepeat) {
					if (fromDate == null || fromDate.length() <= 0 || toDate == null || toDate.length() <= 0) {
						Tools.showAlertDialog(this, "Error", "Please enter date.");
						return;
					} else if (!isValidTimeRange()) {
						Tools.showAlertDialog(this, "Error", "From date must be before To date.");
						return;
					} else {
						if (prevLabelName != null) {
							if (!prevLabelName.equals(labelName)) {
								ruleDataSource.updateTimeLabelName(prevLabelName, labelName);
							}
							int result = timeLabelDataSource.updateTimeLabel(prevLabelName, labelName, fromDate, fromTime, toDate, toTime, isAllDay, isRepeat, repeatType
									, repeatDay, isMon, isTue, isWed, isThu, isFri, isSat, isSun);
							if (result != 1) {
								Tools.showAlertDialog(this, "Error", "Error code = " + result);
								return;
							} else {
								message = "Time label updated.";
							}
						} else {
							timeLabelDataSource.insertAllDay(labelName, fromDate, toDate);
							message = "Time label created.";
						}
					}
				} else if (!isAllDay && isRepeat) {
					if (fromTime == null || fromTime.length() <= 0 || toTime == null || toTime.length() <= 0) {
						Tools.showAlertDialog(this, "Error", "Please enter time.");
						return;
					} else if (!isValidTimeRange()) {
						Tools.showAlertDialog(this, "Error", "From time must be before To time.");
						return;
					} else {
						if (prevLabelName != null) {
							if (!prevLabelName.equals(labelName)) {
								ruleDataSource.updateTimeLabelName(prevLabelName, labelName);
							}
							int result = timeLabelDataSource.updateTimeLabel(prevLabelName, labelName, fromDate, fromTime, toDate, toTime, isAllDay, isRepeat, repeatType
									, repeatDay, isMon, isTue, isWed, isThu, isFri, isSat, isSun);
							if (result != 1) {
								Tools.showAlertDialog(this, "Error", "Error code = " + result);
								return;
							} else {
								message = "Time label updated.";
							}
						} else {
							if (repeatType.equalsIgnoreCase(TimeLabel.REPEAT_TYPE_WEEKLY)) {
								timeLabelDataSource.insertRepeatWeekly(labelName, fromTime, toTime, isMon, isTue, isWed, isThu, isFri, isSat, isSun);
							} else if (repeatType.equalsIgnoreCase(TimeLabel.REPEAT_TYPE_MONTHLY)) {
								timeLabelDataSource.insertRepeatMonthly(labelName, fromTime, toTime, repeatDay);
							} else {
								Tools.showAlertDialog(this, "Error", "Unknown repeat type: " + repeatType);
								return;
							}
							message = "Time label created.";
						}
					}
				} else if (isAllDay && isRepeat) {
					if (prevLabelName != null) {
						if (!prevLabelName.equals(labelName)) {
							ruleDataSource.updateTimeLabelName(prevLabelName, labelName);
						}
						int result = timeLabelDataSource.updateTimeLabel(prevLabelName, labelName, fromDate, fromTime, toDate, toTime, isAllDay, isRepeat, repeatType
								, repeatDay, isMon, isTue, isWed, isThu, isFri, isSat, isSun);
						if (result != 1) {
							Tools.showAlertDialog(this, "Error", "Error code = " + result);
							return;
						} else {
							message = "Time label updated.";
						}
					} else {
						if (repeatType.equalsIgnoreCase(TimeLabel.REPEAT_TYPE_WEEKLY)) {
							timeLabelDataSource.insertAllDayRepeatWeekly(labelName, isMon, isTue, isWed, isThu, isFri, isSat, isSun);
						} else if (repeatType.equalsIgnoreCase(TimeLabel.REPEAT_TYPE_MONTHLY)) {
							timeLabelDataSource.insertAllDayRepeatMonthly(labelName, repeatDay);
						} else {
							Tools.showAlertDialog(this, "Error", "Unknown repeat type: " + repeatType);
							return;
						}
						message = "Time label created.";
					}
				}				
			}
		} catch (SQLiteConstraintException e) {
			if (isSetupLabel) {
				int result = timeLabelDataSource.updateTimeLabel(labelName, labelName, fromDate, fromTime, toDate, toTime, isAllDay, isRepeat, repeatType
						, repeatDay, isMon, isTue, isWed, isThu, isFri, isSat, isSun);
				if (result != 1) {
					Tools.showAlertDialog(this, "Error", "Error code = " + result);
					return;
				} else {
					message = "Time label updated.";
				}
			} else {
				Tools.showAlertDialog(this, "Error", "Label name already exists.");
				return;
			}
		}

		Intent data = new Intent();
		data.putExtra(Const.BUNDLE_KEY_LABEL_NAME, labelName);
		data.putExtra(Const.BUNDLE_KEY_LABEL_TYPE, Const.LABEL_TYPE_TIME);
		setResult(RESULT_OK, data);

		Tools.showAlertDialog(this, "Success", message, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
		
		SyncService.startSyncService(this);
	}
	
	private boolean isValidTimeRange() {
		boolean isAllDay = alldayCheckBox.isChecked();
		boolean isRepeat = repeatCheckBox.isChecked();
		String fromDate = this.fromDate.getText().toString();
		String fromTime = this.fromTime.getText().toString();
		String toDate = this.toDate.getText().toString();
		String toTime = this.toTime.getText().toString();

		DateTimeFormatter fmt = DateTimeFormat.forPattern("MM/dd/yyyy hh:mm aa");

		DateTime start;
		DateTime end;
		if (!isAllDay && !isRepeat) {
			start = fmt.parseDateTime(fromDate + " " + fromTime);
			end = fmt.parseDateTime(toDate + " " + toTime);
		} else if (isAllDay && !isRepeat) {
			start = fmt.parseDateTime(fromDate + " 12:00 AM");
			end = fmt.parseDateTime(toDate + " 12:00 AM");
		} else if (!isAllDay && isRepeat) {
			start = fmt.parseDateTime("12/23/2013 " + fromTime);
			end = fmt.parseDateTime("12/23/2013 " + toTime);
		} else {
			return true;
		}
		if (start.isBefore(end) || start.isEqual(end)) {
			return true;
		} else { 
			return false;
		}
	}

	public void onClickCancelButton(View view) {
		setResult(RESULT_CANCELED);
		finish();
	}

	public void onClickFromDateEditText(View view) {
		showDialog(FROM_DATE_DIALOG_ID);
	}

	public void onClickToDateEditText(View view) {
		showDialog(TO_DATE_DIALOG_ID);
	}

	public void onClickFromTimeEditText(View view) {
		showDialog(FROM_TIME_DIALOG_ID);
	}

	public void onClickToTimeEditText(View view) {
		showDialog(TO_TIME_DIALOG_ID);
	}
}
