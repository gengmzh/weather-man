/**
 * 
 */
package cn.seddat.weatherman;

import android.app.Application;
import cn.seddat.weatherman.api.city.City;
import cn.seddat.weatherman.content.SettingService;
import cn.seddat.weatherman.content.Weather;

/**
 * @since 2012-5-18
 * @author gmz
 * 
 */
public class WeathermanApplication extends Application {

	public static final String DOMOB_PUBLISHER_ID = "56OJz+douM3frcHll+";

	private City city;

	@Override
	public void onCreate() {
		super.onCreate();
		// 恢复城市设置
		SettingService settingService = new SettingService(this);
		Weather.Setting setting = settingService.getSetting();
		if (setting != null) {
			this.city = setting.getDistrict();
		}
	}

	public City getCity() {
		return city;
	}

	public void setCity(City city) {
		this.city = city;
	}

}
