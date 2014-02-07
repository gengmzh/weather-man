package org.weather.weatherman.content;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.net.Uri;
import android.provider.BaseColumns;

public final class Weather {

	public static final String AUTHORITY = "org.weather.weatherman.provider";
	public static final String CITY_PATH = "city", SETTING_PATH = "setting";
	public static final String REALTIME_PATH = "realtime", FORECAST_PATH = "forecast", INDEX_PATH = "index",
			AQI_PATH = "aqi";

	private Weather() {
	}

	public static final class City {
		private City() {
		}

		public static final int TYPE = 0;
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + CITY_PATH);
		public static final String CONTENT_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + CITY_PATH;

		public static final String TABLE_NAME = "city";

		public static final String ID = BaseColumns._ID;
		public static final String CODE = "code";
		public static final String NAME = "name";
		public static final String PARENT = "p";

	}

	public static final class Setting implements BaseColumns {
		private Setting() {
		}

		public static final int TYPE = 1;
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + SETTING_PATH);
		public static final String CONTENT_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + SETTING_PATH;

		public static final String ID = _ID;
		public static final String CITY1_CODE = "c1c", CITY2_CODE = "c2c", CITY3_CODE = "c3c";
		public static final String CITY1_NAME = "c1n", CITY2_NAME = "c2n", CITY3_NAME = "c3n";
		public static final String UPTIME = "uptime";
	}

	/**
	 * 实时天气信息
	 * 
	 * @author gengmaozhang01
	 * @since 2014-1-3 下午6:55:14
	 */
	public static final class RealtimeWeather implements BaseColumns {

		public static final int TYPE = 2;
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + REALTIME_PATH);
		public static final String CONTENT_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + REALTIME_PATH;

		public static final String ID = _ID;
		public static final String NAME = "city";
		public static final String TIME = "time";
		public static final String TEMPERATURE = "temperature";
		public static final String HUMIDITY = "humidity";
		public static final String WINDDIRECTION = "winddirection";
		public static final String WINDFORCE = "windforce";

		private Map<String, String> value;

		public RealtimeWeather(Map<String, Object> value) {
			this.value = new HashMap<String, String>();
			if (value != null) {
				Object obj = value.get("weatherinfo");
				if (obj != null && (obj instanceof Map)) {
					value = (Map<String, Object>) obj;
				}
				for (String key : value.keySet()) {
					this.value.put(key, String.valueOf(value.get(key)));
				}
			}
		}

		public String getTime() {
			return value.get("time");
		}

		public String getCityId() {
			return value.get("cityid");
		}

		public String getCityName() {
			return value.get("city");
		}

		public String getTemperature() {
			String val = value.get("temp");
			return (val != null ? (val.endsWith("℃") ? val : val + "℃") : "");
		}

		public String getHumidity() {
			return value.get("SD");
		}

		public String getWindDirection() {
			return value.get("WD");
		}

		public String getWindForce() {
			return value.get("WS");
		}

		public int getWindForceValue() {
			String wf = value.get("WSE");
			return wf != null && wf.length() > 0 ? Integer.valueOf(wf) : 0;
		}

		public boolean hasRadar() {
			return "1".equals(value.get("isRadar"));
		}

		public String getRadar() {
			return value.get("Radar");
		}

		@Override
		public String toString() {
			return value.toString();
		}

	}

	public static final class ForecastWeather implements BaseColumns {

		public static final int TYPE = 3;
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + FORECAST_PATH);
		public static final String CONTENT_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + FORECAST_PATH;

		public static final String ID = _ID;
		public static final String NAME = "city";
		public static final String TIME = "time";
		public static final String WEATHER = "weather";
		public static final String TEMPERATURE = "temperature";
		public static final String IMAGE = "image";
		public static final String WIND = "wind";
		public static final String WINDFORCE = "windforce";

		private Map<String, String> value;

		public ForecastWeather(Map<String, Object> value) {
			this.value = new HashMap<String, String>();
			if (value != null) {
				Object obj = value.get("weatherinfo");
				if (obj != null && (obj instanceof Map)) {
					value = (Map<String, Object>) obj;
				}
				for (String key : value.keySet()) {
					this.value.put(key, String.valueOf(value.get(key)));
				}
			}
		}

		public String getTime() {
			String date = value.get("date_y");
			String time = value.get("fchh");
			if (date == null && time == null) {
				return "";
			}
			Calendar cal = Calendar.getInstance();
			int si = 0, ei = date.indexOf("年");
			cal.set(Calendar.YEAR, Integer.valueOf(date.substring(si, ei)));
			si = ei + 1;
			ei = date.indexOf("月");
			cal.set(Calendar.MONTH, Integer.valueOf(date.substring(si, ei)) - 1);
			si = ei + 1;
			ei = date.indexOf("日");
			cal.set(Calendar.DAY_OF_MONTH, Integer.valueOf(date.substring(si, ei)));
			cal.set(Calendar.HOUR_OF_DAY, Integer.valueOf(time));
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			return df.format(cal.getTime());
		}

		public String getCityId() {
			return value.get("cityid");
		}

		public String getCityName() {
			return value.get("city");
		}

		public List<String> getWeather() {
			List<String> wtl = new ArrayList<String>();
			for (int i = 1; i < 7; i++) {
				String wt = value.get("weather" + i);
				if (wt != null) {
					String[] wts = wt.split("转");
					wtl.add(wts[0]);
					wtl.add(wts.length < 2 ? wts[0] : wts[1]);
				}
			}
			return wtl;
		}

		public List<String> getTemperature() {
			List<String> tpl = new ArrayList<String>();
			for (int i = 1; i < 7; i++) {
				String tp = value.get("temp" + i);
				if (tp != null) {
					String[] tps = tp.split("~");
					tpl.add(tps[0]);
					tpl.add(tps.length < 2 ? tps[0] : tps[1]);
				}
			}
			return tpl;
		}

		public List<String> getImage() {
			List<String> imgl = new ArrayList<String>();
			for (int i = 1; i < 13; i++) {
				String img = value.get("img" + i);
				if (img != null) {
					imgl.add(img);
				}
			}
			return imgl;
		}

		public List<String> getWind() {
			List<String> wdl = new ArrayList<String>();
			for (int i = 1; i < 7; i++) {
				String wd = value.get("wind" + i);
				if (wd != null) {
					wdl.add(wd);
					wdl.add(wd);
				}
			}
			return wdl;
		}

		public List<String> getWindForce() {
			List<String> wdl = new ArrayList<String>();
			for (int i = 1; i < 7; i++) {
				String wd = value.get("fl" + i);
				if (wd != null) {
					wdl.add(wd);
					wdl.add(wd);
				}
			}
			return wdl;
		}

		/**
		 * @return 穿衣指数
		 */
		public LivingIndex getDressIndex() {
			return new LivingIndex(value.get("index"), value.get("index_d"));
		}

		/**
		 * @return 紫外线指数
		 */
		public LivingIndex getUltravioletIndex() {
			return new LivingIndex(value.get("index_uv"), "");
		}

		/**
		 * @return 洗车指数
		 */
		public LivingIndex getCleanCarIndex() {
			return new LivingIndex(value.get("index_xc"), "");
		}

		/**
		 * @return 出游指数
		 */
		public LivingIndex getTravelIndex() {
			return new LivingIndex(value.get("index_tr"), "");
		}

		/**
		 * @return 舒适度指数
		 */
		public LivingIndex getComfortIndex() {
			return new LivingIndex(value.get("index_co"), "");
		}

		/**
		 * @return 晨练指数
		 */
		public LivingIndex getMorningExerciseIndex() {
			return new LivingIndex(value.get("index_cl"), "");
		}

		/**
		 * @return 晾晒指数
		 */
		public LivingIndex getSunDryIndex() {
			return new LivingIndex(value.get("index_ls"), "");
		}

		/**
		 * @return 过敏指数
		 */
		public LivingIndex getIrritabilityIndex() {
			return new LivingIndex(value.get("index_ag"), "");
		}

		@Override
		public String toString() {
			return value.toString();
		}

	}

	public static final class LivingIndex implements BaseColumns {

		public static final int TYPE = 4;
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + INDEX_PATH);
		public static final String CONTENT_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + INDEX_PATH;

		public static final String ID = _ID;
		public static final String NAME = "city";
		public static final String TIME = "time";
		public static final String DRESS = "dress";
		public static final String ULTRAVIOLET = "ultraviolet";
		public static final String CLEANCAR = "cleancar";
		public static final String TRAVEL = "travel";
		public static final String COMFORT = "comfort";
		public static final String MORNINGEXERCISE = "morningexercise";
		public static final String SUNDRY = "sundry";
		public static final String IRRITABILITY = "irritability";

		private String index, description;

		/**
		 * @param index
		 * @param description
		 */
		public LivingIndex(String index, String description) {
			super();
			this.index = index;
			this.description = description;
		}

		public String getIndex() {
			return index;
		}

		public String getDescription() {
			return description;
		}

		@Override
		public String toString() {
			return "LivingIndex{index:" + index + ", description:" + description + "}";
		}

	}

	public static final class AirQualityIndex implements BaseColumns {

		public static final int TYPE = 5;
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + AQI_PATH);
		public static final String CONTENT_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + AQI_PATH;

		public static final String ID = _ID;
		public static final String NAME = "city";
		public static final String TIME = "time";
		public static final String AQI = "aqi";
		public static final String TAG = "tag";

		private Map<String, Object> value;

		public AirQualityIndex(Map<String, Object> value) {
			this.value = new HashMap<String, Object>();
			if (value != null) {
				Object obj = value.get("weatherinfo");
				if (obj != null && (obj instanceof Map)) {
					value = (Map<String, Object>) obj;
				}
				for (String key : value.keySet()) {
					this.value.put(key, value.get(key));
				}
			}
		}

		public String getTime() {
			return (String) value.get("time");
		}

		public String getCityId() {
			return (String) value.get("cityid");
		}

		public String getCityName() {
			return (String) value.get("city");
		}

		public String getAQICity() {
			return (String) value.get("AQI_city");
		}

		public int getCurrentAQI() {
			Object obj = value.get("AQI");
			return obj == null ? -1 : Integer.parseInt(obj.toString());
		}

		public int getHourlySize() {
			List<Object> hourly = (List<Object>) value.get("hourly");
			return hourly != null ? hourly.size() : 0;
		}

		public String getHourlyTime(int index) {
			List<Map<String, String>> hourly = (List<Map<String, String>>) value.get("hourly");
			if (index >= 0 && index < hourly.size()) {
				return hourly.get(index).get("time");
			}
			return null;
		}

		public int getHourlyAQI(int index) {
			List<Map<String, String>> hourly = (List<Map<String, String>>) value.get("hourly");
			if (index >= 0 && index < hourly.size()) {
				String aqi = hourly.get(index).get("AQI");
				return aqi != null ? Integer.parseInt(aqi) : -1;
			}
			return -1;
		}

		public int getDailySize() {
			List<Object> daily = (List<Object>) value.get("daily");
			return daily != null ? daily.size() : 0;
		}

		public String getDailyTime(int index) {
			List<Map<String, String>> daily = (List<Map<String, String>>) value.get("daily");
			if (index >= 0 && index < daily.size()) {
				return daily.get(index).get("time");
			}
			return null;
		}

		public int getDailyAQI(int index) {
			List<Map<String, String>> daily = (List<Map<String, String>>) value.get("daily");
			if (index >= 0 && index < daily.size()) {
				String aqi = daily.get(index).get("AQI");
				return aqi != null ? Integer.parseInt(aqi) : -1;
			}
			return -1;
		}

		public long getTimestamp() {
			Object obj = value.get("timestamp");
			return obj == null ? 0 : Long.parseLong(obj.toString());
		}

		@Override
		public String toString() {
			return AirQualityIndex.class.getSimpleName() + "{value:" + value.toString() + "}";
		}

	}

}
