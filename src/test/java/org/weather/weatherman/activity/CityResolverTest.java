/**
 * 
 */
package org.weather.weatherman.activity;

import java.util.List;

import org.weather.api.cn.city.City;
import org.weather.weatherman.content.Weather;

import android.test.AndroidTestCase;
import android.util.Log;

/**
 * @since 2012-5-31
 * @author gmz
 * 
 */
public class CityResolverTest extends AndroidTestCase {

	CityResolver cityResolver;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		cityResolver = new CityResolver(getContext().getContentResolver());
	}

	public void test_initCity() throws Exception {
		getContext().getContentResolver().delete(Weather.City.CONTENT_URI, null, null);

		long st = System.currentTimeMillis();
		cityResolver.initCity();
		long et = System.currentTimeMillis();
		Log.i(CityResolverTest.class.getSimpleName(), "initCity spend " + (et - st) + " ms");
	}

	public void test_findCity() throws Exception {
		List<City> cl = cityResolver.findCity(null);
		Log.i(CityResolverTest.class.getSimpleName(), cl.toString());
	}

}
