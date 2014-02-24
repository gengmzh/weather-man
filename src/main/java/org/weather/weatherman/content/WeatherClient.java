/**
 * 
 */
package org.weather.weatherman.content;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import org.json.simple.JSONValue;

import android.content.Context;
import android.util.Log;

import com.baidu.mobstat.StatService;

/**
 * 天气信息API客户端
 * 
 * @author gengmaozhang01
 * @since 2014-2-7 下午8:34:34
 */
public class WeatherClient {

	private static final String tag = WeatherClient.class.getSimpleName();
	// private static final String api = "http://42.96.143.229:8387/openapi/api/weather";
	private static final String api = "http://seddat.duapp.com/openapi/api/weather";

	private Context context;
	private int connectTimeout = 60 * 1000;
	private int readTimeout = 60 * 1000;
	private int retry = 3;

	public WeatherClient(Context context) {
		this(context, -1, -1, 0);
	}

	public WeatherClient(Context context, int connectTimeout, int readTimeout, int retry) {
		this.context = context;
		if (connectTimeout >= 0) {
			this.connectTimeout = connectTimeout;
		}
		if (readTimeout >= 0) {
			this.readTimeout = readTimeout;
		}
		if (retry > 0) {
			this.retry = retry;
		}
	}

	private String request(String url) throws Exception {
		String json = null;
		HttpURLConnection conn = null;
		InputStream ins = null;
		try {
			conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setRequestProperty("User-Agent", "WeatherMan/1.7 Android");
			conn.setRequestProperty("Content-Type", "text/html; charset=utf-8");
			if (connectTimeout > 0) {
				conn.setConnectTimeout(connectTimeout);
			}
			if (readTimeout > 0) {
				conn.setReadTimeout(readTimeout);
			}
			// read
			conn.connect();
			ins = conn.getInputStream();
			ByteArrayOutputStream ous = new ByteArrayOutputStream();
			byte[] b = new byte[1024];
			int len = 0;
			while ((len = ins.read(b)) > -1) {
				ous.write(b, 0, len);
			}
			json = ous.toString();
			ous.close();
		} finally {
			if (ins != null) {
				ins.close();
			}
			if (conn != null) {
				conn.disconnect();
			}
		}
		return json;
	}

	/**
	 * 获取天气实况信息
	 * 
	 * @author gengmaozhang01
	 * @since 2014-1-3 下午7:28:04
	 */
	public Weather.RealtimeWeather getRealtimeWeather(String citycode) throws Exception {
		if (citycode == null || citycode.length() == 0) {
			throw new IllegalArgumentException("citycode is required");
		}
		String url = api + "/realtime/" + citycode;
		for (int i = 0; i < retry; i++) {
			try {
				String json = this.request(url);
				@SuppressWarnings("unchecked")
				Map<String, Object> value = (Map<String, Object>) JSONValue.parse(json);
				Object code = value.get("code");
				if (code == null || !Number.class.isInstance(code) || ((Number) code).intValue() != 0) {
					Object msg = value.get("message");
					throw new Exception(msg != null ? msg.toString() : "get realtime weather failed");
				}
				StatService.onEvent(context, "api", "realtime-success", 1);
				return new Weather.RealtimeWeather(value);
			} catch (Exception ex) {
				Log.e(tag, "get realtime weather failed", ex);
				StatService.onEvent(context, "api", "realtime-failure", 1);
			}
		}
		return null;
	}

	/**
	 * 获取天气预报信息
	 * 
	 * @author gengmaozhang01
	 * @since 2014-1-3 下午7:28:04
	 */
	public Weather.ForecastWeather getForecastWeather(String citycode) throws Exception {
		if (citycode == null || citycode.length() == 0) {
			throw new IllegalArgumentException("citycode is required");
		}
		String url = api + "/forecast/" + citycode;
		for (int i = 0; i < retry; i++) {
			try {
				String json = this.request(url);
				@SuppressWarnings("unchecked")
				Map<String, Object> value = (Map<String, Object>) JSONValue.parse(json);
				Object code = value.get("code");
				if (code == null || !Number.class.isInstance(code) || ((Number) code).intValue() != 0) {
					Object msg = value.get("message");
					throw new Exception(msg != null ? msg.toString() : "get forecast weather failed");
				}
				StatService.onEvent(context, "api", "forecast-success", 1);
				return new Weather.ForecastWeather(value);
			} catch (Exception ex) {
				Log.e(tag, "get forecast weather failed", ex);
				StatService.onEvent(context, "api", "forecast-failure", 1);
			}
		}
		return null;
	}

	/**
	 * 获取空气质量指数
	 * 
	 * @author gengmaozhang01
	 * @since 2014-1-3 下午7:28:04
	 */
	public Weather.AirQualityIndex getAirQualityIndex(String citycode) throws Exception {
		if (citycode == null || citycode.length() == 0) {
			throw new IllegalArgumentException("citycode is required");
		}
		String url = api + "/aqi/" + citycode;
		for (int i = 0; i < retry; i++) {
			try {
				String json = this.request(url);
				@SuppressWarnings("unchecked")
				Map<String, Object> value = (Map<String, Object>) JSONValue.parse(json);
				Object code = value.get("code");
				if (code == null || !Number.class.isInstance(code) || ((Number) code).intValue() != 0) {
					Object msg = value.get("message");
					throw new Exception(msg != null ? msg.toString() : "get AQI failed");
				}
				StatService.onEvent(context, "api", "AQI-success", 1);
				return new Weather.AirQualityIndex(value);
			} catch (Exception ex) {
				Log.e(tag, "get AQI failed", ex);
				StatService.onEvent(context, "api", "AQI-failure", 1);
			}
		}
		return null;
	}

}
