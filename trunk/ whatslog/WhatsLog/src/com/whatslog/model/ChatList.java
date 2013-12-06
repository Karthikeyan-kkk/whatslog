package com.whatslog.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
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

}
