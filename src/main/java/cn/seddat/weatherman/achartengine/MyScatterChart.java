/**
 * 
 */
package cn.seddat.weatherman.achartengine;

import org.achartengine.chart.ScatterChart;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;

class MyScatterChart extends ScatterChart {

	private static final long serialVersionUID = 4165124162353817988L;

	private float size = 3;

	public MyScatterChart(XYMultipleSeriesDataset dataset, XYMultipleSeriesRenderer renderer) {
		super(dataset, renderer);
		size = renderer.getPointSize();
	}

	protected void setDatasetRenderer(XYMultipleSeriesDataset dataset, XYMultipleSeriesRenderer renderer) {
		super.setDatasetRenderer(dataset, renderer);
		size = renderer.getPointSize();
	}

	public void drawSeries(Canvas canvas, Paint paint, float[] points, SimpleSeriesRenderer seriesRenderer,
			float yAxisValue, int seriesIndex, int startIndex) {
		XYSeriesRenderer renderer = (XYSeriesRenderer) seriesRenderer;
		paint.setColor(renderer.getColor());
		if (renderer.isFillPoints()) {
			paint.setStyle(Style.FILL);
		} else {
			paint.setStyle(Style.STROKE);
		}
		int length = points.length;
		switch (renderer.getPointStyle()) {
		case X:
			for (int i = 0; i < length; i += 2) {
				this.resolvePointColor(paint, renderer, i);
				drawX(canvas, paint, points[i], points[i + 1]);
			}
			break;
		case CIRCLE:
			for (int i = 0; i < length; i += 2) {
				this.resolvePointColor(paint, renderer, i);
				drawCircle(canvas, paint, points[i], points[i + 1]);
			}
			break;
		case TRIANGLE:
			float[] path = new float[6];
			for (int i = 0; i < length; i += 2) {
				this.resolvePointColor(paint, renderer, i);
				drawTriangle(canvas, paint, path, points[i], points[i + 1]);
			}
			break;
		case SQUARE:
			for (int i = 0; i < length; i += 2) {
				this.resolvePointColor(paint, renderer, i);
				drawSquare(canvas, paint, points[i], points[i + 1]);
			}
			break;
		case DIAMOND:
			path = new float[8];
			for (int i = 0; i < length; i += 2) {
				this.resolvePointColor(paint, renderer, i);
				drawDiamond(canvas, paint, path, points[i], points[i + 1]);
			}
			break;
		case POINT:
			canvas.drawPoints(points, paint);
			break;
		}
		paint.setColor(renderer.getColor());
	}

	private void resolvePointColor(Paint paint, XYSeriesRenderer renderer, int index) {
		if (renderer instanceof MyXYSeriesRenderer) {
			int color = ((MyXYSeriesRenderer) renderer).getPointColort(index);
			if (color != 0) {
				paint.setColor(color);
			}
		}
	}

	protected void drawX(Canvas canvas, Paint paint, float x, float y) {
		canvas.drawLine(x - size, y - size, x + size, y + size, paint);
		canvas.drawLine(x + size, y - size, x - size, y + size, paint);
	}

	protected void drawCircle(Canvas canvas, Paint paint, float x, float y) {
		canvas.drawCircle(x, y, size, paint);
	}

	protected void drawTriangle(Canvas canvas, Paint paint, float[] path, float x, float y) {
		path[0] = x;
		path[1] = y - size - size / 2;
		path[2] = x - size;
		path[3] = y + size;
		path[4] = x + size;
		path[5] = path[3];
		drawPath(canvas, path, paint, true);
	}

	protected void drawSquare(Canvas canvas, Paint paint, float x, float y) {
		canvas.drawRect(x - size, y - size, x + size, y + size, paint);
	}

	protected void drawDiamond(Canvas canvas, Paint paint, float[] path, float x, float y) {
		path[0] = x;
		path[1] = y - size;
		path[2] = x - size;
		path[3] = y;
		path[4] = x;
		path[5] = y + size;
		path[6] = x + size;
		path[7] = y;
		drawPath(canvas, path, paint, true);
	}

}