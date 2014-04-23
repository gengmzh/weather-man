/**
 * 
 */
package cn.seddat.weatherman.achartengine;

import java.util.HashMap;
import java.util.Map;

import org.achartengine.model.XYSeries;

/**
 * @author gmz
 * 
 */
public class MyXYSeries extends XYSeries {

	private static final long serialVersionUID = 2516000745378907666L;
	private static final String seperator = "#";

	private Map<String, String> labels = new HashMap<String, String>();

	public MyXYSeries(String title) {
		super(title);
	}

	public MyXYSeries(String title, int scaleNumber) {
		super(title, scaleNumber);
	}

	public synchronized void add(double x, double y, String label) {
		add(x, y);
		labels.put(normalize(x) + seperator + normalize(y), label);
	}

	public String getLabel(double x, double y) {
		String label = labels.get(normalize(x) + seperator + normalize(y));
		if (label == null) {
			label = normalize(y);
		}
		return label;
	}

	String normalize(double value) {
		if (value == Math.round(value)) {
			return String.valueOf(Math.round(value));
		} else {
			return String.valueOf(value);
		}
	}

}
