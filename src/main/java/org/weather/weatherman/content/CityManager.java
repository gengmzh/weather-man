package org.weather.weatherman.content;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.weather.api.cn.city.City;
import org.weather.api.cn.city.CityTree;
import org.weather.weatherman.activity.SettingActivity;

import android.util.Log;

/**
 * @author gmz
 * @time 2012-5-18
 */
public class CityManager {

	private CityManager() {
	}

	private static final CityManager instance = new CityManager();

	public static CityManager getInstance() {
		return instance;
	}

	public CityTree readCityFile() {
		InputStream ins = CityManager.class.getClassLoader().getResourceAsStream("city.properties");
		BufferedReader reader = new BufferedReader(new InputStreamReader(ins));
		CityTree tree = new CityTree();
		City c1 = null, c2 = null, c3 = null, tmp = null;
		while ((tmp = readLine(reader)) != null) {
			if (isCity1(tmp.getId())) {
				c1 = tmp;
				tree.addRootCity(c1);
			} else if (isCity2(tmp.getId())) {
				c2 = tmp;
				c1.addChild(c2);
			} else if (isCity3(tmp.getId())) {
				c3 = tmp;
				c2.addChild(c3);
			}
		}
		return tree;
	}

	City readLine(BufferedReader reader) {
		try {
			String line = reader.readLine();
			if (line == null) {
				return null;
			}
			String[] ls = line.split("\t");
			return new CityItem(ls[0], ls[1]);
		} catch (Exception e) {
			Log.e(SettingActivity.class.getSimpleName(), "read city file failed", e);
			return null;
		}
	}

	boolean isCity1(String citycode) {
		return citycode.length() == 5;
	}

	boolean isCity2(String citycode) {
		return citycode.length() == 7;
	}

	boolean isCity3(String citycode) {
		return citycode.length() == 9;
	}

}
