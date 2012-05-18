/**
 * 
 */
package org.weather.weatherman;

import android.app.Application;

/**
 * @since 2012-5-18
 * @author gmz
 * 
 */
public class WeatherApplication extends Application {

	private String citycode;

	public String getCitycode() {
		return citycode;
	}

	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}

}
