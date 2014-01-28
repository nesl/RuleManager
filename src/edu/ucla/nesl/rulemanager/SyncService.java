package edu.ucla.nesl.rulemanager;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.util.ArrayList;
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

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import edu.ucla.nesl.rulemanager.db.LocationLabelDataSource;
import edu.ucla.nesl.rulemanager.db.RuleDataSource;
import edu.ucla.nesl.rulemanager.db.TimeLabelDataSource;
import edu.ucla.nesl.rulemanager.db.model.LocationLabel;
import edu.ucla.nesl.rulemanager.db.model.Rule;
import edu.ucla.nesl.rulemanager.db.model.TimeLabel;

public class SyncService extends IntentService {

	public SyncService() {
		super("SyncService");
	}

	private String serverip;
	private String username;
	private String password;

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle bundle = intent.getExtras();		
		int signalType = bundle.getInt(Const.SIGNAL_TYPE);
		serverip = bundle.getString(Const.PREFS_SERVER_IP);
		username = bundle.getString(Const.PREFS_USERNAME);
		password = bundle.getString(Const.PREFS_PASSWORD);
		
		try {
			switch (signalType) {
			case Const.SIGNAL_UPDATE_ALL:
				syncMacros();
				syncRules();
				break;
			case Const.SIGNAL_LOCATION_LABEL_UPDATED:
				if (username != null || password != null) { 
					syncMacros();
					syncRules();
				}
				break;
			case Const.SIGNAL_TIME_LABEL_UPDATED:
				if (username != null || password != null) { 
					syncMacros();
					syncRules();
				}
				break;
			case Const.SIGNAL_RULE_UPDATED:
				if (username != null || password != null) { 
					syncRules();
				}
				break;
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
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

	private List<JSONObject> getJsonListFromServer(String apiend, boolean deleteId) throws ClientProtocolException, IOException, JSONException, IllegalAccessException {
		final String url = "https://" + serverip + ":8443/api/" + apiend;

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
			jsonList.add(json);
		}

		return jsonList;
	}

	private void uploadJson(String apiend, JSONObject json) throws ClientProtocolException, IOException, IllegalAccessException {
		final String url = "https://" + serverip + ":8443/api/" + apiend;

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
		return getJsonListFromServer("macros", true);
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
		String url = "https://" + serverip + ":8443/api/macros?";

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
			for (JSONObject lr : localRules) {
				if (lr.toString().equals(sr.toString())) {
					isFound = true;
					break;
				}
			}
			if (!isFound) {
				deleteServerRule(sr);
			}
		}

		// Upload macros to server
		for (JSONObject lr : localRules) {
			boolean isFound = false;
			for (JSONObject sr : serverRules) {
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
		return getJsonListFromServer("rules", false);
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

	private void deleteServerRule(JSONObject rule) throws ClientProtocolException, IOException, IllegalAccessException {
		String url = "https://" + serverip + ":8443/api/rules?";

		List<NameValuePair> params = new LinkedList<NameValuePair>();
		try {
			params.add(new BasicNameValuePair("id", Integer.toString(rule.getInt("id"))));
		} catch (JSONException e1) {
			e1.printStackTrace();
			return;
		}

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
