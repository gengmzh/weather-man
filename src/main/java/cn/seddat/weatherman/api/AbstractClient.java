package cn.seddat.weatherman.api;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONValue;

public abstract class AbstractClient {

	private static final Log log = LogFactory.getLog(AbstractClient.class);

	private int connectTimeout = 60000;
	private int readTimeout = 60000;
	private int retry = 3;

	public AbstractClient(int connectTimeout, int readTimeout, int retry) {
		super();
		this.connectTimeout = connectTimeout;
		this.readTimeout = readTimeout;
		this.retry = retry < 1 ? 1 : retry;
	}

	protected Map<String, Object> readOnline(String url) throws Exception {
		String json = null;
		HttpURLConnection conn = null;
		InputStream ins = null;
		try {
			conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setRequestProperty("Accept", "*/*");
			// conn.setRequestProperty("Accept-Encoding", "gzip,deflate,sdch");
			conn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6,zh-TW;q=0.4");
			conn.setRequestProperty("Connection", "keep-alive");
			// conn.setRequestProperty("Cookie",
			// "vjuids=-a91c6f85.13df2da4e82.0.632bf591; vjlast=1365579026.1368776420.11");
			conn.setRequestProperty("Host", "www.weather.com.cn");
			conn.setRequestProperty("Referer", "http://www.weather.com.cn/weather/101010100.shtml");
			conn.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.63 Safari/537.36");
			if (getConnectTimeout() > 0) {
				conn.setConnectTimeout(getConnectTimeout());
			}
			if (getReadTimeout() > 0) {
				conn.setReadTimeout(getReadTimeout());
			}
			// read
			conn.connect();
			ins = conn.getInputStream();
			ByteArrayOutputStream ous = new ByteArrayOutputStream();
			byte[] b = new byte[1024];
			int len = 0;
			while ((len = ins.read(b)) > -1) {
				ous.write(b, 0, len);
			}
			json = ous.toString();
		} finally {
			if (ins != null) {
				ins.close();
			}
			if (conn != null) {
				conn.disconnect();
			}
		}
		return json != null ? (Map<String, Object>) JSONValue.parse(json) : null;
	}

	protected Map<String, Object> readSafely(String url) {
		Exception exception = null;
		for (int i = 0; i < retry; i++) {
			try {
				Map<String, Object> res = readOnline(url);
				if (res != null) {
					return res;
				}
			} catch (Exception e) {
				exception = e;
			}
		}
		log.error("readOnline failed after retrying " + retry + " times", exception);
		return Collections.emptyMap();
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public int getReadTimeout() {
		return readTimeout;
	}

	public int getRetry() {
		return retry;
	}

}
