package org.weather.api.cn.realtime;

import java.util.Map;

import org.weather.api.cn.AbstractClient;
import org.weather.api.cn.Config;

/**
 * read the real time weather info from <a
 * href="http://www.weather.com.cn">weather.com.cn</a>
 * 
 * @author gmz
 * @time 2012-5-13
 */
public class RealtimeWeatherClient extends AbstractClient {

	public RealtimeWeather getWeather(String citycode) throws Exception {
		String url = Config.getInstance().getRealtimeUrl(citycode);
		Map<String, Object> weather = readSafely(url);
		return new RealtimeWeather(weather);
	}

}
