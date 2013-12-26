package edu.ucla.nesl.rulemanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.MultiKeyMap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Toast;
import edu.ucla.nesl.rulemanager.data.RuleGridElement;
import edu.ucla.nesl.rulemanager.db.model.Rule;

public class Tools {

	public static void showAlertDialog(Context context, String title, String message) {
		showAlertDialog(context, title, message, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
	}

	public static void showAlertDialog(Context context, String title, String message, DialogInterface.OnClickListener listener) {
		AlertDialog dialog = new AlertDialog.Builder(context).create();
		if (title != null) 
			dialog.setTitle(title);
		if (message != null)
			dialog.setMessage(message);
		dialog.setButton("OK", listener);
		dialog.setCancelable(false);
		dialog.show();
	}

	public static void showMessage(Context context, String message) {
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}

	public static String[] getConsumerNames() {
		return new String[] { "researchers", "acquaintances", "family", "friends" };
	}

	public static String[] getSensorNames() {
		return new String[] { "location", "audio", "ECG", "skin conductance", "respiration" };
	}

	public static class TableDataResult {
		public Map<String, MultiKeyMap> tableData;
		public List<Integer> conflictRuleIDs; 
	}

	public static TableDataResult prepareTableData(
			List<String> sensorNames, List<String> timeLabels,
			List<String> locationLabels, List<Rule> rules) {

		TableDataResult ret = new TableDataResult();

		Map<String, MultiKeyMap> tableData = new HashMap<String, MultiKeyMap>();

		for (String sensor : sensorNames) {
			MultiKeyMap mkMap = new MultiKeyMap();
			tableData.put(sensor, mkMap);
		}

		for (Rule rule : rules) {
			String timeLabel = rule.getTimeLabel();
			String locationLabel = rule.getLocationLabel();
			String sensor = rule.getData();
			if (sensor.equalsIgnoreCase(Const.ALL)) {
				for (MultiKeyMap mkMap : tableData.values()) {
					List<Integer> conflictRuleIDs = updateMkMap(mkMap, timeLabels, locationLabels, timeLabel, locationLabel, rule);
					if (conflictRuleIDs != null) {
						ret.conflictRuleIDs = conflictRuleIDs;
						return ret;
					}
				}
			} else {
				MultiKeyMap mkMap = tableData.get(sensor);
				List<Integer> conflictRuleIDs = updateMkMap(mkMap, timeLabels, locationLabels, timeLabel, locationLabel, rule);
				if (conflictRuleIDs != null) {
					ret.conflictRuleIDs = conflictRuleIDs;
					return ret;
				}
			}
		}

		ret.tableData = tableData;
		ret.conflictRuleIDs = null;

		return ret;
	}

	private static List<Integer> updateMkMap(MultiKeyMap mkMap, List<String> timeLabels, List<String> locationLabels, String timeLabel, String locationLabel, Rule rule) {
		if (timeLabel == null && locationLabel == null) {
			for (String time : timeLabels) {
				for (String location : locationLabels) {
					List<Integer> conflictRuleIDs = updateMkMapForRule(mkMap, time, location, rule);
					if (conflictRuleIDs != null) {
						return conflictRuleIDs;
					}
				}
			}
		} else if (timeLabel == null && locationLabel != null) {
			for (String time : timeLabels) {
				List<Integer> conflictRuleIDs = updateMkMapForRule(mkMap, time, locationLabel, rule);
				if (conflictRuleIDs != null) {
					return conflictRuleIDs;
				}
			}
		} else if (timeLabel != null && locationLabel == null) {
			for (String location : locationLabels) {
				List<Integer> conflictRuleIDs = updateMkMapForRule(mkMap, timeLabel, location, rule);
				if (conflictRuleIDs != null) {
					return conflictRuleIDs;
				}
			}
		} else if (timeLabel != null && locationLabel != null) {
			List<Integer> conflictRuleIDs = updateMkMapForRule(mkMap, timeLabel, locationLabel, rule);
			if (conflictRuleIDs != null) {
				return conflictRuleIDs;
			}
		}
		return null;
	}

	private static List<Integer> updateMkMapForRule(MultiKeyMap mkMap, String timeLabel, String locationLabel, Rule rule) {
		RuleGridElement elem = (RuleGridElement)mkMap.get(timeLabel, locationLabel);
		if (elem == null) {
			elem = new RuleGridElement();
		}
		if (rule.getAction().equalsIgnoreCase(Const.SHARE)) {
			if (elem.deniedList.size() > 0 && elem.deniedList.contains(Const.EVERYONE)) {
				int id = elem.deniedRuleIDs.get(elem.deniedList.indexOf(Const.EVERYONE));
				List<Integer> ids = new ArrayList<Integer>();
				ids.add(id);
				return ids;
			} else if (elem.deniedList.size() > 0 && elem.deniedList.contains(rule.getConsumer())) {
				int id = elem.deniedRuleIDs.get(elem.deniedList.indexOf(rule.getConsumer()));
				List<Integer> ids = new ArrayList<Integer>();
				ids.add(id);
				return ids;
			} else if (elem.deniedList.size() > 0 && rule.getConsumer().equalsIgnoreCase(Const.EVERYONE)) {
				return elem.deniedRuleIDs;
			}
			if (!elem.allowedList.contains(Const.EVERYONE) && !elem.allowedList.contains(rule.getConsumer())) {
				elem.addAllowed(rule.getConsumer(), rule.getId());
			}
		} else if (rule.getAction().equalsIgnoreCase(Const.NOT_SHARE)) {
			if (elem.allowedList.size() > 0 && elem.allowedList.contains(Const.EVERYONE)) {
				int id = elem.allowedRuleIDs.get(elem.allowedList.indexOf(Const.EVERYONE));
				List<Integer> ids = new ArrayList<Integer>();
				ids.add(id);
				return ids;
			} else if (elem.allowedList.size() > 0 && elem.allowedList.contains(rule.getConsumer())) {
				int id = elem.allowedRuleIDs.get(elem.allowedList.indexOf(rule.getConsumer()));
				List<Integer> ids = new ArrayList<Integer>();
				ids.add(id);
				return ids;
			} else if (elem.allowedList.size() > 0 && rule.getConsumer().equalsIgnoreCase(Const.EVERYONE)) {
				return elem.allowedRuleIDs;
			}
			if (!elem.deniedList.contains(Const.EVERYONE) && !elem.deniedList.contains(rule.getConsumer())) {
				elem.addDenied(rule.getConsumer(), rule.getId());
			}
		} else {
			throw new UnsupportedOperationException("Invalid rule action: " + rule.getAction());
		}
		mkMap.put(timeLabel, locationLabel, elem);
		return null;
	}
}
