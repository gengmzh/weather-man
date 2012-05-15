package org.weather.weatherman;

import org.weather.api.cn.city.CityTree;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class WeathermanActivity extends Activity {

	private CityTree cityTree;
	public static final String DEFAULT_CITY = "101010100";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		// selected city
		TextView cityView = (TextView) findViewById(R.id.city);
		cityView.getPaint().setFakeBoldText(true);
		// change city
		TextView cityList = (TextView) findViewById(R.id.cityList);
		cityList.setMovementMethod(LinkMovementMethod.getInstance());
		String links = "<a href='101010100'>北京</a> <a href='101020100'>上海</a> <a href='101280101'>广州</a> <a href='101280601'>深圳</a> ";
		cityList.setText(Html.fromHtml(links));
		Spannable spannable = (Spannable) cityList.getText();
		URLSpan[] urlSpans = spannable.getSpans(0, links.length(), URLSpan.class);
		SpannableStringBuilder builder = new SpannableStringBuilder(spannable);
		builder.clearSpans();
		for (URLSpan span : urlSpans) {
			Log.i(WeathermanActivity.class.getSimpleName(), "URLSpan: " + span.getURL());
			CitySpan mySpan = new CitySpan(span.getURL());
			builder.setSpan(mySpan, spannable.getSpanStart(span), spannable.getSpanEnd(span),
					Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		}
		cityList.setText(builder);
		// refresh
		refresh(DEFAULT_CITY);

	}

	private void refresh(String citycode) {
		// realtime
		Uri uri = Uri.withAppendedPath(Weather.RealtimeWeather.CONTENT_URI, citycode);
		Cursor cursor = getContentResolver().query(uri, null, null, null, null);
		if (cursor.moveToFirst()) {
			//city
			String text = cursor.getString(cursor.getColumnIndex(Weather.RealtimeWeather.NAME));
			TextView view = (TextView) findViewById(R.id.city);
			view.setText(text);
//			Log.i(WeathermanActivity.class.getName(), "city: " + text);
//			text = cursor.getString(cursor.getColumnIndex(Weather.RealtimeWeather.TIME));
//			view = (TextView) findViewById(R.id.time);
//			view.setText("今日" + text + "点更新");
			//temperature
			text = cursor.getString(cursor.getColumnIndex(Weather.RealtimeWeather.TEMPERATURE));
			view = (TextView) findViewById(R.id.temperatue);
			view.setText(text);
			// wind
			text = cursor.getString(cursor.getColumnIndex(Weather.RealtimeWeather.WINDDIRECTION))
					+ cursor.getString(cursor.getColumnIndex(Weather.RealtimeWeather.WINDFORCE));
			view = (TextView) findViewById(R.id.wind);
			view.setText(text);
			//humidity
			text = cursor.getString(cursor.getColumnIndex(Weather.RealtimeWeather.HUMIDITY));
			view = (TextView) findViewById(R.id.humidity);
			view.setText(text);
		} else {
			Log.e(WeathermanActivity.class.getName(), "can't get realtime weather");
		}
		//forecast
		
	}

	class CitySpan extends URLSpan {

		public CitySpan(String url) {
			super(url);

		}

		@Override
		public void onClick(View widget) {
			String citycode = getURL();
			refresh(citycode);
		}

	}

}