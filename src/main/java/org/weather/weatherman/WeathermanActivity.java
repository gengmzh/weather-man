package org.weather.weatherman;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;

public class WeathermanActivity extends TabActivity {

	private TabHost tabHost;
	private WeatherApplication app;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		// app
		app = (WeatherApplication) getApplication();
		app.setCitycode(getDefaultCitycode());
		// city list
		TextView city = (TextView) findViewById(R.id.city);
		city.getPaint().setFakeBoldText(true);
		city.setText("北京");
		TextView cityList = (TextView) findViewById(R.id.cityList);
		cityList.setMovementMethod(LinkMovementMethod.getInstance());
		String links = "<a href='101010100'>北京</a> <a href='101020100'>上海</a> <a href='101280101'>广州</a> <a href='101280601'>深圳</a> ";
		cityList.setText(Html.fromHtml(links));
		Spannable spannable = (Spannable) cityList.getText();
		URLSpan[] urlSpans = spannable.getSpans(0, links.length(), URLSpan.class);
		SpannableStringBuilder builder = new SpannableStringBuilder(spannable);
		builder.clearSpans();
		for (URLSpan span : urlSpans) {
			Log.i(RealtimeWeatherActivity.class.getSimpleName(), "URLSpan: " + span.getURL());
			CitySpan mySpan = new CitySpan(span.getURL());
			builder.setSpan(mySpan, spannable.getSpanStart(span), spannable.getSpanEnd(span),
					Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		}
		cityList.setText(builder);
		// tab widget
		tabHost = getTabHost();
		Resources res = getResources();
		TabHost.TabSpec tabSpec = tabHost.newTabSpec("realtime").setIndicator(res.getString(R.string.realtime))
				.setContent(new Intent().setClass(this, RealtimeWeatherActivity.class));
		tabHost.addTab(tabSpec);
		tabSpec = tabHost.newTabSpec("forecast").setIndicator(res.getString(R.string.forecast))
				.setContent(new Intent().setClass(this, ForecastWeatherActivity.class));
		tabHost.addTab(tabSpec);
		tabSpec = tabHost.newTabSpec("setting").setIndicator(res.getString(R.string.setting))
				.setContent(new Intent().setClass(this, RealtimeWeatherActivity.class));
		tabHost.addTab(tabSpec);
		tabHost.setCurrentTab(0);
	}

	String getDefaultCitycode() {
		return "101010100";
	}

	class CitySpan extends URLSpan {

		public CitySpan(String url) {
			super(url);
		}

		@Override
		public void onClick(View widget) {
			String citycode = getURL();
			app.setCitycode(citycode);
			// refresh
			Context context = tabHost.getCurrentView().getContext();
			if (context instanceof RealtimeWeatherActivity) {
				RealtimeWeatherActivity realtime = (RealtimeWeatherActivity) context;
				realtime.refresh();
			} else if (context instanceof ForecastWeatherActivity) {
				ForecastWeatherActivity forecast = (ForecastWeatherActivity) context;
				forecast.refresh();
			} else {
				Log.i(WeathermanActivity.class.getSimpleName(), "context " + context + " is illegal");
			}
		}

	}

}