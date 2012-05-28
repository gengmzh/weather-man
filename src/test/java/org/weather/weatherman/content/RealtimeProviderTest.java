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
public class RealtimeProviderTest extends AndroidTestCase {

	private DatabaseSupport databaseSupport;
	private RealtimeProvider realtimeProvider;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		databaseSupport = new DatabaseSupport(getContext());
		realtimeProvider = new RealtimeProvider(databaseSupport, new WeatherService(), new SettingProvider(
				databaseSupport));
	}

	public void test_find() throws Exception {
		Cursor cursor = realtimeProvider.find("101010700");

		Assert.assertNotNull(cursor);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		databaseSupport.close();
		databaseSupport = null;
		realtimeProvider = null;
	}

}
