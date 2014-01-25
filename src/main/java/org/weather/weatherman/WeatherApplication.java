/**
 * 
 */
package org.weather.weatherman;

import org.weather.weatherman.activity.CityService;

import android.app.Application;
import cn.seddat.weatherman.api.city.City;

/**
 * @since 2012-5-18
 * @author gmz
 * 
 */
public class WeatherApplication extends Application {

	public static final String DOMOB_PUBLISHER_ID = "56OJz+douM3frcHll+";

	private City city;

	@Override
	public void onCreate() {
		super.onCreate();
		// 恢复城市设置
		CityService cityService = new CityService(getContentResolver());
		City city = cityService.getCitySetting();
		if (city != null) {
			this.city = city;
		}
	}

	public City getCity() {
		return city;
	}

	public void setCity(City city) {
		this.city = city;
	}

}
