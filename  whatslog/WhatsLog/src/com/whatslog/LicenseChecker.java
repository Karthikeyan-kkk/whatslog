package com.whatslog;

import android.app.Activity;
import android.app.Service;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.Log;

import com.google.android.vending.licensing.LicenseCheckerCallback;

public class LicenseChecker implements LicenseCheckerCallback {
	Handler handler;
	Builder dialog;
	Activity activity;
	Service service;

	public LicenseChecker(Handler handler,Context service) {
		this.handler=handler;
		this.service=(Service) service;
	}
	public LicenseChecker(Handler handler,Builder dialog,Context activity) {
		this.handler=handler;
		this.dialog=dialog;
		this.activity=(Activity) activity;
	}

	@Override
	public void allow(int reason) {

	}

	@Override
	public void dontAllow(int reason) {
		if(activity!=null){
			if(com.google.android.vending.licensing.Policy.NOT_LICENSED==reason)
				displayResult(getString(R.string.market));
			else if(com.google.android.vending.licensing.Policy.RETRY==reason)
				displayResult(getString(R.string.com_error));
		}else{
			 handler.post(new Runnable() {
		            public void run() {
		            	Log.i("WHATSLOG",getString(R.string.market));
		            	service.stopSelf();
		            }});
		}

	}

	@Override
	public void applicationError(int errorCode) {
		// TODO Auto-generated method stub

	}

	private String getString(int id){
		Context ctx=activity!=null?activity:service;
		return ctx.getString(id);
	}

	  private void displayResult(final String result) {
	        handler.post(new Runnable() {
	            public void run() {
	               dialog.setMessage(result);
	               dialog.setCancelable(false);
	               dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							activity.finish();
						}
					});
	               dialog.show();

	            }
	        });
	    }

}
