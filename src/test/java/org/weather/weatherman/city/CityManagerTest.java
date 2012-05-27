package org.weather.weatherman.city;

import junit.framework.Assert;

import org.weather.api.cn.city.CityTree;
import org.weather.weatherman.content.CityManager;

import android.test.AndroidTestCase;

/**
 * @author gmz
 * @time 2012-5-19
 */
public class CityManagerTest extends AndroidTestCase {

	public void testReadCityFile() throws Exception {
		long st = System.currentTimeMillis();
		CityManager mgr = CityManager.getInstance();
		CityTree tree = mgr.readCityFile();

		Assert.assertNotNull(tree);
		long et = System.currentTimeMillis();
		System.out.println((et - st) + "millis: " + tree);
	}

}
