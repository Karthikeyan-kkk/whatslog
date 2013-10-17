package com.android.whatslog;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.whatslog.model.ChatList;

public class ContatoListaAdapter extends ArrayAdapter<ChatList> {

	private Activity activity;

	public ContatoListaAdapter(Context context, int textViewResourceId, List<ChatList> items) {
		super(context, textViewResourceId, items);
		this.activity=(Activity) context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {

			LayoutInflater vi =activity.getLayoutInflater(); //getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.contato_item, null);
		}
		ChatList chatList = getItem(position);



		ImageView imagem= (ImageView) v.findViewById(R.contatoLista.foto);

		Cursor contato=Utils.getContact(chatList.getKey_remote_jid(),activity.getContentResolver());

		if(contato!=null && contato.getCount()>0){
			Uri uri=Utils.getPhotoUri(Long.parseLong(Utils.fetchContactId(contato)),activity.getContentResolver());
			if(uri!=null){
				imagem.setImageURI(uri);
			}

			fillText(v, R.contatoLista.label, Utils.getContactDisplayNameByNumber(contato));
		}

		return v;
	}

	private void fillText(View v, int id, String text) {
		TextView textView = (TextView) v.findViewById(id);
		textView.setText(text == null ? "" : text);
	}



}