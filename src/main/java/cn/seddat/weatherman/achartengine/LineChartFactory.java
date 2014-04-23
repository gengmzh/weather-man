/**
 * 
 */
package cn.seddat.weatherman.achartengine;

import org.achartengine.GraphicalView;
import org.achartengine.chart.XYChart;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import android.content.Context;

/**
 * @author gmz
 * 
 */
public class LineChartFactory {

	public static final GraphicalView getLineChartView(Context context, XYMultipleSeriesDataset dataset,
			XYMultipleSeriesRenderer renderer) {
		checkParameters(dataset, renderer);
		XYChart chart = new MyLineChart(dataset, renderer, 10f);
		return new GraphicalView(context, chart);
	}

	private static void checkParameters(XYMultipleSeriesDataset dataset, XYMultipleSeriesRenderer renderer) {
		if (dataset == null || renderer == null || dataset.getSeriesCount() != renderer.getSeriesRendererCount()) {
			throw new IllegalArgumentException(
					"Dataset and renderer should be not null and should have the same number of series");
		}
	}

}
