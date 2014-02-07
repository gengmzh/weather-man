/**
 * 
 */
package org.weather.weatherman.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.util.Log;

/**
 * @author gengmaozhang01
 * @since 2014-2-7 下午9:38:32
 */
public class AQISupport {

	private static final String tag = AQISupport.class.getSimpleName();

	private DatabaseSupport databaseSupport;
	private WeatherClient weatherClient;

	public AQISupport(DatabaseSupport databaseSupport) {
		if (databaseSupport == null) {
			throw new IllegalArgumentException("databaseSupport is required");
		}
		this.databaseSupport = databaseSupport;
		this.weatherClient = new WeatherClient(databaseSupport.getContext());
	}

	/**
	 * 获取空气质量指数
	 * 
	 * @author gengmaozhang01
	 * @since 2014-2-7 下午9:39:38
	 */
	public Cursor findAirQualityIndex(String citycode) {
		Log.i(tag, "city is " + citycode);
		MatrixCursor result = new MatrixCursor(new String[] { Weather.AirQualityIndex.ID, Weather.AirQualityIndex.NAME,
				Weather.AirQualityIndex.TIME, Weather.AirQualityIndex.AQI, Weather.AirQualityIndex.TAG });
		// database
		Weather.AirQualityIndex aqi = this.getAQI(citycode);
		if (aqi != null) {
			if (System.currentTimeMillis() - aqi.getTimestamp() <= 60 * 1000) { // 1分钟即超时
				try {
					result.addRow(new Object[] { aqi.getCityId(), aqi.getAQICity(), aqi.getTime(), aqi.getCurrentAQI(),
							"current" });
					for (int i = 0; i < aqi.getHourlySize(); i++) {
						result.addRow(new Object[] { aqi.getCityId(), aqi.getAQICity(), aqi.getHourlyTime(i),
								aqi.getHourlyAQI(i), "hourly" });
					}
					for (int i = 0; i < aqi.getDailySize(); i++) {
						result.addRow(new Object[] { aqi.getCityId(), aqi.getAQICity(), aqi.getDailyTime(i),
								aqi.getDailyAQI(i), "daily" });
					}
					Log.i(tag, "get AQI from database");
					return result;
				} catch (Exception ex) {
					Log.e(tag, "parse AQI failed", ex);
				}
			}
		}
		// web
		Weather.AirQualityIndex aqi2 = null;
		try {
			aqi2 = weatherClient.getAirQualityIndex(citycode);
		} catch (Exception e) {
			Log.e(tag, "get AQI failed", e);
		}
		if (aqi2 != null) {
			aqi = aqi2;
			result.addRow(new Object[] { aqi.getCityId(), aqi.getAQICity(), aqi.getTime(), aqi.getCurrentAQI(),
					"current" });
			for (int i = 0; i < aqi.getHourlySize(); i++) {
				result.addRow(new Object[] { aqi.getCityId(), aqi.getAQICity(), aqi.getHourlyTime(i),
						aqi.getHourlyAQI(i), "hourly" });
			}
			for (int i = 0; i < aqi.getDailySize(); i++) {
				result.addRow(new Object[] { aqi.getCityId(), aqi.getAQICity(), aqi.getDailyTime(i),
						aqi.getDailyAQI(i), "daily" });
			}
			this.saveAQI(aqi2);
			Log.i(tag, "refresh AQI by api");
		} else if (aqi != null) { // 网络异常使用旧的AQI
			try {
				result.addRow(new Object[] { aqi.getCityId(), aqi.getAQICity(), aqi.getTime(), aqi.getCurrentAQI(),
						"current" });
				for (int i = 0; i < aqi.getHourlySize(); i++) {
					result.addRow(new Object[] { aqi.getCityId(), aqi.getAQICity(), aqi.getHourlyTime(i),
							aqi.getHourlyAQI(i), "hourly" });
				}
				for (int i = 0; i < aqi.getDailySize(); i++) {
					result.addRow(new Object[] { aqi.getCityId(), aqi.getAQICity(), aqi.getDailyTime(i),
							aqi.getDailyAQI(i), "daily" });
				}
				Log.i(tag, "using old AQI");
			} catch (Exception ex) {
				Log.e(tag, "parse AQI failed", ex);
			}
		}
		return result;
	}

	private Weather.AirQualityIndex getAQI(String citycode) {
		Cursor cursor = databaseSupport.find(DatabaseSupport.COL_TYPE + "=? and " + DatabaseSupport.COL_CODE + "=?",
				new Object[] { Weather.AirQualityIndex.TYPE, citycode });
		if (!cursor.moveToFirst()) {
			cursor.close();
			return null;
		}
		String value = cursor.getString(cursor.getColumnIndex(DatabaseSupport.COL_VALUE));
		String uptime = cursor.getString(cursor.getColumnIndex(DatabaseSupport.COL_UPDATETIME));
		cursor.close();
		if (value == null || value.length() == 0) {
			return null;
		}
		Map<String, Object> weatherinfo = new HashMap<String, Object>();
		weatherinfo.put("timestamp", uptime);
		weatherinfo.put("cityid", citycode);
		String[] values = (value != null ? value.split(";") : new String[0]);
		if (values.length > 0) {
			weatherinfo.put("AQI_city", values[0]);
		}
		if (values.length > 1) {
			weatherinfo.put("time", values[1]);
		}
		if (values.length > 2) {
			weatherinfo.put("AQI", values[2]);
		}
		if (values.length > 3) {
			List<Map<String, String>> hourly = new ArrayList<Map<String, String>>();
			String[] vs = values[3].split(",");
			for (int i = 0; i < vs.length; i += 2) {
				Map<String, String> m = new HashMap<String, String>();
				m.put("time", vs[i]);
				m.put("AQI", vs[i + 1]);
				hourly.add(m);
			}
			weatherinfo.put("hourly", hourly);
		}
		if (values.length > 4) {
			List<Map<String, String>> daily = new ArrayList<Map<String, String>>();
			String[] vs = values[4].split(",");
			for (int i = 0; i < vs.length; i += 2) {
				Map<String, String> m = new HashMap<String, String>();
				m.put("time", vs[i]);
				m.put("AQI", vs[i + 1]);
				daily.add(m);
			}
			weatherinfo.put("daily", daily);
		}
		return new Weather.AirQualityIndex(weatherinfo);
	}

	private void saveAQI(Weather.AirQualityIndex aqi) {
		String city = (aqi != null ? aqi.getCityId() : null);
		if (city == null || city.length() == 0) {
			return;
		}
		// old
		long rowId = -1;
		Cursor cursor = databaseSupport.find(DatabaseSupport.COL_TYPE + "=? and " + DatabaseSupport.COL_CODE + "=?",
				new Object[] { Weather.AirQualityIndex.TYPE, city });
		if (cursor.moveToFirst()) {
			rowId = cursor.getLong(cursor.getColumnIndex(DatabaseSupport.COL_ID));
		}
		cursor.close();
		// save
		ContentValues setting = new ContentValues();
		setting.put(DatabaseSupport.COL_TYPE, Weather.AirQualityIndex.TYPE);
		setting.put(DatabaseSupport.COL_CODE, city);
		StringBuffer value = new StringBuffer();
		value.append(aqi.getAQICity()).append(";").append(aqi.getTime()).append(";");
		value.append(aqi.getCurrentAQI()).append(";");
		// 最近24小时AQI
		for (int i = 0; i < aqi.getHourlySize(); i++) {
			if (i > 0) {
				value.append(",");
			}
			value.append(aqi.getHourlyTime(i)).append(",").append(aqi.getHourlyAQI(i));
		}
		value.append(";");
		// 最近14天AQI
		for (int i = 0; i < aqi.getDailySize(); i++) {
			if (i > 0) {
				value.append(",");
			}
			value.append(aqi.getDailyTime(i)).append(",").append(aqi.getDailyAQI(i));
		}
		value.append(";");
		setting.put(DatabaseSupport.COL_VALUE, value.toString());
		rowId = databaseSupport.save(rowId, setting);
	}

}
