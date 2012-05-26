package org.weather.weatherman.content;

import java.util.Date;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;

public class SettingProvider {

	private DatabaseSupport databaseSupport;

	public SettingProvider(DatabaseSupport databaseSupport) {
		super();
		this.databaseSupport = databaseSupport;
	}

	public Cursor find() {
		MatrixCursor result = new MatrixCursor(new String[] { Weather.Setting.CITY1, Weather.Setting.CITY2,
				Weather.Setting.CITY3, Weather.Setting.UPDATETIME });
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

	public void update(ContentValues values) {
		// find
		long rowId = -1;
		String city1 = values.getAsString(Weather.Setting.CITY1);
		String city2 = values.getAsString(Weather.Setting.CITY2);
		String city3 = values.getAsString(Weather.Setting.CITY3);
		String updateTime = values.getAsString(Weather.Setting.UPDATETIME);
		Cursor old = databaseSupport.find(DatabaseSupport.COL_TYPE + "=?", new Object[] { Weather.Setting.TYPE });
		if (old != null) {
			if (old.moveToFirst()) {
				rowId = old.getLong(old.getColumnIndex(DatabaseSupport.COL_ID));
				String value = old.getString(old.getColumnIndex(DatabaseSupport.COL_VALUE));
				String[] sl = (value != null ? value.split(";") : new String[0]);
				if (city1 == null || city1.length() == 0) {
					if (sl.length > 0) {
						city1 = sl[0];
					}
				}
				if (city2 == null || city2.length() == 0) {
					if (sl.length > 1) {
						city2 = sl[1];
					}
				}
				if (city3 == null || city3.length() == 0) {
					if (sl.length > 2) {
						city3 = sl[2];
					}
				}
				if (updateTime == null || updateTime.length() == 0) {
					if (sl.length > 3) {
						updateTime = sl[3];
					}
				}
			}
			old.close();
		}
		// save
		ContentValues setting = new ContentValues();
		setting.put(DatabaseSupport.COL_TYPE, Weather.Setting.TYPE);
		setting.put(DatabaseSupport.COL_VALUE, city1 + ";" + city2 + ";" + city3 + ";" + updateTime);
		rowId = databaseSupport.save(rowId, setting);
	}

	public boolean isOvertime(Date date) {
		Cursor setting = this.find();
		if (setting.moveToFirst()) {
			String uptime = setting.getString(setting.getColumnIndex(Weather.Setting.UPDATETIME));
			long hour = Long.valueOf(uptime);
			long diff = (new Date().getTime() - date.getTime()) / (1000 * 60 * 60);
			return diff > hour;
		}
		return false;
	}

}
