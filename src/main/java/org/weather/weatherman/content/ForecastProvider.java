package org.weather.weatherman.content;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.weather.api.cn.forecast.ForecastWeather;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.util.Log;

public class ForecastProvider {

	private DatabaseSupport databaseSupport;
	private WeatherService weatherService;
	private SettingProvider settingProvider;

	public ForecastProvider(DatabaseSupport databaseSupport, WeatherService weatherService,
			SettingProvider settingProvider) {
		super();
		this.databaseSupport = databaseSupport;
		this.weatherService = weatherService;
		this.settingProvider = settingProvider;
	}

	public Cursor find(String citycode) {
		Log.i(ForecastProvider.class.getSimpleName(), "citycode: " + citycode);
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
				Log.i(ForecastProvider.class.getSimpleName(), "found forecast weather from sqlite");
				return result;
			}
		}
		cursor.close();
		// query web server
		ForecastWeather fw = weatherService.getForecastWeather(citycode);
		if (fw != null) {
			Log.i(ForecastProvider.class.getSimpleName(), "found forecast weather from web");
			List<String> wl = fw.getWeather(), tl = fw.getTemperature(), il = fw.getImage(), wdl = fw.getWind(), wfl = fw
					.getWindForce();
			int length = Math
					.min(wl.size(), Math.min(tl.size(), Math.min(il.size(), Math.min(wdl.size(), wfl.size()))));
			List<ContentValues> values = new ArrayList<ContentValues>();
			for (int i = 0; i < length; i++) {
				result.addRow(new Object[] { fw.getCityId(), fw.getCityName(), fw.getTime(),
						wl.size() > i ? wl.get(i) : null, tl.size() > i ? tl.get(i) : null,
						il.size() > i ? il.get(i) : null, wdl.size() > i ? wdl.get(i) : null,
						wfl.size() > i ? wfl.get(i) : null });
				ContentValues value = new ContentValues();
				value.put(Weather.ForecastWeather.ID, fw.getCityId());
				value.put(Weather.ForecastWeather.NAME, fw.getCityName());
				value.put(Weather.ForecastWeather.TIME, fw.getTime());
				value.put(Weather.ForecastWeather.WEATHER, wl.size() > i ? wl.get(i) : null);
				value.put(Weather.ForecastWeather.TEMPERATURE, tl.size() > i ? tl.get(i) : null);
				value.put(Weather.ForecastWeather.IMAGE, il.size() > i ? il.get(i) : null);
				value.put(Weather.ForecastWeather.WIND, wdl.size() > i ? wdl.get(i) : null);
				value.put(Weather.ForecastWeather.WINDFORCE, wfl.size() > i ? wfl.get(i) : null);
				values.add(value);
			}
			this.update(values);
		} else {
			Log.e(ForecastProvider.class.getSimpleName(), "get forecast weather failed");
		}
		return result;
	}

	public void update(List<ContentValues> values) {
		String city = (values != null && values.size() > 0 ? values.get(0).getAsString(Weather.ForecastWeather.ID)
				: null);
		if (city == null || city.length() == 0) {
			return;
		}
		// old
		long rowId = -1;
		Cursor cursor = databaseSupport.find(DatabaseSupport.COL_TYPE + "=? and " + DatabaseSupport.COL_CODE + "=?",
				new Object[] { Weather.ForecastWeather.TYPE, city });
		if (cursor.moveToFirst()) {
			rowId = cursor.getLong(cursor.getColumnIndex(DatabaseSupport.COL_ID));
		}
		// save
		ContentValues setting = new ContentValues();
		setting.put(DatabaseSupport.COL_TYPE, Weather.ForecastWeather.TYPE);
		setting.put(DatabaseSupport.COL_CODE, city);
		StringBuffer value = new StringBuffer();
		for (ContentValues cv : values) {
			value.append(cv.getAsString(Weather.ForecastWeather.ID)).append(";");
			value.append(cv.getAsString(Weather.ForecastWeather.NAME)).append(";");
			value.append(cv.getAsString(Weather.ForecastWeather.TIME)).append(";");
			value.append(cv.getAsString(Weather.ForecastWeather.WEATHER)).append(";");
			value.append(cv.getAsString(Weather.ForecastWeather.TEMPERATURE)).append(";");
			value.append(cv.getAsString(Weather.ForecastWeather.IMAGE)).append(";");
			value.append(cv.getAsString(Weather.ForecastWeather.WIND)).append(";");
			value.append(cv.getAsString(Weather.ForecastWeather.WINDFORCE)).append("#");
		}
		setting.put(DatabaseSupport.COL_VALUE, value.toString());
		rowId = databaseSupport.save(rowId, setting);
		Log.i(ForecastProvider.class.getSimpleName(), "updated forecast weather");
	}

}
