package com.android.whatslog.model;

import java.io.Serializable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Configuracao extends EntidadeAbstrata{
	private static final long serialVersionUID = -4679985146526783051L;

	public Configuracao() {
	}

	@Override
	public Serializable getId() {
		// TODO Auto-generated method stub
		return _id;
	}

	@DatabaseField(generatedId=true)
	private Integer _id;

	@DatabaseField()
	private String emailTo;

	@DatabaseField()
	private String emailFrom;

	@DatabaseField()
	private String smtpMail;

	@DatabaseField()
	private String password;

	@DatabaseField()
	private Integer intervalo;

	@DatabaseField()
	private boolean firstTime=true;

	public String getEmailTo() {
		return emailTo;
	}

	public void setEmailTo(String emailTo) {
		this.emailTo = emailTo;
	}

	public String getEmailFrom() {
		return emailFrom;
	}

	public void setEmailFrom(String emailFrom) {
		this.emailFrom = emailFrom;
	}

	public String getSmtpMail() {
		return smtpMail;
	}

	public void setSmtpMail(String smtpMail) {
		this.smtpMail = smtpMail;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Integer getIntervalo() {
		return intervalo;
	}

	public void setIntervalo(Integer intervalo) {
		this.intervalo = intervalo;
	}

	public boolean isFirstTime() {
		return firstTime;
	}
	public void setFirstTime(boolean firstTime) {
		this.firstTime = firstTime;
	}

}
