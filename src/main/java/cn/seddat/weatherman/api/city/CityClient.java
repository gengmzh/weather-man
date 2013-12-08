package cn.seddat.weatherman.api.city;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import cn.seddat.weatherman.api.AbstractClient;
import cn.seddat.weatherman.api.Config;

/**
 * read all city info from <a
 * href="http://www.weather.com.cn">weather.com.cn</a>
 * 
 * @author gmz
 * @time 2012-5-12
 */
public class CityClient extends AbstractClient {

	public CityClient() {
	}

	public CityTree getCity() throws Exception {
		CityTree cityTree = new CityTree();
		// city1
		Map<String, Object> c1m = readSafely(Config.getInstance().getCity1Url());
		// city 2 & 3
		List<String> c1ids = new ArrayList<String>(c1m.keySet());
		Collections.sort(c1ids);
		for (String c1id : c1ids) {
			City c1 = new City(c1id, (String) c1m.get(c1id));
			if (cityTree.addRootCity(c1)) {
				// city2
				Map<String, Object> c2m = readSafely(Config.getInstance().getCity2Url(c1.getId()));
				List<String> c2ids = new ArrayList<String>(c2m.keySet());
				Collections.sort(c2ids);
				for (String c2id : c2ids) {
					City c2 = new City(c1id + c2id, (String) c2m.get(c2id));
					if (c1.addChild(c2)) {
						// city3
						Map<String, Object> c3m = readSafely(Config.getInstance().getCity3Url(c2.getId()));
						List<String> c3ids = new ArrayList<String>(c3m.keySet());
						Collections.sort(c3ids);
						for (String c3id : c3ids) {
							String c3code = ("00".equals(c2id) ? (c1id + c3id + c2id) : (c1id + c2id + c3id));
							City c3 = new City(c3code, (String) c3m.get(c3id));
							c2.addChild(c3);
						}
					}
				}
			}
		}
		return cityTree;
	}

}
