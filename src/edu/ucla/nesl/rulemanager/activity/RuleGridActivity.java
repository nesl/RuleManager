package edu.ucla.nesl.rulemanager.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.MultiKeyMap;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import edu.ucla.nesl.rulemanager.Const;
import edu.ucla.nesl.rulemanager.R;
import edu.ucla.nesl.rulemanager.db.LocationLabelDataSource;
import edu.ucla.nesl.rulemanager.db.RuleDataSource;
import edu.ucla.nesl.rulemanager.db.TimeLabelDataSource;
import edu.ucla.nesl.rulemanager.db.model.Rule;
import edu.ucla.nesl.rulemanager.tools.Tools;
import edu.ucla.nesl.rulemanager.tools.Tools.TableDataResult;
import edu.ucla.nesl.rulemanager.uielement.GridRuleTableLayout;

public class RuleGridActivity extends TabActivity {
	
	private final Context context = this;

	private TimeLabelDataSource timeLabelDataSource;
	private LocationLabelDataSource locationLabelDataSource;
	private RuleDataSource ruleDataSource;
	
	private List<String> sensorNames;
	private List<String> timeLabels;
	private List<String> locationLabels;
	private Map<String, MultiKeyMap> tableData;
	
	private TabHost tabHost;
	
	private boolean isNoRules = false;
	
	private TabContentFactory tabContentFactory = new TabContentFactory() {
		public View createTabContent(String sensorName) {			
			if (isNoRules) {
				return getLayoutInflater().inflate(R.layout.no_rules_text_view, null, false);
			} else {
				if (tableData != null)
					return new GridRuleTableLayout(context, sensorName, "Labels", timeLabels, locationLabels, tableData.get(sensorName));
				else
					return getLayoutInflater().inflate(R.layout.conflict_rules_text_view, null, false);
			}
		}
	};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rule_grid);

        timeLabelDataSource = new TimeLabelDataSource(this);
        locationLabelDataSource = new LocationLabelDataSource(this);
        ruleDataSource = new RuleDataSource(this);
        
		tabHost = getTabHost();
		tabHost.getTabWidget().setDividerDrawable(R.drawable.tab_divider);
		
		sensorNames = new ArrayList<String>();
		sensorNames.addAll(Arrays.asList(Tools.getSensorNames()));
		
		HorizontalScrollView tabScrollView = (HorizontalScrollView)findViewById(R.id.tab_scroll_view);
		tabScrollView.setHorizontalScrollBarEnabled(true);
		tabScrollView.setVerticalScrollBarEnabled(true);
		//tabScrollView.setScrollBarStyle(ScrollView.SCROLLBARS_OUTSIDE_INSET);
		tabScrollView.setScrollbarFadingEnabled(false);
		tabScrollView.setFillViewport(true);
    }
    
    @Override
    public void onResume() {
    	locationLabelDataSource.open();
    	timeLabelDataSource.open();
    	ruleDataSource.open();
		
		timeLabels = timeLabelDataSource.getLabelNamesWithOther();
		locationLabels = locationLabelDataSource.getLabelNamesWithOther();
		
		List<Rule> rules = ruleDataSource.getRules();
		if (rules == null || rules.size() <= 0) {
			isNoRules = true;
		} else {
			isNoRules = false;
			TableDataResult result = Tools.prepareTableData(sensorNames, timeLabels, locationLabels, rules, timeLabelDataSource.getTimeLabels(), locationLabelDataSource.getLocationLabels());
			tableData = result.tableData;
		}
		
		locationLabels.add(getString(R.string.add_new));
		timeLabels.add(getString(R.string.add_new));

		tabHost.setCurrentTab(0);
		tabHost.clearAllTabs();
		
		for (String name : sensorNames) {
			addTab(name);
		}

    	super.onResume();
    }

	@Override
    public void onPause() {
    	locationLabelDataSource.close();
    	timeLabelDataSource.close();
    	ruleDataSource.close();
    	super.onPause();
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.manage_menu, menu);
		return true;
	}
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
		Intent intent;
	    switch (item.getItemId()) {
	    case R.id.manage_rules:
		    intent = new Intent(this, RuleListActivity.class);
		    startActivity(intent);
	        return true;
	    case R.id.manage_location_labels:
		    intent = new Intent(this, ManageLocationLabelActivity.class);
		    startActivity(intent);
	        return true;
	    case R.id.manage_time_labels:
		    intent = new Intent(this, ManageTimeLabelActivity.class);
		    startActivity(intent);
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	private void addTab(final String tag) {
		View tabView = createTabView(tabHost.getContext(), tag);
		TabSpec setContent = tabHost.newTabSpec(tag).setIndicator(tabView).setContent(tabContentFactory);
		//TabSpec setContent = tabHost.newTabSpec(tag).setIndicator(tag).setContent(tabContentFactory);
		//Log.i(Const.TAG, "tag: " + tag + ", tabView: " + tabView + ", setContent: " + setContent);
		tabHost.addTab(setContent);
	}

	private View createTabView(final Context context, final String text) {
		View view = LayoutInflater.from(context).inflate(R.layout.tab_background, null);
		TextView tv = (TextView) view.findViewById(R.id.tab_text);
		tv.setText(text);
		return view;
	}

    public void onClickAddNewRule(View v) {
	    Intent intent = new Intent(this, RuleActivity.class);
	    startActivity(intent);
    }
}