/**
 * 
 */
package cn.seddat.weatherman.content;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONValue;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
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

	private String request(String url, Map<String, String> headers) throws Exception {
		StringBuffer content = new StringBuffer();
		HttpURLConnection conn = null;
		InputStreamReader reader = null;
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
			Charset charset = Charset.forName("UTF-8");
			if (headers != null && !headers.isEmpty()) {
				for (String key : headers.keySet()) {
					conn.setRequestProperty(key, headers.get(key));
					if ("Accept-Charset".equalsIgnoreCase(key)) {
						String value = headers.get(key);
						if (value != null && value.length() > 0) {
							charset = Charset.forName(value);
						}
					}
				}
			}
			// read
			conn.connect();
			reader = new InputStreamReader(conn.getInputStream(), charset);
			char[] cbuf = new char[1024];
			int len = 0;
			while ((len = reader.read(cbuf)) > -1) {
				content.append(cbuf, 0, len);
			}
		} finally {
			if (reader != null) {
				reader.close();
			}
			if (conn != null) {
				conn.disconnect();
			}
		}
		return content.toString();
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
				String json = this.request(url, null);
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
				if (this.checkNetwork()) { // 联网时才统计API请求失败事件
					StatService.onEvent(context, "api", "realtime-failure", 1);
				}
			}
		}
		return null;
	}

	/**
	 * 检查当前是否联网
	 * 
	 * @author gengmaozhang01
	 * @since 2014-3-18 下午10:35:49
	 */
	private boolean checkNetwork() {
		try {
			ConnectivityManager conn = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			State state = conn.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
			if (state != null && (state == State.CONNECTED || state == State.CONNECTING)) {
				return true;
			}
			state = conn.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
			if (state != null && (state == State.CONNECTED || state == State.CONNECTING)) {
				return true;
			}
			return false;
		} catch (Exception ex) {
			Log.e(tag, "check network failed", ex);
			// 检查出异常时不能断定，默认联网
			return true;
		}
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
				String json = this.request(url, null);
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
				if (this.checkNetwork()) { // 联网时才统计API请求失败事件
					StatService.onEvent(context, "api", "forecast-failure", 1);
				}
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
		// cnpm25
		try {
			Map<String, Object> value = this.getAQIOfCNPM25(citycode);
			if (value != null && !value.isEmpty() && (value.containsKey("hourly") || value.containsKey("daily"))) {
				Log.i(tag, "get AQI from CNPM25 succeed");
				StatService.onEvent(context, "api", "AQI-success-CNPM25", 1);
				return new Weather.AirQualityIndex(value);
			}
		} catch (Exception ex) {
			Log.e(tag, "get AQI from CNPM25 failed", ex);
			if (this.checkNetwork()) { // 联网时才统计API请求失败事件
				StatService.onEvent(context, "api", "AQI-failure-CNPM25", 1);
			}
		}
		// api
		String url = api + "/aqi/" + citycode;
		for (int i = 0; i < retry; i++) {
			try {
				String json = this.request(url, null);
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
				if (this.checkNetwork()) { // 联网时才统计API请求失败事件
					StatService.onEvent(context, "api", "AQI-failure", 1);
				}
			}
		}
		return null;
	}

	private static Map<String, String> aqiCities;

	/**
	 * 通过cnpm25.cn的API抓取AQI数据
	 * 
	 * @author gengmaozhang01
	 * @since 2014-2-7 上午8:32:47
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Object> getAQIOfCNPM25(String citycode) throws Exception {
		if (citycode == null || citycode.length() < 9) {
			throw new IllegalArgumentException("citycode is required");
		}
		// init
		if (aqiCities == null || aqiCities.isEmpty()) {
			synchronized (WeatherClient.class) {
				if (aqiCities == null || aqiCities.isEmpty()) {
					aqiCities = new HashMap<String, String>();
					BufferedReader reader = null;
					try {
						InputStream ins = WeatherClient.class.getClassLoader().getResourceAsStream(
								"org/weather/weatherman/content/city.properties");
						reader = new BufferedReader(new InputStreamReader(ins));
						String line = null;
						while ((line = reader.readLine()) != null) {
							String[] ls = line.split("\t");
							if (ls.length > 2) {
								aqiCities.put(ls[0], ls[2]);
							}
						}
					} catch (Exception ex) {
						Log.e(tag, "init AQI cities failed", ex);
					} finally {
						if (reader != null) {
							reader.close();
						}
					}
				}
			}
		}
		String aqiCity = aqiCities.get(citycode);
		if (aqiCity == null || aqiCity.length() == 0) {
			aqiCity = aqiCities.get(citycode.substring(0, 7));
			if (aqiCity == null || aqiCity.length() == 0) {
				aqiCity = aqiCities.get(citycode.substring(0, 6) + "0");
			}
			if (aqiCity == null || aqiCity.length() == 0) {
				aqiCity = aqiCities.get(citycode.substring(0, 5));
			}
		}
		if (aqiCity == null || aqiCity.length() == 0) {
			throw new IllegalArgumentException("can't find AQI city");
		}
		// request
		String url = "http://appapi.cnpm25.cn/TopInfoWeb.aspx?u=" + aqiCity;
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Host", "appapi.cnpm25.cn");
		headers.put("Connection", "Keep-Alive");
		headers.put("User-Agent", "Apache-HttpClient/UNAVAILABLE (java 1.4)");
		headers.put("Accept-Charset", "UTF-8");
		String content = this.request(url, headers);
		if (content == null || content.length() == 0) {
			throw new Exception("query AQI failed, result is empty");
		}
		// parse
		Map<String, Object> weatherinfo = new HashMap<String, Object>();
		weatherinfo.put("cityid", citycode);
		weatherinfo.put("city", "");
		// city
		Map<String, Object> jsonResult = (Map<String, Object>) JSONValue.parse(content);
		Object value = jsonResult.get("CityName");
		weatherinfo.put("AQI_city", value != null ? value.toString() : "");
		// aqi
		value = jsonResult.get("AQI");
		if (value != null) {
			String aqi = value.toString();
			int si = aqi.indexOf('_');
			if (si > -1) {
				aqi = aqi.substring(0, si);
			}
			weatherinfo.put("AQI", aqi);
		}
		// time
		value = jsonResult.get("UpDateTime");
		if (value != null) {
			String time = value.toString().replace('-', '.');
			weatherinfo.put("time", time);
		}
		// hourly
		Map<String, Object> trend = (Map<String, Object>) jsonResult.get("Tread");
		List<Map<String, String>> hourly = new ArrayList<Map<String, String>>();
		if (trend != null) {
			List<Object> hours = (List<Object>) trend.get("Date24hours"), aqis = (List<Object>) trend.get("AQI24hours");
			int size = Math.min(hours != null ? hours.size() : 0, aqis != null ? aqis.size() : 0);
			for (int i = 0; i < size; i++) {
				value = hours.get(i);
				String time = (value != null ? value.toString() : "").replace('-', '.');
				value = aqis.get(i);
				String aqi = (value != null ? value.toString() : "");
				Map<String, String> item = new HashMap<String, String>();
				item.put("time", time);
				item.put("AQI", aqi);
				hourly.add(item);
			}
		}
		if (!hourly.isEmpty()) {
			weatherinfo.put("hourly", hourly);
		}
		// daily
		List<Map<String, String>> daily = new ArrayList<Map<String, String>>();
		if (trend != null) {
			List<Object> days = (List<Object>) trend.get("Date30days"), aqis = (List<Object>) trend.get("AQI30days");
			int size = Math.min(days != null ? days.size() : 0, aqis != null ? aqis.size() : 0);
			for (int i = 0; i < size; i++) {
				value = days.get(i);
				String time = (value != null ? value.toString() : "").replace('-', '.');
				value = aqis.get(i);
				String aqi = (value != null ? value.toString() : "");
				Map<String, String> item = new HashMap<String, String>();
				item.put("time", time);
				item.put("AQI", aqi);
				daily.add(item);
			}
		}
		if (!daily.isEmpty()) {
			weatherinfo.put("daily", daily);
		}
		// result
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("weatherinfo", weatherinfo);
		Log.i(tag, "AQI: " + result);
		return result;
	}

}
