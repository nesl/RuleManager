package edu.ucla.nesl.rulemanager.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;
import edu.ucla.nesl.rulemanager.Const;
import edu.ucla.nesl.rulemanager.R;
import edu.ucla.nesl.rulemanager.Tools;
import edu.ucla.nesl.rulemanager.SyncService;
import edu.ucla.nesl.rulemanager.db.LocationLabelDataSource;
import edu.ucla.nesl.rulemanager.db.RuleDataSource;

@SuppressLint("SetJavaScriptEnabled")
public class LocationLabelActivity extends Activity {

	private LocationLabelDataSource locationLabelDataSource;
	private RuleDataSource ruleDataSource;

	private WebView mapView;
	private LocationManager locationManager;
	private String provider;

	private EditText labelEditText;
	private String prevLabelName;
	
	private boolean isSetupLabel= false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_new_location_label);

		ruleDataSource = new RuleDataSource(this);
		locationLabelDataSource = new LocationLabelDataSource(this);

		labelEditText = (EditText) findViewById(R.id.location_label);

		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

		// check if enabled and if not send user to the GSP settings
		// Better solution would be to display a dialog and suggesting to 
		// go to the settings
		if (!enabled) {
			Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivity(intent);
		} 

		Criteria criteria = new Criteria();
		provider = locationManager.getBestProvider(criteria, false);
		final Location location = locationManager.getLastKnownLocation(provider);

		if (savedInstanceState == null) {
			mapView = (WebView)findViewById(R.id.map_view);

			mapView.loadUrl("file:///android_asset/map.html");
			mapView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
			mapView.getSettings().setJavaScriptEnabled(true);
			mapView.addJavascriptInterface(new WebViewJavaScriptInterface(this), "Android");
			mapView.setWebChromeClient(new WebChromeClient());

			//mapView.requestFocus(View.FOCUS_DOWN | View.FOCUS_UP);
			mapView.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
					case MotionEvent.ACTION_UP:
						if (!v.hasFocus()) {
							v.requestFocus();
						}
						break;
					}
					return false;
				}
			});

			WebViewClient webViewClient = null; 
			Bundle bundle = getIntent().getExtras();
			if (bundle != null) {
				String labelName = bundle.getString(Const.BUNDLE_KEY_LABEL_NAME);
				labelEditText.setText(labelName);
				
				isSetupLabel = bundle.getBoolean(Const.BUNDLE_KEY_IS_SETUP_LABEL);
				if (isSetupLabel) {
					labelEditText.setEnabled(false);
					if (location != null) {
						webViewClient = new WebViewClient() {
							@Override
							public void onPageFinished(WebView view, String url) {
								mapView.loadUrl("javascript:(function() { setCurrentLocation("
										+ location.getLatitude() + ","
										+ location.getLongitude() + ") })()");
							}
						};
					}
				} else {
					prevLabelName = labelName;
					final double latitude = bundle.getDouble(Const.BUNDLE_KEY_LATITUDE);
					final double longitude = bundle.getDouble(Const.BUNDLE_KEY_LONGITUDE);
					final double radius = bundle.getDouble(Const.BUNDLE_KEY_RADIUS);

					if (latitude != 0.0 && longitude != 0.0 && radius != 0.0) {
						webViewClient = new WebViewClient() {
							@Override
							public void onPageFinished(WebView view, String url) {
								mapView.loadUrl("javascript:(function() { setCircle("
										+ latitude + ","
										+ longitude + ","
										+ radius
										+ ") })()");
							}
						};
					} 
				}

			} else if (location != null) {
				webViewClient = new WebViewClient() {
					@Override
					public void onPageFinished(WebView view, String url) {
						mapView.loadUrl("javascript:(function() { setCurrentLocation("
								+ location.getLatitude() + ","
								+ location.getLongitude() + ") })()");
					}
				};
			}
			if (webViewClient != null) {
				mapView.setWebViewClient(webViewClient);
			}
		}
	}

	@Override
	protected void onResume() {
		locationLabelDataSource.open();
		ruleDataSource.open();
		super.onResume();
	}

	@Override
	protected void onPause() {
		locationLabelDataSource.close();
		ruleDataSource.close();
		super.onPause();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.saveState(outState);
	}

	@Override 
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mapView.restoreState(savedInstanceState);
	}

	private void onClickDoneButtonActivity(double latitude, double longitude, double radius) {
		String labelName = labelEditText.getText().toString();
		if (labelName == null || labelName.length() <= 0) {
			Tools.showAlertDialog(this, "Error", "Please enter label name.");
		} else if (latitude == -1 || longitude == -1 || radius == -1) {
			Tools.showAlertDialog(this, "Error", "Please define a region.");
		} else {
			String message = null;
			if (prevLabelName != null) {
				if (!prevLabelName.equals(labelName)) {
					ruleDataSource.updateLocationLabelName(prevLabelName, labelName);
				}
				int result = locationLabelDataSource.updateLocationLabel(prevLabelName, labelName, latitude, longitude, radius);
				if (result != 1) {
					Tools.showAlertDialog(this, "Error", "Error code = " + result);
					return;
				}
				message = "Location label updated.";
			} else {
				try {
					locationLabelDataSource.insert(labelName, latitude, longitude, radius);
				} catch (SQLiteConstraintException e) {
					if (isSetupLabel) {
						int result = locationLabelDataSource.updateLocationLabel(labelName, labelName, latitude, longitude, radius);
						if (result != 1) {
							Tools.showAlertDialog(this, "Error", "Error code = " + result);
							return;
						} else {
							message = "Location label updated.";
						}
					} else {
						Tools.showAlertDialog(this, "Error", "Label name already exists.");
						return;
					}
				} catch (SQLException e) {
					Tools.showAlertDialog(this, "Error", e.getMessage());
					return;
				}
				message = "Location label created.";
			}			

			Intent data = new Intent();
			data.putExtra(Const.BUNDLE_KEY_LABEL_NAME, labelName);
			data.putExtra(Const.BUNDLE_KEY_LABEL_TYPE, Const.LABEL_TYPE_LOCATION);
			setResult(RESULT_OK, data);

			Tools.showAlertDialog(this, "Success", message, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			});		
			
			SyncService.startSyncService(this);
		}
	}

	class WebViewJavaScriptInterface {
		private Activity activity;

		public WebViewJavaScriptInterface(Activity activity) {
			this.activity = activity;
		}

		public void onClickDoneButton(double latitude, double longitude, double radius) {
			onClickDoneButtonActivity(latitude, longitude, radius);
		}

		public void onClickCancelButton() {
			setResult(RESULT_CANCELED);
			finish();
		}

		public void makeToast(String message) {
			Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
		}
	}
}