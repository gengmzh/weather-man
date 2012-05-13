package org.weather.weatherman;

import android.net.Uri;
import android.provider.BaseColumns;

public final class Weather {

	public static final String AUTHORITY = "org.weather.weatherman.provider";
	public static final String REALTIME_PATH = "realtime/#",
			FORECAST_PATH = "forecast/#";
	public static final int REALTIME_ID = 1, FORECAST_ID = 2;

	public static final class RealtimeWeather implements BaseColumns {
		private RealtimeWeather() {
		}

		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/" + REALTIME_PATH);
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd."
				+ AUTHORITY + "." + REALTIME_PATH;

		public static final String NAME = "city";
		public static final String TIME = "time";
		public static final String TEMPERATURE = "temp";
		public static final String HUMIDITY = "SD";
		public static final String WINDDIRECTION = "WD";
		public static final String WINDFORCE = "WF";
	}

	public static final class ForecastWeather implements BaseColumns {
		private ForecastWeather() {
		}

		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/" + FORECAST_PATH);
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd."
				+ AUTHORITY + "." + FORECAST_PATH;

		public static final String NAME = "city";
		public static final String TIME = "time";
		public static final String WEATHER = "weather";
		public static final String TEMPERATURE = "temp";
		public static final String IMAGE = "image";
		public static final String WIND = "wind";
		public static final String WINDFORCE = "WF";

	}

}
