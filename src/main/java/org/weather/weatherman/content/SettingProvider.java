package org.weather.weatherman.content;

import java.util.Date;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.util.Log;

public class SettingProvider {

	private DatabaseSupport databaseSupport;

	public SettingProvider(DatabaseSupport databaseSupport) {
		super();
		this.databaseSupport = databaseSupport;
	}

	public Cursor find() {
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

	public void update(ContentValues values) {
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
		Log.i(SettingProvider.class.getSimpleName(), "updated setting");
	}

	public boolean isOvertime(Date date) {
		Cursor setting = this.find();
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
