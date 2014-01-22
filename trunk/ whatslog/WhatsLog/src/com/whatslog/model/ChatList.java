package com.whatslog.model;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import com.whatslog.Utils;

@DatabaseTable(tableName="chat_list")
public class ChatList extends EntidadeAbstrata{
	private static final long serialVersionUID = -4679985146526783051L;

	public ChatList() {
	}

	@Override
	public Serializable getId() {
		// TODO Auto-generated method stub
		return _id;
	}

	@DatabaseField()
	private Integer _id;

	@DatabaseField(id=true)
	private String key_remote_jid;

	@DatabaseField()
	private Integer message_table_id;

	@ForeignCollectionField(eager=false,orderAscending=false,orderColumnName="timestamp", foreignFieldName="chatList")
	private ForeignCollection<Messages> mensagens;

	@DatabaseField()
	private String subject;

	@DatabaseField(dataType=DataType.DATE_LONG)
	private Date creation;

	private String photo;

	public String getKey_remote_jid() {
		return key_remote_jid;
	}

	public void setKey_remote_jid(String key_remote_jid) {
		this.key_remote_jid = key_remote_jid;
	}

	public Integer getMessage_table_id() {
		return message_table_id;
	}

	public void setMessage_table_id(Integer message_table_id) {
		this.message_table_id = message_table_id;
	}

	public List<Messages> getMensagens() {
		return new ArrayList<Messages>(mensagens);
	}

	public void setMensagens(ForeignCollection<Messages> mensagens) {
		this.mensagens = mensagens;
	}

	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public Date getCreation() {
		return creation;
	}
	public void setCreation(Date creation) {
		this.creation = creation;
	}

	public String getNome(ContentResolver contentResolver){
		if(getSubject()!=null && getSubject()!="")
			return getSubject();

		String nome = getKey_remote_jid();
		try {
			nome = Utils.getContactDisplayNameByNumber(getKey_remote_jid(),
					contentResolver);
		} catch (Exception e) {
		}
		return nome;
	}

	public String getPhoto(ContentResolver contentResolver){
		if(photo==null){
			//photo= "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";
			photo="iVBORw0KGgoAAAANSUhEUgAAAIAAAACACAMAAAD04JH5AAAAA3NCSVQICAjb4U/gAAAASFBMVEX////k5OTe3t7V1dbMzMzFxca9vb29vcW1tbatra2lpaafn6GZmZmUlJWPj5GMjI2Hh4iEhIV+foB6enxzc3tycnRubnBqamz1rEYLAAAAAXRSTlMAQObYZgAAAAlwSFlzAAAK8AAACvABQqw0mAAAAoVJREFUeNrt2tmSozAMBVBkGxqzJeA2/P+fTqamZqar0p1I4ip+iO87xSkvwgvNUThNBVRABVRABVSA9sFtGqbpsm15S68E/H7Zvo59oK8J8foiQPYUPH0XrzXIAB09iB93a8BEj+NiMgWsjp4mZjvAQJy4xQiQW2ImmgAujtgZDACRJBngANn7iSYwQPp+cgkKmEmcgARkJwfwhwED0CveTy7DABupEmGATgfgNsFTQCZlBhBg1gICCDBpAQ7VBb4wQFcGkIDDV4AO4Cvg7QHh7QHt2wM6GKDVAXoYIJquyRgA5aJwxO0LdH0w4wC6RdGKA1wN6xALkAyXA4ZdEFYUQLkzYe4MngPUGwNeLzR2DUCUIYCgB2wQQFsacKILEgQQS4+BqfQsWNWAgAHsakAPqoTqeTiCANFyFnIA2kHgYV9D5bI4wgCDbhImGGBvzYYg87R8D0ZzkH1fkMUC5saMf2Uj/CS1OxogW5g6/hUm+9ZM1AndgQeMJe+MxAemyQAgGYb8GyMJYMHflsgAB78PVhtAxH4G5YANXYWlAHYpWK0AI/gzIAYwy/HFDJANGkD2Ewu6DIsBrEqwlAZcDQEePgmFgADbjigBbWlA9/aAvjQggpdjNi1gCOCtSNpkBGD/TudGE8BVcEoQNjxAtC+5rYsTFvApPy4N4ycMMCuPi1mGp4AtKn9i+TMlxnwKkMcTh/V/DVPWApaOMPl4YPgRkE41/f2JwZwlgH1qCZ5u3pmAS+/IJt8Y7gBp8GQY1y37A8A+t2Sem+EHwGrW9HeG/l87NP+nvKeXpk9fAbApLz5Nbk5X2zNF8lYbGkS1Vceno6Gi8akwgEJpAFVABVRABRQH/AIdJV6rCRVksgAAAABJRU5ErkJggg==";
			Cursor contato=Utils.getContact(getKey_remote_jid(),contentResolver);
			if(contato!=null && contato.getCount()>0){
				Uri uri=Utils.getPhotoUri(Long.parseLong(Utils.fetchContactId(contato)),contentResolver);
				if(uri!=null){
					try {
						byte[] f=Utils.getBytesFromInputStream(contentResolver.openInputStream(uri));
						photo= Utils.encodeBase64(f);
					} catch (FileNotFoundException e) {
					}
				}
			}
		}
		return photo;
	}
}
