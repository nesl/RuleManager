package edu.ucla.nesl.rulemanager.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import edu.ucla.nesl.rulemanager.Const;
import edu.ucla.nesl.rulemanager.R;
import edu.ucla.nesl.rulemanager.Tools;
import edu.ucla.nesl.rulemanager.uielement.SetupUserDialog;
import edu.ucla.nesl.rulemanager.uielement.SetupUserDialog.OnFinishListener;

public class MainActivity extends Activity {

	private final int DIALOG_SETUP_USERNAME_PASSWORD = 1;
	private final Context context = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		SharedPreferences settings = getSharedPreferences(Const.PREFS_NAME, 0);
		boolean isFirst = settings.getBoolean(Const.PREFS_IS_FIRST, true);

		if (isFirst) {
			// Start initial setup process
			Tools.showAlertDialog(this, "Welcome", "Welcome to Rule Manager! You've launched Rule Manager for the first time, so let's go through inital setup process.", welcomeListener);
		} else {
			// Start login activity
			Intent intent = new Intent(this, LoginActivity.class);
			startActivityForResult(intent, Const.REQUEST_CODE_NORMAL);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case Const.REQUEST_CODE_NORMAL:
			finish();
			break;
		case Const.REQUEST_CODE_SETUP_HOME_LOCATION:
			Tools.showAlertDialog(context, "Your work location", "In the following screen, please specify your work location.", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					Intent intent = new Intent(context, LocationLabelActivity.class);
					Bundle bundle = new Bundle();
					bundle.putString(Const.BUNDLE_KEY_LABEL_NAME, "work");
					bundle.putBoolean(Const.BUNDLE_KEY_IS_SETUP_LABEL, true);
					intent.putExtras(bundle);
					startActivityForResult(intent, Const.REQUEST_CODE_SETUP_WORK_LOCATION);		    
				}
			});
			break;
		case Const.REQUEST_CODE_SETUP_WORK_LOCATION:
			Tools.showAlertDialog(context, "Your work time", "In the following screen, please specify your work time.", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					Intent intent = new Intent(context, TimeLabelActivity.class);
					Bundle bundle = new Bundle();
					bundle.putString(Const.BUNDLE_KEY_LABEL_NAME, "work hour");
					bundle.putBoolean(Const.BUNDLE_KEY_IS_SETUP_LABEL, true);
					intent.putExtras(bundle);
					startActivityForResult(intent, Const.REQUEST_CODE_SETUP_WORK_TIME);		    
				}
			});
			break;
		case Const.REQUEST_CODE_SETUP_WORK_TIME:
			Tools.showAlertDialog(context, "Congratualations!", "Now you're ready to use Rule Manager. Please login in the following screen.", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					
					SharedPreferences settings = context.getSharedPreferences(Const.PREFS_NAME, 0);
					SharedPreferences.Editor editor = settings.edit();
					editor.putBoolean(Const.PREFS_IS_FIRST, false);
					editor.commit();
					
					Intent intent = new Intent(context, LoginActivity.class);
					startActivityForResult(intent, Const.REQUEST_CODE_NORMAL);
				}
			});
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private OnClickListener welcomeListener = new OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
			showDialog(DIALOG_SETUP_USERNAME_PASSWORD);
		}
	};

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_SETUP_USERNAME_PASSWORD:
			return new SetupUserDialog(this, onFinishListener);
		}
		return null;
	}

	private OnFinishListener onFinishListener = new OnFinishListener() {
		@Override
		public void onFinish() {
			Tools.showAlertDialog(context, "Your home location", "In the following screen, please specify your home location.", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(context, LocationLabelActivity.class);
					Bundle bundle = new Bundle();
					bundle.putString(Const.BUNDLE_KEY_LABEL_NAME, "home");
					bundle.putBoolean(Const.BUNDLE_KEY_IS_SETUP_LABEL, true);
					intent.putExtras(bundle);
					startActivityForResult(intent, Const.REQUEST_CODE_SETUP_HOME_LOCATION);		    
				}
			});
		}
	};
}
