/**
 * 
 */
package cn.seddat.weatherman.content;

import java.util.Map;

import org.json.simple.JSONValue;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

/**
 * 天气实况支持类
 * 
 * @author gengmaozhang01
 * @since 2014-1-3 下午7:05:57
 */
public class WeatherService {

	private static final String tag = WeatherService.class.getSimpleName();

	private ContentResolver contentResolver;
	private WeatherClient weatherClient;

	public WeatherService(Context context) {
		super();
		this.contentResolver = context.getContentResolver();
		this.weatherClient = new WeatherClient(context);
	}

	/**
	 * 获取天气实况信息
	 * 
	 * @author gengmaozhang01
	 * @since 2014-1-3 下午7:53:19
	 */
	public Weather.RealtimeWeather findRealtimeWeather(String citycode) {
		Log.i(tag, "city is " + citycode);
		// database
		Weather.RealtimeWeather weather = this.getRealtimeWeather(citycode);
		if (weather != null) {
			String uptime = (String) weather.getValue().get("uptime");
			if (!this.isOvertime(uptime)) {
				return weather;
			}
		}
		// webservice
		Weather.RealtimeWeather realtime = null;
		try {
			realtime = weatherClient.getRealtimeWeather(citycode);
		} catch (Exception e) {
			Log.e(tag, "get realtime weather failed", e);
		}
		if (realtime != null) {
			this.saveRealtimeWeather(realtime);
			Log.i(tag, "refresh realtime weather by api");
			return realtime;
		} else if (weather != null) { // 网络异常使用旧的实况信息
			Log.i(tag, "using old realtime weather");
			return weather;
		}
		return null;
	}

	private Weather.RealtimeWeather getRealtimeWeather(String citycode) {
		// query
		Uri uri = Uri.withAppendedPath(DatabaseSupport.Content.CONTENT_URI, Weather.RealtimeWeather.PATH_REALTIME);
		String where = DatabaseSupport.Content.COL_CODE + "=?";
		String[] args = new String[] { citycode };
		Cursor cursor = contentResolver.query(uri, null, where, args, null);
		if (!cursor.moveToFirst()) {
			cursor.close();
			return null;
		}
		// parse
		String value = cursor.getString(cursor.getColumnIndex(DatabaseSupport.Content.COL_VALUE));
		String uptime = cursor.getString(cursor.getColumnIndex(DatabaseSupport.Content.COL_UPDATETIME));
		@SuppressWarnings("unchecked")
		Map<String, Object> weatherinfo = (Map<String, Object>) JSONValue.parse(value);
		weatherinfo.put("uptime", uptime);
		cursor.close();
		return new Weather.RealtimeWeather(weatherinfo);
	}

	private void saveRealtimeWeather(Weather.RealtimeWeather realtime) {
		String city = (realtime != null ? realtime.getCityId() : null);
		if (city == null || city.length() == 0) {
			return;
		}
		// update
		Uri uri = Uri.withAppendedPath(DatabaseSupport.Content.CONTENT_URI, Weather.RealtimeWeather.PATH_REALTIME);
		String where = DatabaseSupport.Content.COL_CODE + "=?";
		String[] args = new String[] { city };
		ContentValues values = new ContentValues();
		values.put(DatabaseSupport.Content.COL_CODE, city);
		values.put(DatabaseSupport.Content.COL_VALUE, JSONValue.toJSONString(realtime.getValue()));
		int rows = contentResolver.update(uri, values, where, args);
		// insert
		if (rows <= 0) {
			contentResolver.insert(uri, values);
		}
	}

	/**
	 * 上次更新时间超过10min即为超时
	 */
	private boolean isOvertime(String uptime) {
		if (System.currentTimeMillis() - Long.parseLong(uptime) > 10 * 60 * 1000) {
			return true;
		}
		return false;
	}

	/**
	 * 获取天气 预报信息
	 * 
	 * @author gengmaozhang01
	 * @since 2014-1-3 下午7:53:19
	 */
	public Weather.ForecastWeather findForecastWeather(String citycode) {
		Log.i(tag, "city is " + citycode);
		// database
		Weather.ForecastWeather weather = this.getForecastWeather(citycode);
		if (weather != null) {
			String uptime = (String) weather.getValue().get("uptime");
			if (!this.isOvertime(uptime)) {
				return weather;
			}
		}
		// webservice
		Weather.ForecastWeather forecast = null;
		try {
			forecast = weatherClient.getForecastWeather(citycode);
		} catch (Exception e) {
			Log.e(tag, "get forecast weather failed", e);
		}
		if (forecast != null) {
			this.saveForecastWeather(forecast);
			Log.i(tag, "refresh forecast weather by api");
			return forecast;
		} else if (weather != null) { // 网络异常使用旧的实况信息
			Log.i(tag, "using old forecast weather");
			return weather;
		}
		return null;
	}

	private Weather.ForecastWeather getForecastWeather(String citycode) {
		// query
		Uri uri = Uri.withAppendedPath(DatabaseSupport.Content.CONTENT_URI, Weather.ForecastWeather.PATH_FORECAST);
		String where = DatabaseSupport.Content.COL_CODE + "=?";
		String[] args = new String[] { citycode };
		Cursor cursor = contentResolver.query(uri, null, where, args, null);
		if (!cursor.moveToFirst()) {
			cursor.close();
			return null;
		}
		// parse
		String value = cursor.getString(cursor.getColumnIndex(DatabaseSupport.Content.COL_VALUE));
		String uptime = cursor.getString(cursor.getColumnIndex(DatabaseSupport.Content.COL_UPDATETIME));
		@SuppressWarnings("unchecked")
		Map<String, Object> weatherinfo = (Map<String, Object>) JSONValue.parse(value);
		weatherinfo.put("uptime", uptime);
		cursor.close();
		return new Weather.ForecastWeather(weatherinfo);
	}

	private void saveForecastWeather(Weather.ForecastWeather forecast) {
		String city = (forecast != null ? forecast.getCityId() : null);
		if (city == null || city.length() == 0) {
			return;
		}
		// update
		Uri uri = Uri.withAppendedPath(DatabaseSupport.Content.CONTENT_URI, Weather.ForecastWeather.PATH_FORECAST);
		String where = DatabaseSupport.Content.COL_CODE + "=?";
		String[] args = new String[] { city };
		ContentValues values = new ContentValues();
		values.put(DatabaseSupport.Content.COL_CODE, city);
		values.put(DatabaseSupport.Content.COL_VALUE, JSONValue.toJSONString(forecast.getValue()));
		int rows = contentResolver.update(uri, values, where, args);
		// insert
		if (rows <= 0) {
			contentResolver.insert(uri, values);
		}
	}

	/**
	 * 获取空气质量信息
	 * 
	 * @author gengmaozhang01
	 * @since 2014-1-3 下午7:53:19
	 */
	public Weather.AirQualityIndex findAirQualityIndex(String citycode) {
		Log.i(tag, "city is " + citycode);
		// database
		Weather.AirQualityIndex weather = this.getAirQualityIndex(citycode);
		if (weather != null) {
			String uptime = (String) weather.getValue().remove("uptime");
			if (!this.isOvertime(uptime)) {
				return weather;
			}
		}
		// webservice
		Weather.AirQualityIndex aqi = null;
		try {
			aqi = weatherClient.getAirQualityIndex(citycode);
		} catch (Exception e) {
			Log.e(tag, "get AQI failed", e);
		}
		if (aqi != null) {
			this.saveAirQualityIndex(aqi);
			Log.i(tag, "refresh AQI by api");
			return aqi;
		} else if (weather != null) { // 网络异常使用旧的实况信息
			Log.i(tag, "using old AQI");
			return weather;
		}
		return null;
	}

	private Weather.AirQualityIndex getAirQualityIndex(String citycode) {
		// query
		Uri uri = Uri.withAppendedPath(DatabaseSupport.Content.CONTENT_URI, Weather.AirQualityIndex.PATH_AQI);
		String where = DatabaseSupport.Content.COL_CODE + "=?";
		String[] args = new String[] { citycode };
		Cursor cursor = contentResolver.query(uri, null, where, args, null);
		if (!cursor.moveToFirst()) {
			cursor.close();
			return null;
		}
		// parse
		String value = cursor.getString(cursor.getColumnIndex(DatabaseSupport.Content.COL_VALUE));
		String uptime = cursor.getString(cursor.getColumnIndex(DatabaseSupport.Content.COL_UPDATETIME));
		@SuppressWarnings("unchecked")
		Map<String, Object> weatherinfo = (Map<String, Object>) JSONValue.parse(value);
		weatherinfo.put("uptime", uptime);
		cursor.close();
		return new Weather.AirQualityIndex(weatherinfo);
	}

	private void saveAirQualityIndex(Weather.AirQualityIndex aqi) {
		String city = (aqi != null ? aqi.getCityId() : null);
		if (city == null || city.length() == 0) {
			return;
		}
		// update
		Uri uri = Uri.withAppendedPath(DatabaseSupport.Content.CONTENT_URI, Weather.AirQualityIndex.PATH_AQI);
		String where = DatabaseSupport.Content.COL_CODE + "=?";
		String[] args = new String[] { city };
		ContentValues values = new ContentValues();
		values.put(DatabaseSupport.Content.COL_CODE, city);
		values.put(DatabaseSupport.Content.COL_VALUE, JSONValue.toJSONString(aqi.getValue()));
		int rows = contentResolver.update(uri, values, where, args);
		// insert
		if (rows <= 0) {
			contentResolver.insert(uri, values);
		}
	}

}
