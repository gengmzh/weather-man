/**
 * 
 */
package org.weather.weatherman.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONValue;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import cn.seddat.weatherman.api.city.City;

/**
 * 配置信息服务类
 * 
 * @author gengmaozhang01
 * @since 2014-1-3 下午7:05:57
 */
public class SettingService {

	private static final String tag = SettingService.class.getSimpleName();

	private ContentResolver contentResolver;

	public SettingService(Context context) {
		super();
		this.contentResolver = context.getContentResolver();
	}

	/**
	 * 查询城市信息，parent为null表示查询一级城市
	 * 
	 * @author gengmaozhang01
	 * @since 2013-12-28 下午9:35:04
	 * @param parent
	 * @return
	 */
	public List<City> findCity(String parent) {
		// query
		Cursor cursor = null;
		if (parent == null || parent.length() == 0) {
			cursor = contentResolver.query(DatabaseSupport.City.CONTENT_URI, null, DatabaseSupport.City.PARENT
					+ " ISNULL", null, null);
		} else {
			cursor = contentResolver.query(DatabaseSupport.City.CONTENT_URI, null, DatabaseSupport.City.PARENT + "=?",
					new String[] { parent }, null);
		}
		// parse
		List<City> cities = new ArrayList<City>();
		if (cursor.moveToFirst()) {
			int codeIndex = cursor.getColumnIndex(DatabaseSupport.City.CODE);
			int nameIndex = cursor.getColumnIndex(DatabaseSupport.City.NAME);
			do {
				cities.add(new City(cursor.getString(codeIndex), cursor.getString(nameIndex)));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return cities;
	}

	/**
	 * 获取城市等设置信息
	 * 
	 * @author gengmaozhang01
	 * @since 2013-12-28 下午9:41:18
	 * @return
	 */
	public Weather.Setting getSetting() {
		Uri uri = Uri.withAppendedPath(DatabaseSupport.Content.CONTENT_URI, Weather.Setting.PATH_SETTING);
		Cursor cursor = contentResolver.query(uri, null, null, null, null);
		if (!cursor.moveToFirst()) {
			cursor.close();
			return null;
		}
		// parse
		String value = cursor.getString(cursor.getColumnIndex(DatabaseSupport.Content.COL_VALUE));
		// String uptime = cursor.getString(cursor.getColumnIndex(DatabaseSupport.Content.COL_UPDATETIME));
		@SuppressWarnings("unchecked")
		Map<String, Object> values = (Map<String, Object>) JSONValue.parse(value);
		// values.put("uptime", uptime);
		cursor.close();
		return new Weather.Setting(values);
	}

	/**
	 * 保存城市等设置信息
	 * 
	 * @author gengmaozhang01
	 * @since 2013-12-28 下午9:37:41
	 * @param province
	 * @param city
	 * @param district
	 */
	public void saveSetting(City province, City city, City district) {
		if (province == null || province.getId() == null || province.getId().length() == 0) {
			throw new IllegalArgumentException("province is required");
		}
		if (city == null || city.getId() == null || city.getId().length() == 0) {
			throw new IllegalArgumentException("city is required");
		}
		if (district == null || district.getId() == null || district.getId().length() == 0) {
			throw new IllegalArgumentException("district is required");
		}
		// update
		Uri uri = Uri.withAppendedPath(DatabaseSupport.Content.CONTENT_URI, Weather.Setting.PATH_SETTING);
		ContentValues values = new ContentValues();
		Map<String, String> setting = new HashMap<String, String>();
		setting.put("c1c", province.getId());
		setting.put("c1n", province.getName());
		setting.put("c2c", city.getId());
		setting.put("c2n", city.getName());
		setting.put("c3c", district.getId());
		setting.put("c3n", district.getName());
		values.put(DatabaseSupport.Content.COL_VALUE, JSONValue.toJSONString(setting));
		int rows = contentResolver.update(uri, values, null, null);
		// insert
		if (rows <= 0) {
			contentResolver.insert(uri, values);
		}
		Log.i(tag, "save setting done");
	}

}
