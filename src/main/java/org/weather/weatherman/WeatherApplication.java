/**
 * 
 */
package org.weather.weatherman;

import org.weather.api.cn.city.City;

import android.app.Application;

/**
 * @since 2012-5-18
 * @author gmz
 * 
 */
public class WeatherApplication extends Application {

	private City city;

	public City getCity() {
		return city;
	}

	public void setCity(City city) {
		this.city = city;
	}

}
