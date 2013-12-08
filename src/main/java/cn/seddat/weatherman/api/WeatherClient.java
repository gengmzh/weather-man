/**
 * 
 */
package cn.seddat.weatherman.api;

import cn.seddat.weatherman.api.forecast.ForecastWeather;
import cn.seddat.weatherman.api.forecast.ForecastWeatherClient;
import cn.seddat.weatherman.api.realtime.RealtimeWeather;
import cn.seddat.weatherman.api.realtime.RealtimeWeatherClient;

/**
 * @since 2012-5-16
 * @author gmz
 * 
 */
public class WeatherClient {

	private RealtimeWeatherClient realtimeWeatherClient;
	private ForecastWeatherClient forecastWeatherClient;

	public WeatherClient() {
		this(0, 0, 1);
	}

	public WeatherClient(int connTimeout, int readTimeout, int retry) {
		super();
		realtimeWeatherClient = new RealtimeWeatherClient(connTimeout, readTimeout, retry);
		forecastWeatherClient = new ForecastWeatherClient(connTimeout, readTimeout, retry);
	}

	public RealtimeWeather getRealWeather(String citycode) throws Exception {
		return realtimeWeatherClient.getWeather(citycode);
	}

	public ForecastWeather getForecastWeather(String citycode) throws Exception {
		return forecastWeatherClient.getWeather(citycode);
	}

}
