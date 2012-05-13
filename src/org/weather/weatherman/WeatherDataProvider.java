package org.weather.weatherman;

import org.weather.client.cn.forecast.ForecastWeather;
import org.weather.client.cn.forecast.ForecastWeatherClient;
import org.weather.client.cn.realtime.RealtimeWeather;
import org.weather.client.cn.realtime.RealtimeWeatherClient;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

public class WeatherDataProvider extends ContentProvider {

	private static final UriMatcher URI_MATCHER;
	static {
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URI_MATCHER.addURI(Weather.AUTHORITY, Weather.REALTIME_PATH,
				Weather.REALTIME_ID);
		URI_MATCHER.addURI(Weather.AUTHORITY, Weather.FORECAST_PATH,
				Weather.FORECAST_ID);
	}

	private RealtimeWeatherClient realtimeWeatherClient;
	private ForecastWeatherClient forecastWeatherClient;

	@Override
	public boolean onCreate() {
		realtimeWeatherClient = new RealtimeWeatherClient();
		forecastWeatherClient = new ForecastWeatherClient();
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		String citycode = uri.getLastPathSegment();
		switch (URI_MATCHER.match(uri)) {
		case Weather.REALTIME_ID:
			MatrixCursor rtc = new MatrixCursor(new String[] {
					Weather.RealtimeWeather._ID, Weather.RealtimeWeather.NAME,
					Weather.RealtimeWeather.TIME,
					Weather.RealtimeWeather.TEMPERATURE,
					Weather.RealtimeWeather.HUMIDITY,
					Weather.RealtimeWeather.WINDDIRECTION,
					Weather.RealtimeWeather.WINDFORCE });
			try {
				RealtimeWeather weather = realtimeWeatherClient
						.getWeather(citycode);
				rtc.addRow(new Object[] { weather.getCityId(),
						weather.getCityName(), weather.getTime(),
						weather.getTemperature(), weather.getHumidity(),
						weather.getWindDirection(), weather.getWindForce() });
			} catch (Exception e) {
				e.printStackTrace();
			}
			return rtc;
		case Weather.FORECAST_ID:
			MatrixCursor fcc = new MatrixCursor(new String[] {
					Weather.ForecastWeather._ID, Weather.ForecastWeather.NAME,
					Weather.ForecastWeather.TIME,
					Weather.ForecastWeather.WEATHER,
					Weather.ForecastWeather.TEMPERATURE,
					Weather.ForecastWeather.IMAGE,
					Weather.ForecastWeather.WIND,
					Weather.ForecastWeather.WINDFORCE });
			try {
				ForecastWeather weather = forecastWeatherClient
						.getWeather(citycode);
				// TODO
			} catch (Exception e) {
				e.printStackTrace();
			}
			return fcc;
		default:
			throw new IllegalArgumentException("unknown Uri " + uri);
		}
	}

	@Override
	public String getType(Uri uri) {
		switch (URI_MATCHER.match(uri)) {
		case Weather.REALTIME_ID:
			return Weather.RealtimeWeather.CONTENT_TYPE;
		case Weather.FORECAST_ID:
			return Weather.ForecastWeather.CONTENT_TYPE;
		default:
			throw new IllegalArgumentException("unknown Uri " + uri);
		}
	}

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
