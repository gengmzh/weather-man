package org.weather.weatherman.activity;

import org.weather.api.cn.city.City;
import org.weather.api.cn.city.CityTree;
import org.weather.weatherman.R;
import org.weather.weatherman.content.CityManager;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

/**
 * @author gmz
 * @time 2012-5-19
 */
public class SettingActivity extends Activity {

	private CityTree cityTree;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);
		// city
		CityManager cityManager = CityManager.getInstance();
		cityTree = cityManager.readCityFile();
		Spinner city1 = (Spinner) findViewById(R.id.city1);
		Spinner city2 = (Spinner) findViewById(R.id.city2);
		Spinner city3 = (Spinner) findViewById(R.id.city3);
		ArrayAdapter<City> adapter = new ArrayAdapter<City>(this,
				android.R.layout.simple_spinner_item, cityTree.getProvince());
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		city1.setAdapter(adapter);
		city1.setOnItemSelectedListener(new CitySelectedListener(city2));
		city2.setOnItemSelectedListener(new CitySelectedListener(city3));
		// update time
		Spinner updateSpinner = (Spinner) findViewById(R.id.updateTimeSpinner);
		ArrayAdapter<CharSequence> updateAdapter = ArrayAdapter
				.createFromResource(this, R.array.updateTime,
						android.R.layout.simple_spinner_item);
		updateAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		updateSpinner.setAdapter(updateAdapter);
	}

	class CitySelectedListener implements OnItemSelectedListener {

		private Spinner spinner;

		public CitySelectedListener(Spinner spinner) {
			super();
			this.spinner = spinner;
		}

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			City city = (City) arg0.getItemAtPosition(arg2);
			ArrayAdapter<City> adapter = new ArrayAdapter<City>(
					arg0.getContext(), android.R.layout.simple_spinner_item,
					city.getChildren());
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner.setAdapter(adapter);
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		}

	}

}
