package org.weather.weatherman.content;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.weather.api.cn.forecast.ForecastWeather;
import org.weather.api.cn.forecast.LivingIndex;
import org.weather.api.cn.realtime.RealtimeWeather;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class WeatherDataProvider extends ContentProvider {

	private static final UriMatcher URI_MATCHER;
	static {
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URI_MATCHER.addURI(Weather.AUTHORITY, Weather.SETTING_PATH + "/#",
				Weather.Setting.TYPE);
		URI_MATCHER.addURI(Weather.AUTHORITY, Weather.REALTIME_PATH + "/#",
				Weather.RealtimeWeather.TYPE);
		URI_MATCHER.addURI(Weather.AUTHORITY, Weather.FORECAST_PATH + "/#",
				Weather.ForecastWeather.TYPE);
	}

	private DatabaseSupport databaseSupport;
	private CachedWeatherClient weatherClient;

	@Override
	public boolean onCreate() {
		databaseSupport = new DatabaseSupport(getContext());
		weatherClient = new CachedWeatherClient();
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		switch (URI_MATCHER.match(uri)) {
		case Weather.Setting.TYPE:
			return findSetting(uri);
		case Weather.RealtimeWeather.TYPE:
			return findRealtime(uri);
		case Weather.ForecastWeather.TYPE:
			return findForecast(uri);
		default:
			throw new IllegalArgumentException("unknown Uri " + uri);
		}
	}

	private Cursor findSetting(Uri uri) {
		MatrixCursor result = new MatrixCursor(new String[] {
				Weather.Setting.CITY, Weather.Setting.UPDATETIME });
		Cursor cursor = databaseSupport.find(DatabaseSupport.COL_TYPE + "=?",
				new Object[] { Weather.Setting.TYPE });
		if (cursor != null && cursor.moveToFirst()) {
			String value = cursor.getString(cursor
					.getColumnIndex(DatabaseSupport.COL_VALUE));
			if (value != null && value.length() > 0) {
				String[] sl = value.split(";");
				result.addRow(new Object[] { sl[0],
						sl.length > 1 ? sl[1] : null });
			}
		}
		return result;
	}

	private Cursor findRealtime(Uri uri) {
		String citycode = uri.getLastPathSegment();
		Log.i(WeatherDataProvider.class.getSimpleName(), "citycode: "
				+ citycode);
		MatrixCursor realtimeCursor = new MatrixCursor(new String[] {
				Weather.RealtimeWeather.ID, Weather.RealtimeWeather.NAME,
				Weather.RealtimeWeather.TIME,
				Weather.RealtimeWeather.TEMPERATURE,
				Weather.RealtimeWeather.HUMIDITY,
				Weather.RealtimeWeather.WINDDIRECTION,
				Weather.RealtimeWeather.WINDFORCE,
				Weather.RealtimeWeather.DRESS,
				Weather.RealtimeWeather.ULTRAVIOLET,
				Weather.RealtimeWeather.CLEANCAR,
				Weather.RealtimeWeather.TRAVEL,
				Weather.RealtimeWeather.COMFORT,
				Weather.RealtimeWeather.MORNINGEXERCISE,
				Weather.RealtimeWeather.SUNDRY,
				Weather.RealtimeWeather.IRRITABILITY });

		List<Object> row = new ArrayList<Object>();
		RealtimeWeather realtime = weatherClient.getRealtimeWeather(citycode);
		if (realtime != null) {
			Collections.addAll(row, realtime.getCityId(),
					realtime.getCityName(), realtime.getTime(),
					realtime.getTemperature(), realtime.getHumidity(),
					realtime.getWindDirection(), realtime.getWindForce());
		}
		ForecastWeather forecast = weatherClient.getForecastWeather(citycode);
		if (forecast != null) {
			row.set(2, forecast.getTime());
			LivingIndex li = forecast.getDressIndex();
			row.add(li != null ? li.getIndex() : null);
			li = forecast.getUltravioletIndex();
			row.add(li != null ? li.getIndex() : null);
			li = forecast.getCleanCarIndex();
			row.add(li != null ? li.getIndex() : null);
			li = forecast.getTravelIndex();
			row.add(li != null ? li.getIndex() : null);
			li = forecast.getComfortIndex();
			row.add(li != null ? li.getIndex() : null);
			li = forecast.getMorningExerciseIndex();
			row.add(li != null ? li.getIndex() : null);
			li = forecast.getSunDryIndex();
			row.add(li != null ? li.getIndex() : null);
			li = forecast.getIrritabilityIndex();
			row.add(li != null ? li.getIndex() : null);
		}
		if (row.size() > 0) {
			realtimeCursor.addRow(row);
		} else {
			Log.e(WeatherDataProvider.class.getName(),
					"get realtime weather failed");
		}
		return realtimeCursor;
	}

	private Cursor findForecast(Uri uri) {
		String citycode = uri.getLastPathSegment();
		Log.i(WeatherDataProvider.class.getSimpleName(), "citycode: "
				+ citycode);
		MatrixCursor forecastCusor = new MatrixCursor(new String[] {
				Weather.ForecastWeather.ID, Weather.ForecastWeather.NAME,
				Weather.ForecastWeather.TIME, Weather.ForecastWeather.WEATHER,
				Weather.ForecastWeather.TEMPERATURE,
				Weather.ForecastWeather.IMAGE, Weather.ForecastWeather.WIND,
				Weather.ForecastWeather.WINDFORCE });
		ForecastWeather fw = weatherClient.getForecastWeather(citycode);
		if (fw != null) {
			List<String> wl = fw.getWeather(), tl = fw.getTemperature(), il = fw
					.getImage(), wdl = fw.getWind(), wfl = fw.getWindForce();
			int length = Math.min(
					wl.size(),
					Math.min(
							tl.size(),
							Math.min(il.size(),
									Math.min(wdl.size(), wfl.size()))));
			for (int i = 0; i < length; i++) {
				forecastCusor.addRow(new Object[] { fw.getCityId(),
						fw.getCityName(), fw.getTime(),
						wl.size() > i ? wl.get(i) : null,
						tl.size() > i ? tl.get(i) : null,
						il.size() > i ? il.get(i) : null,
						wdl.size() > i ? wdl.get(i) : null,
						wfl.size() > i ? wfl.get(i) : null });
			}
		} else {
			Log.e(WeatherDataProvider.class.getSimpleName(),
					"get forecast weather failed");
		}
		return forecastCusor;
	}

	@Override
	public String getType(Uri uri) {
		switch (URI_MATCHER.match(uri)) {
		case Weather.Setting.TYPE:
			return Weather.Setting.CONTENT_TYPE;
		case Weather.RealtimeWeather.TYPE:
			return Weather.RealtimeWeather.CONTENT_TYPE;
		case Weather.ForecastWeather.TYPE:
			return Weather.ForecastWeather.CONTENT_TYPE;
		default:
			throw new IllegalArgumentException("unknown Uri " + uri);
		}
	}

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		if (URI_MATCHER.match(uri) == Weather.Setting.TYPE) {
			// find
			long rowId = -1;
			String city = values.getAsString(Weather.Setting.CITY);
			String updateTime = values.getAsString(Weather.Setting.UPDATETIME);
			Cursor old = databaseSupport.find(DatabaseSupport.COL_TYPE + "=?",
					new Object[] { Weather.Setting.TYPE });
			if (old != null && old.moveToFirst()) {
				rowId = old.getLong(old.getColumnIndex(BaseColumns._ID));
				String value = old.getString(old
						.getColumnIndex(DatabaseSupport.COL_VALUE));
				String[] sl = (value != null ? value.split(";") : new String[0]);
				if (city == null || city.length() == 0) {
					if (sl.length > 0) {
						city = sl[0];
					}
				}
				if (updateTime == null || updateTime.length() == 0) {
					if (sl.length > 1) {
						updateTime = sl[1];
					}
				}
			}
			// save
			ContentValues setting = new ContentValues();
			setting.put(DatabaseSupport.COL_TYPE, Weather.Setting.TYPE);
			setting.put(DatabaseSupport.COL_VALUE, city + ";" + updateTime);
			rowId = databaseSupport.save(rowId, setting);
			if (rowId > 0) {
				Uri settingUri = Uri.withAppendedPath(uri,
						String.valueOf(rowId));
				getContext().getContentResolver()
						.notifyChange(settingUri, null);
			}
		}
		return 0;
	}

}
