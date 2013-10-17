package com.android.whatslog;

import java.sql.SQLException;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.whatslog.dao.DatabaseHelperConfiguracao;
import com.android.whatslog.model.Configuracao;

public class ConfActivity extends Activity {

	private EditText smtp;
	private EditText intervalo;
	private EditText to;
	private EditText password;
	private Button btn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.conf);

		smtp= (EditText) findViewById(R.configuracao.smtp);
		intervalo= (EditText) findViewById(R.configuracao.intervalo);
		to= (EditText) findViewById(R.configuracao.to);
		password= (EditText) findViewById(R.configuracao.password);
		btn=(Button) findViewById(R.configuracao.btn);

		List<Configuracao> confs;
		try {
			DatabaseHelperConfiguracao database=new DatabaseHelperConfiguracao(this);

			confs = database.getDao().queryForAll();
			Configuracao conf=null;

			if(confs.size()>0){
				conf=confs.get(0);
			}
			if(conf!=null){
				smtp.setText(conf.getSmtpMail());
				intervalo.setText(conf.getIntervalo().toString());
				to.setText(conf.getEmailTo());
				password.setText(conf.getPassword());
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.conf, menu);
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
