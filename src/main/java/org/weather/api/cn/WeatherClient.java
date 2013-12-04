/**
 * 
 */
package org.weather.api.cn;

import org.weather.api.cn.forecast.ForecastWeather;
import org.weather.api.cn.forecast.ForecastWeatherClient;
import org.weather.api.cn.realtime.RealtimeWeather;
import org.weather.api.cn.realtime.RealtimeWeatherClient;

/**
 * @since 2012-5-16
 * @author gmz
 * 
 */
public class WeatherClient {

	private RealtimeWeatherClient realtimeWeatherClient;
	private ForecastWeatherClient forecastWeatherClient;

	public WeatherClient() {
		this(5000, 10000, 3);
	}

	public WeatherClient(int connTimeout, int readTimeout, int retry) {
		super();
		realtimeWeatherClient = new RealtimeWeatherClient();
		realtimeWeatherClient.setConnTimeout(connTimeout);
		realtimeWeatherClient.setReadTimeout(readTimeout);
		realtimeWeatherClient.setRetry(retry);
		forecastWeatherClient = new ForecastWeatherClient();
		forecastWeatherClient.setConnTimeout(connTimeout);
		forecastWeatherClient.setReadTimeout(readTimeout);
		forecastWeatherClient.setRetry(retry);
	}

	public RealtimeWeather getRealWeather(String citycode) throws Exception {
		return realtimeWeatherClient.getWeather(citycode);
	}

	public ForecastWeather getForecastWeather(String citycode) throws Exception {
		return forecastWeatherClient.getWeather(citycode);
	}

}
