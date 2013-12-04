package org.weather.api.cn.forecast;

import java.util.Map;

import org.weather.api.cn.AbstractClient;
import org.weather.api.cn.Config;

public class ForecastWeatherClient extends AbstractClient {

	public ForecastWeather getWeather(String citycode) throws Exception {
		String url = Config.getInstance().getForecastUrl(citycode);
		Map<String, Object> weather = readSafely(url);
		return new ForecastWeather(weather);
	}

}
