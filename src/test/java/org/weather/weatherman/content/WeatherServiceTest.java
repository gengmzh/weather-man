/**
 * 
 */
package org.weather.weatherman.content;

import junit.framework.Assert;
import android.test.AndroidTestCase;

/**
 * @since 2012-5-28
 * @author gmz
 * 
 */
public class WeatherServiceTest extends AndroidTestCase {

	private WeatherService weatherService;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		weatherService = new WeatherService(getContext());
	}

	public void test_findRealtimeWeather() throws Exception {
		Weather.RealtimeWeather realtime = weatherService.findRealtimeWeather("101010700");

		Assert.assertNotNull(realtime);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
