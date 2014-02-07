package org.weather.weatherman.content;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

public class WeatherContentProvider extends ContentProvider {

	private static final UriMatcher URI_MATCHER;
	static {
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		// setting
		URI_MATCHER.addURI(Weather.AUTHORITY, Weather.SETTING_PATH, Weather.Setting.TYPE);
		URI_MATCHER.addURI(Weather.AUTHORITY, Weather.SETTING_PATH + "/#", Weather.Setting.TYPE);
		// weather
		URI_MATCHER.addURI(Weather.AUTHORITY, Weather.REALTIME_PATH + "/#", Weather.RealtimeWeather.TYPE);
		URI_MATCHER.addURI(Weather.AUTHORITY, Weather.FORECAST_PATH + "/#", Weather.ForecastWeather.TYPE);
		URI_MATCHER.addURI(Weather.AUTHORITY, Weather.INDEX_PATH + "/#", Weather.LivingIndex.TYPE);
		URI_MATCHER.addURI(Weather.AUTHORITY, Weather.AQI_PATH + "/#", Weather.AirQualityIndex.TYPE);
		// city
		URI_MATCHER.addURI(Weather.AUTHORITY, Weather.CITY_PATH, Weather.City.TYPE);
		URI_MATCHER.addURI(Weather.AUTHORITY, Weather.CITY_PATH + "/#", Weather.City.TYPE);
	}

	private CitySupport citySupport;
	private WeatherSupport weatherSupport;
	private AQISupport aqiSupport;

	@Override
	public boolean onCreate() {
		DatabaseSupport databaseSupport = new DatabaseSupport(getContext());
		citySupport = new CitySupport(databaseSupport);
		weatherSupport = new WeatherSupport(databaseSupport);
		aqiSupport = new AQISupport(databaseSupport);
		return true;
	}

	@Override
	public String getType(Uri uri) {
		switch (URI_MATCHER.match(uri)) {
		case Weather.City.TYPE:
			return Weather.City.CONTENT_TYPE;
		case Weather.Setting.TYPE:
			return Weather.Setting.CONTENT_TYPE;
		case Weather.RealtimeWeather.TYPE:
			return Weather.RealtimeWeather.CONTENT_TYPE;
		case Weather.ForecastWeather.TYPE:
			return Weather.ForecastWeather.CONTENT_TYPE;
		case Weather.LivingIndex.TYPE:
			return Weather.LivingIndex.CONTENT_TYPE;
		case Weather.AirQualityIndex.TYPE:
			return Weather.AirQualityIndex.CONTENT_TYPE;
		default:
			throw new IllegalArgumentException("unknown Uri " + uri);
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		switch (URI_MATCHER.match(uri)) {
		case Weather.City.TYPE:
			return citySupport.findCity(selection, selectionArgs, sortOrder);
		case Weather.Setting.TYPE:
			return citySupport.findSetting();
		case Weather.RealtimeWeather.TYPE:// 天气实况
			String citycode = uri.getLastPathSegment();
			return weatherSupport.findRealtimeWeather(citycode);
		case Weather.ForecastWeather.TYPE:// 天气预报
			citycode = uri.getLastPathSegment();
			return weatherSupport.findForecastWeather(citycode);
		case Weather.LivingIndex.TYPE:// 天气指数
			citycode = uri.getLastPathSegment();
			return weatherSupport.findIndexWeather(citycode);
		case Weather.AirQualityIndex.TYPE:
			citycode = uri.getLastPathSegment();
			return aqiSupport.findAirQualityIndex(citycode);
		default:
			throw new IllegalArgumentException("unknown Uri " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		switch (URI_MATCHER.match(uri)) {
		case Weather.City.TYPE:
			return null;
		case Weather.Setting.TYPE:
			return null;
		case Weather.RealtimeWeather.TYPE:
			return null;
		case Weather.ForecastWeather.TYPE:
			return null;
		case Weather.LivingIndex.TYPE:
			return null;
		case Weather.AirQualityIndex.TYPE:
			return null;
		default:
			throw new IllegalArgumentException("unknown Uri " + uri);
		}
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		switch (URI_MATCHER.match(uri)) {
		case Weather.City.TYPE:
			return citySupport.insertCity(values);
		case Weather.Setting.TYPE:
			return 0;
		case Weather.RealtimeWeather.TYPE:
			return 0;
		case Weather.ForecastWeather.TYPE:
			return 0;
		case Weather.LivingIndex.TYPE:
			return 0;
		case Weather.AirQualityIndex.TYPE:
			return 0;
		default:
			throw new IllegalArgumentException("unknown Uri " + uri);
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		switch (URI_MATCHER.match(uri)) {
		case Weather.City.TYPE:
			return 0;
		case Weather.Setting.TYPE:
			citySupport.updateSetting(values);
			return 1;
		case Weather.RealtimeWeather.TYPE:
			return 0;
		case Weather.ForecastWeather.TYPE:
			return 0;
		case Weather.LivingIndex.TYPE:
			return 0;
		case Weather.AirQualityIndex.TYPE:
			return 0;
		default:
			throw new IllegalArgumentException("unknown Uri " + uri);
		}
	}

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		switch (URI_MATCHER.match(arg0)) {
		case Weather.City.TYPE:
			return 0;
		case Weather.Setting.TYPE:
			return 0;
		case Weather.RealtimeWeather.TYPE:
			return 0;
		case Weather.ForecastWeather.TYPE:
			return 0;
		case Weather.LivingIndex.TYPE:
			return 0;
		case Weather.AirQualityIndex.TYPE:
			return 0;
		default:
			throw new IllegalArgumentException("unknown Uri " + arg0);
		}
	}

}
