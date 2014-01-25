package org.weather.weatherman.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Toast;

public class ToastService {

	private static Toast toast = null;

	@SuppressLint("ShowToast")
	private static Toast getToast(Context context) {
		if (toast == null) {
			synchronized (ToastService.class) {
				if (toast == null) {
					toast = Toast.makeText(context.getApplicationContext(), "", Toast.LENGTH_SHORT);
				}
			}
		}
		return toast;
	}

	public static void toastLong(Context context, String text) {
		toast(context, text, Toast.LENGTH_LONG);
	}

	public static void toast(Context context, String text, int duration) {
		Toast toast = getToast(context);
		toast.setText(text);
		toast.setDuration(duration);
		toast.show();
	}

	public static void cancel() {
		if (toast != null) {
			toast.cancel();
		}
	}

}
