package org.weather.weatherman.content;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.weather.api.cn.forecast.ForecastWeather;
import org.weather.api.cn.forecast.LivingIndex;
import org.weather.api.cn.realtime.RealtimeWeather;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.util.Log;

public class RealtimeProvider {

	private DatabaseSupport databaseSupport;
	private WeatherService weatherService;
	private SettingProvider settingProvider;

	public RealtimeProvider(DatabaseSupport databaseSupport, WeatherService weatherService,
			SettingProvider settingProvider) {
		super();
		this.databaseSupport = databaseSupport;
		this.weatherService = weatherService;
		this.settingProvider = settingProvider;
	}

	public Cursor find(String citycode) {
		Log.i(RealtimeProvider.class.getSimpleName(), "citycode: " + citycode);
		MatrixCursor result = new MatrixCursor(new String[] { Weather.RealtimeWeather.ID, Weather.RealtimeWeather.NAME,
				Weather.RealtimeWeather.TIME, Weather.RealtimeWeather.TEMPERATURE, Weather.RealtimeWeather.HUMIDITY,
				Weather.RealtimeWeather.WINDDIRECTION, Weather.RealtimeWeather.WINDFORCE,
				Weather.RealtimeWeather.DRESS, Weather.RealtimeWeather.ULTRAVIOLET, Weather.RealtimeWeather.CLEANCAR,
				Weather.RealtimeWeather.TRAVEL, Weather.RealtimeWeather.COMFORT,
				Weather.RealtimeWeather.MORNINGEXERCISE, Weather.RealtimeWeather.SUNDRY,
				Weather.RealtimeWeather.IRRITABILITY });
		// query database
		Cursor cursor = databaseSupport.find(DatabaseSupport.COL_TYPE + "=? and " + DatabaseSupport.COL_CODE + "=?",
				new Object[] { Weather.RealtimeWeather.TYPE, citycode });
		if (cursor.moveToFirst()) {
			String uptime = cursor.getString(cursor.getColumnIndex(DatabaseSupport.COL_UPDATETIME));
			if (!settingProvider.isOvertime(new Date(Long.valueOf(uptime)))) {
				String value = cursor.getString(cursor.getColumnIndex(DatabaseSupport.COL_VALUE));
				result.addRow(value.split(";"));
				cursor.close();
				Log.i(RealtimeProvider.class.getSimpleName(), "found realtime weather from sqlite");
				return result;
			}
		}
		cursor.close();
		// query web server
		List<Object> row = new ArrayList<Object>();
		RealtimeWeather realtime = weatherService.getRealtimeWeather(citycode);
		if (realtime != null) {
			Log.i(RealtimeProvider.class.getSimpleName(), "found realtime weather from web");
			Collections.addAll(row, realtime.getCityId(), realtime.getCityName(), realtime.getTime(),
					realtime.getTemperature(), realtime.getHumidity(), realtime.getWindDirection(),
					realtime.getWindForce());
			ContentValues values = new ContentValues();
			values.put(Weather.RealtimeWeather.ID, realtime.getCityId());
			values.put(Weather.RealtimeWeather.NAME, realtime.getCityName());
			values.put(Weather.RealtimeWeather.TIME, realtime.getTime());
			values.put(Weather.RealtimeWeather.TEMPERATURE, realtime.getTemperature());
			values.put(Weather.RealtimeWeather.HUMIDITY, realtime.getHumidity());
			values.put(Weather.RealtimeWeather.WINDDIRECTION, realtime.getWindDirection());
			values.put(Weather.RealtimeWeather.WINDFORCE, realtime.getWindForce());
			ForecastWeather forecast = weatherService.getForecastWeather(citycode);
			if (forecast != null) {
				row.set(2, forecast.getTime());
				values.put(Weather.RealtimeWeather.TIME, forecast.getTime());
				LivingIndex li = forecast.getDressIndex();
				row.add(li != null ? li.getIndex() : null);
				values.put(Weather.RealtimeWeather.DRESS, li != null ? li.getIndex() : null);
				li = forecast.getUltravioletIndex();
				row.add(li != null ? li.getIndex() : null);
				values.put(Weather.RealtimeWeather.ULTRAVIOLET, li != null ? li.getIndex() : null);
				li = forecast.getCleanCarIndex();
				row.add(li != null ? li.getIndex() : null);
				values.put(Weather.RealtimeWeather.CLEANCAR, li != null ? li.getIndex() : null);
				li = forecast.getTravelIndex();
				row.add(li != null ? li.getIndex() : null);
				values.put(Weather.RealtimeWeather.TRAVEL, li != null ? li.getIndex() : null);
				li = forecast.getComfortIndex();
				row.add(li != null ? li.getIndex() : null);
				values.put(Weather.RealtimeWeather.COMFORT, li != null ? li.getIndex() : null);
				li = forecast.getMorningExerciseIndex();
				row.add(li != null ? li.getIndex() : null);
				values.put(Weather.RealtimeWeather.MORNINGEXERCISE, li != null ? li.getIndex() : null);
				li = forecast.getSunDryIndex();
				row.add(li != null ? li.getIndex() : null);
				values.put(Weather.RealtimeWeather.SUNDRY, li != null ? li.getIndex() : null);
				li = forecast.getIrritabilityIndex();
				row.add(li != null ? li.getIndex() : null);
				values.put(Weather.RealtimeWeather.IRRITABILITY, li != null ? li.getIndex() : null);
			}
			result.addRow(row);
			this.update(values);
		} else {
			Log.e(RealtimeProvider.class.getName(), "get realtime weather failed");
		}
		return result;
	}

	public void update(ContentValues values) {
		String city = (values != null ? values.getAsString(Weather.RealtimeWeather.ID) : null);
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
		value.append(values.getAsString(Weather.RealtimeWeather.ID)).append(";");
		value.append(values.getAsString(Weather.RealtimeWeather.NAME)).append(";");
		value.append(values.getAsString(Weather.RealtimeWeather.TIME)).append(";");
		value.append(values.getAsString(Weather.RealtimeWeather.TEMPERATURE)).append(";");
		value.append(values.getAsString(Weather.RealtimeWeather.HUMIDITY)).append(";");
		value.append(values.getAsString(Weather.RealtimeWeather.WINDDIRECTION)).append(";");
		value.append(values.getAsString(Weather.RealtimeWeather.WINDFORCE)).append(";");
		value.append(values.getAsString(Weather.RealtimeWeather.DRESS)).append(";");
		value.append(values.getAsString(Weather.RealtimeWeather.ULTRAVIOLET)).append(";");
		value.append(values.getAsString(Weather.RealtimeWeather.CLEANCAR)).append(";");
		value.append(values.getAsString(Weather.RealtimeWeather.TRAVEL)).append(";");
		value.append(values.getAsString(Weather.RealtimeWeather.COMFORT)).append(";");
		value.append(values.getAsString(Weather.RealtimeWeather.MORNINGEXERCISE)).append(";");
		value.append(values.getAsString(Weather.RealtimeWeather.SUNDRY)).append(";");
		value.append(values.getAsString(Weather.RealtimeWeather.IRRITABILITY)).append(";");
		setting.put(DatabaseSupport.COL_VALUE, value.toString());
		rowId = databaseSupport.save(rowId, setting);
		Log.i(RealtimeProvider.class.getSimpleName(), "updated realtime weather");
	}

}
