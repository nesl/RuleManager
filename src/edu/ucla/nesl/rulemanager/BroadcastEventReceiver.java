package edu.ucla.nesl.rulemanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BroadcastEventReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(final Context context, final Intent intent) {
		
		int status = NetworkUtils.getConnectivityStatus(context);

		Log.i(Const.TAG, "Received: " + intent.getAction() + ", status = " + status);
		
		if (status == NetworkUtils.TYPE_WIFI) {
			Tools.startSyncService(context, Const.SIGNAL_UPDATE_ALL);
		}
	}
}
