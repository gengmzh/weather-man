/**
 * 
 */
package org.weather.api.cn.city;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @since 2012-5-14
 * @author gmz
 * 
 */
public class CityTree {

	private List<City> rootCities;

	public CityTree() {
		rootCities = new ArrayList<City>();
	}

	public boolean addRootCity(City city) {
		if (!rootCities.contains(city)) {
			rootCities.add(city);
			return true;
		}
		return false;
	}

	public List<City> getProvince() {
		return Collections.unmodifiableList(rootCities);
	}

	public List<City> getWeatherCity() {
		List<City> leafs = new ArrayList<City>();
		for (City city : rootCities) {
			visit(city, leafs);
		}
		return leafs;
	}

	private void visit(City city, List<City> leafs) {
		if (city.hasChild()) {
			for (City child : city.getChildren()) {
				visit(child, leafs);
			}
		} else {
			leafs.add(city);
		}
	}

	public City findCity(String id) {
		for (City city : rootCities) {
			City res = findCity0(city, id);
			if (res != null) {
				return res;
			}
		}
		return null;
	}

	private City findCity0(City city, String id) {
		if (city.getId().equals(id)) {
			return city;
		}
		if (city.hasChild()) {
			for (City child : city.getChildren()) {
				City res = findCity0(child, id);
				if (res != null) {
					return res;
				}
			}
		}
		return null;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("CityTree{");
		if (!rootCities.isEmpty()) {
			buf.append(rootCities.get(0));
			for (int i = 1; i < rootCities.size(); i++) {
				buf.append(",").append(rootCities.get(i));
			}
		}
		buf.append("}");
		return buf.toString();
	}

}
