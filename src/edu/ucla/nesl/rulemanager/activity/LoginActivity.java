package edu.ucla.nesl.rulemanager.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import edu.ucla.nesl.rulemanager.Const;
import edu.ucla.nesl.rulemanager.R;
import edu.ucla.nesl.rulemanager.Tools;
import edu.ucla.nesl.rulemanager.SyncService;

public class LoginActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
	}

	public void onClickLoginButton(View view) {

		SharedPreferences settings = getSharedPreferences(Const.PREFS_NAME, 0);
		String serverip = settings.getString(Const.PREFS_SERVER_IP, null);
		String username = settings.getString(Const.PREFS_USERNAME, null);
		String password = settings.getString(Const.PREFS_PASSWORD, null);

		EditText usernameEditText = (EditText)findViewById(R.id.username);
		EditText passwordEditText = (EditText)findViewById(R.id.password);

		String usernameEntered = usernameEditText.getText().toString();
		String passwordEntered = passwordEditText.getText().toString();

		if (usernameEntered.equals(username) && passwordEntered.equals(password)) {
			// Start upload service
			Tools.startSyncService(this, Const.SIGNAL_UPDATE_ALL);	
			
			// Start activity
			Intent i = new Intent(this, RuleGridActivity.class);
			startActivity(i);
		} else {
			Tools.showAlertDialog(this, "Error", "Your username and password don't match!");
		}
	}
}
