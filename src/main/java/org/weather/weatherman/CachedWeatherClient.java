/**
 * 
 */
package org.weather.weatherman;

import org.weather.api.cn.WeatherClient;
import org.weather.api.cn.forecast.ForecastWeather;
import org.weather.api.cn.realtime.RealtimeWeather;

import android.util.Log;

/**
 * @since 2012-5-16
 * @author gmz
 * 
 */
public class CachedWeatherClient {

	private WeatherClient weatherClient;

	public CachedWeatherClient() {
		weatherClient = new WeatherClient(5000, 10000, 5);
	}

	public RealtimeWeather getRealtimeWeather(String citycode) {
		try {
			return weatherClient.getRealWeather(citycode);
		} catch (Exception e) {
			Log.e(CachedWeatherClient.class.getSimpleName(), "get weather failed", e);
			return null;
		}
	}

	public ForecastWeather getForecastWeather(String citycode) {
		try {
			return weatherClient.getForecastWeather(citycode);
		} catch (Exception e) {
			Log.e(CachedWeatherClient.class.getSimpleName(), "get weather failed", e);
			return null;
		}
	}

}
