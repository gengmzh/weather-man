/**
 * 
 */
package org.weather.weatherman;

import org.weather.api.cn.city.CityTree;

import android.app.Application;
import android.widget.TextView;

/**
 * @since 2012-5-18
 * @author gmz
 * 
 */
public class WeatherApplication extends Application {

	private CityTree cityTree;
	private String citycode;
	private TextView cityView;

	public CityTree getCityTree() {
		return cityTree;
	}

	public void setCityTree(CityTree cityTree) {
		this.cityTree = cityTree;
	}

	public String getCitycode() {
		return citycode;
	}

	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}

	public TextView getCityView() {
		return cityView;
	}

	public void setCityView(TextView cityView) {
		this.cityView = cityView;
	}

//	public void setCity(City city) {
//		this.citycode = city.getId();
//		this.cityView.setText(city.getName());
//	}

}
