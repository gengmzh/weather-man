/**
 * 
 */
package org.weather.weatherman.content;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.util.Log;

/**
 * 天气信息服务类
 * 
 * @author gengmaozhang01
 * @since 2014-1-3 下午7:05:57
 */
public class WeatherSupport {

	private static final String tag = WeatherSupport.class.getSimpleName();

	private DatabaseSupport databaseSupport;
	private WeatherClient weatherClient;

	public WeatherSupport(DatabaseSupport databaseSupport) {
		super();
		if (databaseSupport == null) {
			throw new IllegalArgumentException("databaseSupport is required");
		}
		this.databaseSupport = databaseSupport;
		this.weatherClient = new WeatherClient(databaseSupport.getContext());
	}

	/**
	 * 获取天气实况信息
	 * 
	 * @author gengmaozhang01
	 * @since 2014-1-3 下午7:53:19
	 */
	public Cursor findRealtimeWeather(String citycode) {
		Log.i(tag, "city is " + citycode);
		MatrixCursor result = new MatrixCursor(new String[] { Weather.RealtimeWeather.ID, Weather.RealtimeWeather.NAME,
				Weather.RealtimeWeather.TIME, Weather.RealtimeWeather.TEMPERATURE, Weather.RealtimeWeather.HUMIDITY,
				Weather.RealtimeWeather.WINDDIRECTION, Weather.RealtimeWeather.WINDFORCE });
		// database
		String value = null;
		Cursor cursor = databaseSupport.find(DatabaseSupport.COL_TYPE + "=? and " + DatabaseSupport.COL_CODE + "=?",
				new Object[] { Weather.RealtimeWeather.TYPE, citycode });
		if (cursor.moveToFirst()) {
			value = cursor.getString(cursor.getColumnIndex(DatabaseSupport.COL_VALUE));
			String uptime = cursor.getString(cursor.getColumnIndex(DatabaseSupport.COL_UPDATETIME));
			if (this.isNotOvertime(uptime)) {
				try {
					result.addRow(value.split(";"));
					Log.i(tag, "get realtime weather from database");
					return result;
				} catch (Exception ex) {
					Log.e(tag, "parse realtime weather failed", ex);
				} finally {
					cursor.close();
				}
			}
		}
		cursor.close();
		// web
		Weather.RealtimeWeather realtime = null;
		try {
			realtime = weatherClient.getRealtimeWeather(citycode);
		} catch (Exception e) {
			Log.e(tag, "get realtime weather failed", e);
		}
		if (realtime != null) {
			result.addRow(new Object[] { realtime.getCityId(), realtime.getCityName(), realtime.getTime(),
					realtime.getTemperature(), realtime.getHumidity(), realtime.getWindDirection(),
					realtime.getWindForce() });
			databaseSupport.saveRealtimeWeather(realtime);
			Log.i(tag, "refresh realtime weather by api");
		} else if (value != null) { // 网络异常使用旧的实况信息
			try {
				result.addRow(value.split(";"));
				Log.i(tag, "using old realtime weather");
			} catch (Exception ex) {
				Log.e(tag, "parse realtime weather failed", ex);
			}
		}
		return result;
	}

	/**
	 * 获取天气预报信息
	 * 
	 * @author gengmaozhang01
	 * @since 2014-1-3 下午7:53:35
	 */
	public Cursor findForecastWeather(String citycode) {
		Log.i(tag, "city is " + citycode);
		MatrixCursor result = new MatrixCursor(new String[] { Weather.ForecastWeather.ID, Weather.ForecastWeather.NAME,
				Weather.ForecastWeather.TIME, Weather.ForecastWeather.WEATHER, Weather.ForecastWeather.TEMPERATURE,
				Weather.ForecastWeather.IMAGE, Weather.ForecastWeather.WIND, Weather.ForecastWeather.WINDFORCE });
		// query database
		String value = null;
		Cursor cursor = databaseSupport.find(DatabaseSupport.COL_TYPE + "=? and " + DatabaseSupport.COL_CODE + "=?",
				new Object[] { Weather.ForecastWeather.TYPE, citycode });
		if (cursor.moveToFirst()) {
			value = cursor.getString(cursor.getColumnIndex(DatabaseSupport.COL_VALUE));
			String uptime = cursor.getString(cursor.getColumnIndex(DatabaseSupport.COL_UPDATETIME));
			if (this.isNotOvertime(uptime)) {
				try {
					String[] rows = value.split("#");
					for (String row : rows) {
						result.addRow(row.split(";"));
					}
					Log.i(tag, "get forecast weather from database");
					return result;
				} catch (Exception ex) {
					result.close();
					result = new MatrixCursor(new String[] { Weather.ForecastWeather.ID, Weather.ForecastWeather.NAME,
							Weather.ForecastWeather.TIME, Weather.ForecastWeather.WEATHER,
							Weather.ForecastWeather.TEMPERATURE, Weather.ForecastWeather.IMAGE,
							Weather.ForecastWeather.WIND, Weather.ForecastWeather.WINDFORCE });
					Log.e(tag, "parse forcast weather failed", ex);
				} finally {
					cursor.close();
				}
			}
		}
		cursor.close();
		// query web server
		Weather.ForecastWeather forecast = null;
		try {
			forecast = weatherClient.getForecastWeather(citycode);
		} catch (Exception e) {
			Log.e(tag, "get forecast weather failed", e);
		}
		if (forecast != null) {
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
			databaseSupport.saveForecastAndIndexWeather(forecast);
			Log.i(tag, "refresh forecast weather by api");
		} else if (value != null && value.length() > 0) { // 网络异常使用旧的预报信息
			try {
				String[] rows = value.split("#");
				for (String row : rows) {
					result.addRow(row.split(";"));
				}
				Log.i(tag, "using old forecast weather");
			} catch (Exception ex) {
				Log.e(tag, "parse forcast weather failed", ex);
				result.close();
				result = new MatrixCursor(new String[] { Weather.ForecastWeather.ID, Weather.ForecastWeather.NAME,
						Weather.ForecastWeather.TIME, Weather.ForecastWeather.WEATHER,
						Weather.ForecastWeather.TEMPERATURE, Weather.ForecastWeather.IMAGE,
						Weather.ForecastWeather.WIND, Weather.ForecastWeather.WINDFORCE });
			}
		}
		return result;
	}

	/**
	 * 获取天气指数信息
	 * 
	 * @author gengmaozhang01
	 * @since 2014-1-3 下午7:57:48
	 */
	public Cursor findIndexWeather(String citycode) {
		Log.i(tag, "city is " + citycode);
		MatrixCursor result = new MatrixCursor(new String[] { Weather.LivingIndex.ID, Weather.LivingIndex.NAME,
				Weather.LivingIndex.TIME, Weather.LivingIndex.DRESS, Weather.LivingIndex.ULTRAVIOLET,
				Weather.LivingIndex.CLEANCAR, Weather.LivingIndex.TRAVEL, Weather.LivingIndex.COMFORT,
				Weather.LivingIndex.MORNINGEXERCISE, Weather.LivingIndex.SUNDRY, Weather.LivingIndex.IRRITABILITY });
		// database
		String value = null;
		Cursor cursor = databaseSupport.find(DatabaseSupport.COL_TYPE + "=? and " + DatabaseSupport.COL_CODE + "=?",
				new Object[] { Weather.LivingIndex.TYPE, citycode });
		if (cursor.moveToFirst()) {
			value = cursor.getString(cursor.getColumnIndex(DatabaseSupport.COL_VALUE));
			String uptime = cursor.getString(cursor.getColumnIndex(DatabaseSupport.COL_UPDATETIME));
			if (this.isNotOvertime(uptime)) {
				try {
					result.addRow(value.split(";"));
					Log.i(tag, "get living index from database");
					return result;
				} catch (Exception ex) {
					Log.e(tag, "parse living index failed", ex);
				} finally {
					cursor.close();
				}
			}
		}
		cursor.close();
		// web
		Weather.ForecastWeather forecast = null;
		try {
			forecast = weatherClient.getForecastWeather(citycode);
		} catch (Exception e) {
			Log.e(tag, "get living index failed", e);
		}
		if (forecast != null) {
			List<Object> row = new ArrayList<Object>();
			Collections.addAll(row, forecast.getCityId(), forecast.getCityName(), forecast.getTime());
			Weather.LivingIndex li = forecast.getDressIndex();
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
			databaseSupport.saveForecastAndIndexWeather(forecast);
			Log.i(tag, "refresh living index by api");
		} else if (value != null && value.length() > 0) { // 网络异常使用旧的指数信息
			try {
				result.addRow(value.split(";"));
				Log.i(tag, "using old living index");
			} catch (Exception ex) {
				Log.e(tag, "using old living index failed", ex);
			}
		}
		return result;
	}

	private boolean isNotOvertime(String uptime) {
		if (System.currentTimeMillis() - Long.parseLong(uptime) > 10 * 60 * 1000) {
			return false;
		}
		return true;
	}

}
