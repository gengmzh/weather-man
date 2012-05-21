package org.weather.weatherman.content;

import android.net.Uri;
import android.provider.BaseColumns;

public final class Weather {

	public static final String AUTHORITY = "org.weather.weatherman.provider";
	public static final String SETTING_PATH = "setting", REALTIME_PATH = "realtime", FORECAST_PATH = "forecast";

	private Weather() {
	}

	public static final class Setting implements BaseColumns {
		private Setting() {
		}

		public static final int TYPE = 1;
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + SETTING_PATH);
		public static final String CONTENT_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + SETTING_PATH;

		public static final String ID = _ID;
		public static final String CITY1 = "city1", CITY2 = "city2", CITY3 = "city3";
		public static final String UPDATETIME = "updatetime";
	}

	public static final class RealtimeWeather implements BaseColumns {
		private RealtimeWeather() {
		}

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

		public static final String DRESS = "dress";
		public static final String ULTRAVIOLET = "ultraviolet";
		public static final String CLEANCAR = "cleancar";
		public static final String TRAVEL = "travel";
		public static final String COMFORT = "comfort";
		public static final String MORNINGEXERCISE = "morningexercise";
		public static final String SUNDRY = "sundry";
		public static final String IRRITABILITY = "irritability";
	}

	public static final class ForecastWeather implements BaseColumns {
		private ForecastWeather() {
		}

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

	}

}
