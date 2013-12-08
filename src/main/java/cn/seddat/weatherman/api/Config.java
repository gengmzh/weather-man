/**
 * 
 */
package cn.seddat.weatherman.api;

/**
 * @author gmz
 * @time 2012-5-12
 */
public class Config {

	private String CITY1_URL = "http://www.weather.com.cn/data/city3jdata/china.html";
	private String CITY2_URL_PREFIX = "http://www.weather.com.cn/data/city3jdata/provshi/";
	private String CITY3_URL_PREFIX = "http://www.weather.com.cn/data/city3jdata/station/";
	private String URL_SUFFIX = ".html";

	private Config() {
	}

	public static final Config instance = new Config();

	public static Config getInstance() {
		return instance;
	}

	public String getCity1Url() {
		return CITY1_URL;
	}

	public String getCity2Url(String city1) {
		return CITY2_URL_PREFIX + city1 + URL_SUFFIX;
	}

	public String getCity3Url(String city2) {
		return CITY3_URL_PREFIX + city2 + URL_SUFFIX;
	}

	private String RT_URL_PREFIX = "http://www.weather.com.cn/data/sk/";

	public String getRealtimeUrl(String citycode) {
		return RT_URL_PREFIX + citycode + URL_SUFFIX;
	}

	private String FC_URL_PREFIX = "http://m.weather.com.cn/data/";

	public String getForecastUrl(String citycode) {
		return FC_URL_PREFIX + citycode + URL_SUFFIX;
	}

}
