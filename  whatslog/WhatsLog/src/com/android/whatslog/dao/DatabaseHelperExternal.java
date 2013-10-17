package com.android.whatslog.dao;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.android.whatslog.R;
import com.android.whatslog.model.ChatList;
import com.android.whatslog.model.Messages;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

/**
 * Database helper which creates and upgrades the database and provides the DAOs for the app.
 *
 * @author bruno
 */
public class DatabaseHelperExternal extends OrmLiteSqliteOpenHelper {

	/************************************************
	 * Suggested Copy/Paste code. Everything from here to the done block.
	 ************************************************/

	private static final String DATABASE_NAME = "/data/data/com.whatsapp/databases/msgstore.db";
	private static final int DATABASE_VERSION = 1;
	private Dao<Messages, Integer> messagesDao;
	private Dao<ChatList, Integer> chatDao;

	public DatabaseHelperExternal(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
	}

	/************************************************
	 * Suggested Copy/Paste Done
	 ************************************************/

	@Override
	public void onCreate(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource) {
		try {
			TableUtils.createTableIfNotExists(connectionSource, Messages.class);
			TableUtils.createTableIfNotExists(connectionSource, ChatList.class);

		} catch (SQLException e) {
			Log.e(DatabaseHelperExternal.class.getName(), "Unable to create datbases", e);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, ConnectionSource arg1, int arg2,
			int arg3) {
		// TODO Auto-generated method stub

	}

	public Dao<Messages, Integer> getMessagesDao() throws SQLException {
		if (messagesDao == null) {
			messagesDao = getDao(Messages.class);
		}
		return messagesDao;
	}

	public Dao<ChatList, Integer> getChatDao() throws SQLException {
		if (chatDao == null) {
			chatDao = getDao(ChatList.class);
		}
		return chatDao;
	}
}
