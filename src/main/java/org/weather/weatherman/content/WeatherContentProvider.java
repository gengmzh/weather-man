package org.weather.weatherman.content;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.util.Log;
import cn.seddat.weatherman.api.city.City;

public class WeatherContentProvider extends ContentProvider {

	private static final String tag = WeatherContentProvider.class.getSimpleName();

	private static final UriMatcher URI_MATCHER;
	static {
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		// city
		URI_MATCHER.addURI(DatabaseSupport.AUTHORITY, DatabaseSupport.PATH_CITY, DatabaseSupport.City.TYPE);
		URI_MATCHER.addURI(DatabaseSupport.AUTHORITY, DatabaseSupport.PATH_CITY + "/#", DatabaseSupport.City.TYPE);
		// setting
		URI_MATCHER.addURI(DatabaseSupport.AUTHORITY,
				DatabaseSupport.PATH_CONTENT + "/" + Weather.Setting.PATH_SETTING, Weather.Setting.TYPE);
		URI_MATCHER.addURI(DatabaseSupport.AUTHORITY, DatabaseSupport.PATH_CONTENT + "/" + Weather.Setting.PATH_SETTING
				+ "/#", Weather.Setting.TYPE);
		// realtime weather
		URI_MATCHER.addURI(DatabaseSupport.AUTHORITY, DatabaseSupport.PATH_CONTENT + "/"
				+ Weather.RealtimeWeather.PATH_REALTIME, Weather.RealtimeWeather.TYPE);
		URI_MATCHER.addURI(DatabaseSupport.AUTHORITY, DatabaseSupport.PATH_CONTENT + "/"
				+ Weather.RealtimeWeather.PATH_REALTIME + "/#", Weather.RealtimeWeather.TYPE);
		// forecast weather
		URI_MATCHER.addURI(DatabaseSupport.AUTHORITY, DatabaseSupport.PATH_CONTENT + "/"
				+ Weather.ForecastWeather.PATH_FORECAST, Weather.ForecastWeather.TYPE);
		URI_MATCHER.addURI(DatabaseSupport.AUTHORITY, DatabaseSupport.PATH_CONTENT + "/"
				+ Weather.ForecastWeather.PATH_FORECAST + "/#", Weather.ForecastWeather.TYPE);
		// AQI
		URI_MATCHER.addURI(DatabaseSupport.AUTHORITY, DatabaseSupport.PATH_CONTENT + "/"
				+ Weather.AirQualityIndex.PATH_AQI, Weather.AirQualityIndex.TYPE);
		URI_MATCHER.addURI(DatabaseSupport.AUTHORITY, DatabaseSupport.PATH_CONTENT + "/"
				+ Weather.AirQualityIndex.PATH_AQI + "/#", Weather.AirQualityIndex.TYPE);
	}

	private DatabaseSupport databaseSupport;

	@Override
	public boolean onCreate() {
		databaseSupport = new DatabaseSupport(getContext());
		try {
			this.init();
		} catch (Exception e) {
			Log.e(tag, "init failed", e);
		}
		return true;
	}

	private void init() throws Exception {
		// check
		String where = DatabaseSupport.City.PARENT + " ISNULL";
		Cursor cursor = this.query(DatabaseSupport.City.CONTENT_URI, null, where, null, null);
		if (cursor.moveToFirst()) {
			Log.i(tag, "city has been initialized already");
			cursor.close();
			return;
		}
		cursor.close();
		// read
		Log.i(tag, "init city starts");
		List<ContentValues> valuesList = new ArrayList<ContentValues>();
		BufferedReader reader = null;
		try {
			InputStream ins = SettingService.class.getClassLoader().getResourceAsStream(
					"org/weather/weatherman/content/city.properties");
			reader = new BufferedReader(new InputStreamReader(ins));
			City c1 = null, c2 = null;
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] ls = line.split("\t");
				if (ls.length < 2) {
					continue;
				}
				City tmp = new City(ls[0], ls[1]);
				ContentValues values = new ContentValues();
				values.put(DatabaseSupport.City.CODE, tmp.getId());
				values.put(DatabaseSupport.City.NAME, tmp.getName());
				if (tmp.getId().length() == 5) {
					c1 = tmp;
				} else if (tmp.getId().length() == 7) {
					c2 = tmp;
					values.put(DatabaseSupport.City.PARENT, c1.getId());
				} else if (tmp.getId().length() == 9) {
					values.put(DatabaseSupport.City.PARENT, c2.getId());
				}
				valuesList.add(values);
			}
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		// insert
		if (!valuesList.isEmpty()) {
			Uri uri = DatabaseSupport.City.CONTENT_URI;
			this.bulkInsert(uri, valuesList.toArray(new ContentValues[valuesList.size()]));
		}
		Log.i(tag, "init city done");
	}

	@Override
	public String getType(Uri uri) {
		switch (URI_MATCHER.match(uri)) {
		case DatabaseSupport.City.TYPE:
			return DatabaseSupport.City.CONTENT_TYPE;

		case Weather.Setting.TYPE:
		case Weather.RealtimeWeather.TYPE:
		case Weather.ForecastWeather.TYPE:
		case Weather.AirQualityIndex.TYPE:
			return DatabaseSupport.Content.CONTENT_TYPE;

		default:
			throw new IllegalArgumentException("unknown Uri " + uri);
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		final SQLiteDatabase db = databaseSupport.getReadableDatabase();
		final int type = URI_MATCHER.match(uri);
		switch (type) {
		case DatabaseSupport.City.TYPE: // 城市信息
			return db.query(DatabaseSupport.City.TABLE_NAME, projection, selection, selectionArgs, null, null,
					sortOrder);

		case Weather.Setting.TYPE: // 设置信息
			selection = this.getWhereWithType(selection);
			selectionArgs = this.getArgsWithType(selectionArgs, Weather.Setting.TYPE);
			return db.query(DatabaseSupport.Content.TABLE_NAME, projection, selection, selectionArgs, null, null,
					sortOrder);

		case Weather.RealtimeWeather.TYPE:// 天气实况
			selection = this.getWhereWithType(selection);
			selectionArgs = this.getArgsWithType(selectionArgs, Weather.RealtimeWeather.TYPE);
			return db.query(DatabaseSupport.Content.TABLE_NAME, projection, selection, selectionArgs, null, null,
					sortOrder);

		case Weather.ForecastWeather.TYPE:// 天气预报
			selection = this.getWhereWithType(selection);
			selectionArgs = this.getArgsWithType(selectionArgs, Weather.ForecastWeather.TYPE);
			return db.query(DatabaseSupport.Content.TABLE_NAME, projection, selection, selectionArgs, null, null,
					sortOrder);

		case Weather.AirQualityIndex.TYPE:
			selection = this.getWhereWithType(selection);
			selectionArgs = this.getArgsWithType(selectionArgs, Weather.AirQualityIndex.TYPE);
			return db.query(DatabaseSupport.Content.TABLE_NAME, projection, selection, selectionArgs, null, null,
					sortOrder);

		default:
			throw new IllegalArgumentException("unknown Uri " + uri);
		}
	}

	private String getWhereWithType(String where) {
		if (where == null || where.length() == 0) {
			return DatabaseSupport.Content.COL_TYPE + "=?";
		} else {
			return where + " and " + DatabaseSupport.Content.COL_TYPE + "=?";
		}
	}

	private String[] getArgsWithType(String[] args, int type) {
		String[] ps = new String[(args != null ? args.length : 0) + 1];
		if (args != null && args.length > 0) {
			System.arraycopy(args, 0, ps, 0, args.length);
		}
		ps[ps.length - 1] = String.valueOf(type);
		return ps;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		final SQLiteDatabase db = databaseSupport.getWritableDatabase();
		switch (URI_MATCHER.match(uri)) {
		case DatabaseSupport.City.TYPE: // 城市信息
			long id = db.insert(DatabaseSupport.City.TABLE_NAME, null, values);
			if (id > -1) {
				return Uri.withAppendedPath(DatabaseSupport.City.CONTENT_URI, String.valueOf(id));
			} else {
				return null;
			}

		case Weather.Setting.TYPE: // 设置信息
			values.put(DatabaseSupport.Content.COL_TYPE, Weather.Setting.TYPE);
			values.put(DatabaseSupport.Content.COL_UPDATETIME, new Date().getTime());
			id = db.insert(DatabaseSupport.Content.TABLE_NAME, null, values);
			if (id > -1) {
				return Uri.withAppendedPath(DatabaseSupport.Content.CONTENT_URI, Weather.Setting.PATH_SETTING + "/"
						+ String.valueOf(id));
			} else {
				return null;
			}

		case Weather.RealtimeWeather.TYPE:// 天气实况
			values.put(DatabaseSupport.Content.COL_TYPE, Weather.RealtimeWeather.TYPE);
			values.put(DatabaseSupport.Content.COL_UPDATETIME, new Date().getTime());
			id = db.insert(DatabaseSupport.Content.TABLE_NAME, null, values);
			if (id > -1) {
				return Uri.withAppendedPath(DatabaseSupport.Content.CONTENT_URI, Weather.RealtimeWeather.PATH_REALTIME
						+ "/" + String.valueOf(id));
			} else {
				return null;
			}

		case Weather.ForecastWeather.TYPE:// 天气预报
			values.put(DatabaseSupport.Content.COL_TYPE, Weather.ForecastWeather.TYPE);
			values.put(DatabaseSupport.Content.COL_UPDATETIME, new Date().getTime());
			id = db.insert(DatabaseSupport.Content.TABLE_NAME, null, values);
			if (id > -1) {
				return Uri.withAppendedPath(DatabaseSupport.Content.CONTENT_URI, Weather.ForecastWeather.PATH_FORECAST
						+ "/" + String.valueOf(id));
			} else {
				return null;
			}

		case Weather.AirQualityIndex.TYPE:
			values.put(DatabaseSupport.Content.COL_TYPE, Weather.AirQualityIndex.TYPE);
			values.put(DatabaseSupport.Content.COL_UPDATETIME, new Date().getTime());
			id = db.insert(DatabaseSupport.Content.TABLE_NAME, null, values);
			if (id > -1) {
				return Uri.withAppendedPath(DatabaseSupport.Content.CONTENT_URI, Weather.AirQualityIndex.PATH_AQI + "/"
						+ String.valueOf(id));
			} else {
				return null;
			}

		default:
			throw new IllegalArgumentException("unknown Uri " + uri);
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		final SQLiteDatabase db = databaseSupport.getWritableDatabase();
		switch (URI_MATCHER.match(uri)) {
		case DatabaseSupport.City.TYPE: // 城市信息
			return db.update(DatabaseSupport.City.TABLE_NAME, values, selection, selectionArgs);

		case Weather.Setting.TYPE: // 设置信息
			selection = this.getWhereWithType(selection);
			selectionArgs = this.getArgsWithType(selectionArgs, Weather.Setting.TYPE);
			values.put(DatabaseSupport.Content.COL_UPDATETIME, new Date().getTime());
			return db.update(DatabaseSupport.Content.TABLE_NAME, values, selection, selectionArgs);

		case Weather.RealtimeWeather.TYPE:// 天气实况
			selection = this.getWhereWithType(selection);
			selectionArgs = this.getArgsWithType(selectionArgs, Weather.RealtimeWeather.TYPE);
			values.put(DatabaseSupport.Content.COL_UPDATETIME, new Date().getTime());
			return db.update(DatabaseSupport.Content.TABLE_NAME, values, selection, selectionArgs);

		case Weather.ForecastWeather.TYPE:// 天气预报
			selection = this.getWhereWithType(selection);
			selectionArgs = this.getArgsWithType(selectionArgs, Weather.ForecastWeather.TYPE);
			values.put(DatabaseSupport.Content.COL_UPDATETIME, new Date().getTime());
			return db.update(DatabaseSupport.Content.TABLE_NAME, values, selection, selectionArgs);

		case Weather.AirQualityIndex.TYPE:
			selection = this.getWhereWithType(selection);
			selectionArgs = this.getArgsWithType(selectionArgs, Weather.AirQualityIndex.TYPE);
			values.put(DatabaseSupport.Content.COL_UPDATETIME, new Date().getTime());
			return db.update(DatabaseSupport.Content.TABLE_NAME, values, selection, selectionArgs);

		default:
			throw new IllegalArgumentException("unknown Uri " + uri);
		}
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		final SQLiteDatabase db = databaseSupport.getWritableDatabase();
		switch (URI_MATCHER.match(uri)) {
		case DatabaseSupport.City.TYPE: // 城市信息
			return db.delete(DatabaseSupport.City.TABLE_NAME, selection, selectionArgs);

		case Weather.Setting.TYPE: // 设置信息
			selection = this.getWhereWithType(selection);
			selectionArgs = this.getArgsWithType(selectionArgs, Weather.Setting.TYPE);
			return db.delete(DatabaseSupport.Content.TABLE_NAME, selection, selectionArgs);

		case Weather.RealtimeWeather.TYPE:// 天气实况
			selection = this.getWhereWithType(selection);
			selectionArgs = this.getArgsWithType(selectionArgs, Weather.RealtimeWeather.TYPE);
			return db.delete(DatabaseSupport.Content.TABLE_NAME, selection, selectionArgs);

		case Weather.ForecastWeather.TYPE:// 天气预报
			selection = this.getWhereWithType(selection);
			selectionArgs = this.getArgsWithType(selectionArgs, Weather.ForecastWeather.TYPE);
			return db.delete(DatabaseSupport.Content.TABLE_NAME, selection, selectionArgs);

		case Weather.AirQualityIndex.TYPE:
			selection = this.getWhereWithType(selection);
			selectionArgs = this.getArgsWithType(selectionArgs, Weather.AirQualityIndex.TYPE);
			return db.delete(DatabaseSupport.Content.TABLE_NAME, selection, selectionArgs);

		default:
			throw new IllegalArgumentException("unknown Uri " + uri);
		}
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] valuesArray) {
		switch (URI_MATCHER.match(uri)) {
		case DatabaseSupport.City.TYPE:
			int result = 0;
			final SQLiteDatabase db = databaseSupport.getWritableDatabase();
			String sql = "insert into " + DatabaseSupport.City.TABLE_NAME + "(" + DatabaseSupport.City.CODE + ","
					+ DatabaseSupport.City.NAME + "," + DatabaseSupport.City.PARENT + ") values(?,?,?) ";
			final SQLiteStatement stat = db.compileStatement(sql);
			db.beginTransaction();
			try {
				for (ContentValues values : valuesArray) {
					stat.bindString(1, values.getAsString(DatabaseSupport.City.CODE));
					stat.bindString(2, values.getAsString(DatabaseSupport.City.NAME));
					String parent = values.getAsString(DatabaseSupport.City.PARENT);
					if (parent != null && parent.length() > 0) {
						stat.bindString(3, parent);
					} else {
						stat.bindNull(3);
					}
					stat.executeInsert();
					result++;
				}
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
			}
			return result;

		case Weather.Setting.TYPE:
		case Weather.RealtimeWeather.TYPE:
		case Weather.ForecastWeather.TYPE:
		case Weather.AirQualityIndex.TYPE:
			return super.bulkInsert(uri, valuesArray);

		default:
			throw new IllegalArgumentException("unknown Uri " + uri);
		}
	}

}
