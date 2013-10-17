package com.android.whatslog;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.FileObserver;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.android.whatslog.dao.DatabaseHelperExternal;
import com.android.whatslog.dao.DatabaseHelperInternal;
import com.android.whatslog.model.ChatList;
import com.android.whatslog.model.Messages;
import com.j256.ormlite.dao.Dao;

public class MyService extends Service {

	private FileObserver observer;
    private String pathToWatch ;
    private Dao<Messages, Integer> daoMsgInternal;
    private Dao<Messages, Integer> daoMsgExternal;
    private Dao<ChatList, Integer> daoChatInternal;
    private Dao<ChatList, Integer> daoChatExternal;

	public MyService() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}

	private void chmod(){
		Process process = null;
		DataOutputStream dataOutputStream = null;
		try {
		    process = Runtime.getRuntime().exec("su");
		    dataOutputStream = new DataOutputStream(process.getOutputStream());
		    dataOutputStream.writeBytes("chmod -R 777 "+android.os.Environment.getDataDirectory().toString()+"/data/com.whatsapp\n");
		    dataOutputStream.writeBytes("chmod -R 777 "+getApplicationContext().getPackageManager().getPackageInfo(getPackageName(), 0).applicationInfo.dataDir+"\n");
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
		pathToWatch= android.os.Environment.getDataDirectory().toString()+"/data/com.whatsapp/databases/msgstore.db";

		chmod();
	    try {
			String inFileName =  getApplicationContext().getPackageManager().getPackageInfo(getPackageName(), 0).applicationInfo.dataDir+"/databases/msgstore.db";

			File dest=new File(inFileName);

			if(!dest.exists()){

				File source=new File(pathToWatch);
				try {

					File dir=new File(getApplicationContext().getPackageManager().getPackageInfo(getPackageName(), 0).applicationInfo.dataDir+"/databases/");
					if(!dir.exists())
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
    public void onStart(Intent intent, int startId) {

//        final String pathToWatch = android.os.Environment.getDataDirectory().toString()+"/data/com.whatsapp/databases/msgstore.db";

    	  DatabaseHelperInternal internal=new DatabaseHelperInternal(getApplicationContext());
    	  DatabaseHelperExternal external=new DatabaseHelperExternal(getApplicationContext());

		    try {
				daoMsgInternal=internal.getMessagesDao();
				daoMsgExternal=external.getMessagesDao();
				daoChatInternal=internal.getChatDao();
				daoChatExternal=external.getChatDao();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

    	observer = new FileObserver(pathToWatch) { // set up a file observer to watch this directory on sd card

    	     @Override
    	     public void onEvent(int event, String file) {

    	    	 switch(event){
    	    		case FileObserver.MODIFY:
    	    			Log.d("DEBUG", "MODIFY:" + pathToWatch + file);

    	    			updateMsg();
    	    			updateChat();
    	    			break;
    	    		default:
    	    			// just ignore
    	    			break;
    	    		}

    	        // Toast.makeText(getBaseContext(), file + " was saved!", Toast.LENGTH_LONG);
    	         //}
    	     }
    	 };
    	 observer.startWatching(); //START OBSERVING


    	// For time consuming an long tasks you can launch a new thread here...
        Toast.makeText(this, " Service Started", Toast.LENGTH_LONG).show();


    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();

    }

    private void updateMsg(){

    	try {
    		List<Messages> mensagens=daoMsgInternal.queryForAll();
			List<Messages> m=daoMsgExternal.queryForAll();
			Set<Messages> nova=new HashSet<Messages>();
			for(Messages mensagem:m){

				if(!mensagens.contains(mensagem)){
					nova.add(mensagem);
				}

			}

			for(Messages mensagem:nova){
				try{
					daoMsgInternal.create(mensagem);
				}catch(SQLException e){
					e.printStackTrace();
				}
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    private void updateChat(){

    	try {
    		List<ChatList> chats=daoChatInternal.queryForAll();
			List<ChatList> m=daoChatExternal.queryForAll();
			Set<ChatList> nova=new HashSet<ChatList>();
			for(ChatList chat:m){

				if(!chats.contains(chat)){
					nova.add(chat);
				}

			}

			for(ChatList chat:nova){
				try{
					daoChatInternal.create(chat);
				}catch(SQLException e){
					e.printStackTrace();
				}
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}