/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package gda.plots;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.LegendItem;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.ShapeUtilities;

/**
 * Extends StandardXYItemRenderer in order to force the dataset to be a SimpleXYSeriesCollection and to use the Paint,
 * Stroke etc from the SimpleXYSeries.
 */
class SimpleXYItemRenderer extends StandardXYItemRenderer {
	protected SimpleValueTransformer xValueTransformer;

	/**
	 * Constructor.
	 */
	public SimpleXYItemRenderer() {
		xValueTransformer = new SimpleValueTransformer() {
			@Override
			public double transformValue(double toBeTransformed) {
				return toBeTransformed;
			}
			@Override
			public double transformValueBack(double toBeTransformedBack) {
				return toBeTransformedBack;
			}

		};

	}

	/**
	 * Draws the visual representation of a single data item. This mostly reproduces the code of StandardXYItemRenderer
	 * but using the line by line information stored in the SimpleXYSeries instead of the series indexed information
	 * stored in the Renderer itself.
	 *
	 * @param g2
	 *            the graphics device.
	 * @param state
	 *            the renderer state.
	 * @param dataArea
	 *            the area within which the data is being drawn.
	 * @param info
	 *            collects information about the drawing.
	 * @param plot
	 *            the plot (can be used to obtain standard color information etc).
	 * @param domainAxis
	 *            the domain axis.
	 * @param rangeAxis
	 *            the range axis.
	 * @param dataset
	 *            the dataset.
	 * @param series
	 *            the series index (zero-based).
	 * @param item
	 *            the item index (zero-based).
	 * @param crosshairState
	 *            crosshair information for the plot ( <code>null</code> permitted).
	 * @param pass
	 *            the pass index.
	 */
	@Override
	public void drawItem(Graphics2D g2, XYItemRendererState state, Rectangle2D dataArea, PlotRenderingInfo info,
			XYPlot plot, ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset, int series, int item,
			CrosshairState crosshairState, int pass) {
		SimpleXYSeries sxys = (SimpleXYSeries) ((SimpleXYSeriesCollection) dataset).getSeries(series);

		if (!sxys.isVisible()) {
			return;
		}
		// setup for collecting optional entity info...
		Shape entityArea = null;
		EntityCollection entities = null;
		if (info != null) {
			entities = info.getOwner().getEntityCollection();
		}

		PlotOrientation orientation = plot.getOrientation();
		g2.setPaint(sxys.getPaint());
		g2.setStroke(sxys.getStroke());

		// get the data point
		double x1 = dataset.getXValue(series, item);
		double y1 = dataset.getYValue(series, item);

		// Test
		x1 = xValueTransformer.transformValue(x1);

		if (Double.isNaN(x1) || Double.isNaN(y1)) {
			return;
		}

		RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
		RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
		double transX1 = domainAxis.valueToJava2D(x1, dataArea, xAxisLocation);
		double transY1 = rangeAxis.valueToJava2D(y1, dataArea, yAxisLocation);

		if (sxys.isDrawLines()) {
			if (item > 0) {
				// get the previous data point...
				double x0 = dataset.getXValue(series, item - 1);
				double y0 = dataset.getYValue(series, item - 1);

				// Test
				// System.out.print("tranformed " + x0);
				x0 = xValueTransformer.transformValue(x0);
				// Message.debug(" to " + x0);
				if (!Double.isNaN(x0) && !Double.isNaN(y0)) {
					boolean drawLine = true;
					if (getPlotDiscontinuous()) {
						// only draw a line if the gap between the current and
						// previous data
						// point is within the threshold
						int numX = dataset.getItemCount(series);
						double minX = dataset.getXValue(series, 0);
						double maxX = dataset.getXValue(series, numX - 1);
						drawLine = (x1 - x0) <= ((maxX - minX) / numX * getGapThreshold());
					}
					if (drawLine) {
						double transX0 = domainAxis.valueToJava2D(x0, dataArea, xAxisLocation);
						double transY0 = rangeAxis.valueToJava2D(y0, dataArea, yAxisLocation);

						// only draw if we have good values
						if (Double.isNaN(transX0) || Double.isNaN(transY0) || Double.isNaN(transX1)
								|| Double.isNaN(transY1)) {
							return;
						}

						if (orientation == PlotOrientation.HORIZONTAL) {
							state.workingLine.setLine(transY0, transX0, transY1, transX1);
						} else if (orientation == PlotOrientation.VERTICAL) {
							state.workingLine.setLine(transX0, transY0, transX1, transY1);
						}

						if (state.workingLine.intersects(dataArea)) {
							g2.draw(state.workingLine);
						}
					}
				}
			}
		}

		if (sxys.isDrawMarkers()) {

			Shape shape = sxys.getSymbol();
			if (orientation == PlotOrientation.HORIZONTAL) {
				shape = ShapeUtilities.createTranslatedShape(shape, transY1, transX1);
			} else if (orientation == PlotOrientation.VERTICAL) {
				shape = ShapeUtilities.createTranslatedShape(shape, transX1, transY1);
			}
			if (shape.intersects(dataArea)) {
				g2.setPaint(sxys.getSymbolPaint());
				// Always use full stroke for drawing marker
				g2.setStroke(new BasicStroke());
				if (sxys.getFilled()) {
					g2.fill(shape);
				} else {
					g2.draw(shape);
				}
				g2.setPaint(sxys.getPaint());
				g2.setStroke(sxys.getStroke());
			}
			entityArea = shape;

		}

		if (getPlotImages()) {
			// use shape scale with transform??
			// double scale = getShapeScale(plot, series, item, transX1,
			// transY1);
			Image image = getImage(plot, series, item, transX1, transY1);
			if (image != null) {
				Point hotspot = getImageHotspot(plot, series, item, transX1, transY1, image);
				g2.drawImage(image, (int) (transX1 - hotspot.getX()), (int) (transY1 - hotspot.getY()), null);
				entityArea = new Rectangle2D.Double(transX1 - hotspot.getX(), transY1 - hotspot.getY(), image
						.getWidth(null), image.getHeight(null));
			}

		}

		// draw the item label if there is one...
		if (isItemLabelVisible(series, item)) {
			drawItemLabel(g2, orientation, dataset, series, item, transX1, transY1, (y1 < 0.0));
		}

		updateCrosshairValues(crosshairState, x1, y1, transX1, transY1, orientation);

		// add an entity for the item...
		if (entities != null) {
			addEntity(entities, entityArea, dataset, series, item, transX1, transY1);
		}

	}

	/**
	 * Creates a LegendItem for a SimpleXYSeries in the dataset - note that here we create a SimpleLegendItem which
	 * knows about the data it belongs to.
	 *
	 * @param datasetIndex
	 *            which dataset (left or right effectively)
	 * @param series
	 *            which data series
	 * @return the created legend
	 */
	@Override
	public LegendItem getLegendItem(int datasetIndex, int series) {
		LegendItem result = null;

		XYPlot plot = getPlot();
		if (plot != null) {
			SimpleXYSeriesCollection dataset = (SimpleXYSeriesCollection) plot.getDataset(datasetIndex);
			if (dataset != null) {
				SimpleXYSeries sxys = (SimpleXYSeries) dataset.getSeries(series);

				if (sxys.isVisible() && sxys.isVisibleInLegend()) {
					result = new SimpleLegendItem(sxys);
				}
			}

		}

		return result;

	}

	/**
	 * Sets the x value transformer
	 *
	 * @see #setXValueTransformer for an explanation of SimpleValueTransformer.
	 * @param valueTransformer
	 *            the new xValueTransformer
	 */
	void setXValueTransformer(SimpleValueTransformer valueTransformer) {
		xValueTransformer = valueTransformer;
	}
}