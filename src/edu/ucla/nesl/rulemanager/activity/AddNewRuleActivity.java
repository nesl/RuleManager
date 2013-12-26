package edu.ucla.nesl.rulemanager.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import edu.ucla.nesl.rulemanager.Const;
import edu.ucla.nesl.rulemanager.R;
import edu.ucla.nesl.rulemanager.Tools;
import edu.ucla.nesl.rulemanager.db.LocationLabelDataSource;
import edu.ucla.nesl.rulemanager.db.RuleDataSource;
import edu.ucla.nesl.rulemanager.db.TimeLabelDataSource;
import edu.ucla.nesl.rulemanager.db.model.Rule;
import edu.ucla.nesl.rulemanager.uielement.MySpinner;

public class AddNewRuleActivity extends Activity {

	private LocationLabelDataSource locationLabelDataSource;
	private TimeLabelDataSource timeLabelDataSource;
	private RuleDataSource ruleDataSource;

	private Spinner actionSpinner;
	private Spinner sensorSpinner;
	private Spinner consumerSpinner;

	private MySpinner timeLabelSpinner;
	private MySpinner locationLabelSpinner;

	private CheckBox timeCheckBox;
	private CheckBox locationCheckBox;

	private ArrayAdapter<CharSequence> timeLabelAdapter;
	private ArrayAdapter<CharSequence> locationLabelAdapter;

	private String pendingLocationLabelName;
	private String pendingTimeLabelName;

	private String prevTimeLabel;
	private String prevLocationLabel;

	private Context context = this;

	private boolean isActivityResultCanceled1 = false; 
	private boolean isActivityResultCanceled2 = false;

	private Bundle startBundle;
	
	private ArrayAdapter<CharSequence> actionSpinnerAdapter;
	private ArrayAdapter<CharSequence> sensorSpinnerAdapter;
	private ArrayAdapter<CharSequence> consumerSpinnerAdapter;
	
	private int prevRuleId = 0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_new_rule);

		locationLabelDataSource = new LocationLabelDataSource(this);
		timeLabelDataSource = new TimeLabelDataSource(this);
		ruleDataSource = new RuleDataSource(this);

		actionSpinner = (Spinner) findViewById(R.id.rule_action_spinner);
		actionSpinnerAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, new String[] { Const.SHARE, Const.NOT_SHARE });
		actionSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		actionSpinner.setAdapter(actionSpinnerAdapter);

		sensorSpinner = (Spinner) findViewById(R.id.sensor_spinner);
		List<String> sensorNames = new ArrayList<String>();
		sensorNames.add(Const.ALL);
		sensorNames.addAll(Arrays.asList(Tools.getSensorNames()));
		sensorSpinnerAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, sensorNames);
		sensorSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sensorSpinner.setAdapter(sensorSpinnerAdapter);

		consumerSpinner = (Spinner) findViewById(R.id.consumer_spinner);
		List<String> consumerNames = new ArrayList<String>();
		consumerNames.add(Const.EVERYONE);
		consumerNames.addAll(Arrays.asList(Tools.getConsumerNames()));		
		consumerSpinnerAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, consumerNames);
		consumerSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		consumerSpinner.setAdapter(consumerSpinnerAdapter);

		timeLabelSpinner = (MySpinner) findViewById(R.id.time_label_spinner);
		timeLabelSpinner.setOnItemSelectedEvenIfUnchangedListener(timeLabelSpinnerListener);

		locationLabelSpinner = (MySpinner) findViewById(R.id.location_label_spinner);
		locationLabelSpinner.setOnItemSelectedEvenIfUnchangedListener(locationLabelSpinnerListener);

		// make spinner disabled.
		disableSpinner(timeLabelSpinner);
		disableSpinner(locationLabelSpinner);

		timeCheckBox = (CheckBox) findViewById(R.id.checkbox_time);
		locationCheckBox = (CheckBox) findViewById(R.id.checkbox_location);
		
		timeCheckBox.setOnCheckedChangeListener(timeCheckBoxListener);
		locationCheckBox.setOnCheckedChangeListener(locationCheckBoxListener);
		
		startBundle = getIntent().getExtras();
	}

	@Override
	protected void onResume() {

		locationLabelDataSource.open();
		timeLabelDataSource.open();
		ruleDataSource.open();

		List<String> timeLabels = timeLabelDataSource.getLabelNames();
		timeLabels.add(getString(R.string.add_new_label));
		timeLabelAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, timeLabels); 
		timeLabelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		timeLabelSpinner.setAdapter(timeLabelAdapter);

		if (prevTimeLabel != null) {
			int position = timeLabelAdapter.getPosition(prevTimeLabel);
			timeLabelSpinner.setSelection(position);
		}

		if (pendingTimeLabelName != null) {
			int position = timeLabelAdapter.getPosition(pendingTimeLabelName);
			timeLabelSpinner.setSelection(position);
			pendingTimeLabelName = null;
		}

		List<String> locationLabels = locationLabelDataSource.getLabelNames();
		locationLabels.add(getString(R.string.add_new_label));
		locationLabelAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, locationLabels); 
		locationLabelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		locationLabelSpinner.setAdapter(locationLabelAdapter);

		if (prevLocationLabel != null) {
			int position = locationLabelAdapter.getPosition(prevLocationLabel);
			locationLabelSpinner.setSelection(position);
		}

		if (pendingLocationLabelName != null) {
			int position = locationLabelAdapter.getPosition(pendingLocationLabelName);
			locationLabelSpinner.setSelection(position);
			pendingLocationLabelName = null;
		}

		if (startBundle != null) {
			prevRuleId = startBundle.getInt(Const.BUNDLE_KEY_ID);
			String action = startBundle.getString(Const.BUNDLE_KEY_ACTION);
			String sensor = startBundle.getString(Const.BUNDLE_KEY_SENSOR);
			String consumer = startBundle.getString(Const.BUNDLE_KEY_CONSUMER);
			String timeLabel = startBundle.getString(Const.BUNDLE_KEY_TIME_LABEL);
			String locationLabel = startBundle.getString(Const.BUNDLE_KEY_LOCATION_LABEL);
			actionSpinner.setSelection(actionSpinnerAdapter.getPosition(action));
			sensorSpinner.setSelection(sensorSpinnerAdapter.getPosition(sensor));
			consumerSpinner.setSelection(consumerSpinnerAdapter.getPosition(consumer));
			if (timeLabel != null) {
				timeLabelSpinner.setSelection(timeLabelAdapter.getPosition(timeLabel));
				timeCheckBox.setChecked(true);				
			}
			if (locationLabel != null) {
				locationLabelSpinner.setSelection(locationLabelAdapter.getPosition(locationLabel));
				locationCheckBox.setChecked(true);
			}
			startBundle = null;
		}

		prevTimeLabel = (String)timeLabelSpinner.getSelectedItem();
		prevLocationLabel = (String)locationLabelSpinner.getSelectedItem();

		super.onResume();
	}

	@Override
	protected void onPause() {
		locationLabelDataSource.close();
		timeLabelDataSource.close();
		ruleDataSource.close();
		super.onPause();
	}

	private void disableSpinner(Spinner spinner) {
		AlphaAnimation alpha = new AlphaAnimation(0.5F, 0.5F);
		alpha.setDuration(0);
		alpha.setFillAfter(true);
		spinner.startAnimation(alpha);
		spinner.setEnabled(false);
	}

	private void enableSpinner(Spinner spinner) {
		AlphaAnimation alpha = new AlphaAnimation(1.0F, 1.0F);
		alpha.setDuration(0);
		alpha.setFillAfter(true);
		spinner.startAnimation(alpha);
		spinner.setEnabled(true);
	}

	private OnItemSelectedListener timeLabelSpinnerListener = new OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			if (isActivityResultCanceled1) {
				isActivityResultCanceled1 = false;
				return;
			}

			int lastPos = timeLabelAdapter.getCount() - 1;
			if (pos == lastPos) {
				// start new time label activity
				Intent intent = new Intent(context, AddNewTimeLabelActivity.class);
				startActivityForResult(intent, Const.REQUEST_CODE_NEW_LABEL);
			} else {
				prevTimeLabel = (String)timeLabelSpinner.getSelectedItem();
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == Const.REQUEST_CODE_NEW_LABEL) {
			isActivityResultCanceled1 = true;
			isActivityResultCanceled2 = true;

			if (resultCode == RESULT_OK) {
				if (data != null) {
					Bundle bundle = data.getExtras();
					String labelType = bundle.getString(Const.BUNDLE_KEY_LABEL_TYPE);
					String labelName = bundle.getString(Const.BUNDLE_KEY_LABEL_NAME);
					if (labelType.equalsIgnoreCase(Const.LABEL_TYPE_LOCATION)) {
						pendingLocationLabelName = labelName;
						pendingTimeLabelName = null;
					} else if (labelType.equalsIgnoreCase(Const.LABEL_TYPE_TIME)) {
						pendingLocationLabelName = null;
						pendingTimeLabelName = labelName;
					} else {
						pendingLocationLabelName = null;
						pendingTimeLabelName = null;
					}
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private OnItemSelectedListener locationLabelSpinnerListener = new OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			if (isActivityResultCanceled2) {
				isActivityResultCanceled2 = false;
				return;
			}
			int lastPos = locationLabelAdapter.getCount() - 1;
			if (pos == lastPos) {
				// start new location label activity
				Intent intent = new Intent(context, AddNewLocationLabelActivity.class);
				startActivityForResult(intent, Const.REQUEST_CODE_NEW_LABEL);
			} else {
				prevLocationLabel = (String)locationLabelSpinner.getSelectedItem();
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
		}
	};

	private OnCheckedChangeListener timeCheckBoxListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if (isChecked) {
				enableSpinner(timeLabelSpinner);
				((TextView)findViewById(R.id.time_is)).setEnabled(true);
			} else {
				disableSpinner(timeLabelSpinner);
				((TextView)findViewById(R.id.time_is)).setEnabled(false);
			}
			updateWhenAndTextStatus();
		}
	};
	
	private OnCheckedChangeListener locationCheckBoxListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if (isChecked) {
				enableSpinner(locationLabelSpinner);
				((TextView)findViewById(R.id.location_is)).setEnabled(true);
			} else {
				disableSpinner(locationLabelSpinner);
				((TextView)findViewById(R.id.location_is)).setEnabled(false);
			}
			updateWhenAndTextStatus();
		}
	};
	
	private void updateWhenAndTextStatus() {
		TextView when = (TextView)findViewById(R.id.when);
		TextView and = (TextView)findViewById(R.id.and);
		boolean isTime = timeCheckBox.isChecked();
		boolean isLocation = locationCheckBox.isChecked();

		if (isTime || isLocation) {
			when.setEnabled(true);
		} else {
			when.setEnabled(false);
		}

		if (isTime && isLocation) {
			and.setEnabled(true);
		} else {
			and.setEnabled(false);
		}
	}

	public void onClickDoneButton(View view) {
		String action = (String)actionSpinner.getSelectedItem();
		String data = (String)sensorSpinner.getSelectedItem();
		String consumer = (String)consumerSpinner.getSelectedItem();
		
		String timeLabel = null;
		if (timeCheckBox.isChecked()) {
			timeLabel = (String)timeLabelSpinner.getSelectedItem();
			if (timeLabel.equalsIgnoreCase(getString(R.string.add_new_label))) {
				Tools.showAlertDialog(this, "Error", "Please select a time label.");
				return;
			}
		}
		
		String locationLabel = null;
		if (locationCheckBox.isChecked()) {
			locationLabel = (String)locationLabelSpinner.getSelectedItem();
			if (locationLabel.equalsIgnoreCase(getString(R.string.add_new_label))) {
				Tools.showAlertDialog(this, "Error", "Please select a location label.");
				return;
			}
		}
		
		String message;
		try {		
			if (prevRuleId > 0) {
				// Check rule conflict
				List<Rule> rules = ruleDataSource.getRules();
				Rule prevRule = null;
				for (Rule rule : rules) {
					if (rule.getId() == prevRuleId) {
						prevRule = rule;
						break;
					}
				}
				rules.remove(prevRule);

				prevRule.setAction(action);
				prevRule.setData(data);
				prevRule.setConsumer(consumer);
				prevRule.setTimeLabel(timeLabel);
				prevRule.setLocationLabel(locationLabel);
				
				rules.add(prevRule);

				if (isConflictRules(rules)) {
					return;
				}
				
				int result = ruleDataSource.update(prevRuleId, action, data, consumer, timeLabel, locationLabel);
				if (result != 1) {
					Tools.showAlertDialog(this, "Error", "Error code = " + result);
					return;
				} else {
					message = "The rule has been updated.";
				}
			} else if (prevRuleId == 0) {
				// Check rule conflict
				List<Rule> rules = ruleDataSource.getRules();
				rules.add(new Rule(action, data, consumer, timeLabel, locationLabel));

				if (isConflictRules(rules)) {
					return;
				}

				ruleDataSource.insert(action, data, consumer, timeLabel, locationLabel);
				message = "A new rule has been created.";
			} else {
				Tools.showAlertDialog(this, "Error", "Invalid prevRuleId: " + prevRuleId);
				return;
			}
		} catch (SQLiteConstraintException e) {
			Tools.showAlertDialog(this, "Error", "Duplicate rule already exists.");
			return;
		}
		
		Tools.showAlertDialog(this, "Success", message, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
	}

	private boolean isConflictRules(List<Rule> rules) {
		List<String> timeLabels = timeLabelDataSource.getLabelNamesWithOther();
		List<String> locationLabels = locationLabelDataSource.getLabelNamesWithOther();
		List<String> sensorNames = Arrays.asList(Tools.getSensorNames());
		
		Tools.TableDataResult tableDataResult = Tools.prepareTableData(sensorNames, timeLabels, locationLabels, rules);
		
		if (tableDataResult.conflictRuleIDs != null) {
			List<Rule> cRules = new ArrayList<Rule>();
			for (int id : tableDataResult.conflictRuleIDs) {
				Rule conflictRule = ruleDataSource.getARule(id);
				cRules.add(conflictRule);
			}
			
			String summaryText = "";
			for (Rule rule : cRules) {
				summaryText += "- " + rule.getSummaryText() + "\n";
			}
			
			if (cRules.size() <= 1) 
				Tools.showAlertDialog(this, "Error", "Your rule conflicts with the following rule:\n" + summaryText);
			else 
				Tools.showAlertDialog(this, "Error", "Your rule conflicts with the following rules:\n" + summaryText);
			return true;
		} 
		return false;
	}

	public void onClickCancelButton(View view) {
		finish();
	}
}
