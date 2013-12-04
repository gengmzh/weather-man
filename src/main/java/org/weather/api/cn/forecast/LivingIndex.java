package org.weather.api.cn.forecast;

import java.io.Serializable;

public class LivingIndex implements Serializable {

	private static final long serialVersionUID = 6409713638294741767L;

	private String index, description;

	/**
	 * @param index
	 * @param description
	 */
	public LivingIndex(String index, String description) {
		super();
		this.index = index;
		this.description = description;
	}

	public String getIndex() {
		return index;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		return "LivingIndex{index:" + index + ", description:" + description
				+ "}";
	}

}
