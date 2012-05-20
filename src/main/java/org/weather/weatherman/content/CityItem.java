package org.weather.weatherman.content;

import org.weather.api.cn.city.City;

public class CityItem extends City {

	private static final long serialVersionUID = -1094480140502815346L;

	public CityItem(String id, String name) {
		super(id, name);
	}

	@Override
	public String toString() {
		return getName();
	}

}
