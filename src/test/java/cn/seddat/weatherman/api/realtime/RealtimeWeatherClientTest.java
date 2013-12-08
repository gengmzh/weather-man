package cn.seddat.weatherman.api.realtime;

import junit.framework.Assert;
import android.test.AndroidTestCase;
import android.util.Log;

public class RealtimeWeatherClientTest extends AndroidTestCase {

	private RealtimeWeatherClient client = new RealtimeWeatherClient(60000, 60000, 3);

	public void test_getWeather() throws Exception {
		RealtimeWeather weather = client.getWeather("101200101");

		Assert.assertNotNull(weather);
		Log.i(RealtimeWeatherClientTest.class.getSimpleName(), weather.toString());
	}

}
