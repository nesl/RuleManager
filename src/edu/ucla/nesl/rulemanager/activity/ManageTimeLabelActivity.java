package edu.ucla.nesl.rulemanager.activity;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import edu.ucla.nesl.rulemanager.Const;
import edu.ucla.nesl.rulemanager.R;
import edu.ucla.nesl.rulemanager.SyncService;
import edu.ucla.nesl.rulemanager.Tools;
import edu.ucla.nesl.rulemanager.db.RuleDataSource;
import edu.ucla.nesl.rulemanager.db.TimeLabelDataSource;
import edu.ucla.nesl.rulemanager.db.model.Rule;
import edu.ucla.nesl.rulemanager.db.model.TimeLabel;

public class ManageTimeLabelActivity extends Activity {

	private TimeLabelDataSource timeLabelDataSource;
	private RuleDataSource ruleDataSource;
	private List<TimeLabel> labels;

	private TimeLabelItemsAdapter timeLabelItemsAdapter;
	private ListView timeLabelListView;
	private Context context = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manage_time_labels);

		timeLabelDataSource = new TimeLabelDataSource(this);
		ruleDataSource = new RuleDataSource(this);

		timeLabelListView = (ListView) findViewById(R.id.time_labels_listview);
		timeLabelItemsAdapter = new TimeLabelItemsAdapter();
		timeLabelListView.setAdapter(timeLabelItemsAdapter);
		timeLabelListView.setOnItemClickListener(timeLabelItemOnClickListener);
		registerForContextMenu(timeLabelListView);
	}

	@Override
	protected void onResume() {
		timeLabelDataSource.open();
		labels = timeLabelDataSource.getTimeLabels(); 
		timeLabelItemsAdapter.notifyDataSetChanged();

		ruleDataSource.open();

		/*
		if (labels.size() == 2) {
			int checkOverlap = labels.get(0).checkOverlap(labels.get(1));
			Tools.showAlertDialog(this, "Test", "checkOverlap: " + checkOverlap);		
		}*/
		
		/*for (TimeLabel label : labels) {
			Log.i(Const.TAG, label.toJsonString());
		}*/
		
		super.onResume();
	}

	@Override
	protected void onPause() {
		timeLabelDataSource.close();
		ruleDataSource.close();
		super.onPause();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.time_labels_listview) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
			menu.setHeaderTitle(labels.get(info.position).getLabelName());
			String[] menuItems = getResources().getStringArray(R.array.label_context_menu);
			for (int i = 0; i < menuItems.length; i++) {
				menu.add(Menu.NONE, i, i, menuItems[i]);
			}
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		int menuItemIndex = item.getItemId();
		String[] menuItems = getResources().getStringArray(R.array.label_context_menu);
		String menuItemName = menuItems[menuItemIndex];
		TimeLabel selectedLabel = labels.get(info.position);

		if (menuItemName.equalsIgnoreCase("Edit")) {
			Intent intent = new Intent(this, TimeLabelActivity.class);
			Bundle bundle = new Bundle();
			bundle.putString(Const.BUNDLE_KEY_LABEL_NAME, selectedLabel.getLabelName());
			bundle.putString(Const.BUNDLE_KEY_FROM_DATE, selectedLabel.getFromDate());
			bundle.putString(Const.BUNDLE_KEY_FROM_TIME, selectedLabel.getFromTime());
			bundle.putString(Const.BUNDLE_KEY_TO_DATE, selectedLabel.getToDate());
			bundle.putString(Const.BUNDLE_KEY_TO_TIME, selectedLabel.getToTime());
			bundle.putBoolean(Const.BUNDLE_KEY_IS_ALL_DAY, selectedLabel.isAllDay());
			bundle.putBoolean(Const.BUNDLE_KEY_IS_REPEAT, selectedLabel.isRepeat());
			bundle.putString(Const.BUNDLE_KEY_REPEAT_TYPE, selectedLabel.getRepeatType());
			bundle.putInt(Const.BUNDLE_KEY_REPEAT_DAY, selectedLabel.getRepeatDay());
			bundle.putBoolean(Const.BUNDLE_KEY_IS_REPEAT_MON, selectedLabel.isRepeatMon());
			bundle.putBoolean(Const.BUNDLE_KEY_IS_REPEAT_TUE, selectedLabel.isRepeatTue());
			bundle.putBoolean(Const.BUNDLE_KEY_IS_REPEAT_WED, selectedLabel.isRepeatWed());
			bundle.putBoolean(Const.BUNDLE_KEY_IS_REPEAT_THU, selectedLabel.isRepeatThu());
			bundle.putBoolean(Const.BUNDLE_KEY_IS_REPEAT_FRI, selectedLabel.isRepeatFri());
			bundle.putBoolean(Const.BUNDLE_KEY_IS_REPEAT_SAT, selectedLabel.isRepeatSat());
			bundle.putBoolean(Const.BUNDLE_KEY_IS_REPEAT_SUN, selectedLabel.isRepeatSun());
			intent.putExtras(bundle);
			startActivity(intent);
			
		} else if (menuItemName.equalsIgnoreCase("Delete")) {
			final String deleteLabel = selectedLabel.getLabelName();			
			List<Rule> deleteRules = ruleDataSource.getRulesWithTimeLabel(deleteLabel);
			if (deleteRules.size() <= 0) {
				int result = timeLabelDataSource.deleteTimeLabel(selectedLabel.getLabelName());
				if (result != 1) {
					Tools.showMessage(this, "Error code: " + result);
				} else {
					Tools.showMessage(this, "Successfully deleted " + selectedLabel.getLabelName() + ".");
					labels = timeLabelDataSource.getTimeLabels();
					timeLabelItemsAdapter.notifyDataSetChanged();
					SyncService.startSyncService(this);
				}
			} else {
				
				String message = "The following rules will be deleted, too.";
				
				for (Rule rule : deleteRules) {
					message += "\n- " + rule.getSummaryText();
				}
				
				OnClickListener okListener = new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						int result = ruleDataSource.deleteRuleWithTimeLabel(deleteLabel);
						if (result < 1) {
							Tools.showMessage(context, "Error code: " + result);
						} else {
							result = timeLabelDataSource.deleteTimeLabel(deleteLabel);
							if (result != 1) {
								Tools.showMessage(context, "Error code: " + result);
							} else {
								Tools.showMessage(context, "Successfully deleted " + deleteLabel + ".");
								labels = timeLabelDataSource.getTimeLabels();
								timeLabelItemsAdapter.notifyDataSetChanged();
								SyncService.startSyncService(context);
							}
						}
						dialog.dismiss();
					}
				};
				
				OnClickListener cancelListener = new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				};
					
				Tools.showAlertDialog(context, "Are you sure?", message, okListener, cancelListener);
			}
		} else {
			return false;
		}

		return true;
	}

	private static class TimeLabelItemViewHolder {
		public TextView labelName;
		public TextView contentSummary;
	}

	private class TimeLabelItemsAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			if (labels != null) {
				return labels.size();
			} else {
				return 0;
			}
		}

		@Override
		public Object getItem(int position) {
			if (labels != null) {
				return labels.get(position);
			} else {
				return null;				
			}
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (labels != null) {

				TimeLabelItemViewHolder holder = null;

				if (convertView == null) {
					convertView = getLayoutInflater().inflate(R.layout.label_list_item, parent, false);
					holder = new TimeLabelItemViewHolder();
					holder.labelName = (TextView) convertView.findViewById(R.id.label_name);
					holder.contentSummary = (TextView) convertView.findViewById(R.id.content_summary);
					convertView.setTag(holder);
				} else {
					holder = (TimeLabelItemViewHolder)convertView.getTag();
				}

				TimeLabel label = labels.get(position);
				holder.labelName.setText(label.getLabelName());
				holder.contentSummary.setText(label.getSummaryText());
			}
			return convertView;
		}
	}

	private OnItemClickListener timeLabelItemOnClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
			TimeLabel label = labels.get(position);
			Tools.showAlertDialog(context, label.getLabelName(), label.getSummaryText());
		}
	};

	public void onClickAddNewTimeLabel(View v) {
		Intent intent = new Intent(this, TimeLabelActivity.class);
		startActivity(intent);
	}
}    
