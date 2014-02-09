/**
 * 
 */
package org.weather.weatherman.achartengine;

import org.achartengine.renderer.XYSeriesRenderer;

import android.util.SparseIntArray;

/**
 * @author gengmaozhang01
 * @since 2014-2-9 下午8:52:09
 */
public class MyXYSeriesRenderer extends XYSeriesRenderer {

	private static final long serialVersionUID = -1300855627960730062L;

	private SparseIntArray colors = new SparseIntArray();

	public void setPointColor(int index, int color) {
		colors.put(index * 2, color);
	}

	public int getPointColort(int index) {
		return colors.get(index);
	}

}
