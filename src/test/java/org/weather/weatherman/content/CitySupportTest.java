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
public class CitySupportTest extends AndroidTestCase {

	private DatabaseSupport databaseSupport;
	private CitySupport citySupport;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		databaseSupport = new DatabaseSupport(getContext());
		citySupport = new CitySupport(databaseSupport);
	}

	public void test_isOvertime() throws Exception {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR_OF_DAY, -4);
		boolean isOver = citySupport.isOvertime(cal.getTime());
		Log.i(CitySupportTest.class.getSimpleName(), "isOvertime: " + isOver);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		databaseSupport.close();
		databaseSupport = null;
		citySupport = null;
	}

}
