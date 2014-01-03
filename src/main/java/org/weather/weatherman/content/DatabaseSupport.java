package org.weather.weatherman.content;

import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class DatabaseSupport extends SQLiteOpenHelper {

	public static final String DBNAME = "org.weather.weatherman.db";
	public static final int DBVERSION = 4;

	public static final String TABLE_NAME = "content";
	public static final String COL_ID = BaseColumns._ID;
	public static final String COL_CODE = "code";
	public static final String COL_TYPE = "type";
	public static final String COL_VALUE = "value";
	public static final String COL_UPDATETIME = "ut";

	public DatabaseSupport(Context context) {
		super(context, DBNAME, null, DBVERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase arg0) {
		String sql = "create table if not exists " + TABLE_NAME + "( " + COL_ID + " integer primary key, " + COL_CODE
				+ " text, " + COL_TYPE + " integer, " + COL_VALUE + " text, " + COL_UPDATETIME + " integer " + "); ";
		arg0.execSQL(sql);
		sql = "create table if not exists " + Weather.City.TABLE_NAME + "( " + Weather.City.ID
				+ " integer primary key, " + Weather.City.CODE + " text, " + Weather.City.NAME + " text, "
				+ Weather.City.PARENT + " text " + "); ";
		arg0.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		arg0.execSQL("drop table if exists " + TABLE_NAME);
		arg0.execSQL("drop table if exists " + Weather.City.TABLE_NAME);
		onCreate(arg0);
	}

	public Cursor find(String where, Object[] args) {
		String sel = where;
		String[] selArgs = new String[args != null ? args.length : 0];
		for (int i = 0; i < selArgs.length; i++) {
			selArgs[i] = args[i].toString();
		}
		SQLiteDatabase db = getReadableDatabase();
		return db.query(TABLE_NAME, null, sel, selArgs, null, null, null);
	}

	public long save(long rowId, ContentValues values) {
		values.put(COL_UPDATETIME, new Date().getTime());
		SQLiteDatabase db = getWritableDatabase();
		if (rowId > 0) {
			db.update(TABLE_NAME, values, COL_ID + "=?", new String[] { String.valueOf(rowId) });
		} else {
			rowId = db.insert(TABLE_NAME, null, values);
		}
		return rowId;
	}

	/**
	 * 保存天气实况
	 * 
	 * @author gengmaozhang01
	 * @since 2014-1-3 下午7:37:36
	 */
	public void saveRealtimeWeather(Weather.RealtimeWeather realtime) {
		String city = (realtime != null ? realtime.getCityId() : null);
		if (city == null || city.length() == 0) {
			return;
		}
		// old
		long rowId = -1;
		Cursor cursor = this.find(COL_TYPE + "=? and " + COL_CODE + "=?", new Object[] { Weather.RealtimeWeather.TYPE,
				city });
		if (cursor.moveToFirst()) {
			rowId = cursor.getLong(cursor.getColumnIndex(DatabaseSupport.COL_ID));
		}
		cursor.close();
		// save
		ContentValues setting = new ContentValues();
		setting.put(DatabaseSupport.COL_TYPE, Weather.RealtimeWeather.TYPE);
		setting.put(DatabaseSupport.COL_CODE, city);
		StringBuffer value = new StringBuffer();
		value.append(city).append(";");
		value.append(realtime.getCityName()).append(";");
		value.append(realtime.getTime()).append(";");
		value.append(realtime.getTemperature()).append(";");
		value.append(realtime.getHumidity()).append(";");
		value.append(realtime.getWindDirection()).append(";");
		value.append(realtime.getWindForce()).append(";");
		setting.put(DatabaseSupport.COL_VALUE, value.toString());
		rowId = this.save(rowId, setting);
	}

	/**
	 * 保存天气预报和各个指数
	 * 
	 * @author gengmaozhang01
	 * @since 2014-1-3 下午7:50:52
	 */
	public void saveForecastAndIndexWeather(Weather.ForecastWeather forecast) {
		String citycode = (forecast != null ? forecast.getCityId() : null);
		if (citycode == null || citycode.length() == 0) {
			return;
		}
		// forecast
		// old
		long rowId = -1;
		Cursor cursor = this.find(COL_TYPE + "=? and " + COL_CODE + "=?", new Object[] { Weather.ForecastWeather.TYPE,
				citycode });
		if (cursor.moveToFirst()) {
			rowId = cursor.getLong(cursor.getColumnIndex(DatabaseSupport.COL_ID));
		}
		// save
		ContentValues values = new ContentValues();
		values.put(DatabaseSupport.COL_TYPE, Weather.ForecastWeather.TYPE);
		values.put(DatabaseSupport.COL_CODE, citycode);
		StringBuffer value = new StringBuffer();
		List<String> wl = forecast.getWeather(), tl = forecast.getTemperature(), il = forecast.getImage(), wdl = forecast
				.getWind(), wfl = forecast.getWindForce();
		int length = Math.min(wl.size(), Math.min(tl.size(), Math.min(il.size(), Math.min(wdl.size(), wfl.size()))));
		for (int i = 0; i < length; i++) {
			value.append(citycode).append(";");
			value.append(forecast.getCityName()).append(";");
			value.append(forecast.getTime()).append(";");
			value.append(wl.size() > i ? wl.get(i) : null).append(";");
			value.append(tl.size() > i ? tl.get(i) : null).append(";");
			value.append(il.size() > i ? il.get(i) : null).append(";");
			value.append(wdl.size() > i ? wdl.get(i) : null).append(";");
			value.append(wfl.size() > i ? wfl.get(i) : null).append("#");
		}
		values.put(DatabaseSupport.COL_VALUE, value.toString());
		rowId = this.save(rowId, values);
		// index
		// old
		rowId = -1;
		cursor = this.find(COL_TYPE + "=? and " + COL_CODE + "=?", new Object[] { Weather.LivingIndex.TYPE, citycode });
		if (cursor.moveToFirst()) {
			rowId = cursor.getLong(cursor.getColumnIndex(DatabaseSupport.COL_ID));
		}
		// save
		ContentValues index = new ContentValues();
		index.put(DatabaseSupport.COL_TYPE, Weather.LivingIndex.TYPE);
		index.put(DatabaseSupport.COL_CODE, citycode);
		value = new StringBuffer();
		value.append(citycode).append(";");
		value.append(forecast.getCityName()).append(";");
		value.append(forecast.getTime()).append(";");
		Weather.LivingIndex li = forecast.getDressIndex();
		value.append(li != null ? li.getIndex() : null).append(";");
		li = forecast.getUltravioletIndex();
		value.append(li != null ? li.getIndex() : null).append(";");
		li = forecast.getCleanCarIndex();
		value.append(li != null ? li.getIndex() : null).append(";");
		li = forecast.getTravelIndex();
		value.append(li != null ? li.getIndex() : null).append(";");
		li = forecast.getComfortIndex();
		value.append(li != null ? li.getIndex() : null).append(";");
		li = forecast.getMorningExerciseIndex();
		value.append(li != null ? li.getIndex() : null).append(";");
		li = forecast.getSunDryIndex();
		value.append(li != null ? li.getIndex() : null).append(";");
		li = forecast.getIrritabilityIndex();
		value.append(li != null ? li.getIndex() : null).append(";");
		index.put(DatabaseSupport.COL_VALUE, value.toString());
		rowId = this.save(rowId, index);
	}

}
