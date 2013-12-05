package com.whatslog.dao;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.whatslog.R;
import com.whatslog.model.Configuracao;

/**
 * Database helper which creates and upgrades the database and provides the DAOs for the app.
 *
 * @author bruno
 */
public class DatabaseHelperConfiguracao extends OrmLiteSqliteOpenHelper {

	/************************************************
	 * Suggested Copy/Paste code. Everything from here to the done block.
	 ************************************************/

	private static final String DATABASE_NAME = "configuracao.db";
	private static final int DATABASE_VERSION = 2;
	private Dao<Configuracao, Integer> dao;

	public DatabaseHelperConfiguracao(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
	}

	/************************************************
	 * Suggested Copy/Paste Done
	 ************************************************/

	@Override
	public void onCreate(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource) {
		try {
			TableUtils.createTableIfNotExists(connectionSource, Configuracao.class);
		} catch (SQLException e) {
			Log.e(DatabaseHelperConfiguracao.class.getName(), "Unable to create datbases", e);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, ConnectionSource arg1, int oldVer, int newVer) {

		if(oldVer==1){
			try {
				getDao().executeRaw("ALTER TABLE `configuracao` ADD COLUMN miniatura INTEGER;");
				getDao().executeRaw("ALTER TABLE `configuracao` ADD COLUMN media INTEGER;");

			} catch (SQLException e) {
			}

		}
		// TODO Auto-generated method stub

	}

	public Dao<Configuracao, Integer> getDao() throws SQLException {
		if (dao == null) {
			dao = getDao(Configuracao.class);
		}
		return dao;
	}
}