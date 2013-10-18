package com.android.whatslog.activities;

import java.sql.SQLException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.whatslog.R;
import com.android.whatslog.R.layout;
import com.android.whatslog.R.listaLog;
import com.android.whatslog.R.menu;
import com.android.whatslog.adapter.ContatoListaAdapter;
import com.android.whatslog.dao.DatabaseHelperInternal;
import com.android.whatslog.model.ChatList;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;

public class LogActivity extends OrmLiteBaseActivity<DatabaseHelperInternal> {

	private ListView contatos;
	private Activity activity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_log);
		this.activity=this;
		contatos=(ListView) findViewById(R.listaLog.listaContatos);

		try {
			ContatoListaAdapter adapter=new ContatoListaAdapter(this, R.layout.contato_item, getHelper().getChatDao().queryBuilder().groupBy("key_remote_jid").query() );
			contatos.setAdapter(adapter);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		contatos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapterView,
					View view, int i, long l) {
				ChatList contato = (ChatList) contatos.getAdapter().getItem(i);
				Intent intent=new Intent(activity, ChatActivity.class);
				intent.putExtra("id", contato.getKey_remote_jid());
				startActivity(intent);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.log, menu);
		return true;
	}

}
