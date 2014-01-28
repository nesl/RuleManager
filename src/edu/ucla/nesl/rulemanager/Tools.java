package edu.ucla.nesl.rulemanager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.map.MultiKeyMap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;
import edu.ucla.nesl.rulemanager.data.RuleGridElement;
import edu.ucla.nesl.rulemanager.db.model.LocationLabel;
import edu.ucla.nesl.rulemanager.db.model.Rule;
import edu.ucla.nesl.rulemanager.db.model.TimeLabel;

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

	public static void showAlertDialog(Context context, String title, String message, DialogInterface.OnClickListener okListener, DialogInterface.OnClickListener cancelListener) {
		AlertDialog dialog = new AlertDialog.Builder(context).create();
		if (title != null) 
			dialog.setTitle(title);
		if (message != null)
			dialog.setMessage(message);
		dialog.setButton("OK", okListener);
		dialog.setButton2("CANCEL", cancelListener);
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
		public Map<Integer, Set<Integer>> conflictMap; 
	}

	public static TableDataResult prepareTableData(
			List<String> sensorNames, List<String> timeLabels,
			List<String> locationLabels, List<Rule> rules,
			List<TimeLabel> timeLabelObjs, List<LocationLabel> locationLabelObjs) {

		TableDataResult ret = new TableDataResult();

		Map<String, MultiKeyMap> tableData = new HashMap<String, MultiKeyMap>();
		Map<Integer, Set<Integer>> conflictMap = new HashMap<Integer, Set<Integer>>();

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
					updateMkMap(mkMap, conflictMap, timeLabels, locationLabels, timeLabel, locationLabel, rule, timeLabelObjs, locationLabelObjs);
				}
			} else {
				MultiKeyMap mkMap = tableData.get(sensor);
				updateMkMap(mkMap, conflictMap, timeLabels, locationLabels, timeLabel, locationLabel, rule, timeLabelObjs, locationLabelObjs);
			}
		}

		ret.tableData = tableData;
		ret.conflictMap = conflictMap;

		return ret;
	}

	private static void updateMkMap(MultiKeyMap mkMap, Map<Integer, Set<Integer>> conflictMap, List<String> timeLabels, List<String> locationLabels, String timeLabel, String locationLabel, Rule rule, List<TimeLabel> timeLabelObjs, List<LocationLabel> locationLabelObjs) {

		if (timeLabel == null && locationLabel == null) {
			for (String time : timeLabels) {
				for (String location : locationLabels) {
					updateMkMapForRule(mkMap, conflictMap, time, location, rule);
				}
			}			
		} else if (timeLabel == null && locationLabel != null) {
			for (String time : timeLabels) {
				updateMkMapForRule(mkMap, conflictMap, time, locationLabel, rule);
			}

			// update for overlapping labels
			LocationLabel curLabel = null;
			for (LocationLabel loc : locationLabelObjs) {
				if (loc.getLabelName().equals(locationLabel)) {
					curLabel = loc;
				}
			}
			for (LocationLabel loc : locationLabelObjs) {
				if (loc == curLabel) {
					continue;
				}
				switch (curLabel.checkOverlap(loc)) {
				case LocationLabel.NON_OVERLAP:
					continue;
				case LocationLabel.EXACTLY_SAME:
				case LocationLabel.SUPERSET:
					for (String time : timeLabels) {
						updateMkMapForRule(mkMap, conflictMap, time, loc.getLabelName(), rule);
					}
					break;
				case LocationLabel.PARTIAL_OVERLAP:
				case LocationLabel.SUBSET:
					for (String time : timeLabels) {
						updateMkMapPartialForRule(mkMap, conflictMap, time, loc.getLabelName(), rule);
					}
					break;
				}
			}
		} else if (timeLabel != null && locationLabel == null) {
			for (String location : locationLabels) {
				updateMkMapForRule(mkMap, conflictMap, timeLabel, location, rule);
			}

			// update for overlapping labels
			TimeLabel curLabel = null;
			for (TimeLabel time : timeLabelObjs) {
				if (time.getLabelName().equals(timeLabel)) {
					curLabel = time;
				}
			}
			for (TimeLabel time : timeLabelObjs) {
				if (time == curLabel) {
					continue;
				}
				switch (curLabel.checkOverlap(time)) {
				case TimeLabel.NON_OVERLAP:
					continue;
				case TimeLabel.EXACTLY_SAME:
				case TimeLabel.SUPERSET:
					for (String loc : locationLabels) {
						updateMkMapForRule(mkMap, conflictMap, time.getLabelName(), loc, rule);
					}
					break;
				case TimeLabel.PARTIAL_OVERLAP:
				case TimeLabel.SUBSET:
					for (String loc : locationLabels) {
						updateMkMapPartialForRule(mkMap, conflictMap, time.getLabelName(), loc, rule);
					}
					break;
				}
			}
		} else if (timeLabel != null && locationLabel != null) {
			updateMkMapForRule(mkMap, conflictMap, timeLabel, locationLabel, rule);

			// update for overlapping labels
			TimeLabel curTimeLabel = null;
			for (TimeLabel time : timeLabelObjs) {
				if (time.getLabelName().equals(timeLabel)) {
					curTimeLabel = time;
				}
			}
			LocationLabel curLocationLabel = null;
			for (LocationLabel loc : locationLabelObjs) {
				if (loc.getLabelName().equals(locationLabel)) {
					curLocationLabel = loc;
				}
			}

			for (LocationLabel loc : locationLabelObjs) {
				for (TimeLabel time : timeLabelObjs) {
					if (curTimeLabel == time && curLocationLabel == loc) {
						continue;
					} else if (curTimeLabel == time && curLocationLabel != loc) {
						switch (curLocationLabel.checkOverlap(loc)) {
						case LocationLabel.NON_OVERLAP:
							continue;
						case LocationLabel.EXACTLY_SAME:
						case LocationLabel.SUPERSET:
							updateMkMapForRule(mkMap, conflictMap, time.getLabelName(), loc.getLabelName(), rule);
							break;
						case LocationLabel.PARTIAL_OVERLAP:
						case LocationLabel.SUBSET:
							updateMkMapPartialForRule(mkMap, conflictMap, time.getLabelName(), loc.getLabelName(), rule);
							break;
						}
					} else if (curTimeLabel != time && curLocationLabel == loc) {
						switch (curTimeLabel.checkOverlap(time)) {
						case TimeLabel.NON_OVERLAP:
							continue;
						case TimeLabel.EXACTLY_SAME:
						case TimeLabel.SUPERSET:
							updateMkMapForRule(mkMap, conflictMap, time.getLabelName(), loc.getLabelName(), rule);
							break;
						case TimeLabel.PARTIAL_OVERLAP:
						case TimeLabel.SUBSET:
							updateMkMapPartialForRule(mkMap, conflictMap, time.getLabelName(), loc.getLabelName(), rule);
							break;
						}
					} else {  // if (curTimeLabel != time && curLocationLabel != loc)
						int timeOverlap = curTimeLabel.checkOverlap(time);
						int locationOverlap = curLocationLabel.checkOverlap(loc);

						if (timeOverlap != TimeLabel.NON_OVERLAP && locationOverlap != LocationLabel.NON_OVERLAP) {
							if ( (timeOverlap == TimeLabel.EXACTLY_SAME || timeOverlap == TimeLabel.SUPERSET)
									&& (locationOverlap == LocationLabel.EXACTLY_SAME || locationOverlap == LocationLabel.SUPERSET) ) {
								updateMkMapForRule(mkMap, conflictMap, time.getLabelName(), loc.getLabelName(), rule);
							} else {
								updateMkMapPartialForRule(mkMap, conflictMap, time.getLabelName(), loc.getLabelName(), rule);
							}
						} else {
							continue;
						}
					}
				}
			}
		}
	}

	private static void updateMkMapPartialForRule(MultiKeyMap mkMap, Map<Integer, Set<Integer>> conflictMap, String timeLabel, String locationLabel, Rule rule) {

		RuleGridElement elem = (RuleGridElement)mkMap.get(timeLabel, locationLabel);
		if (elem == null) {
			elem = new RuleGridElement();
		}

		if (rule.getAction().equalsIgnoreCase(Const.SHARE)) {
			elem.addPartialAllowed(rule.getConsumer());
		} else if (rule.getAction().equalsIgnoreCase(Const.NOT_SHARE)) {
			elem.addPartialDenied(rule.getConsumer());
		} else {
			throw new UnsupportedOperationException("Invalid rule action: " + rule.getAction());
		}
		
		mkMap.put(timeLabel, locationLabel, elem);
	}

	private static void updateMkMapForRule(MultiKeyMap mkMap, Map<Integer, Set<Integer>> conflictMap, String timeLabel, String locationLabel, Rule rule) {

		RuleGridElement elem = (RuleGridElement)mkMap.get(timeLabel, locationLabel);
		if (elem == null) {
			elem = new RuleGridElement();
		}

		if (rule.getAction().equalsIgnoreCase(Const.SHARE)) {

			if (elem.deniedList.size() > 0) {
				if (rule.getConsumer().equalsIgnoreCase(Const.EVERYONE)) {
					// conflict with every denied rules
					Set<Integer> set = conflictMap.get(rule.getId());
					if (set == null) {
						set = new HashSet<Integer>();
					}
					set.addAll(elem.deniedRuleIDs);
					conflictMap.put(rule.getId(), set);
				} else {					
					for (int i = 0; i < elem.deniedList.size(); i++) {
						String denied = elem.deniedList.get(i);
						int id = elem.deniedRuleIDs.get(i);
						if (denied.equalsIgnoreCase(Const.EVERYONE) || denied.equalsIgnoreCase(rule.getConsumer())) {
							Set<Integer> set = conflictMap.get(rule.getId());
							if (set == null) {
								set = new HashSet<Integer>();
							}
							set.add(id);
							conflictMap.put(rule.getId(), set);
						}
					}
				}
			}

			elem.addAllowed(rule.getConsumer(), rule.getId());

		} else if (rule.getAction().equalsIgnoreCase(Const.NOT_SHARE)) {

			if (elem.allowedList.size() > 0) {
				if (rule.getConsumer().equalsIgnoreCase(Const.EVERYONE)) {
					// conflict with every allow rules
					Set<Integer> set = conflictMap.get(rule.getId());
					if (set == null) {
						set = new HashSet<Integer>();
					}
					set.addAll(elem.allowedRuleIDs);
					conflictMap.put(rule.getId(), set);
				} else {
					for (int i = 0; i < elem.allowedList.size(); i++) {
						String allowed = elem.allowedList.get(i);
						int id = elem.allowedRuleIDs.get(i);
						if (allowed.equalsIgnoreCase(Const.EVERYONE) || allowed.equalsIgnoreCase(rule.getConsumer())) {
							Set<Integer> set = conflictMap.get(rule.getId());
							if (set == null) {
								set = new HashSet<Integer>();
							}
							set.add(id);
							conflictMap.put(rule.getId(), set);
						}
					}
				}
			}

			elem.addDenied(rule.getConsumer(), rule.getId());

		} else {
			throw new UnsupportedOperationException("Invalid rule action: " + rule.getAction());
		}

		mkMap.put(timeLabel, locationLabel, elem);
	}

	public static String makeValidMacroName(String name) {
		return name.replace(" ", "_");
	}
	
	public static String makeMacroRefer(String macro) {
		return "$(" + makeValidMacroName(macro) + ")";
	}

	public static String joinString(List<String> strlist, String joiner) {
		String ret = "";
		for (String str : strlist) {
			ret += str + joiner;
		}
		ret = ret.substring(0, ret.length() - joiner.length());
		return ret;
	}
	
	public static void startSyncService(Context context, int signalType) {
		SharedPreferences settings = context.getSharedPreferences(Const.PREFS_NAME, 0);
		String serverip = settings.getString(Const.PREFS_SERVER_IP, null);
		String username = settings.getString(Const.PREFS_USERNAME, null);
		String password = settings.getString(Const.PREFS_PASSWORD, null);

		if (serverip != null && username != null && password != null) {
			// Start upload service
			Intent i = new Intent(context, SyncService.class);
			i.putExtra(Const.SIGNAL_TYPE, signalType);
			i.putExtra(Const.PREFS_SERVER_IP, serverip);
			i.putExtra(Const.PREFS_USERNAME, username);
			i.putExtra(Const.PREFS_PASSWORD, password);
			context.startService(i); 
		}
	}
}
