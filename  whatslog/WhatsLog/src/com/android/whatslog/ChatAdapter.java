package com.android.whatslog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.whatslog.model.Messages;

public class ChatAdapter extends ArrayAdapter<Messages> {

	private Activity activity;

	public ChatAdapter(Context context, int textViewResourceId, List<Messages> items) {
		super(context, textViewResourceId, items);
		this.activity=(Activity) context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {

			LayoutInflater vi =activity.getLayoutInflater(); //getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.chat_item, null);
		}
		Messages mensagem = getItem(position);

		TextView textView = (TextView) v.findViewById(R.chatLista.mensagem);
		TextView hora = (TextView) v.findViewById(R.chatLista.hora);
		TextView quem = (TextView) v.findViewById(R.chatLista.quem);

		//Date data=new Date(mensagem.getTimestamp());
		Date data=mensagem.getTimestamp();
		hora.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(data));

		textView.setText(mensagem.getData().trim());
		if(mensagem.getKey_from_me()==1){
			textView.setBackgroundResource(R.drawable.fromme);

			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)textView.getLayoutParams();
			params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			textView.setLayoutParams(params); //causes layout update
			textView.setGravity(Gravity.LEFT);

			RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams)hora.getLayoutParams();
			params2.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			hora.setLayoutParams(params2); //causes layout update


			RelativeLayout.LayoutParams params3 = (RelativeLayout.LayoutParams)quem.getLayoutParams();
			params3.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			quem.setLayoutParams(params3); //causes layout update
			quem.setText("Me");

		}
		else{
			textView.setBackgroundResource(R.drawable.fromother);
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)textView.getLayoutParams();
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			textView.setLayoutParams(params); //causes layout update
			textView.setGravity(Gravity.RIGHT);

			RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams)hora.getLayoutParams();
			params2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			hora.setLayoutParams(params2); //causes layout update

			RelativeLayout.LayoutParams params3 = (RelativeLayout.LayoutParams)quem.getLayoutParams();
			params3.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			quem.setLayoutParams(params3); //causes layout update

			Cursor contato=Utils.getContact(mensagem.getKey_remote_jid(),activity.getContentResolver());
			try{
				quem.setText(Utils.getContactDisplayNameByNumber(contato));
			}catch(Exception e){
				quem.setText( mensagem.getKey_remote_jid());
			}
		}


		return v;
	}

	private void fillText(View v, int id, String text) {
		TextView textView = (TextView) v.findViewById(id);
		textView.setText(text == null ? "" : text);
	}

}