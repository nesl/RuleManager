package edu.ucla.nesl.rulemanager.uielement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.map.MultiKeyMap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import edu.ucla.nesl.rulemanager.Const;
import edu.ucla.nesl.rulemanager.R;
import edu.ucla.nesl.rulemanager.Tools;
import edu.ucla.nesl.rulemanager.activity.AddNewLocationLabelActivity;
import edu.ucla.nesl.rulemanager.activity.AddNewTimeLabelActivity;
import edu.ucla.nesl.rulemanager.data.RuleGridElement;

public class GridRuleTableLayout extends RelativeLayout {

	public final String TAG = "TableMainLayout.java";

	private static boolean isFixedCellSize = true;	
	private static final int TABLE_CELL_WIDTH = 80;
	private static final int TABLE_CELL_HEIGHT = 70;
	private static final int TABLE_LEFT_HEADER_WIDTH = 70;

	private static final int TABLE_HEADER_BORDER_WIDTH = 4;
	private static final int TABLE_BORDER_WIDTH = 1;

	private static final int TABLE_BORDER_COLOR = Color.GRAY;

	private static final int TABLE_CELL_BACKGROUND = Color.WHITE;
	private static final int TABLE_CELL_BACKGROUND_RESOURCE = R.drawable.selector_table_cell_white_bg;

	private static final int TABLE_CELL_HEADER_BACKGROUND_COLOR_RESOURCE = R.color.header_gray;
	private static final int TABLE_CELL_HEADER_BACKGROUND_RESOURCE = R.drawable.selector_table_cell_header_bg;

	private static final int TABLE_CELL_ALLOW_BACKGROUND_RESOURCE = R.drawable.selector_table_cell_allow_bg;
	private static final int TABLE_CELL_DENY_BACKGROUND_RESOURCE = R.drawable.selector_table_cell_deny_bg;

	private static final int DENY_TEXT_COLOR_RESOURCE = R.color.white;
	private static final int ALLOW_TEXT_COLOR_RESOURCE = R.color.black;

	private TableLayout tableA;
	private TableLayout tableB;
	private TableLayout tableC;
	private TableLayout tableD;

	private HorizontalScrollView horizontalScrollViewB;
	private HorizontalScrollView horizontalScrollViewD;

	private ScrollView scrollViewC;
	private ScrollView scrollViewD;

	private Context context;

	int headerCellsWidth[];

	private String tableName;
	private List<String> columnHeaders;
	private List<String> rowHeaders;
	private MultiKeyMap tableData;

	private final float scale;

	private int dp2pixel(int dp) {
		return (int)(dp * scale + 0.5f);
	}

	public GridRuleTableLayout(Context context, String tableName, String cornerHeader, List<String> columnHeaders, List<String> rowHeaders, MultiKeyMap tableData) {
		super(context);
		this.context = context;

		this.scale = context.getResources().getDisplayMetrics().density;

		this.tableName = tableName;
		this.columnHeaders = columnHeaders;
		this.rowHeaders = rowHeaders;
		this.tableData = tableData;
		headerCellsWidth = new int[columnHeaders.size() + 1];

		// initialize the main components (TableLayouts, HorizontalScrollView, ScrollView)
		this.initComponents();
		this.setComponentsId();
		this.setScrollViewAndHorizontalScrollViewTag();

		// no need to assemble component A, since it is just a table
		this.horizontalScrollViewB.addView(this.tableB);
		this.scrollViewC.addView(this.tableC);
		this.scrollViewD.addView(this.horizontalScrollViewD);
		this.horizontalScrollViewD.addView(this.tableD);

		// set scroll bar visibilities
		this.scrollViewD.setHorizontalScrollBarEnabled(true);
		this.scrollViewD.setVerticalScrollBarEnabled(true);
		//this.scrollViewD.setScrollBarStyle(ScrollView.SCROLLBARS_OUTSIDE_INSET);
		this.scrollViewD.setScrollbarFadingEnabled(false);
		this.scrollViewD.setFillViewport(true);

		this.horizontalScrollViewD.setHorizontalScrollBarEnabled(true);
		this.horizontalScrollViewD.setVerticalScrollBarEnabled(true);
		this.horizontalScrollViewD.setScrollbarFadingEnabled(false);
		this.horizontalScrollViewD.setFillViewport(true);

		this.horizontalScrollViewB.setHorizontalScrollBarEnabled(false);
		this.horizontalScrollViewB.setVerticalScrollBarEnabled(false);

		this.scrollViewC.setVerticalScrollBarEnabled(false);
		this.scrollViewC.setHorizontalScrollBarEnabled(false);

		// add the components to be part of the main layout
		this.addComponentToMainLayout();

		// add some table rows
		this.addTableRowToTableA(cornerHeader);
		this.addTableRowToTableB();

		if (!isFixedCellSize) {
			resizeHeaderHeight();
			getTableRowHeaderCellWidth();
		}

		this.generateTableC_AndTable_D();

		if (!isFixedCellSize) {
			resizeBodyTableRowHeight();
		}
	}

	// initalized components 
	private void initComponents(){

		this.tableA = new TableLayout(this.context); 
		this.tableB = new TableLayout(this.context); 
		this.tableC = new TableLayout(this.context); 
		this.tableD = new TableLayout(this.context);

		this.horizontalScrollViewB = new MyHorizontalScrollView(this.context);
		this.horizontalScrollViewD = new MyHorizontalScrollView(this.context);

		this.scrollViewC = new MyScrollView(this.context);
		this.scrollViewD = new MyScrollView(this.context);

		//this.tableA.setBackgroundColor(Color.GREEN);
		this.horizontalScrollViewB.setBackgroundColor(TABLE_BORDER_COLOR);
	}

	// set essential component IDs
	private void setComponentsId() {
		this.tableA.setId(1);
		this.horizontalScrollViewB.setId(2);
		this.scrollViewC.setId(3);
		this.scrollViewD.setId(4);
	}

	// set tags for some horizontal and vertical scroll view
	private void setScrollViewAndHorizontalScrollViewTag() {

		this.horizontalScrollViewB.setTag("horizontal scroll view b");
		this.horizontalScrollViewD.setTag("horizontal scroll view d");

		this.scrollViewC.setTag("scroll view c");
		this.scrollViewD.setTag("scroll view d");
	}

	// we add the components here in our TableMainLayout
	private void addComponentToMainLayout() {

		// RelativeLayout params were very useful here
		// the addRule method is the key to arrange the components properly
		RelativeLayout.LayoutParams componentB_Params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		componentB_Params.addRule(RelativeLayout.RIGHT_OF, this.tableA.getId());

		RelativeLayout.LayoutParams componentC_Params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		componentC_Params.addRule(RelativeLayout.BELOW, this.tableA.getId());

		RelativeLayout.LayoutParams componentD_Params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		componentD_Params.addRule(RelativeLayout.RIGHT_OF, this.scrollViewC.getId());
		componentD_Params.addRule(RelativeLayout.BELOW, this.horizontalScrollViewB.getId());

		// 'this' is a relative layout, 
		// we extend this table layout as relative layout as seen during the creation of this class
		this.addView(this.tableA);
		this.addView(this.horizontalScrollViewB, componentB_Params);
		this.addView(this.scrollViewC, componentC_Params);
		this.addView(this.scrollViewD, componentD_Params);
	}

	private void addTableRowToTableA(String header){
		this.tableA.addView(this.componentATableRow(header));
	}

	private void addTableRowToTableB(){
		this.tableB.addView(this.componentBTableRow());
	}

	// generate table row of table A
	private TableRow componentATableRow(String header){

		TableRow componentATableRow = new TableRow(this.context);
		TextView textView = this.headerTextView(header);

		if (isFixedCellSize) {
			TableRow.LayoutParams params = new TableRow.LayoutParams(dp2pixel(TABLE_LEFT_HEADER_WIDTH), dp2pixel(TABLE_CELL_HEIGHT));
			params.setMargins(0, 0, dp2pixel(TABLE_HEADER_BORDER_WIDTH), dp2pixel(TABLE_HEADER_BORDER_WIDTH));
			textView.setLayoutParams(params);
		}

		componentATableRow.addView(textView);

		if (isFixedCellSize) {
			componentATableRow.setVerticalGravity(CENTER_VERTICAL);
			componentATableRow.setBackgroundColor(TABLE_BORDER_COLOR);
		}

		return componentATableRow;
	}

	// generate table row of table B
	private TableRow componentBTableRow(){

		TableRow componentBTableRow = new TableRow(this.context);

		TableRow.LayoutParams params;
		if (isFixedCellSize) {
			params = new TableRow.LayoutParams(dp2pixel(TABLE_CELL_WIDTH), dp2pixel(TABLE_CELL_HEIGHT));
			params.setMargins(0, 0, dp2pixel(TABLE_BORDER_WIDTH), dp2pixel(TABLE_HEADER_BORDER_WIDTH));
		} else {
			params = new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT);
			params.setMargins(0, 0, dp2pixel(TABLE_BORDER_WIDTH), dp2pixel(TABLE_HEADER_BORDER_WIDTH));
		}

		String addNew = context.getString(R.string.add_new);
		for(String label : columnHeaders){
			TextView textView = this.headerTextView(label);
			textView.setLayoutParams(params);
			componentBTableRow.addView(textView);

			if (label.equalsIgnoreCase(addNew)) {
				textView.setBackgroundResource(TABLE_CELL_HEADER_BACKGROUND_RESOURCE);
				textView.setClickable(true);
				textView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(context, AddNewTimeLabelActivity.class);
						context.startActivity(intent);
					}
				});
			}
		}

		if (isFixedCellSize) {
			componentBTableRow.setVerticalGravity(CENTER_VERTICAL);
		}

		return componentBTableRow;
	}

	// generate table row of table C and table D
	private void generateTableC_AndTable_D(){

		for (String rowHeader : rowHeaders) {
			TableRow tableRowForTableC = this.tableRowForTableC(rowHeader);
			tableRowForTableC.setBackgroundColor(TABLE_BORDER_COLOR);
			this.tableC.addView(tableRowForTableC);

			TableRow taleRowForTableD = this.taleRowForTableD(rowHeader);
			taleRowForTableD.setBackgroundColor(TABLE_BORDER_COLOR);
			this.tableD.addView(taleRowForTableD);
		}
	}

	// a TableRow for table C
	private TableRow tableRowForTableC(String label){

		TableRow.LayoutParams params;
		if (isFixedCellSize) {
			params = new TableRow.LayoutParams(dp2pixel(TABLE_LEFT_HEADER_WIDTH), dp2pixel(TABLE_CELL_HEIGHT));
		} else {
			params = new TableRow.LayoutParams(this.headerCellsWidth[0], LayoutParams.FILL_PARENT);
		}
		params.setMargins(0, 0, dp2pixel(TABLE_HEADER_BORDER_WIDTH), dp2pixel(TABLE_BORDER_WIDTH));

		TableRow tableRowForTableC = new TableRow(this.context);
		TextView textView = this.leftHeaderTextView(label);
		tableRowForTableC.addView(textView, params);

		if (isFixedCellSize) {
			tableRowForTableC.setVerticalGravity(CENTER_VERTICAL);
		}

		return tableRowForTableC;
	}

	private TableRow taleRowForTableD(String rowHeader){

		TableRow taleRowForTableD = new TableRow(this.context);

		int loopCount = ((TableRow)this.tableB.getChildAt(0)).getChildCount();

		for (int i = 0 ; i < loopCount; i++) {
			TableRow.LayoutParams params;
			if (isFixedCellSize) {
				params = new TableRow.LayoutParams(dp2pixel(TABLE_CELL_WIDTH), dp2pixel(TABLE_CELL_HEIGHT));
			} else {
				params = new TableRow.LayoutParams(headerCellsWidth[i + 1], LayoutParams.FILL_PARENT);
			}
			params.setMargins(0, 0, dp2pixel(TABLE_BORDER_WIDTH), dp2pixel(TABLE_BORDER_WIDTH));

			String columnHeader = columnHeaders.get(i);
			String addNew = context.getString(R.string.add_new);
			View cellView;
			if (columnHeader.equals(addNew) || rowHeader.equals(addNew)) {
				cellView = createEmptyViewForTableDCell();
			} else {
				RuleGridElement elem = (RuleGridElement)tableData.get(columnHeaders.get(i), rowHeader);
				cellView = createViewForTableDCell(rowHeader, columnHeader, elem);
			}
			taleRowForTableD.addView(cellView, params);
		}

		if (isFixedCellSize) {
			taleRowForTableD.setVerticalGravity(CENTER_VERTICAL);
		}

		return taleRowForTableD;
	}

	/*private TextView bodyTextView(String label){

		TextView bodyTextView = new TextView(this.context);
		bodyTextView.setBackgroundColor(TABLE_CELL_BACKGROUND);
		bodyTextView.setText(label);
		bodyTextView.setGravity(Gravity.CENTER);
		bodyTextView.setPadding(5, 5, 5, 5);

		return bodyTextView;
	}*/

	private TextView leftHeaderTextView(String label){

		TextView textView = new TextView(this.context);
		textView.setBackgroundColor(getResources().getColor(TABLE_CELL_HEADER_BACKGROUND_COLOR_RESOURCE));
		textView.setText(label);
		textView.setGravity(Gravity.CENTER);
		textView.setPadding(5, 5, 5, 5);

		String addNew = context.getString(R.string.add_new);
		if (label.equalsIgnoreCase(addNew)) {
			textView.setBackgroundResource(TABLE_CELL_HEADER_BACKGROUND_RESOURCE);
			textView.setClickable(true);
			textView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(context, AddNewLocationLabelActivity.class);
					context.startActivity(intent);
				}
			});
		}

		return textView;
	}

	private View createEmptyViewForTableDCell() {
		TextView textView = new TextView(this.context);
		textView.setBackgroundColor(TABLE_CELL_BACKGROUND);
		textView.setGravity(Gravity.CENTER);
		textView.setPadding(5, 5, 5, 5);
		textView.setText("");
		return textView;
	}

	private View createTextViewForTableDCell(final String text) {
		TextView textView = new TextView(this.context);
		textView.setBackgroundColor(TABLE_CELL_BACKGROUND);
		textView.setGravity(Gravity.CENTER);
		textView.setPadding(5, 5, 5, 5);
		textView.setText(text);
		textView.setClickable(true);
		textView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog dialog = new AlertDialog.Builder(context).create();
				dialog.setMessage(text);
				dialog.setButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				dialog.show();
			}
		});
		return textView;
	}


	private View createViewForTableDCell(final String leftHeader, final String topHeader, RuleGridElement elem){

		//return createDebugViewForTableDCell(leftHeader, topHeader, elem);

		if (elem == null) {
			return createDenyEveryoneTableCell();
		}

		// prepare sets
		Set<String> everyone = new HashSet<String>();
		everyone.addAll(Arrays.asList(Tools.getConsumerNames()));
		Set<String> allowed = new HashSet<String>();
		allowed.addAll(elem.allowedList);
		Set<String> denied = new HashSet<String>();
		denied.addAll(elem.deniedList);
		Set<String> partialAllowed = new HashSet<String>();
		partialAllowed.addAll(elem.partialAllowedList);
		Set<String> partialDenied = new HashSet<String>();
		partialDenied.addAll(elem.partialDeniedList);

		// blow up everyone.
		if (allowed.contains(Const.EVERYONE)) {
			allowed.remove(Const.EVERYONE);
			allowed.addAll(everyone);
		}
		if (denied.contains(Const.EVERYONE)) {
			denied.remove(Const.EVERYONE);
			denied.addAll(everyone);
		}
		if (partialAllowed.contains(Const.EVERYONE)) {
			partialAllowed.remove(Const.EVERYONE);
			partialAllowed.addAll(everyone);
		}
		if (partialDenied.contains(Const.EVERYONE)) {
			partialDenied.remove(Const.EVERYONE);
			partialDenied.addAll(everyone);
		}

		// adjust sets
		partialAllowed.removeAll(allowed);
		allowed.removeAll(denied);
		partialAllowed.removeAll(denied);
		if (denied.size() == 0) {
			Set<String> tempSet = new HashSet<String>();
			tempSet.addAll(everyone);
			tempSet.removeAll(allowed);
			denied.addAll(tempSet);
		}
		if (allowed.size() == 0) {
			denied.clear();
			denied.addAll(everyone);
		}
		partialDenied.removeAll(denied);

		// convert back to everyone
		if (allowed.containsAll(everyone)) {
			allowed.clear();
			allowed.add(Const.EVERYONE);
		}
		if (denied.containsAll(everyone)) {
			denied.clear();
			denied.add(Const.EVERYONE);
		}
		if (partialAllowed.containsAll(everyone)) {
			partialAllowed.clear();
			partialAllowed.add(Const.EVERYONE);
		}
		if (partialDenied.containsAll(everyone)) {
			partialDenied.clear();
			partialDenied.add(Const.EVERYONE);
		}

		// Generate text
		String allowText = "";
		String denyText = "";
		String partialAllowText = "";
		String partialDenyText = "";
		
		if (allowed.size() > 0) {
			for (String allow : allowed) {
				allowText += allow + ", ";
			}
			allowText = allowText.substring(0, allowText.length() - 2);
		}
		if (partialAllowed.size() > 0) {
			for (String allow : partialAllowed) {
				partialAllowText += allow + ", ";
			}
			partialAllowText = partialAllowText.substring(0, partialAllowText.length() - 2);
		}

		if (denied.size() > 0) {
			for (String deny : denied) {
				denyText += deny + ", ";
			}
			denyText = denyText.substring(0, denyText.length() - 2);
		}
		if (partialDenied.size() > 0) {
			for (String deny : partialDenied) {
				partialDenyText += deny + ", ";
			}
			partialDenyText = partialDenyText.substring(0, partialDenyText.length() - 2);
		}

		return createTableDCellViewWithText(allowText, partialAllowText, denyText, partialDenyText);
	}

	private View createTableDCellViewWithText(String allowText, String partialAllowText, String denyText, String partialDenyText) {
		boolean isAllow = (allowText.length() > 0 || partialAllowText.length() > 0);
		boolean isDeny = (denyText.length() > 0 || partialDenyText.length() > 0);
		
		View retView = null;
		if (isAllow && isDeny) {
			LinearLayout linearLayout = new LinearLayout(this.context);
			linearLayout.setOrientation(LinearLayout.VERTICAL);

			TextView allowTextView = createAllowDenyCellView(allowText, partialAllowText, true);
			TextView denyTextView = createAllowDenyCellView(denyText, partialDenyText, false);

			LinearLayout.LayoutParams params;
			params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, 0, 1);
			allowTextView.setLayoutParams(params);
			denyTextView.setLayoutParams(params);

			linearLayout.addView(allowTextView);
			linearLayout.addView(denyTextView);

			linearLayout.setBackgroundResource(TABLE_CELL_BACKGROUND_RESOURCE);
			
			retView = linearLayout;
		} else if (isAllow && !isDeny) {
			retView = createAllowDenyCellView(allowText, partialAllowText, true);
		} else if (!isAllow && isDeny) {
			retView = createAllowDenyCellView(denyText, partialDenyText, false);
		} else {
			assert false;
			return null;
		}

		String text = generateDialogText(allowText, partialAllowText, denyText, partialDenyText);
		setClickDialog(retView, text);

		return retView;
	}

	private String generateDialogText(String allowText, String partialAllowText, String denyText, String partialDenyText) {
		String text = "";
		if (allowText.length() > 0) {
			text += "- Shared with " + allowText;
		}
		if (denyText.length() > 0) {
			if (text.length() > 0) {
				text += "\n";
			}
			text += "- Not shared with " + denyText;
		}
		if (partialAllowText.length() > 0) {
			if (text.length() > 0) {
				text += "\n";
			}
			text += "- Partially shared with " + partialAllowText;
		}
		if (partialDenyText.length() > 0) {
			if (text.length() > 0) {
				text += "\n";
			}
			text += "- Partially not shared with " + partialDenyText;
		}
		return text;
	}

	private void setClickDialog(View view, final String text) {
		view.setClickable(true);
		view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog dialog = new AlertDialog.Builder(context).create();
				dialog.setMessage(text);
				dialog.setButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				dialog.show();
			}
		});
	}

	private View createDebugViewForTableDCell(String leftHeader, String topHeader, RuleGridElement elem) {

		if (elem == null) {
			return createTextViewForTableDCell("null");
		} else {
			String allowText = "";
			String denyText = "";
			if (elem.allowedList.size() > 0) {
				for (String allow : elem.allowedList) {
					allowText += allow + ", ";
				}
				allowText = allowText.substring(0, allowText.length() - 2);
			}

			if (elem.deniedList.size() > 0) {
				for (String deny : elem.deniedList){
					denyText += deny + ", ";
				}
				denyText = denyText.substring(0, denyText.length() - 2);
			}

			String paText = "";
			if (elem.partialAllowedList.size() > 0) {
				for (String pa : elem.partialAllowedList) {
					paText += pa + ", ";
				}
				paText = paText.substring(0, paText.length() - 2);
			}

			String pdText = "";
			if (elem.partialDeniedList.size() > 0) {
				for (String pd : elem.partialDeniedList) {
					pdText += pd + ", ";
				}
				pdText = pdText.substring(0, pdText.length() - 2);
			}
			String text = "A: " + allowText + " (" + paText + ")\nD: " + denyText + " (" + pdText + ")";
			return createTextViewForTableDCell(text);
		}
	}

	private boolean isEveryoneAllowed(List<String> allowedList) {
		List<String> allConsumers = new ArrayList<String>();
		allConsumers.addAll(Arrays.asList(Tools.getConsumerNames()));
		for (String allowed : allowedList) {
			allConsumers.remove(allowed);
		}
		if (allConsumers.size() <= 0) {
			return true;
		}
		return false;
	}

	private TextView createAllowDenyCellView(String consumers, String partialConsumers, boolean isAllow) {

		TextView textView = new TextView(this.context);

		String text = "";
		if (consumers.length() > 0) {
			text += consumers;
		}
		if (partialConsumers.length() > 0) {
			if (text.length() > 0) {
				text += " ";
			}
			text += "(" + partialConsumers + ")";
		}
		
		if (isAllow) {
			textView.setBackgroundResource(TABLE_CELL_ALLOW_BACKGROUND_RESOURCE);
			textView.setTextColor(getResources().getColor(ALLOW_TEXT_COLOR_RESOURCE));
		} else {
			textView.setBackgroundResource(TABLE_CELL_DENY_BACKGROUND_RESOURCE);
			textView.setTextColor(getResources().getColor(DENY_TEXT_COLOR_RESOURCE));
		}

		textView.setGravity(Gravity.CENTER);
		textView.setPadding(5, 5, 5, 5);
		textView.setText(text);

		if (isFixedCellSize) {
			textView.setSingleLine();
			textView.setEllipsize(TruncateAt.END);
		}

		return textView;
	}

	private View createDenyEveryoneTableCell() {
		TextView textView = new TextView(this.context);
		textView.setBackgroundResource(TABLE_CELL_DENY_BACKGROUND_RESOURCE);
		textView.setTextColor(getResources().getColor(DENY_TEXT_COLOR_RESOURCE));
		textView.setGravity(Gravity.CENTER);
		textView.setPadding(5, 5, 5, 5);
		textView.setText(Const.EVERYONE);

		final String text = "- Not shared with " + Const.EVERYONE;
		textView.setClickable(true);
		textView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog dialog = new AlertDialog.Builder(context).create();
				dialog.setMessage(text);
				dialog.setButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				dialog.show();
			}
		});

		return textView;
	}

	// header standard TextView
	private TextView headerTextView(String label) {

		TextView headerTextView = new TextView(this.context);
		headerTextView.setBackgroundColor(getResources().getColor(TABLE_CELL_HEADER_BACKGROUND_COLOR_RESOURCE));
		headerTextView.setText(label);
		headerTextView.setGravity(Gravity.CENTER);
		headerTextView.setPadding(5, 5, 5, 5);

		return headerTextView;
	}

	// resizing TableRow height starts here
	private void resizeHeaderHeight() {

		TableRow tableRowA = (TableRow) this.tableA.getChildAt(0);
		TableRow tableRowB = (TableRow)  this.tableB.getChildAt(0);

		int rowAHeight = this.viewHeight(tableRowA);
		int rowBHeight = this.viewHeight(tableRowB);

		TableRow tableRow = rowAHeight < rowBHeight ? tableRowA : tableRowB;
		int finalHeight = rowAHeight > rowBHeight ? rowAHeight : rowBHeight;

		this.matchLayoutHeight(tableRow, finalHeight);
	}

	private void getTableRowHeaderCellWidth(){

		int tableAChildCount = ((TableRow)this.tableA.getChildAt(0)).getChildCount();
		int tableBChildCount = ((TableRow)this.tableB.getChildAt(0)).getChildCount();

		for (int x = 0; x < (tableAChildCount + tableBChildCount); x++){

			if (x == 0){
				this.headerCellsWidth[x] = this.viewWidth(((TableRow)this.tableA.getChildAt(0)).getChildAt(x));
			} else {
				this.headerCellsWidth[x] = this.viewWidth(((TableRow)this.tableB.getChildAt(0)).getChildAt(x-1));
			}

		}
	}

	// resize body table row height
	private void resizeBodyTableRowHeight(){

		int tableC_ChildCount = this.tableC.getChildCount();

		for(int x=0; x<tableC_ChildCount; x++){

			TableRow productNameHeaderTableRow = (TableRow) this.tableC.getChildAt(x);
			TableRow productInfoTableRow = (TableRow)  this.tableD.getChildAt(x);

			int rowAHeight = this.viewHeight(productNameHeaderTableRow);
			int rowBHeight = this.viewHeight(productInfoTableRow);

			TableRow tableRow = rowAHeight < rowBHeight ? productNameHeaderTableRow : productInfoTableRow;
			int finalHeight = rowAHeight > rowBHeight ? rowAHeight : rowBHeight;

			this.matchLayoutHeight(tableRow, finalHeight);		
		}

	}

	// match all height in a table row
	// to make a standard TableRow height
	private void matchLayoutHeight(TableRow tableRow, int height) {

		int tableRowChildCount = tableRow.getChildCount();

		// if a TableRow has only 1 child
		if(tableRow.getChildCount() == 1){

			View view = tableRow.getChildAt(0);
			TableRow.LayoutParams params = (TableRow.LayoutParams) view.getLayoutParams();
			params.height = height - (params.bottomMargin + params.topMargin);

			return ;
		}

		// if a TableRow has more than 1 child
		for (int x = 0; x < tableRowChildCount; x++) {

			View view = tableRow.getChildAt(x);

			TableRow.LayoutParams params = (TableRow.LayoutParams) view.getLayoutParams();

			if (!isTheHeighestLayout(tableRow, x)) {
				params.height = height - (params.bottomMargin + params.topMargin);
				return;
			}
		}

	}

	// check if the view has the highest height in a TableRow
	private boolean isTheHeighestLayout(TableRow tableRow, int layoutPosition) {

		int tableRowChildCount = tableRow.getChildCount();
		int heighestViewPosition = -1;
		int viewHeight = 0;

		for (int x = 0; x < tableRowChildCount; x++) {
			View view = tableRow.getChildAt(x);
			int height = this.viewHeight(view);

			if (viewHeight < height) {
				heighestViewPosition = x;
				viewHeight = height;
			}
		}

		return heighestViewPosition == layoutPosition;
	}

	// read a view's height
	private int viewHeight(View view) {
		view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
		return view.getMeasuredHeight();
	}

	// read a view's width
	private int viewWidth(View view) {
		view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
		return view.getMeasuredWidth();
	}

	// horizontal scroll view custom class
	class MyHorizontalScrollView extends HorizontalScrollView{

		public MyHorizontalScrollView(Context context) {
			super(context);
		}

		@Override
		protected void onScrollChanged(int l, int t, int oldl, int oldt) {
			String tag = (String) this.getTag();

			if (tag.equalsIgnoreCase("horizontal scroll view b")) {
				horizontalScrollViewD.scrollTo(l, 0);
			} else {
				horizontalScrollViewB.scrollTo(l, 0);
			}
		}

	}

	// scroll view custom class
	class MyScrollView extends ScrollView{

		public MyScrollView(Context context) {
			super(context);
		}

		@Override
		protected void onScrollChanged(int l, int t, int oldl, int oldt) {

			String tag = (String) this.getTag();

			if (tag.equalsIgnoreCase("scroll view c")) {
				scrollViewD.scrollTo(0, t);
			} else {
				scrollViewC.scrollTo(0,t);
			}
		}
	}
}
