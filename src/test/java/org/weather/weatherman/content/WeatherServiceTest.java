/**
 * 
 */
package org.weather.weatherman.content;

import junit.framework.Assert;
import android.database.Cursor;
import android.test.AndroidTestCase;

/**
 * @since 2012-5-28
 * @author gmz
 * 
 */
public class WeatherServiceTest extends AndroidTestCase {

	private DatabaseSupport databaseSupport;
	private WeatherService weatherService;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		databaseSupport = new DatabaseSupport(getContext());
		weatherService = new WeatherService(databaseSupport, new SettingService(databaseSupport));
	}

	public void test_find() throws Exception {
		Cursor cursor = weatherService.findRealtimeWeather("101010700");

		Assert.assertNotNull(cursor);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		databaseSupport.close();
		databaseSupport = null;
		weatherService = null;
	}

}
