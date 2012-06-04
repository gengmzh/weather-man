package org.weather.weatherman.content;

import java.util.Date;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

public class SettingService {

	private DatabaseSupport databaseSupport;

	public SettingService(DatabaseSupport databaseSupport) {
		super();
		this.databaseSupport = databaseSupport;
	}

	public Cursor findCity(String selection, String[] selectionArgs, String sortOrder) {
		return databaseSupport.getReadableDatabase().query(Weather.City.TABLE_NAME, null, selection, selectionArgs,
				null, null, sortOrder);
	}

	public int insertCity(ContentValues[] valuesArray) {
		int result = 0;
		SQLiteDatabase sqlite = databaseSupport.getWritableDatabase();
		String sql = "insert into " + Weather.City.TABLE_NAME + "(" + Weather.City.CODE + "," + Weather.City.NAME + ","
				+ Weather.City.PARENT + ") values(?,?,?) ";
		SQLiteStatement stat = sqlite.compileStatement(sql);
		sqlite.beginTransaction();
		try {
			for (ContentValues values : valuesArray) {
				stat.bindString(1, values.getAsString(Weather.City.CODE));
				stat.bindString(2, values.getAsString(Weather.City.NAME));
				String p = values.getAsString(Weather.City.PARENT);
				if (p != null && p.length() > 0) {
					stat.bindString(3, p);
				} else {
					stat.bindNull(3);
				}
				stat.executeInsert();
				result++;
			}
			sqlite.setTransactionSuccessful();
		} finally {
			sqlite.endTransaction();
		}
		return result;
	}

	public Cursor findSetting() {
		MatrixCursor result = new MatrixCursor(new String[] { Weather.Setting.CITY1_CODE, Weather.Setting.CITY1_NAME,
				Weather.Setting.CITY2_CODE, Weather.Setting.CITY2_NAME, Weather.Setting.CITY3_CODE,
				Weather.Setting.CITY3_NAME, Weather.Setting.UPTIME });
		Cursor cursor = databaseSupport.find(DatabaseSupport.COL_TYPE + "=?", new Object[] { Weather.Setting.TYPE });
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				String value = cursor.getString(cursor.getColumnIndex(DatabaseSupport.COL_VALUE));
				if (value != null && value.length() > 0) {
					result.addRow(value.split(";"));
				}
			}
			cursor.close();
		}
		return result;
	}

	public void updateSetting(ContentValues values) {
		// old
		long rowId = -1;
		Cursor old = databaseSupport.find(DatabaseSupport.COL_TYPE + "=?", new Object[] { Weather.Setting.TYPE });
		if (old.moveToFirst()) {
			rowId = old.getLong(old.getColumnIndex(DatabaseSupport.COL_ID));
			String value = old.getString(old.getColumnIndex(DatabaseSupport.COL_VALUE));
			String[] sl = (value != null ? value.split(";") : new String[0]);
			if (!values.containsKey(Weather.Setting.CITY1_CODE) && sl.length > 0) {
				values.put(Weather.Setting.CITY1_CODE, sl[0]);
			}
			if (!values.containsKey(Weather.Setting.CITY1_NAME) && sl.length > 1) {
				values.put(Weather.Setting.CITY1_NAME, sl[1]);
			}
			if (!values.containsKey(Weather.Setting.CITY2_CODE) && sl.length > 2) {
				values.put(Weather.Setting.CITY2_CODE, sl[2]);
			}
			if (!values.containsKey(Weather.Setting.CITY2_NAME) && sl.length > 3) {
				values.put(Weather.Setting.CITY2_NAME, sl[3]);
			}
			if (!values.containsKey(Weather.Setting.CITY3_CODE) && sl.length > 4) {
				values.put(Weather.Setting.CITY3_CODE, sl[4]);
			}
			if (!values.containsKey(Weather.Setting.CITY3_NAME) && sl.length > 5) {
				values.put(Weather.Setting.CITY3_NAME, sl[5]);
			}
			if (!values.containsKey(Weather.Setting.UPTIME) && sl.length > 6) {
				values.put(Weather.Setting.UPTIME, sl[6]);
			}
		}
		old.close();
		// save
		ContentValues setting = new ContentValues();
		setting.put(DatabaseSupport.COL_TYPE, Weather.Setting.TYPE);
		StringBuffer buf = new StringBuffer();
		buf.append(values.getAsString(Weather.Setting.CITY1_CODE)).append(";");
		buf.append(values.getAsString(Weather.Setting.CITY1_NAME)).append(";");
		buf.append(values.getAsString(Weather.Setting.CITY2_CODE)).append(";");
		buf.append(values.getAsString(Weather.Setting.CITY2_NAME)).append(";");
		buf.append(values.getAsString(Weather.Setting.CITY3_CODE)).append(";");
		buf.append(values.getAsString(Weather.Setting.CITY3_NAME)).append(";");
		buf.append(values.getAsString(Weather.Setting.UPTIME)).append(";");
		setting.put(DatabaseSupport.COL_VALUE, buf.toString());
		rowId = databaseSupport.save(rowId, setting);
		Log.i(SettingService.class.getSimpleName(), "updated setting");
	}

	public boolean isOvertime(Date date) {
		Cursor setting = this.findSetting();
		if (setting.moveToFirst()) {
			String uptime = setting.getString(setting.getColumnIndex(Weather.Setting.UPTIME));
			long hour = Long.valueOf(uptime);
			Date now = new Date();
			long diff = (now.getTime() - date.getTime()) / (1000 * 60 * 60);
			return diff > hour;
		} else {
			return false;
		}
	}

}
