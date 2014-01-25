/**
 * 
 */
package org.weather.weatherman.activity;

import java.util.ArrayList;
import java.util.List;

import org.weather.weatherman.content.Weather;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import cn.seddat.weatherman.api.city.City;

/**
 * @since 2012-5-31
 * @author gmz
 * 
 */
public class CityService {

	private ContentResolver contentResolver;

	public CityService(ContentResolver contentResolver) {
		this.contentResolver = contentResolver;
	}

	/**
	 * 查询城市形象，parent为null表示查询一级城市
	 * 
	 * @author gengmaozhang01
	 * @since 2013-12-28 下午9:35:04
	 * @param parent
	 * @return
	 */
	public List<City> findCityByParent(String parent) {
		List<City> cities = new ArrayList<City>();
		Cursor cursor = null;
		if (parent == null || parent.length() == 0) {
			cursor = contentResolver.query(Weather.City.CONTENT_URI, null, Weather.City.PARENT + " ISNULL", null, null);
		} else {
			cursor = contentResolver.query(Weather.City.CONTENT_URI, null, Weather.City.PARENT + "=?",
					new String[] { parent }, null);
		}
		if (cursor.moveToFirst()) {
			int codeIndex = cursor.getColumnIndex(Weather.City.CODE);
			int nameIndex = cursor.getColumnIndex(Weather.City.NAME);
			do {
				cities.add(new SpinnerCity(cursor.getString(codeIndex), cursor.getString(nameIndex)));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return cities;
	}

	private class SpinnerCity extends City {

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
	public City getCitySetting() {
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
	public void saveCitySetting(City province, City city, City district) {
		if (province == null || province.getId() == null || province.getId().length() == 0) {
			throw new IllegalArgumentException("province is required");
		}
		if (city == null || city.getId() == null || city.getId().length() == 0) {
			throw new IllegalArgumentException("city is required");
		}
		if (district == null || district.getId() == null || district.getId().length() == 0) {
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
