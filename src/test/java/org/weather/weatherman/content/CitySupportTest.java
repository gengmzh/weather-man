/**
 * 
 */
package org.weather.weatherman.content;

import java.util.List;

import junit.framework.Assert;
import android.test.AndroidTestCase;
import cn.seddat.weatherman.api.city.City;

/**
 * @since 2012-5-28
 * @author gmz
 * 
 */
public class CitySupportTest extends AndroidTestCase {

	private SettingService settingService;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		settingService = new SettingService(getContext());
	}

	public void test_isOvertime() throws Exception {
		List<City> cities = settingService.findCity(null);

		Assert.assertNotNull(cities);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
