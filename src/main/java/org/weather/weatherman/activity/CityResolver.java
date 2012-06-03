/**
 * 
 */
package org.weather.weatherman.activity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.weather.api.cn.city.City;
import org.weather.weatherman.content.Weather;
import org.weather.weatherman.content.WeatherContentProvider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

/**
 * @since 2012-5-31
 * @author gmz
 * 
 */
public class CityResolver {

	private ContentResolver contentResolver;

	public CityResolver(ContentResolver contentResolver) {
		this.contentResolver = contentResolver;
	}

	public void initCity() throws Exception {
		Log.i(CityResolver.class.getSimpleName(), "init city starts");
		BufferedReader reader = null;
		try {
			InputStream ins = WeatherContentProvider.class.getClassLoader().getResourceAsStream(
					"org/weather/weatherman/activity/city.properties");
			reader = new BufferedReader(new InputStreamReader(ins));
			City c1 = null, c2 = null;
			String line = null;
			List<ContentValues> cvl = new ArrayList<ContentValues>();
			while ((line = reader.readLine()) != null) {
				String[] ls = line.split("\t");
				if (ls.length < 2) {
					continue;
				}
				City tmp = new City(ls[0], ls[1]);
				ContentValues values = new ContentValues();
				values.put(Weather.City.CODE, tmp.getId());
				values.put(Weather.City.NAME, tmp.getName());
				if (tmp.getId().length() == 5) {
					c1 = tmp;
				} else if (tmp.getId().length() == 7) {
					c2 = tmp;
					values.put(Weather.City.PARENT, c1.getId());
				} else if (tmp.getId().length() == 9) {
					values.put(Weather.City.PARENT, c2.getId());
				}
				cvl.add(values);
			}
			contentResolver.bulkInsert(Weather.City.CONTENT_URI, cvl.toArray(new ContentValues[cvl.size()]));
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		Log.i(CityResolver.class.getSimpleName(), "init city done");
	}

	List<City> findCity(String parent) {
		List<City> cl = new ArrayList<City>();
		Cursor cursor = contentResolver.query(Weather.City.CONTENT_URI, null, Weather.City.PARENT
				+ (parent == null || parent.length() == 0 ? " ISNULL" : "=" + parent), null, null);
		if (cursor.moveToFirst()) {
			int codeIndex = cursor.getColumnIndex(Weather.City.CODE);
			int nameIndex = cursor.getColumnIndex(Weather.City.NAME);
			do {
				cl.add(new SpinnerCity(cursor.getString(codeIndex), cursor.getString(nameIndex)));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return cl;
	}

	class SpinnerCity extends City {

		private static final long serialVersionUID = -1094480140502815346L;

		public SpinnerCity(String id, String name) {
			super(id, name);
		}

		@Override
		public String toString() {
			return getName();
		}

	}

}
