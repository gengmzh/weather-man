package org.weather.weatherman;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * @since 2012-5-18
 * @author gmz
 * 
 */
public class ForecastWeatherActivity extends Activity {

	private static final DateFormat DF_1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private static final DateFormat DF_2 = new SimpleDateFormat("MM.dd a");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.forecast);
		refresh();
	}

	void refresh() {
		WeatherApplication app = (WeatherApplication) getApplication();
		String citycode = app.getCitycode();
		if (citycode != null && citycode.length() > 0) {
			Uri uri = Uri.withAppendedPath(Weather.ForecastWeather.CONTENT_URI, citycode);
			Cursor cursor = getContentResolver().query(uri, null, null, null, null);
			if (cursor.moveToFirst()) {
				// update time
				String text = cursor.getString(cursor.getColumnIndex(Weather.ForecastWeather.TIME));
				TextView view = (TextView) findViewById(R.id.updateTime);
				view.setText(text + "更新");
				TableLayout layout = (TableLayout) view.getParent().getParent();
				Calendar cal = Calendar.getInstance();
				try {
					cal.setTime(DF_1.parse(text));
				} catch (Exception e) {
					Log.e(ForecastWeatherActivity.class.getSimpleName(), "parse update time failed", e);
				}
				cal.add(Calendar.HOUR_OF_DAY, -12);
				do {
					TableRow row = new TableRow(this);
					// date
					cal.add(Calendar.HOUR_OF_DAY, 12);
					view = new TextView(this);
					view.setPadding(3, 3, 3, 3);
					view.setTextSize(14);
					view.setText(DF_2.format(cal.getTime()));
					row.addView(view);
					// image
					view = new TextView(this);
					view.setPadding(3, 3, 3, 3);
					view.setTextSize(14);
					view.setText(cursor.getString(cursor.getColumnIndex(Weather.ForecastWeather.IMAGE)));
					row.addView(view);
					// weather
					view = new TextView(this);
					view.setPadding(3, 3, 3, 3);
					view.setTextSize(14);
					view.setText(cursor.getString(cursor.getColumnIndex(Weather.ForecastWeather.WEATHER)));
					row.addView(view);
					// temperature
					view = new TextView(this);
					view.setPadding(3, 3, 3, 3);
					view.setTextSize(14);
					view.setText(cursor.getString(cursor.getColumnIndex(Weather.ForecastWeather.TEMPERATURE)));
					row.addView(view);
					// wind
					view = new TextView(this);
					view.setPadding(3, 3, 3, 3);
					view.setTextSize(14);
					view.setText(cursor.getString(cursor.getColumnIndex(Weather.ForecastWeather.WIND)));
					row.addView(view);
					layout.addView(row);
				} while (cursor.moveToNext());
			} else {
				Log.e(RealtimeWeatherActivity.class.getName(), "can't get realtime weather");
			}
		}
	}

}
