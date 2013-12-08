package cn.seddat.weatherman.api.realtime;

import java.util.Map;

import cn.seddat.weatherman.api.AbstractClient;
import cn.seddat.weatherman.api.Config;

/**
 * read the real time weather info from <a
 * href="http://www.weather.com.cn">weather.com.cn</a>
 * 
 * @author gmz
 * @time 2012-5-13
 */
public class RealtimeWeatherClient extends AbstractClient {

	public RealtimeWeatherClient(int connTimeout, int readTimeout, int retry) {
		super(connTimeout, readTimeout, retry);
	}

	public RealtimeWeather getWeather(String citycode) throws Exception {
		String url = Config.getInstance().getRealtimeUrl(citycode);
		Map<String, Object> weather = readSafely(url);
		return new RealtimeWeather(weather);
	}

}
