package edu.ucla.nesl.rulemanager.activity;

import java.util.List;

import android.app.Activity;
import android.content.Context;
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
import edu.ucla.nesl.rulemanager.Tools;
import edu.ucla.nesl.rulemanager.db.RuleDataSource;
import edu.ucla.nesl.rulemanager.db.model.Rule;

public class RuleListActivity extends Activity {

	private RuleDataSource dataSource;
	
	private List<Rule> rules;
	
	private RuleItemsAdapter ruleItemsAdapter;
	private ListView rulesListView;
	private Context context = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rule_list);

		dataSource = new RuleDataSource(this);
		
		rulesListView = (ListView) findViewById(R.id.rules_listview);
		ruleItemsAdapter = new RuleItemsAdapter();
		rulesListView.setAdapter(ruleItemsAdapter);
		rulesListView.setOnItemClickListener(ruleItemOnClickListener);
		registerForContextMenu(rulesListView);
	}

	@Override
	protected void onResume() {
		dataSource.open();
		rules = dataSource.getRules();
		ruleItemsAdapter.notifyDataSetChanged();
		super.onResume();
	}

	@Override
	protected void onPause() {
		dataSource.close();
		super.onPause();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.rules_listview) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
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
		Rule rule = rules.get(info.position);

		if (menuItemName.equalsIgnoreCase("Edit")) {
			Intent intent = new Intent(this, AddNewRuleActivity.class);
			Bundle bundle = new Bundle();
			bundle.putInt(Const.BUNDLE_KEY_ID, rule.getId());
			bundle.putString(Const.BUNDLE_KEY_ACTION, rule.getAction());
			bundle.putString(Const.BUNDLE_KEY_SENSOR, rule.getData());
			bundle.putString(Const.BUNDLE_KEY_CONSUMER, rule.getConsumer());
			bundle.putString(Const.BUNDLE_KEY_TIME_LABEL, rule.getTimeLabel());
			bundle.putString(Const.BUNDLE_KEY_LOCATION_LABEL, rule.getLocationLabel());
			intent.putExtras(bundle);
			startActivity(intent);
		} else if (menuItemName.equalsIgnoreCase("Delete")) {
			int result = dataSource.deleteRule(rule.getId());
			if (result != 1) {
				Tools.showMessage(this, "Error code: " + result);
			} else {
				Tools.showMessage(this, "Successfully deleted a rule.");
				rules = dataSource.getRules();
				ruleItemsAdapter.notifyDataSetChanged();
			}
		} else {
			return false;
		}
		
		return true;
	}

	private static class RuleItemViewHolder {
		public TextView ruleContent;
		//public ToggleButton toggleButton;
	}

	private class RuleItemsAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			if (rules != null) {
				return rules.size();
			} else {
				return 0;
			}
		}

		@Override
		public Object getItem(int position) {
			if (rules != null) {
				return rules.get(position);
			} else {
				return null;
			}
		}

		@Override
		public long getItemId(int position) {
			if (rules != null) {
				return rules.get(position).getId();
			} else {
				return -1;
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			RuleItemViewHolder holder = null;

			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.rule_list_item, parent, false);
				holder = new RuleItemViewHolder();
				holder.ruleContent = (TextView) convertView.findViewById(R.id.rule_content);
				//holder.toggleButton = (ToggleButton) convertView.findViewById(R.id.rule_toggle_button);
				//holder.toggleButton.setOnClickListener(ruleToggleButtonListener);
				convertView.setTag(holder);
			} else {
				holder = (RuleItemViewHolder)convertView.getTag();
			}

			Rule rule = rules.get(position);
			holder.ruleContent.setText(rule.getSummaryText());

			return convertView;
		}
	}

	private OnItemClickListener ruleItemOnClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
			Tools.showAlertDialog(context, "Rule Detail", rules.get(position).getSummaryText());
		}
	};

	/*private OnClickListener ruleToggleButtonListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			final int position = rulesListView.getPositionForView(v);
			Tools.showMessage(context, "Toggle Button Position " + position);
		}
	};*/

	public void onClickAddNewRule(View v) {
		Intent intent = new Intent(this, AddNewRuleActivity.class);
		startActivity(intent);
	}
}    
