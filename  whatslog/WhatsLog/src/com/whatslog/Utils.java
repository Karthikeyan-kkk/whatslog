package com.whatslog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;

public class Utils {

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
			// name =
			// contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
			// String contactId =
			// contactLookup.getString(contactLookup.getColumnIndex(BaseColumns._ID));
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
}
