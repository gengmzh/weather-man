package org.weather.weatherman.content;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

public class WeatherDataProvider extends ContentProvider {

	private static final UriMatcher URI_MATCHER;
	static {
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URI_MATCHER.addURI(Weather.AUTHORITY, Weather.SETTING_PATH, Weather.Setting.TYPE);
		URI_MATCHER.addURI(Weather.AUTHORITY, Weather.SETTING_PATH + "/#", Weather.Setting.TYPE);
		URI_MATCHER.addURI(Weather.AUTHORITY, Weather.REALTIME_PATH + "/#", Weather.RealtimeWeather.TYPE);
		URI_MATCHER.addURI(Weather.AUTHORITY, Weather.FORECAST_PATH + "/#", Weather.ForecastWeather.TYPE);
	}

	private SettingProvider settingProvider;
	private RealtimeProvider realtimeProvider;
	private ForecastProvider forecastProvider;

	@Override
	public boolean onCreate() {
		DatabaseSupport databaseSupport = new DatabaseSupport(getContext());
		WeatherService weatherService = new WeatherService();
		settingProvider = new SettingProvider(databaseSupport);
		realtimeProvider = new RealtimeProvider(databaseSupport, weatherService, settingProvider);
		forecastProvider = new ForecastProvider(databaseSupport, weatherService, settingProvider);
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		switch (URI_MATCHER.match(uri)) {
		case Weather.Setting.TYPE:
			return settingProvider.find();
		case Weather.RealtimeWeather.TYPE:
			String citycode = uri.getLastPathSegment();
			return realtimeProvider.find(citycode);
		case Weather.ForecastWeather.TYPE:
			citycode = uri.getLastPathSegment();
			return forecastProvider.find(citycode);
		default:
			throw new IllegalArgumentException("unknown Uri " + uri);
		}
	}

	@Override
	public String getType(Uri uri) {
		switch (URI_MATCHER.match(uri)) {
		case Weather.Setting.TYPE:
			return Weather.Setting.CONTENT_TYPE;
		case Weather.RealtimeWeather.TYPE:
			return Weather.RealtimeWeather.CONTENT_TYPE;
		case Weather.ForecastWeather.TYPE:
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
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		if (URI_MATCHER.match(uri) == Weather.Setting.TYPE) {
			settingProvider.update(values);
			return 1;
		}
		return 0;
	}

}
