package cn.seddat.weatherman.api.forecast;

import java.util.Map;

import cn.seddat.weatherman.api.AbstractClient;
import cn.seddat.weatherman.api.Config;

public class ForecastWeatherClient extends AbstractClient {

	public ForecastWeather getWeather(String citycode) throws Exception {
		String url = Config.getInstance().getForecastUrl(citycode);
		Map<String, Object> weather = readSafely(url);
		return new ForecastWeather(weather);
	}

}
