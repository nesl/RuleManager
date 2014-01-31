package edu.ucla.nesl.rulemanager;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
import edu.ucla.nesl.rulemanager.db.LocationLabelDataSource;
import edu.ucla.nesl.rulemanager.db.RuleDataSource;
import edu.ucla.nesl.rulemanager.db.TimeLabelDataSource;
import edu.ucla.nesl.rulemanager.db.model.LocationLabel;
import edu.ucla.nesl.rulemanager.db.model.Rule;
import edu.ucla.nesl.rulemanager.db.model.TimeLabel;
import edu.ucla.nesl.rulemanager.tools.Base64;
import edu.ucla.nesl.rulemanager.tools.MySSLSocketFactory;
import edu.ucla.nesl.rulemanager.tools.NetworkUtils;

public class SyncService extends IntentService {

	private static final String PORT = "9443";
	private static int SERVICE_RESTART_INTERVAL = 5 * 60; // seconds

	private String serverip;
	private String username;
	private String password;

	private Handler handler;

	public SyncService() {
		super("SyncService");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		handler = new Handler();
	}

	public static void startSyncService(Context context) {
		// Start upload service
		Intent i = new Intent(context, SyncService.class);
		context.startService(i); 
	}

	private void postToast(final String msg) {
		handler.post(new Runnable() {            
			@Override
			public void run() {
				Toast.makeText(SyncService.this, msg, Toast.LENGTH_LONG).show();                
			}
		});
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		Context context = getApplicationContext();
		int status = NetworkUtils.getConnectivityStatus(context);

		if (status == NetworkUtils.TYPE_WIFI) {
			SharedPreferences settings = context.getSharedPreferences(Const.PREFS_NAME, 0);
			serverip = settings.getString(Const.PREFS_SERVER_IP, null);
			username = settings.getString(Const.PREFS_USERNAME, null);
			password = settings.getString(Const.PREFS_PASSWORD, null);

			if (serverip != null && username != null && password != null) {
				try {
					syncMacros();
					syncRules();
					cancelNotification();
					cancelServiceSchedule();
				} catch (ClientProtocolException e) {
					e.printStackTrace();
					createNotification("Server Authentication Problem.");
				} catch (IOException e) {
					e.printStackTrace();
					createNotification("Server Connection Problem.");
					// Schedule next check.
					if (NetworkUtils.getConnectivityStatus(context) == NetworkUtils.TYPE_WIFI 
							&& !isServiceScheduled()) {
						scheduleStartService();
					}
				} catch (JSONException e) {
					e.printStackTrace();
					createNotification("JSON Exception.");
				} catch (IllegalAccessException e) {
					e.printStackTrace();
					createNotification("Server Response Problem: " + e);
				}
			}
		} else {
			cancelNotification();
			cancelServiceSchedule();
		}
	}

	private void cancelNotification() {
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.cancel(0);
	}

	private void createNotification(String message) {
		PendingIntent pintent = PendingIntent.getActivity(
				getApplicationContext(),
				0,
				new Intent(),
				PendingIntent.FLAG_UPDATE_CURRENT);

		Notification noti = new NotificationCompat.Builder(this)
		.setContentTitle("RuleManager Error")
		.setContentText(message)
		.setContentIntent(pintent)
		.setSmallIcon(R.drawable.ic_launcher)
		.build();

		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		noti.flags |= Notification.FLAG_AUTO_CANCEL;
		notificationManager.notify(0, noti);
	}

	private boolean isServiceScheduled() {
		return PendingIntent.getBroadcast(this, 0, new Intent(this, SyncService.class), PendingIntent.FLAG_NO_CREATE) != null;
	}

	private void scheduleStartService() {
		Calendar cal = Calendar.getInstance();

		Intent intent = new Intent(this, SyncService.class);
		PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);

		AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		alarm.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis() + SERVICE_RESTART_INTERVAL*1000, pintent);
	}

	private void cancelServiceSchedule() {
		Intent intent = new Intent(this, SyncService.class);
		PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);
		AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		alarm.cancel(pintent);
	}

	private HttpClient getNewHttpClient() {
		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);

			SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			registry.register(new Scheme("https", sf, 443));

			ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

			return new DefaultHttpClient(ccm, params);
		} catch (Exception e) {
			return new DefaultHttpClient();
		}
	}

	private List<JSONObject> getJsonListFromServer(String apiend, boolean deleteId, String tags) throws ClientProtocolException, IOException, JSONException, IllegalAccessException {
		String url = "https://" + serverip + ":" + PORT + "/api/" + apiend;

		if (tags != null) {
			List<NameValuePair> params = new LinkedList<NameValuePair>();
			params.add(new BasicNameValuePair("tags", tags));

			String paramString = URLEncodedUtils.format(params, "utf-8");
			url += "?" + paramString;
		}

		HttpClient httpClient = getNewHttpClient();
		HttpGet httpGet = new HttpGet(url);

		// Add authorization
		httpGet.setHeader("Authorization", "basic " + Base64.encodeToString((username + ":" + password).getBytes(), Base64.NO_WRAP));

		HttpResponse response = httpClient.execute(httpGet);
		InputStream is = response.getEntity().getContent();
		long length = response.getEntity().getContentLength();
		byte[] buffer = new byte[(int)length];
		is.read(buffer);
		String content = new String(buffer);

		if (response.getStatusLine().getStatusCode() != 200) {
			throw new IllegalAccessException("HTTP Server Error: " + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase() + "(" + content + ")");
		}

		Log.i(Const.TAG, "Server Content: " + content);

		JSONTokener tokener = new JSONTokener(content);
		JSONArray array = new JSONArray(tokener);
		List<JSONObject> jsonList = new ArrayList<JSONObject>();
		for (int i = 0; i < array.length(); i++) {
			JSONObject json = array.getJSONObject(i);
			if (deleteId) {
				json.remove("id");
			}
			json.remove("priority");
			jsonList.add(json);
		}

		return jsonList;
	}

	private void uploadJson(String apiend, JSONObject json) throws ClientProtocolException, IOException, IllegalAccessException {
		final String url = "https://" + serverip + ":" + PORT + "/api/" + apiend;

		HttpClient httpClient = getNewHttpClient();
		HttpPost httpPost = new HttpPost(url);

		// Add authorization
		httpPost.setHeader("Authorization", "basic " + Base64.encodeToString((username + ":" + password).getBytes(), Base64.NO_WRAP));

		httpPost.setHeader("Content-Type", "application/json");		
		try {
			httpPost.setEntity(new StringEntity(json.toString()));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			return;
		}

		HttpResponse response = httpClient.execute(httpPost);
		InputStream is = response.getEntity().getContent();
		long length = response.getEntity().getContentLength();
		byte[] buffer = new byte[(int)length];
		is.read(buffer);
		String content = new String(buffer);

		if (response.getStatusLine().getStatusCode() != 200) {
			throw new IllegalAccessException("HTTP Server Error: " + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase() + "(" + content + ")");			}

		Log.i(Const.TAG, "Server Content: " + content);
	}

	private void syncMacros() throws ClientProtocolException, IOException, JSONException, IllegalAccessException {
		List<JSONObject> serverMacros = getServerMacros();
		List<JSONObject> localMacros = getLocalMacros();

		// Delete macros from server
		for (JSONObject sm : serverMacros) {
			boolean isFound = false;
			for (JSONObject lm : localMacros) {
				if (lm.toString().equals(sm.toString())) {
					isFound = true;
					break;
				}
			}
			if (!isFound) {
				deleteServerMacro(sm);
			}
		}

		// Upload macros to server
		for (JSONObject lm : localMacros) {
			boolean isFound = false;
			for (JSONObject sm : serverMacros) {
				if (sm.toString().equals(lm.toString())) {
					isFound = true;
					break;
				}
			}
			if (!isFound) {
				uploadMacro(lm);
			}
		}
	}

	private List<JSONObject> getServerMacros() throws ClientProtocolException, IOException, JSONException, IllegalAccessException {
		return getJsonListFromServer("macros", true, null);
	}

	private List<JSONObject> getLocalMacros() {
		List<JSONObject> macros = new ArrayList<JSONObject>();

		LocationLabelDataSource lds = new LocationLabelDataSource(this);
		lds.open();
		List<LocationLabel> locationLabels = lds.getLocationLabels();
		lds.close();

		for (LocationLabel label : locationLabels) {
			macros.add(label.toJson());
		}

		TimeLabelDataSource tds = new TimeLabelDataSource(this);
		tds.open();
		List<TimeLabel> timeLabels = tds.getTimeLabels();
		tds.close();

		for (TimeLabel label : timeLabels) {
			macros.add(label.toJson());
		}
		return macros;
	}

	private void deleteServerMacro(JSONObject macro) throws JSONException, IllegalAccessException, ClientProtocolException, IOException {
		String url = "https://" + serverip + ":" + PORT + "/api/macros?";

		List<NameValuePair> params = new LinkedList<NameValuePair>();
		params.add(new BasicNameValuePair("macro_name", macro.getString("name")));

		String paramString = URLEncodedUtils.format(params, "utf-8");
		url += paramString;

		HttpClient httpClient = getNewHttpClient();
		HttpDelete httpDelete = new HttpDelete(url);

		// Add authorization
		httpDelete.setHeader("Authorization", "basic " + Base64.encodeToString((username + ":" + password).getBytes(), Base64.NO_WRAP));

		HttpResponse response = httpClient.execute(httpDelete);
		InputStream is = response.getEntity().getContent();
		long length = response.getEntity().getContentLength();
		byte[] buffer = new byte[(int)length];
		is.read(buffer);
		String content = new String(buffer);

		if (response.getStatusLine().getStatusCode() != 200) {
			throw new IllegalAccessException("HTTP Server Error: " + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase() + "(" + content + ")");
		}

		Log.i(Const.TAG, "Server Content: " + content);
	}

	private void uploadMacro(JSONObject json) throws ClientProtocolException, IOException, IllegalAccessException {
		uploadJson("macros", json);
	}

	private void syncRules() throws ClientProtocolException, IOException, JSONException, IllegalAccessException {
		List<JSONObject> serverRules = getServerRules();
		List<JSONObject> localRules = getLocalRules();

		// Delete rules from server
		for (JSONObject sr : serverRules) {
			boolean isFound = false;
			long id = 0;
			id = sr.getLong("id");
			sr.remove("id");
			for (JSONObject lr : localRules) {
				lr.remove("id");
				if (lr.toString().equals(sr.toString())) {
					isFound = true;
					break;
				}
			}
			if (!isFound) {
				deleteServerRule(id);
			}
		}

		// Upload rules to server
		for (JSONObject lr : localRules) {
			boolean isFound = false;
			for (JSONObject sr : serverRules) {
				sr.remove("id");
				lr.remove("id");
				if (sr.toString().equals(lr.toString())) {
					isFound = true;
					break;
				}
			}
			if (!isFound) {
				uploadRule(lr);
			}
		}
	}

	private List<JSONObject> getServerRules() throws ClientProtocolException, IOException, JSONException, IllegalAccessException {
		return getJsonListFromServer("rules", false, Const.RULE_TAG);
	}

	private List<JSONObject> getLocalRules() {
		List<JSONObject> localRules = new ArrayList<JSONObject>();
		RuleDataSource rds = new RuleDataSource(this);
		rds.open();
		List<Rule> rules = rds.getRules();
		rds.close();

		for (Rule rule : rules) {
			localRules.add(rule.toJson());
		}
		return localRules;
	}

	private void deleteServerRule(long id) throws ClientProtocolException, IOException, IllegalAccessException {
		String url = "https://" + serverip + ":" + PORT + "/api/rules?";

		List<NameValuePair> params = new LinkedList<NameValuePair>();
		params.add(new BasicNameValuePair("id", Long.toString(id)));

		String paramString = URLEncodedUtils.format(params, "utf-8");
		url += paramString;

		HttpClient httpClient = getNewHttpClient();
		HttpDelete httpDelete = new HttpDelete(url);

		// Add authorization
		httpDelete.setHeader("Authorization", "basic " + Base64.encodeToString((username + ":" + password).getBytes(), Base64.NO_WRAP));

		HttpResponse response = httpClient.execute(httpDelete);

		InputStream is = response.getEntity().getContent();

		long length = response.getEntity().getContentLength();

		byte[] buffer = new byte[(int)length];
		is.read(buffer);

		String content = new String(buffer);

		if (response.getStatusLine().getStatusCode() != 200) {
			throw new IllegalAccessException("HTTP Server Error: " + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase() + "(" + content + ")");
		}

		Log.i(Const.TAG, "Server Content: " + content);
	}

	private void uploadRule(JSONObject json) throws ClientProtocolException, IOException, IllegalAccessException {
		uploadJson("rules", json);
	}
}
