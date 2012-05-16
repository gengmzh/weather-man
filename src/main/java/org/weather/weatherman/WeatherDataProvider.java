package org.weather.weatherman;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.weather.api.cn.forecast.ForecastWeather;
import org.weather.api.cn.forecast.LivingIndex;
import org.weather.api.cn.realtime.RealtimeWeather;

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

	private CachedWeatherClient weatherClient;

	@Override
	public boolean onCreate() {
		weatherClient = new CachedWeatherClient();
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		String citycode = uri.getLastPathSegment();
		Log.i(WeatherDataProvider.class.getSimpleName(), "citycode: " + citycode);
		switch (URI_MATCHER.match(uri)) {
		case URI_CODE_REALTIME:
			MatrixCursor realtimeCursor = new MatrixCursor(new String[] { Weather.RealtimeWeather.ID,
					Weather.RealtimeWeather.NAME, Weather.RealtimeWeather.TIME, Weather.RealtimeWeather.TEMPERATURE,
					Weather.RealtimeWeather.HUMIDITY, Weather.RealtimeWeather.WINDDIRECTION,
					Weather.RealtimeWeather.WINDFORCE, Weather.RealtimeWeather.DRESS,
					Weather.RealtimeWeather.ULTRAVIOLET, Weather.RealtimeWeather.CLEANCAR,
					Weather.RealtimeWeather.TRAVEL, Weather.RealtimeWeather.COMFORT,
					Weather.RealtimeWeather.MORNINGEXERCISE, Weather.RealtimeWeather.SUNDRY,
					Weather.RealtimeWeather.IRRITABILITY });
			List<Object> row = new ArrayList<Object>();
			RealtimeWeather realtime = weatherClient.getRealtimeWeather(citycode);
			if (realtime != null) {
				Collections.addAll(row, realtime.getCityId(), realtime.getCityName(), realtime.getTime(),
						realtime.getTemperature(), realtime.getHumidity(), realtime.getWindDirection(),
						realtime.getWindForce());
			}
			ForecastWeather forecast = weatherClient.getForecastWeather(citycode);
			if (forecast != null) {
				row.set(2, forecast.getTime());
				LivingIndex li = forecast.getDressIndex();
				row.add(li != null ? li.getIndex() : null);
				li = forecast.getUltravioletIndex();
				row.add(li != null ? li.getIndex() : null);
				li = forecast.getCleanCarIndex();
				row.add(li != null ? li.getIndex() : null);
				li = forecast.getTravelIndex();
				row.add(li != null ? li.getIndex() : null);
				li = forecast.getComfortIndex();
				row.add(li != null ? li.getIndex() : null);
				li = forecast.getMorningExerciseIndex();
				row.add(li != null ? li.getIndex() : null);
				li = forecast.getSunDryIndex();
				row.add(li != null ? li.getIndex() : null);
				li = forecast.getIrritabilityIndex();
				row.add(li != null ? li.getIndex() : null);
			}
			if (row.size() > 0) {
				realtimeCursor.addRow(row);
			} else {
				Log.e(WeatherDataProvider.class.getName(), "get realtime weather failed");
			}
			return realtimeCursor;
		case URI_CODE_FORECAST:
			MatrixCursor forecastCusor = new MatrixCursor(new String[] { Weather.ForecastWeather.ID,
					Weather.ForecastWeather.NAME, Weather.ForecastWeather.TIME, Weather.ForecastWeather.WEATHER,
					Weather.ForecastWeather.TEMPERATURE, Weather.ForecastWeather.IMAGE, Weather.ForecastWeather.WIND,
					Weather.ForecastWeather.WINDFORCE });
			ForecastWeather fw = weatherClient.getForecastWeather(citycode);
			if (fw != null) {
				List<String> wl = fw.getWeather(), tl = fw.getTemperature(), il = fw.getImage(), wdl = fw.getWind(), wfl = fw
						.getWindForce();
				int length = Math.min(wl.size(),
						Math.min(tl.size(), Math.min(il.size(), Math.min(wdl.size(), wfl.size()))));
				for (int i = 0; i < length; i++) {
					forecastCusor.addRow(new Object[] { fw.getCityId(), fw.getCityName(), fw.getTime(),
							wl.size() > i ? wl.get(i) : null, tl.size() > i ? tl.get(i) : null,
							il.size() > i ? il.get(i) : null, wdl.size() > i ? wdl.get(i) : null,
							wfl.size() > i ? wfl.get(i) : null });
				}
			} else {
				Log.e(WeatherDataProvider.class.getSimpleName(), "get forecast weather failed");
			}
			return forecastCusor;
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
