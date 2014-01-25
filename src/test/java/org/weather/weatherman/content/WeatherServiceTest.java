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
	private WeatherSupport weatherSupport;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		databaseSupport = new DatabaseSupport(getContext());
		weatherSupport = new WeatherSupport(databaseSupport);
	}

	public void test_find() throws Exception {
		Cursor cursor = weatherSupport.findRealtimeWeather("101010700");

		Assert.assertNotNull(cursor);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		databaseSupport.close();
		databaseSupport = null;
		weatherSupport = null;
	}

}
