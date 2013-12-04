/**
 * 
 */
package org.weather.api.cn.city;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author gmz
 * @time 2012-5-12
 */
public class City implements Serializable {

	private static final long serialVersionUID = 5689405965249801645L;

	private String id, name;
	private List<City> children;

	public City(String id, String name) {
		super();
		this.id = id;
		this.name = name;
		this.children = new ArrayList<City>();
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	public boolean addChild(City child) {
		if (!children.contains(child)) {
			children.add(child);
			return true;
		}
		return false;
	}

	/**
	 * @return the children
	 */
	public List<City> getChildren() {
		return Collections.unmodifiableList(children);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return id.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof City)) {
			return false;
		}
		City other = (City) obj;
		return this.id.equals(other.id);
	}

	public boolean hasChild() {
		return children != null && !children.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return toString0(this);
	}

	private String toString0(City city) {
		StringBuffer buf = new StringBuffer();
		buf.append("{id: ").append(city.getId()).append(", name:").append(city.getName()).append(", children: {");
		if (city.hasChild()) {
			buf.append(toString0(city.getChildren().get(0)));
			for (int i = 1; i < city.getChildren().size(); i++) {
				buf.append(", ").append(toString0(city.getChildren().get(i)));
			}
		}
		buf.append("}}");
		return buf.toString();
	}
}
