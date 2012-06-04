/**
 * 
 */
package org.weather.weatherman.content;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.weather.api.cn.WeatherClient;
import org.weather.api.cn.forecast.ForecastWeather;
import org.weather.api.cn.forecast.LivingIndex;
import org.weather.api.cn.realtime.RealtimeWeather;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.util.Log;

/**
 * @since 2012-5-16
 * @author gmz
 * 
 */
public class WeatherService {

	private WeatherClient weatherClient;
	private DatabaseSupport databaseSupport;
	private SettingService settingProvider;

	public WeatherService(DatabaseSupport databaseSupport, SettingService settingProvider) {
		weatherClient = new WeatherClient(5000, 10000, 5);
		this.databaseSupport = databaseSupport;
		this.settingProvider = settingProvider;
	}

	public Cursor findRealtime(String citycode) {
		Log.i(WeatherService.class.getSimpleName(), "citycode: " + citycode);
		MatrixCursor result = new MatrixCursor(new String[] { Weather.RealtimeWeather.ID, Weather.RealtimeWeather.NAME,
				Weather.RealtimeWeather.TIME, Weather.RealtimeWeather.TEMPERATURE, Weather.RealtimeWeather.HUMIDITY,
				Weather.RealtimeWeather.WINDDIRECTION, Weather.RealtimeWeather.WINDFORCE });
		// database
		Cursor cursor = databaseSupport.find(DatabaseSupport.COL_TYPE + "=? and " + DatabaseSupport.COL_CODE + "=?",
				new Object[] { Weather.RealtimeWeather.TYPE, citycode });
		if (cursor.moveToFirst()) {
			String uptime = cursor.getString(cursor.getColumnIndex(DatabaseSupport.COL_UPDATETIME));
			if (!settingProvider.isOvertime(new Date(Long.valueOf(uptime)))) {
				String value = cursor.getString(cursor.getColumnIndex(DatabaseSupport.COL_VALUE));
				result.addRow(value.split(";"));
				cursor.close();
				Log.i(WeatherService.class.getSimpleName(), "found realtime weather from database");
				return result;
			}
		}
		cursor.close();
		// web
		RealtimeWeather realtime = this.fetchRealtime(citycode);
		if (realtime != null) {
			Log.i(WeatherService.class.getSimpleName(), "found realtime weather from web");
			result.addRow(new Object[] { realtime.getCityId(), realtime.getCityName(), realtime.getTime(),
					realtime.getTemperature(), realtime.getHumidity(), realtime.getWindDirection(),
					realtime.getWindForce() });
			this.updateRealtime(realtime);
		} else {
			Log.e(WeatherService.class.getName(), "get realtime weather failed");
		}
		return result;
	}

	public RealtimeWeather fetchRealtime(String citycode) {
		try {
			return weatherClient.getRealWeather(citycode);
		} catch (Exception e) {
			Log.e(WeatherService.class.getSimpleName(), "get weather failed", e);
			return null;
		}
	}

	public void updateRealtime(RealtimeWeather realtime) {
		String city = (realtime != null ? realtime.getCityId() : null);
		if (city == null || city.length() == 0) {
			return;
		}
		// old
		long rowId = -1;
		Cursor cursor = databaseSupport.find(DatabaseSupport.COL_TYPE + "=? and " + DatabaseSupport.COL_CODE + "=?",
				new Object[] { Weather.RealtimeWeather.TYPE, city });
		if (cursor.moveToFirst()) {
			rowId = cursor.getLong(cursor.getColumnIndex(DatabaseSupport.COL_ID));
		}
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
		rowId = databaseSupport.save(rowId, setting);
		Log.i(WeatherService.class.getSimpleName(), "updated realtime weather");
	}

	public Cursor findForecast(String citycode) {
		Log.i(WeatherService.class.getSimpleName(), "citycode: " + citycode);
		MatrixCursor result = new MatrixCursor(new String[] { Weather.ForecastWeather.ID, Weather.ForecastWeather.NAME,
				Weather.ForecastWeather.TIME, Weather.ForecastWeather.WEATHER, Weather.ForecastWeather.TEMPERATURE,
				Weather.ForecastWeather.IMAGE, Weather.ForecastWeather.WIND, Weather.ForecastWeather.WINDFORCE });
		// query database
		Cursor cursor = databaseSupport.find(DatabaseSupport.COL_TYPE + "=? and " + DatabaseSupport.COL_CODE + "=?",
				new Object[] { Weather.ForecastWeather.TYPE, citycode });
		if (cursor.moveToFirst()) {
			String uptime = cursor.getString(cursor.getColumnIndex(DatabaseSupport.COL_UPDATETIME));
			if (!settingProvider.isOvertime(new Date(Long.valueOf(uptime)))) {
				String value = cursor.getString(cursor.getColumnIndex(DatabaseSupport.COL_VALUE));
				String[] rows = value.split("#");
				for (String row : rows) {
					result.addRow(row.split(";"));
				}
				cursor.close();
				Log.i(WeatherService.class.getSimpleName(), "found forecast weather from database");
				return result;
			}
		}
		cursor.close();
		// query web server
		ForecastWeather forecast = this.fetchForecast(citycode);
		if (forecast != null) {
			Log.i(WeatherService.class.getSimpleName(), "found forecast weather from web");
			List<String> wl = forecast.getWeather(), tl = forecast.getTemperature(), il = forecast.getImage(), wdl = forecast
					.getWind(), wfl = forecast.getWindForce();
			int length = Math
					.min(wl.size(), Math.min(tl.size(), Math.min(il.size(), Math.min(wdl.size(), wfl.size()))));
			for (int i = 0; i < length; i++) {
				result.addRow(new Object[] { forecast.getCityId(), forecast.getCityName(), forecast.getTime(),
						wl.size() > i ? wl.get(i) : null, tl.size() > i ? tl.get(i) : null,
						il.size() > i ? il.get(i) : null, wdl.size() > i ? wdl.get(i) : null,
						wfl.size() > i ? wfl.get(i) : null });
			}
			this.updateForecastAndIndex(forecast);
		} else {
			Log.e(WeatherService.class.getSimpleName(), "get forecast weather failed");
		}
		return result;
	}

	public ForecastWeather fetchForecast(String citycode) {
		try {
			return weatherClient.getForecastWeather(citycode);
		} catch (Exception e) {
			Log.e(WeatherService.class.getSimpleName(), "get weather failed", e);
			return null;
		}
	}

	public Cursor findIndex(String citycode) {
		Log.i(WeatherService.class.getSimpleName(), "citycode: " + citycode);
		MatrixCursor result = new MatrixCursor(new String[] { Weather.LivingIndex.ID, Weather.LivingIndex.NAME,
				Weather.LivingIndex.TIME, Weather.LivingIndex.DRESS, Weather.LivingIndex.ULTRAVIOLET,
				Weather.LivingIndex.CLEANCAR, Weather.LivingIndex.TRAVEL, Weather.LivingIndex.COMFORT,
				Weather.LivingIndex.MORNINGEXERCISE, Weather.LivingIndex.SUNDRY, Weather.LivingIndex.IRRITABILITY });
		// database
		Cursor cursor = databaseSupport.find(DatabaseSupport.COL_TYPE + "=? and " + DatabaseSupport.COL_CODE + "=?",
				new Object[] { Weather.LivingIndex.TYPE, citycode });
		if (cursor.moveToFirst()) {
			String uptime = cursor.getString(cursor.getColumnIndex(DatabaseSupport.COL_UPDATETIME));
			if (!settingProvider.isOvertime(new Date(Long.valueOf(uptime)))) {
				String value = cursor.getString(cursor.getColumnIndex(DatabaseSupport.COL_VALUE));
				result.addRow(value.split(";"));
				cursor.close();
				Log.i(WeatherService.class.getSimpleName(), "found living index from database");
				return result;
			}
		}
		cursor.close();
		// web
		ForecastWeather forecast = this.fetchForecast(citycode);
		if (forecast != null) {
			Log.i(WeatherService.class.getSimpleName(), "found living index from web");
			List<Object> row = new ArrayList<Object>();
			Collections.addAll(row, forecast.getCityId(), forecast.getCityName(), forecast.getTime());
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
			result.addRow(row);
			this.updateForecastAndIndex(forecast);
		} else {
			Log.e(WeatherService.class.getName(), "get living index failed");
		}
		return result;
	}

	public void updateForecastAndIndex(ForecastWeather forecast) {
		String citycode = (forecast != null ? forecast.getCityId() : null);
		if (citycode == null || citycode.length() == 0) {
			return;
		}
		// forecast
		// old
		long rowId = -1;
		Cursor cursor = databaseSupport.find(DatabaseSupport.COL_TYPE + "=? and " + DatabaseSupport.COL_CODE + "=?",
				new Object[] { Weather.ForecastWeather.TYPE, citycode });
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
		rowId = databaseSupport.save(rowId, values);
		Log.i(WeatherService.class.getSimpleName(), "updated forecast weather");
		// index
		// old
		rowId = -1;
		cursor = databaseSupport.find(DatabaseSupport.COL_TYPE + "=? and " + DatabaseSupport.COL_CODE + "=?",
				new Object[] { Weather.LivingIndex.TYPE, citycode });
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
		LivingIndex li = forecast.getDressIndex();
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
		rowId = databaseSupport.save(rowId, index);
		Log.i(WeatherService.class.getSimpleName(), "updated living index");
	}

}
