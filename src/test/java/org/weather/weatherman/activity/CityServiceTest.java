/**
 * 
 */
package org.weather.weatherman.activity;

import java.util.List;

import org.weather.weatherman.content.Weather;

import cn.seddat.weatherman.api.city.City;

import android.content.ContentResolver;
import android.database.Cursor;
import android.test.AndroidTestCase;
import android.util.Log;

/**
 * @since 2012-5-31
 * @author gmz
 * 
 */
public class CityServiceTest extends AndroidTestCase {

	CityService cityService;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		cityService = new CityService(getContext().getContentResolver());
	}

	public void test_findCity() throws Exception {
		List<City> cl = cityService.findCityByParent(null);
		Log.i(CityServiceTest.class.getSimpleName(), "province count: " + cl.size());
		Log.i(CityServiceTest.class.getSimpleName(), cl.toString());
	}

	public void test_findAllCity() throws Exception {
		ContentResolver resolver = getContext().getContentResolver();
		Cursor cursor = resolver.query(Weather.City.CONTENT_URI, null, null, null, null);
		if (cursor.moveToFirst()) {
			int ci = cursor.getColumnIndex(Weather.City.CODE), ni = cursor.getColumnIndex(Weather.City.NAME), pi = cursor
					.getColumnIndex(Weather.City.PARENT);
			do {
				String code = cursor.getString(ci), name = cursor.getString(ni), parent = cursor.getString(pi);
				Log.i(CityServiceTest.class.getSimpleName(), "City{code:" + code + ",name:" + name + ",parent:"
						+ parent + "}");
			} while (cursor.moveToNext());
		}
		cursor.close();
	}

}
