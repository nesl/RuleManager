package edu.ucla.nesl.rulemanager.uielement;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import edu.ucla.nesl.rulemanager.R;
import edu.ucla.nesl.rulemanager.Tools;
import edu.ucla.nesl.rulemanager.db.SQLiteHelper;
import edu.ucla.nesl.rulemanager.db.model.TimeLabel;

public class TimeRepeatDialog extends Dialog {

	private Context context;
	private Dialog dialog = this;

	private Spinner repeatsSpinner;

	private TimeRepeatDialogListener listener;

	String repeatType;
	int repeatDay;
	boolean isMon;
	boolean isTue;
	boolean isWed;
	boolean isThu;
	boolean isFri;
	boolean isSat;
	boolean isSun;

	public static interface TimeRepeatDialogListener {
		public void uncheckRepeatCheckbox();

		void setDialogData(String repeatType, int repeatDay, boolean isMon,
				boolean isTue, boolean isWed, boolean isThu, boolean isFri,
				boolean isSat, boolean isSun);
	}

	public TimeRepeatDialog(Context context
			, String repeatType, int repeatDay
			, boolean isMon, boolean isTue, boolean isWed, boolean isThu, boolean isFri, boolean isSat, boolean isSun
			, TimeRepeatDialogListener listener) {
		super(context);
		this.context = context;
		this.listener = listener;

		this.repeatType = repeatType;
		this.repeatDay = repeatDay;
		this.isMon = isMon;
		this.isTue = isTue;
		this.isWed = isWed;
		this.isThu = isThu;
		this.isFri = isFri;
		this.isSat = isSat;
		this.isSun = isSun;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_repeat);

		setCancelable(false);
		setTitle("Repeat");

		Button doneButton = (Button) findViewById(R.id.repeat_done);
		doneButton.setOnClickListener(onClickDoneButtonListener);

		Button cancelButton = (Button) findViewById(R.id.repeat_cancel);
		cancelButton.setOnClickListener(onClickCancelButtonListener);

		repeatsSpinner = (Spinner) findViewById(R.id.repeats_spinner);
		ArrayAdapter<CharSequence> repeatsSpinnerAdapter = ArrayAdapter.createFromResource(context, R.array.repeat_types, android.R.layout.simple_spinner_item);
		repeatsSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		repeatsSpinner.setAdapter(repeatsSpinnerAdapter);
		repeatsSpinner.setOnItemSelectedListener(repeatsSpinnerListener);

		if (repeatType != null) {
			if (repeatType.equalsIgnoreCase(TimeLabel.REPEAT_TYPE_WEEKLY)) {
				if (isMon && isTue && isWed && isThu && isFri && !isSat && !isSun) {
					repeatsSpinner.setSelection(1);
					repeatsSpinnerListener.onItemSelected(null, null, 1, 0);
				} else if (!isMon && !isTue && !isWed && !isThu && !isFri && isSat && isSun) {
					repeatsSpinner.setSelection(2);
					repeatsSpinnerListener.onItemSelected(null, null, 2, 0);
				} else if (isMon && !isTue && isWed && !isThu && isFri && !isSat && !isSun) {
					repeatsSpinner.setSelection(3);
					repeatsSpinnerListener.onItemSelected(null, null, 3, 0);
				} else if (!isMon && isTue && !isWed && isThu && !isFri && !isSat && !isSun) {
					repeatsSpinner.setSelection(4);
					repeatsSpinnerListener.onItemSelected(null, null, 4, 0);
				} else {
					repeatsSpinner.setSelection(0);
					((CheckBox)findViewById(R.id.checkbox_monday)).setChecked(isMon);
					((CheckBox)findViewById(R.id.checkbox_tuesday)).setChecked(isTue);
					((CheckBox)findViewById(R.id.checkbox_wednesday)).setChecked(isWed);
					((CheckBox)findViewById(R.id.checkbox_thursday)).setChecked(isThu);
					((CheckBox)findViewById(R.id.checkbox_friday)).setChecked(isFri);
					((CheckBox)findViewById(R.id.checkbox_saturday)).setChecked(isSat);
					((CheckBox)findViewById(R.id.checkbox_sunday)).setChecked(isSun);
					repeatsSpinnerListener.onItemSelected(null, null, 0, 0);
				}
			} else if (repeatType.equalsIgnoreCase(TimeLabel.REPEAT_TYPE_MONTHLY)) {
				repeatsSpinner.setSelection(5);
				repeatsSpinnerListener.onItemSelected(null, null, 5, 0);
				((EditText)findViewById(R.id.day_edit_text)).setText(String.valueOf(repeatDay));
			} else {
				Tools.showAlertDialog(context, "Error", "Invalid repeatType: " + repeatType);
			}
		}
	}

	private OnItemSelectedListener repeatsSpinnerListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			if (pos == 0) { // Weekly
				setRepeatOnDayVisibility(View.INVISIBLE);
				setWeekdayVisibility(View.VISIBLE);
			} else if (pos == 1) { // Every weekday
				setRepeatOnDayVisibility(View.INVISIBLE);
				setWeekdayVisibility(View.INVISIBLE);
			} else if (pos == 2) { // Every weekend
				setRepeatOnDayVisibility(View.INVISIBLE);
				setWeekdayVisibility(View.INVISIBLE);
			} else if (pos == 3) { // M, W, F
				setRepeatOnDayVisibility(View.INVISIBLE);
				setWeekdayVisibility(View.INVISIBLE);
			} else if (pos == 4) { // Tu, Th
				setRepeatOnDayVisibility(View.INVISIBLE);
				setWeekdayVisibility(View.INVISIBLE);
			} else if (pos == 5) { // Monthly
				setWeekdayVisibility(View.INVISIBLE);
				setRepeatOnDayVisibility(View.VISIBLE);
			}
		}

		private void setWeekdayVisibility(int visibility) {
			findViewById(R.id.repeat_on).setVisibility(visibility);
			findViewById(R.id.weekday_checkboxes_1).setVisibility(visibility);
			findViewById(R.id.weekday_checkboxes_2).setVisibility(visibility);
		}

		private void setRepeatOnDayVisibility(int visibility) {
			findViewById(R.id.repeat_on).setVisibility(visibility);
			findViewById(R.id.day_text).setVisibility(visibility);
			findViewById(R.id.day_edit_text).setVisibility(visibility);
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {

		}

	};

	private android.view.View.OnClickListener onClickDoneButtonListener = new android.view.View.OnClickListener() {
		@Override
		public void onClick(View v) {
			String repeatType = null;
			int repeatDay = 0;
			boolean isMon, isTue, isWed, isThu, isFri, isSat, isSun;
			int repeatsSpinnerPos = repeatsSpinner.getSelectedItemPosition();
			switch (repeatsSpinnerPos) {
			case 0: 
				repeatType = TimeLabel.REPEAT_TYPE_WEEKLY;
				isMon = ((CheckBox)findViewById(R.id.checkbox_monday)).isChecked();
				isTue = ((CheckBox)findViewById(R.id.checkbox_tuesday)).isChecked();
				isWed = ((CheckBox)findViewById(R.id.checkbox_wednesday)).isChecked();
				isThu = ((CheckBox)findViewById(R.id.checkbox_thursday)).isChecked();
				isFri = ((CheckBox)findViewById(R.id.checkbox_friday)).isChecked();
				isSat = ((CheckBox)findViewById(R.id.checkbox_saturday)).isChecked();
				isSun = ((CheckBox)findViewById(R.id.checkbox_sunday)).isChecked();
				if (!isMon && !isTue && !isWed && !isThu && !isFri && !isSat && !isSun) {
					Tools.showAlertDialog(context, "Error", "Please check at least one weekday.");
					return;
				}
				break;
			case 1:
				repeatType = TimeLabel.REPEAT_TYPE_WEEKLY;
				isMon = isTue = isWed = isThu = isFri = true;
				isSat = isSun = false;
				break;
			case 2:
				repeatType = TimeLabel.REPEAT_TYPE_WEEKLY;
				isSat = isSun = true;
				isMon = isTue = isWed = isThu = isFri = false;
				break;
			case 3:
				repeatType = TimeLabel.REPEAT_TYPE_WEEKLY;
				isMon = isWed = isFri = true;
				isTue = isThu = isSat = isSun = false;
				break;
			case 4:
				repeatType = TimeLabel.REPEAT_TYPE_WEEKLY;
				isTue = isThu = true;
				isMon = isWed = isFri = isSat = isSun = false;
				break;
			case 5:
				repeatType = TimeLabel.REPEAT_TYPE_MONTHLY;
				EditText et = (EditText)findViewById(R.id.day_edit_text);
				String text = et.getText().toString();
				if (text == null || text.length() <= 0) {
					Tools.showAlertDialog(context, "Error", "Please enter repeat day.");
					return;
				} else {
					repeatDay = Integer.valueOf(et.getText().toString());
					if (repeatDay > 31) {
						Tools.showAlertDialog(context, "Error", "Please enter valid repeat day.");
						return;
					}
					isMon = isTue = isWed = isThu = isFri = isSat = isSun = false;
				}
				break;
			default:
				isMon = isTue = isWed = isThu = isFri = isSat = isSun = false;
			}
			listener.setDialogData(repeatType, repeatDay, isMon, isTue, isWed, isThu, isFri, isSat, isSun);
			dialog.dismiss();
		}
	};

	private android.view.View.OnClickListener onClickCancelButtonListener = new android.view.View.OnClickListener() {
		@Override
		public void onClick(View v) {
			listener.uncheckRepeatCheckbox();
			dialog.dismiss();
		}
	};
}
