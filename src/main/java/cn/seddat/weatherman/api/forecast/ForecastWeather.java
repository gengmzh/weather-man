package cn.seddat.weatherman.api.forecast;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ForecastWeather implements Serializable {

	private static final long serialVersionUID = -3216725315611512348L;

	private Map<String, String> value;

	public ForecastWeather(Map<String, Object> value) {
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
		String date = value.get("date_y");
		String time = value.get("fchh");
		if (date == null && time == null) {
			return "";
		}
		Calendar cal = Calendar.getInstance();
		int si = 0, ei = date.indexOf("年");
		cal.set(Calendar.YEAR, Integer.valueOf(date.substring(si, ei)));
		si = ei + 1;
		ei = date.indexOf("月");
		cal.set(Calendar.MONTH, Integer.valueOf(date.substring(si, ei)) - 1);
		si = ei + 1;
		ei = date.indexOf("日");
		cal.set(Calendar.DAY_OF_MONTH, Integer.valueOf(date.substring(si, ei)));
		cal.set(Calendar.HOUR_OF_DAY, Integer.valueOf(time));
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		return df.format(cal.getTime());
	}

	public String getCityId() {
		return value.get("cityid");
	}

	public String getCityName() {
		return value.get("city");
	}

	public List<String> getWeather() {
		List<String> wtl = new ArrayList<String>();
		for (int i = 1; i < 7; i++) {
			String wt = value.get("weather" + i);
			if (wt != null) {
				String[] wts = wt.split("转");
				wtl.add(wts[0]);
				wtl.add(wts.length < 2 ? wts[0] : wts[1]);
			}
		}
		return wtl;
	}

	public List<String> getTemperature() {
		List<String> tpl = new ArrayList<String>();
		for (int i = 1; i < 7; i++) {
			String tp = value.get("temp" + i);
			if (tp != null) {
				String[] tps = tp.split("~");
				tpl.add(tps[0]);
				tpl.add(tps.length < 2 ? tps[0] : tps[1]);
			}
		}
		return tpl;
	}

	public List<String> getImage() {
		List<String> imgl = new ArrayList<String>();
		for (int i = 1; i < 13; i++) {
			String img = value.get("img" + i);
			if (img != null) {
				imgl.add(img);
			}
		}
		return imgl;
	}

	public List<String> getWind() {
		List<String> wdl = new ArrayList<String>();
		for (int i = 1; i < 7; i++) {
			String wd = value.get("wind" + i);
			if (wd != null) {
				wdl.add(wd);
				wdl.add(wd);
			}
		}
		return wdl;
	}

	public List<String> getWindForce() {
		List<String> wdl = new ArrayList<String>();
		for (int i = 1; i < 7; i++) {
			String wd = value.get("fl" + i);
			if (wd != null) {
				wdl.add(wd);
				wdl.add(wd);
			}
		}
		return wdl;
	}

	/**
	 * @return 穿衣指数
	 */
	public LivingIndex getDressIndex() {
		return new LivingIndex(value.get("index"), value.get("index_d"));
	}

	/**
	 * @return 紫外线指数
	 */
	public LivingIndex getUltravioletIndex() {
		return new LivingIndex(value.get("index_uv"), "");
	}

	/**
	 * @return 洗车指数
	 */
	public LivingIndex getCleanCarIndex() {
		return new LivingIndex(value.get("index_xc"), "");
	}

	/**
	 * @return 出游指数
	 */
	public LivingIndex getTravelIndex() {
		return new LivingIndex(value.get("index_tr"), "");
	}

	/**
	 * @return 舒适度指数
	 */
	public LivingIndex getComfortIndex() {
		return new LivingIndex(value.get("index_co"), "");
	}

	/**
	 * @return 晨练指数
	 */
	public LivingIndex getMorningExerciseIndex() {
		return new LivingIndex(value.get("index_cl"), "");
	}

	/**
	 * @return 晾晒指数
	 */
	public LivingIndex getSunDryIndex() {
		return new LivingIndex(value.get("index_ls"), "");
	}

	/**
	 * @return 过敏指数
	 */
	public LivingIndex getIrritabilityIndex() {
		return new LivingIndex(value.get("index_ag"), "");
	}

	@Override
	public String toString() {
		return value.toString();
	}

}
