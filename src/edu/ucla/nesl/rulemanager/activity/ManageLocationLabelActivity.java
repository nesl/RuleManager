package edu.ucla.nesl.rulemanager.activity;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
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
import edu.ucla.nesl.rulemanager.db.LocationLabelDataSource;
import edu.ucla.nesl.rulemanager.db.RuleDataSource;
import edu.ucla.nesl.rulemanager.db.model.LocationLabel;
import edu.ucla.nesl.rulemanager.db.model.Rule;
import edu.ucla.nesl.rulemanager.tools.Tools;

public class ManageLocationLabelActivity extends Activity {

	private LocationLabelDataSource locationLabelDataSource;
	private RuleDataSource ruleDataSource;
	private List<LocationLabel> labels;

	private LocationLabelItemsAdapter locationLabelItemsAdapter;
	private ListView locationLabelListView;
	private Context context = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manage_location_labels);

		locationLabelDataSource = new LocationLabelDataSource(this);
		ruleDataSource = new RuleDataSource(this);

		locationLabelListView = (ListView) findViewById(R.id.location_labels_listview);
		locationLabelItemsAdapter = new LocationLabelItemsAdapter();
		locationLabelListView.setAdapter(locationLabelItemsAdapter);
		locationLabelListView.setOnItemClickListener(locationLabelItemOnClickListener);
		registerForContextMenu(locationLabelListView);
	}
	
	@Override
	protected void onResume() {
		ruleDataSource.open();
		locationLabelDataSource.open();
		labels = locationLabelDataSource.getLocationLabels(); 
		locationLabelItemsAdapter.notifyDataSetChanged();
		
		/*if (labels.size() == 2) {
			LocationLabel l1 = labels.get(0);
			LocationLabel l2 = labels.get(1);
			String msg = "distance: " + String.format("%.2f", l1.getDistance(l2)) + " meters.\n";
			msg += "checkOverlap? " + l1.checkOverlap(l2);
			Tools.showAlertDialog(this, "Test", msg);
		}*/
		
		/*for (LocationLabel label : labels) {
			Log.i(Const.TAG, label.toJsonString());
		}*/
		
		super.onResume();
	}

	@Override
	protected void onPause() {
		ruleDataSource.close();
		locationLabelDataSource.close();
		super.onPause();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.location_labels_listview) {
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
		LocationLabel selectedLabel = labels.get(info.position);

		if (menuItemName.equalsIgnoreCase("Edit")) {
			Intent intent = new Intent(this, LocationLabelActivity.class);
			Bundle bundle = new Bundle();
			bundle.putString(Const.BUNDLE_KEY_LABEL_NAME, selectedLabel.getLabelName());
			bundle.putDouble(Const.BUNDLE_KEY_LATITUDE, selectedLabel.getLatitude());
			bundle.putDouble(Const.BUNDLE_KEY_LONGITUDE, selectedLabel.getLongitude());
			bundle.putDouble(Const.BUNDLE_KEY_RADIUS, selectedLabel.getRadius());
			intent.putExtras(bundle);
			startActivity(intent);
			
		} else if (menuItemName.equalsIgnoreCase("Delete")) {
			
			final String deleteLabel = selectedLabel.getLabelName();			
			List<Rule> deleteRules = ruleDataSource.getRulesWithLocationLabel(deleteLabel);
			
			if (deleteRules.size() <= 0) {
				int result = locationLabelDataSource.deleteLocationLabel(selectedLabel.getLabelName());
				if (result != 1) {
					Tools.showMessage(this, "Error code: " + result);
				} else {
					Tools.showMessage(this, "Successfully deleted " + selectedLabel.getLabelName() + ".");
					labels = locationLabelDataSource.getLocationLabels();
					locationLabelItemsAdapter.notifyDataSetChanged();
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
						int result = ruleDataSource.deleteRuleWithLocationLabel(deleteLabel);
						if (result < 1) {
							Tools.showMessage(context, "Error code: " + result);
						} else {
							result = locationLabelDataSource.deleteLocationLabel(deleteLabel);
							if (result != 1) {
								Tools.showMessage(context, "Error code: " + result);
							} else {
								Tools.showMessage(context, "Successfully deleted " + deleteLabel + ".");
								labels = locationLabelDataSource.getLocationLabels();
								locationLabelItemsAdapter.notifyDataSetChanged();
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

	private static class LocationLabelItemViewHolder {
		public TextView labelName;
		public TextView contentSummary;
	}

	private class LocationLabelItemsAdapter extends BaseAdapter {

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

				LocationLabelItemViewHolder holder = null;

				if (convertView == null) {
					convertView = getLayoutInflater().inflate(R.layout.label_list_item, parent, false);
					holder = new LocationLabelItemViewHolder();
					holder.labelName = (TextView) convertView.findViewById(R.id.label_name);
					holder.contentSummary = (TextView) convertView.findViewById(R.id.content_summary);
					convertView.setTag(holder);
				} else {
					holder = (LocationLabelItemViewHolder)convertView.getTag();
				}

				LocationLabel label = labels.get(position);
				holder.labelName.setText(label.getLabelName());
				holder.contentSummary.setText(label.getSummaryText());
			}
			return convertView;
		}
	}

	private OnItemClickListener locationLabelItemOnClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
			LocationLabel label = labels.get(position);
			String message = "Latitude:\n  " + label.getLatitude() + "\n"
					+ "Longitude:\n  " + label.getLongitude() + "\n"
					+ "Radius:\n  " + String.format("%.2f m", label.getRadius());
			Tools.showAlertDialog(context, label.getLabelName(), message);
		}
	};

	public void onClickAddNewLocationLabel(View v) {
		Intent intent = new Intent(this, LocationLabelActivity.class);
		startActivity(intent);
	}
}    
