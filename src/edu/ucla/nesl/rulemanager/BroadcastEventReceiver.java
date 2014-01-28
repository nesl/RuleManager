package edu.ucla.nesl.rulemanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BroadcastEventReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(final Context context, final Intent intent) {
		SyncService.startSyncService(context);
	}
}
