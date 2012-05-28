/**
 * 
 */
package org.weather.weatherman.content;

import java.util.Calendar;

import android.test.AndroidTestCase;
import android.util.Log;

/**
 * @since 2012-5-28
 * @author gmz
 * 
 */
public class SettingProviderTest extends AndroidTestCase {

	private DatabaseSupport databaseSupport;
	private SettingProvider settingProvider;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		databaseSupport = new DatabaseSupport(getContext());
		settingProvider = new SettingProvider(databaseSupport);
	}

	public void test_isOvertime() throws Exception {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR_OF_DAY, -4);
		boolean isOver = settingProvider.isOvertime(cal.getTime());
		Log.i(SettingProviderTest.class.getSimpleName(), "isOvertime: " + isOver);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		databaseSupport.close();
		databaseSupport = null;
		settingProvider = null;
	}

}
