/**
 * 
 */
package org.weather.weatherman;

import org.weather.weatherman.activity.CityResolver;

import cn.seddat.weatherman.api.city.City;

import android.app.Application;

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
		CityResolver cityResolver = new CityResolver(getContentResolver());
		City city = cityResolver.getLocationSetting();
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
