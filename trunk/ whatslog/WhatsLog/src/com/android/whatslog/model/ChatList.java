package com.android.whatslog.model;

import java.io.Serializable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

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

	@DatabaseField(generatedId=true)
	private Integer _id;

	@DatabaseField()
	private String key_remote_jid;

	@DatabaseField()
	private Integer message_table_id;

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


}
