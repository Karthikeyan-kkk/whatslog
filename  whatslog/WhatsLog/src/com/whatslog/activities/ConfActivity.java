package com.whatslog.activities;

import java.sql.SQLException;
import java.util.List;

import android.app.Activity;
import android.opengl.Visibility;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.whatslog.R;
import com.whatslog.dao.DatabaseHelperConfiguracao;
import com.whatslog.model.Configuracao;

public class ConfActivity extends Activity {

	private EditText intervalo,to,dialer,subject;
	private CheckBox media,minuatura;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.conf);

		intervalo = (EditText) findViewById(R.configuracao.intervalo);
		to = (EditText) findViewById(R.configuracao.to);
		dialer = (EditText) findViewById(R.configuracao.dialer);
		subject = (EditText) findViewById(R.configuracao.subject);
		media = (CheckBox) findViewById(R.configuracao.media);
		minuatura = (CheckBox) findViewById(R.configuracao.miniatura);

		List<Configuracao> confs;
		try {
			DatabaseHelperConfiguracao database = new DatabaseHelperConfiguracao(
					this);

			confs = database.getDao().queryForAll();
			Configuracao conf = null;

			if (confs.size() > 0) {
				conf = confs.get(0);
			}
			if (conf != null) {
				intervalo.setText(conf.getIntervalo().toString());
				to.setText(conf.getEmailTo());
				dialer.setText(conf.getDialer());
				subject.setText(conf.getSubject());
				minuatura.setChecked(conf.isMiniatura());
				media.setChecked(conf.isMedia());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.conf, menu);
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
				conf.setMiniatura(minuatura.isChecked());
				conf.setMedia(media.isChecked());

				database.getDao().createOrUpdate(conf);

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
				|| dialer.getText().toString().length()==0) {
			Toast.makeText(this, "All fields is required!", Toast.LENGTH_LONG).show();
			return false;
		}
		return true;
	}

}
