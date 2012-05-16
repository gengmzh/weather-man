package org.weather.weatherman;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class WeathermanActivity extends TabActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		TabHost tabHost = getTabHost();
		Resources res = getResources();

		TabHost.TabSpec tabSpec = tabHost.newTabSpec("realtime").setIndicator(res.getString(R.string.realtime))
				.setContent(new Intent().setClass(this, RealtimeActivity.class));
		tabHost.addTab(tabSpec);

		tabSpec = tabHost.newTabSpec("forecast").setIndicator(res.getString(R.string.forecast))
				.setContent(new Intent().setClass(this, RealtimeActivity.class));
		tabHost.addTab(tabSpec);

		tabSpec = tabHost.newTabSpec("setting").setIndicator(res.getString(R.string.setting))
				.setContent(new Intent().setClass(this, RealtimeActivity.class));
		tabHost.addTab(tabSpec);

		tabHost.setCurrentTab(0);
	}

}