package com.whatslog.model;

import java.io.Serializable;
import java.util.Date;

import android.content.ContentResolver;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.whatslog.Utils;

@DatabaseTable
public class Messages extends EntidadeAbstrata implements Comparable<Messages>{
	private static final long serialVersionUID = -4679985146526783051L;

	public Messages() {
	}

	@Override
	public Serializable getId() {
		// TODO Auto-generated method stub
		return _id;
	}

	@DatabaseField(generatedId=true)
	private Integer _id;

	//@DatabaseField()
	//private String key_remote_jid;

	@DatabaseField()
	private int key_from_me;

	@DatabaseField()
	private String key_id;

	@DatabaseField()
	private int status;

	@DatabaseField()
	private int needs_push;

	@DatabaseField()
	private String data;

	@DatabaseField(dataType=DataType.DATE_LONG)
	private Date timestamp;

	@DatabaseField()
	private String media_url;

	@DatabaseField()
	private String media_mime_type;

	@DatabaseField()
	private String media_wa_type;

	@DatabaseField()
	private String media_size;

	@DatabaseField()
	private String media_name;

	@DatabaseField()
	private String media_hash;

	@DatabaseField()
	private double latitude;

	@DatabaseField()
	private double longitude;

	@DatabaseField(dataType=DataType.BYTE_ARRAY)
	private byte[] thumb_image;

	@DatabaseField()
	private String remote_resource;

	@DatabaseField(dataType=DataType.DATE_LONG)
	private Date received_timestamp;

	@DatabaseField()
	private int send_timestamp;

	@DatabaseField()
	private int receipt_server_timestamp;

	@DatabaseField( dataType = DataType.BYTE_ARRAY)
	private byte[] raw_data=null;

	@DatabaseField()
	private int recipient_count;

	@DatabaseField()
	private int media_duration;

	@DatabaseField()
	private int origin;

	@DatabaseField(foreign=true, foreignColumnName="key_remote_jid",columnName="key_remote_jid")
	private ChatList chatList;

	public String getKey_remote_jid() {
		return chatList.getKey_remote_jid();
	}
//
//	public void setKey_remote_jid(String key_remote_jid) {
//		this.key_remote_jid = key_remote_jid;
//	}

	public int getKey_from_me() {
		return key_from_me;
	}

	public void setKey_from_me(int key_from_me) {
		this.key_from_me = key_from_me;
	}

	public String getKey_id() {
		return key_id;
	}

	public void setKey_id(String key_id) {
		this.key_id = key_id;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getNeeds_push() {
		return needs_push;
	}

	public void setNeeds_push(int needs_push) {
		this.needs_push = needs_push;
	}

	public String getData() {
		if(data==null)
			data="";
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getMedia_url() {
		return media_url;
	}

	public void setMedia_url(String media_url) {
		this.media_url = media_url;
	}

	public String getMedia_mime_type() {
		return media_mime_type;
	}

	public void setMedia_mime_type(String media_mime_type) {
		this.media_mime_type = media_mime_type;
	}

	public String getMedia_wa_type() {
		return media_wa_type;
	}

	public void setMedia_wa_type(String media_wa_type) {
		this.media_wa_type = media_wa_type;
	}

	public String getMedia_size() {
		return media_size;
	}

	public void setMedia_size(String media_size) {
		this.media_size = media_size;
	}

	public String getMedia_name() {
		return media_name;
	}

	public void setMedia_name(String media_name) {
		this.media_name = media_name;
	}

	public String getMedia_hash() {
		return media_hash;
	}

	public void setMedia_hash(String media_hash) {
		this.media_hash = media_hash;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public byte[] getThumb_image() {
		return thumb_image;
	}

	public void setThumb_image(byte[] thumb_image) {
		this.thumb_image = thumb_image;
	}

	public String getRemote_resource() {
		return remote_resource;
	}

	public void setRemote_resource(String remote_resource) {
		this.remote_resource = remote_resource;
	}

	public Date getReceived_timestamp() {
		return received_timestamp;
	}

	public void setReceived_timestamp(Date received_timestamp) {
		this.received_timestamp = received_timestamp;
	}

	public int getSend_timestamp() {
		return send_timestamp;
	}

	public void setSend_timestamp(int send_timestamp) {
		this.send_timestamp = send_timestamp;
	}

	public int getReceipt_server_timestamp() {
		return receipt_server_timestamp;
	}

	public void setReceipt_server_timestamp(int receipt_server_timestamp) {
		this.receipt_server_timestamp = receipt_server_timestamp;
	}

	public int getRecipient_count() {
		return recipient_count;
	}

	public void setRecipient_count(int recipient_count) {
		this.recipient_count = recipient_count;
	}

	public int getMedia_duration() {
		return media_duration;
	}

	public void setMedia_duration(int media_duration) {
		this.media_duration = media_duration;
	}

	public int getOrigin() {
		return origin;
	}

	public void setOrigin(int origin) {
		this.origin = origin;
	}

	public byte[] getRaw_data() {
		return raw_data;
	}
	public void setRaw_data(byte[] raw_data) {
		this.raw_data = raw_data;
	}

	@Override
	public int compareTo(Messages another) {
		if(getTimestamp().after(another.getTimestamp()))
			return 1;
		else if(getTimestamp().before(another.getTimestamp()))
			return -1;
		return 0;
	}

	public ChatList getChatList() {
		return chatList;
	}
	public void setChatList(ChatList chatList) {
		this.chatList = chatList;
	}

	public boolean isVideo(){
		return getMedia_wa_type().trim().equals("3");
	}
	public boolean isAudio(){
		return getMedia_wa_type().trim().equals("2");
	}
	public boolean isImagem(){
		return getMedia_wa_type().trim().equals("1");
	}
	public boolean isMap(){
		return getMedia_wa_type().trim().equals("5");
	}

	public boolean isMedia(){
		return isVideo() || isAudio() || isMap() || isImagem();
	}

	public String getNome(ContentResolver contentResolver) {
		if(getKey_from_me()==1)
			return "";
		String nome = getKey_remote_jid();
		try {
			nome = Utils.getContactDisplayNameByNumber(getKey_remote_jid(),
					contentResolver);
		} catch (Exception e) {
		}
		return nome;
	}
}
