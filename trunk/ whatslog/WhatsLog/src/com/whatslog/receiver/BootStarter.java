package com.whatslog.receiver;

import com.whatslog.MainService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootStarter extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		Intent myIntent = new Intent(context, MainService.class);
		context.startService(myIntent);
	}
}