package com.whatslog.activities;

import java.sql.SQLException;
import java.util.List;

import android.os.Bundle;
import android.view.Menu;
import android.widget.ListView;

import com.android.whatslog.R;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.whatslog.adapter.ChatAdapter;
import com.whatslog.dao.DatabaseHelperInternal;
import com.whatslog.model.Messages;

public class ChatActivity extends OrmLiteBaseActivity<DatabaseHelperInternal> {
	private ListView chat;
	private String id;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat_list);

		chat=(ListView) findViewById(R.listaChat.listaChat);

		id=getIntent().getStringExtra("id");

	}
	@Override
	protected void onResume() {
		super.onResume();
		fillList();
	}

	private void fillList(){
		List<Messages> mensagens=null;
		try {
			mensagens = getHelper().getMessagesDao().queryBuilder().orderBy("timestamp", true).where().eq("key_remote_jid", id).query();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		ChatAdapter adapter=new ChatAdapter(this, R.layout.chat_item, mensagens);

		chat.setAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.chat, menu);
		return true;
	}

}
