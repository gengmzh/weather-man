/**
 * 
 */
package org.weather.weatherman.achartengine;

import org.achartengine.chart.LineChart;
import org.achartengine.chart.ScatterChart;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * @author gmz
 * 
 */
public class MyLineChart extends LineChart {
	private static final long serialVersionUID = -2629116346597334828L;

	private float displayPointDistance = 10f;

	public MyLineChart(XYMultipleSeriesDataset dataset, XYMultipleSeriesRenderer renderer, float displayPointDistance) {
		super(dataset, renderer);
		this.displayPointDistance = displayPointDistance;
	}

	@Override
	public ScatterChart getPointsChart() {
		return new MyScatterChart(getDataset(), getRenderer());
	}

	@Override
	protected void drawChartValuesText(Canvas canvas, XYSeries series, SimpleSeriesRenderer renderer, Paint paint,
			float[] points, int seriesIndex, int startIndex) {
		if (points.length > 1) { // there are more than one point
			// record the first point's position
			float distance = displayPointDistance;
			float previousPointX = points[0];
			float previousPointY = points[1];
			for (int k = 0; k < points.length; k += 2) {
				if (k == 2) { // decide whether to display first two points'
								// values or not
					if (Math.abs(points[2] - points[0]) > distance || Math.abs(points[3] - points[1]) > distance) {
						// first point
						drawText(canvas, getLabel(series, series.getX(startIndex), series.getY(startIndex)), points[0],
								points[1] - renderer.getChartValuesSpacing(), paint, 0);
						// second point
						drawText(canvas, getLabel(series, series.getX(startIndex + 1), series.getY(startIndex + 1)),
								points[2], points[3] - renderer.getChartValuesSpacing(), paint, 0);

						previousPointX = points[2];
						previousPointY = points[3];
					}
				} else if (k > 2) {
					// compare current point's position with the previous
					// point's, if they are not too close, display
					if (Math.abs(points[k] - previousPointX) > distance
							|| Math.abs(points[k + 1] - previousPointY) > distance) {
						drawText(canvas,
								getLabel(series, series.getX(startIndex + k / 2), series.getY(startIndex + k / 2)),
								points[k], points[k + 1] - renderer.getChartValuesSpacing(), paint, 0);
						previousPointX = points[k];
						previousPointY = points[k + 1];
					}
				}
			}
		} else { // if only one point, display it
			for (int k = 0; k < points.length; k += 2) {
				drawText(canvas, getLabel(series.getY(startIndex + k / 2)), points[k],
						points[k + 1] - renderer.getChartValuesSpacing(), paint, 0);
			}
		}
	}

	protected String getLabel(XYSeries series, double x, double y) {
		if (series instanceof MyXYSeries) {
			return ((MyXYSeries) series).getLabel(x, y);
		}
		return getLabel(y);
	}

}