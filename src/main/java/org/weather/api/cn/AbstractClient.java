package org.weather.api.cn;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONValue;

public abstract class AbstractClient {

	private static final Log log = LogFactory.getLog(AbstractClient.class);

	private int connTimeout = 5000;
	private int readTimeout = 10000;
	private int retry = 3;

	public AbstractClient() {
	}

	protected Map<String, Object> readOnline(String url) throws Exception {
		URL _url = new URL(url);
		URLConnection conn = _url.openConnection();
		conn.setConnectTimeout(connTimeout);
		conn.setReadTimeout(readTimeout);
		conn.connect();
		InputStream ins = conn.getInputStream();
		byte[] bytes = new byte[ins.available()];
		ins.read(bytes);
		ins.close();
		String json = new String(bytes);
		return (Map<String, Object>) JSONValue.parse(json);
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

	public int getConnTimeout() {
		return connTimeout;
	}

	public void setConnTimeout(int connTimeout) {
		this.connTimeout = connTimeout;
	}

	public int getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}

	public int getRetry() {
		return retry;
	}

	public void setRetry(int retry) {
		this.retry = retry;
	}

}
