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
		CityManager mgr = CityManager.getInstance();
		CityTree tree = mgr.readCityFile();

		Assert.assertNotNull(tree);
		System.out.println(tree);
	}

}
