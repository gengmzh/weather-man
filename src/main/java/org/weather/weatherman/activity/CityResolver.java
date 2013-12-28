/**
 * 
 */
package org.weather.weatherman.activity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.weather.weatherman.content.Weather;
import org.weather.weatherman.content.WeatherContentProvider;

import cn.seddat.weatherman.api.city.City;

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

	/**
	 * 查询城市形象，parent为null表示查询一级城市
	 * 
	 * @author gengmaozhang01
	 * @since 2013-12-28 下午9:35:04
	 * @param parent
	 * @return
	 */
	public List<City> findCity(String parent) {
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

	/**
	 * 获取设置的城市
	 * 
	 * @author gengmaozhang01
	 * @since 2013-12-28 下午9:41:18
	 * @return
	 */
	public City getLocationSetting() {
		Cursor cursor = contentResolver.query(Weather.Setting.CONTENT_URI, null, null, null, null);
		if (cursor != null && cursor.moveToFirst()) {
			String id = cursor.getString(cursor.getColumnIndex(Weather.Setting.CITY3_CODE));
			String name = cursor.getString(cursor.getColumnIndex(Weather.Setting.CITY3_NAME));
			return new City(id, name);
		}
		return null;
	}

	/**
	 * 保存设置的城市
	 * 
	 * @author gengmaozhang01
	 * @since 2013-12-28 下午9:37:41
	 * @param province
	 * @param city
	 * @param district
	 */
	public void saveLocationSetting(City province, City city, City district) {
		if (province == null) {
			throw new IllegalArgumentException("province is required");
		}
		if (city == null) {
			throw new IllegalArgumentException("city is required");
		}
		if (district == null) {
			throw new IllegalArgumentException("district is required");
		}
		ContentValues values = new ContentValues();
		values.put(Weather.Setting.CITY1_CODE, province.getId());
		values.put(Weather.Setting.CITY1_NAME, province.getName());
		values.put(Weather.Setting.CITY2_CODE, city.getId());
		values.put(Weather.Setting.CITY2_NAME, city.getName());
		values.put(Weather.Setting.CITY3_CODE, district.getId());
		values.put(Weather.Setting.CITY3_NAME, district.getName());
		contentResolver.update(Weather.Setting.CONTENT_URI, values, null, null);
	}

}
