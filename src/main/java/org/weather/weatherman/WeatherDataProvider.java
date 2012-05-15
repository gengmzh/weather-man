package org.weather.weatherman;

import org.weather.api.cn.forecast.ForecastWeather;
import org.weather.api.cn.forecast.ForecastWeatherClient;
import org.weather.api.cn.realtime.RealtimeWeather;
import org.weather.api.cn.realtime.RealtimeWeatherClient;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

public class WeatherDataProvider extends ContentProvider {

	private static final UriMatcher URI_MATCHER;
	public static final int URI_CODE_REALTIME = 1, URI_CODE_FORECAST = 2;
	static {
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URI_MATCHER.addURI(Weather.AUTHORITY, Weather.REALTIME_PATH + "/#", URI_CODE_REALTIME);
		URI_MATCHER.addURI(Weather.AUTHORITY, Weather.FORECAST_PATH + "/#", URI_CODE_FORECAST);
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
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		String citycode = uri.getLastPathSegment();
		Log.i(WeatherDataProvider.class.getSimpleName(), "citycode: " + citycode);
		switch (URI_MATCHER.match(uri)) {
		case URI_CODE_REALTIME:
			MatrixCursor rtc = new MatrixCursor(new String[] { Weather.RealtimeWeather.ID,
					Weather.RealtimeWeather.NAME, Weather.RealtimeWeather.TIME, Weather.RealtimeWeather.TEMPERATURE,
					Weather.RealtimeWeather.HUMIDITY, Weather.RealtimeWeather.WINDDIRECTION,
					Weather.RealtimeWeather.WINDFORCE });
			try {
				RealtimeWeather weather = realtimeWeatherClient.getWeather(citycode);
				Log.i(WeatherDataProvider.class.getSimpleName(), "RealtimeWeather: " + weather);
				rtc.addRow(new Object[] { weather.getCityId(), weather.getCityName(), weather.getTime(),
						weather.getTemperature(), weather.getHumidity(), weather.getWindDirection(),
						weather.getWindForce() });
			} catch (Exception e) {
				Log.e(WeatherDataProvider.class.getName(), "get realtime weather failed", e);
			}
			return rtc;
		case URI_CODE_FORECAST:
			MatrixCursor fcc = new MatrixCursor(new String[] { Weather.ForecastWeather.ID,
					Weather.ForecastWeather.NAME, Weather.ForecastWeather.TIME, Weather.ForecastWeather.WEATHER,
					Weather.ForecastWeather.TEMPERATURE, Weather.ForecastWeather.IMAGE, Weather.ForecastWeather.WIND,
					Weather.ForecastWeather.WINDFORCE });
			try {
				ForecastWeather weather = forecastWeatherClient.getWeather(citycode);
				Log.i(WeatherDataProvider.class.getSimpleName(), "ForecastWeather: " + weather);
				fcc.addRow(new Object[] { weather.getCityId(), weather.getCityName(), weather.getTime(),
						weather.getWeather(), weather.getTemperature(), weather.getImage(), weather.getWind(),
						weather.getWindForce() });
			} catch (Exception e) {
				Log.e(WeatherDataProvider.class.getSimpleName(), "get forecast weather failed", e);
			}
			return fcc;
		default:
			throw new IllegalArgumentException("unknown Uri " + uri);
		}
	}

	@Override
	public String getType(Uri uri) {
		switch (URI_MATCHER.match(uri)) {
		case URI_CODE_REALTIME:
			return Weather.RealtimeWeather.CONTENT_TYPE;
		case URI_CODE_FORECAST:
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
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
