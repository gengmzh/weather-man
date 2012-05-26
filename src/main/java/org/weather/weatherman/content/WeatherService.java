/**
 * 
 */
package org.weather.weatherman.content;

import org.weather.api.cn.WeatherClient;
import org.weather.api.cn.forecast.ForecastWeather;
import org.weather.api.cn.realtime.RealtimeWeather;

import android.util.Log;

/**
 * @since 2012-5-16
 * @author gmz
 * 
 */
public class WeatherService {

	private WeatherClient weatherClient;

	public WeatherService() {
		weatherClient = new WeatherClient(5000, 10000, 5);
	}

	public RealtimeWeather getRealtimeWeather(String citycode) {
		try {
			return weatherClient.getRealWeather(citycode);
		} catch (Exception e) {
			Log.e(WeatherService.class.getSimpleName(), "get weather failed", e);
			return null;
		}
	}

	public ForecastWeather getForecastWeather(String citycode) {
		try {
			return weatherClient.getForecastWeather(citycode);
		} catch (Exception e) {
			Log.e(WeatherService.class.getSimpleName(), "get weather failed", e);
			return null;
		}
	}

}
