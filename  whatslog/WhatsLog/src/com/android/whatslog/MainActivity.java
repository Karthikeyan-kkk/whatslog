package com.android.whatslog;

import java.sql.SQLException;
import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.whatslog.dao.DatabaseHelperConfiguracao;
import com.android.whatslog.dao.DatabaseHelperInternal;
import com.android.whatslog.model.Configuracao;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;

public class MainActivity extends OrmLiteBaseActivity<DatabaseHelperInternal> {

	private EditText smtp;
	private EditText intervalo;
	private EditText to;
	private EditText password;
	private Button btn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(isFirstTime()){
			setContentView(R.layout.conf);
			smtp= (EditText) findViewById(R.configuracao.smtp);
			intervalo= (EditText) findViewById(R.configuracao.intervalo);
			to= (EditText) findViewById(R.configuracao.to);
			password= (EditText) findViewById(R.configuracao.password);
			btn=(Button) findViewById(R.configuracao.btn);
		}else{
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

	private void setTimerEmail(){
		DatabaseHelperConfiguracao database=new DatabaseHelperConfiguracao(this);

		List<Configuracao> confs;
		try {
			confs = database.getDao().queryForAll();
			Configuracao conf=null;

			if(confs.size()>0){
				conf=confs.get(0);
			}
			if(conf!=null){
			        Intent intent = new Intent(this, MailService.class);

			        PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);

			        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			        //for 30 mint 60*60*1000
			        alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), conf.getIntervalo()*60*1000, pintent);

			       // Toast.makeText(this, "The new Service was Created", Toast.LENGTH_LONG).show();

			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// Start the  service
	public void startNewService(View view) {

		startService(new Intent(this, MyService.class));
		setTimerEmail();
	}

	// Stop the  service
	public void stopNewService(View view) {

		stopService(new Intent(this, MyService.class));
		stopService(new Intent(this, MailService.class));

	}

	private boolean isFirstTime(){
		DatabaseHelperConfiguracao database=new DatabaseHelperConfiguracao(this);
		List<Configuracao> confs;
		try {
			confs = database.getDao().queryForAll();
			if(confs.size()>0){
				return false;
			}
			return true;
		} catch (SQLException e) {
			return false;
		}


	}

	public void showLog(View view){

//		GMailSender sender = new GMailSender("bruno.teixeira.canto@gmail.com", "apolinario");
//        try {
//			sender.sendMail("This is Subject",
//			        "This is Body",
//			        "bruno.teixeira.canto@gmail.com",
//			        "bruno.teixeira.canto@gmail.com");
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}


		Intent intent = new Intent(this, LogActivity.class);
		setIntent(intent);
		startActivityForResult(intent, 1);
	}

	public void sendMail(View view){
		Intent emailIntent = new Intent(
                android.content.Intent.ACTION_SEND);
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
                "bruno.teixeira.canto@gmail.com");
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                "teste");
        emailIntent.setType("plain/text");
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                "teste");
        startActivity(emailIntent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void save(View view){

		try {
			DatabaseHelperConfiguracao database=new DatabaseHelperConfiguracao(this);

			List<Configuracao> confs=database.getDao().queryForAll();
			Configuracao conf=new Configuracao();

			if(confs.size()>0){
				conf=confs.get(0);
			}

			conf.setEmailTo(to.getText().toString());
			conf.setPassword(password.getText().toString());
			conf.setSmtpMail(smtp.getText().toString());
			conf.setFirstTime(false);
			conf.setIntervalo(Integer.parseInt(intervalo.getText().toString()));

			database.getDao().createOrUpdate(conf);

			finish();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
