package cn.seddat.weatherman.api.realtime;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class RealtimeWeather implements Serializable {

	private static final long serialVersionUID = -185537407563324567L;

	private Map<String, String> value;

	public RealtimeWeather(Map<String, Object> value) {
		this.value = new HashMap<String, String>();
		if (value != null) {
			Object obj = value.get("weatherinfo");
			if (obj != null && (obj instanceof Map)) {
				value = (Map<String, Object>) obj;
			}
			for (String key : value.keySet()) {
				this.value.put(key, String.valueOf(value.get(key)));
			}
		}
	}

	public String getTime() {
		return value.get("time");
	}

	public String getCityId() {
		return value.get("cityid");
	}

	public String getCityName() {
		return value.get("city");
	}

	public String getTemperature() {
		String val = value.get("temp");
		return (val != null ? (val.endsWith("℃") ? val : val + "℃") : "");
	}

	public String getHumidity() {
		return value.get("SD");
	}

	public String getWindDirection() {
		return value.get("WD");
	}

	public String getWindForce() {
		return value.get("WS");
	}

	public int getWindForceValue() {
		String wf = value.get("WSE");
		return wf != null && wf.length() > 0 ? Integer.valueOf(wf) : 0;
	}

	public boolean hasRadar() {
		return "1".equals(value.get("isRadar"));
	}

	public String getRadar() {
		return value.get("Radar");
	}

	@Override
	public String toString() {
		return value.toString();
	}

}
