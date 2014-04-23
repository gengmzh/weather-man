/**
 * 
 */
package cn.seddat.weatherman.activity;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import cn.seddat.weatherman.R;
import cn.seddat.weatherman.api.city.City;
import cn.seddat.weatherman.content.SettingService;

import com.baidu.mobstat.StatService;

/**
 * @author gengmaozhang01
 * @since 2014-1-19 下午4:39:46
 */
public class CityActivity extends Activity {

	private static final String tag = CityActivity.class.getSimpleName();

	private SettingService settingService;
	private Spinner city1Spinner, city2Spinner, city3Spinner;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.city);
		settingService = new SettingService(this);
		// register events
		city1Spinner = (Spinner) findViewById(R.id.city1);
		city2Spinner = (Spinner) findViewById(R.id.city2);
		city3Spinner = (Spinner) findViewById(R.id.city3);
		city1Spinner.setOnItemSelectedListener(new CitySelectedListener(city2Spinner));
		city2Spinner.setOnItemSelectedListener(new CitySelectedListener(city3Spinner));
		city3Spinner.setOnItemSelectedListener(new CitySelectedListener(null));
		// init city
		List<City> city1List = this.findCityByParent(null);
		Log.i(tag, "find " + city1List.size() + " cities");
		ArrayAdapter<City> city1Adapter = new ArrayAdapter<City>(city1Spinner.getContext(),
				android.R.layout.simple_spinner_item, city1List);
		city1Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		city1Spinner.setAdapter(city1Adapter);
		// button events
		Button btnSave = (Button) findViewById(R.id.citySave), btnCancel = (Button) findViewById(R.id.cityCancel);
		btnSave.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				City city1 = (City) city1Spinner.getSelectedItem(), city2 = (City) city2Spinner.getSelectedItem();
				City city3 = (City) city3Spinner.getSelectedItem();
				Intent data = new Intent();
				data.putExtra("city1Id", city1.getId()).putExtra("city1Name", city1.getName());
				data.putExtra("city2Id", city2.getId()).putExtra("city2Name", city2.getName());
				data.putExtra("city3Id", city3.getId()).putExtra("city3Name", city3.getName());
				final CityActivity act = CityActivity.this;
				act.setResult(1, data);
				act.finish();
			}
		});
		btnCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final CityActivity act = CityActivity.this;
				act.setResult(0);
				act.finish();
			}
		});
	}

	class CitySelectedListener implements OnItemSelectedListener {

		private Spinner nextSpinner;

		public CitySelectedListener(Spinner spinner) {
			super();
			this.nextSpinner = spinner;
		}

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			City city = (City) arg0.getItemAtPosition(arg2);
			if (nextSpinner != null) {
				final List<City> nextCities = findCityByParent(city.getId());
				Log.i(tag, "find " + nextCities.size() + " cities");
				ArrayAdapter<City> adapter = new ArrayAdapter<City>(arg0.getContext(),
						android.R.layout.simple_spinner_item, nextCities);
				adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				nextSpinner.setAdapter(adapter);
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		}

	}

	private List<City> findCityByParent(String parent) {
		List<City> cl = settingService.findCity(parent);
		for (int i = 0; i < cl.size(); i++) {
			City city = cl.get(i);
			cl.set(i, new SpinnerCity(city.getId(), city.getName()));
		}
		return cl;
	}

	private class SpinnerCity extends City {

		private static final long serialVersionUID = -1094480140502815346L;

		public SpinnerCity(String id, String name) {
			super(id, name);
		}

		@Override
		public String toString() {
			return getName();
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		StatService.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		StatService.onPause(this);
	}

}
