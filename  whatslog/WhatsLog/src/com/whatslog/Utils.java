package com.whatslog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.MediaStore;
import android.provider.Settings.Secure;
import android.util.Base64;

import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.ServerManagedPolicy;
import com.whatslog.dao.DatabaseHelperConfiguracao;
import com.whatslog.model.Configuracao;

public class Utils {

	public static final String BASE64_PUBLIC_KEY="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtNBwCmA8QI1p0POCCxNbWYJNw4RP9r3SvumIDcbnSmZQvIGtceRvAU521LR+v8rWd+H4sseghzjGTDHoVu8bSEth1i5iPnFhEn+X7JKnicP+ZWP2AjcYjFCJ/mDLfBYNPVpLe3UiD53Jqswu6JBjEjvDF9Xk8PfiKH0H49ydeTCpnWeyYSEfD07iqv+BpIzKYckaEqACJzKBDfVLP5RNGOPhClcs8Jfpu8+oI7ILzn6hsIvpghmqzrglDZgplMh1Fz2dePYNic/TOS/jexUt2OmofKyu32pwjtcW0tO+nfgMAQ9kXOnbs7GBVJofKwrf1q9zMRoDUsFZV5sddskECQIDAQAB";
	public static final byte[] SALT = new byte[] {-92,98,-32,49,65,34,23,44,65,-23,-12,-9,-3,5,-23,23,-94,123,-11,4};
	private static ScheduledExecutorService scheduleTaskExecutor;
	public static ScheduledFuture<?> scheduledFuture;
	private static Configuracao configuracao;

	public static String getDeviceId(ContentResolver contentResolver){
		String deviceId = Secure.getString(contentResolver,Secure.ANDROID_ID);
		return "_#$A12abk%"+deviceId;
	}

	public static boolean isDebugglabe(Context context){
		return  ( 0 != ( context.getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE ) );
	}


	public static ScheduledExecutorService getScheduleTaskExecutor() {
		if(scheduleTaskExecutor==null)
			scheduleTaskExecutor = Executors.newSingleThreadScheduledExecutor();
		return scheduleTaskExecutor;
	}

	public static void verifyLicense(Context ctx){
		if(!isDebugglabe(ctx)){
			Handler mHandler;
			LicenseCheckerCallback mLicenseCheckerCallback;
		    LicenseChecker mChecker;

		    mHandler=new Handler();

			if(ctx instanceof Activity){
				Builder dialog=new AlertDialog.Builder(ctx);

				mLicenseCheckerCallback=new com.whatslog.LicenseChecker(mHandler,dialog,ctx);
			}
			else
				mLicenseCheckerCallback=new com.whatslog.LicenseChecker(mHandler,ctx);


		    mChecker = new LicenseChecker(ctx, new ServerManagedPolicy(ctx, new AESObfuscator(Utils.SALT, ctx.getPackageName(), Utils.getDeviceId(ctx.getContentResolver()))), Utils.BASE64_PUBLIC_KEY);

		    mChecker.checkAccess(mLicenseCheckerCallback);
		}

	}

	public static void copyFile(File source, File dest) throws IOException {
		FileInputStream fis = new FileInputStream(source);

		OutputStream output = new FileOutputStream(dest);
		// Transfer bytes from the inputfile to the outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ((length = fis.read(buffer)) > 0) {
			output.write(buffer, 0, length);
		}

		// Close the streams
		output.flush();
		output.close();
		fis.close();

	}

	public static void createFile(File dest, String msg) throws IOException {

		OutputStream output = new FileOutputStream(dest);
		output.write(msg.getBytes());

		// Close the streams
		output.flush();
		output.close();

	}


	public static Cursor getContact(String number,
			ContentResolver contentResolver) {
		Uri uri = Uri.withAppendedPath(
				ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(number));

		Cursor contactLookup = contentResolver.query(uri, new String[] {
				BaseColumns._ID, ContactsContract.PhoneLookup.DISPLAY_NAME },
				null, null, null);

		if (contactLookup != null && contactLookup.getCount() > 0) {
			contactLookup.moveToNext();
			return contactLookup;
		}

		return contactLookup;
	}

	public static String getContactDisplayNameByNumber(Cursor contactLookup) {
		return contactLookup.getString(contactLookup
				.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
	}

	public static String getContactDisplayNameByNumber(String number,
			ContentResolver contentResolver) {
		Cursor contact = getContact(number, contentResolver);
		return getContactDisplayNameByNumber(contact);
	}

	public static Uri getPhotoUri(long contactId,
			ContentResolver contentResolver) {

		try {
			Cursor cursor = contentResolver
					.query(ContactsContract.Data.CONTENT_URI,
							null,
							ContactsContract.Data.CONTACT_ID
									+ "="
									+ contactId
									+ " AND "

									+ ContactsContract.Data.MIMETYPE
									+ "='"
									+ ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
									+ "'", null, null);

			if (cursor != null) {
				if (!cursor.moveToFirst()) {
					return null; // no photo
				}
			} else {
				return null; // error in cursor process
			}

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		Uri person = ContentUris.withAppendedId(
				ContactsContract.Contacts.CONTENT_URI, contactId);
		return Uri.withAppendedPath(person,
				ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
	}

	public static String fetchContactId(Cursor cFetch) {

		String contactId = "";

		if (cFetch.moveToFirst()) {
			cFetch.moveToFirst();

			contactId = cFetch
					.getString(cFetch.getColumnIndex(PhoneLookup._ID));

		}

		return contactId;

	}

	public static boolean isConnected(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mobileInfo = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		State mobile = NetworkInfo.State.DISCONNECTED;
		if (mobileInfo != null) {
			mobile = mobileInfo.getState();
		}
		NetworkInfo wifiInfo = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		State wifi = NetworkInfo.State.DISCONNECTED;
		if (wifiInfo != null) {
			wifi = wifiInfo.getState();
		}
		boolean dataOnWifiOnly = (Boolean) PreferenceManager
				.getDefaultSharedPreferences(context).getBoolean(
						"data_wifi_only", true);
		if ((!dataOnWifiOnly && (mobile.equals(NetworkInfo.State.CONNECTED) || wifi
				.equals(NetworkInfo.State.CONNECTED)))
				|| (dataOnWifiOnly && wifi.equals(NetworkInfo.State.CONNECTED))) {
			return true;
		} else {
			return false;
		}
	}

	public static void showIcon(boolean show,Context ctx){
		ComponentName componentToDisable = new ComponentName("com.whatslog", "com.whatslog.activities.MainActivity");
		PackageManager p = ctx.getPackageManager();
		if(show)
			p.setComponentEnabledSetting(componentToDisable, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
		else
			p.setComponentEnabledSetting(componentToDisable, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
	}

	public static String encodeBase64(byte[] data){
		return Base64.encodeToString(data, Base64.NO_WRAP);
	}

	public static byte[] getMediaFile(String tipo,long tamanho,String data,int dias){
		String path=Environment.getExternalStorageDirectory() + "/WhatsApp/Media/WhatsApp "+tipo;

		String prefixo="";
		if(tipo.equals("Video"))
			prefixo="vid";
		else if(tipo.equals("Images"))
			prefixo="img";
		else if(tipo.equals("Audio"))
			prefixo="aud";

		final String nome=prefixo+"-"+data;
	    File dir = new File(path);

		File[] arquivos=dir.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String filename) {
				return filename.toLowerCase().startsWith(nome);
			}
		});

		for(File file : arquivos){
			if(file.length()==tamanho)
				try {
					return com.gmailsender.Utils.getBytes(file);
				} catch (IOException e) {
					break;
				}
		}
		return null;
	}

	public static String getFilePathFromContentUri(Uri uri,
	        ContentResolver contentResolver) {
		String fileName="unknown";//default fileName
	    Uri filePathUri = uri;
	    if (uri.getScheme().toString().compareTo("content")==0)
	    {
	        Cursor cursor = contentResolver.query(uri, null, null, null, null);
	        if (cursor.moveToFirst())
	        {
	            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);//Instead of "MediaStore.Images.Media.DATA" can be used "_data"
	            filePathUri = Uri.parse(cursor.getString(column_index));
	            fileName = filePathUri.getLastPathSegment().toString();
	        }
	    }
	    else if (uri.getScheme().compareTo("file")==0)
	    {
	        fileName = filePathUri.getLastPathSegment().toString();
	    }
	    else
	    {
	        fileName = fileName+"_"+filePathUri.getLastPathSegment();
	    }
	    return fileName;
	}
	public static byte[] getBytesFromInputStream(InputStream is)
	{
	    try
	    {
	    	ByteArrayOutputStream os = new ByteArrayOutputStream();

	        byte[] buffer = new byte[0xFFFF];

	        for (int len; (len = is.read(buffer)) != -1;)
	            os.write(buffer, 0, len);

	        os.flush();

	        return os.toByteArray();
	    }
	    catch (IOException e)
	    {
	        return null;
	    }
	}

	public static boolean estaNoIntervalo(Date data,int dias){
		Calendar dia = Calendar.getInstance();

		dia.set(Calendar.HOUR_OF_DAY, 0);
		dia.set(Calendar.MINUTE, 0);
		dia.set(Calendar.SECOND, 0);
		dia.set(Calendar.MILLISECOND, 0);

		dia.add(Calendar.DATE, dias*-1);

		return (data.getTime()>=dia.getTimeInMillis());
	}

	public static byte[] compactar(Map<String, byte[]> anexos){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipOutputStream zip = new ZipOutputStream(baos);

		Set<String> keys=anexos.keySet();
		try{
			for(String key:keys){

				byte[] anexo=anexos.get(key);
				if(anexo!=null){
					ZipEntry entry=new ZipEntry(key);
					zip.putNextEntry(entry);
					zip.write(anexo);
					zip.closeEntry();
				}
			}

			zip.close();

			return baos.toByteArray();
		}catch(IOException e){
			return null;
		}
	}

	public static Configuracao getConfiguracao(Context context) {
		if (configuracao == null) {
			DatabaseHelperConfiguracao database = new DatabaseHelperConfiguracao(
					context);
			List<Configuracao> confs;
			try {
				confs = database.getDao().queryForAll();
				Configuracao conf = null;

				if (confs.size() > 0) {
					conf = confs.get(0);
				}
				if (conf != null) {
					configuracao = conf;
				}
			} catch (Exception e) {
			}
		}
		return configuracao;
	}
}
