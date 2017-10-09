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
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.List;

import org.jfree.chart.LegendItem;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.ShapeUtilities;

import gda.configuration.properties.LocalProperties;

/**
 * Extends StandardXYItemRenderer in order to force the dataset to be a SimpleXYSeriesCollection and to use the Paint,
 * Stroke etc from the SimpleXYSeries.
 */
class TurboXYItemRenderer extends SimpleXYItemRenderer {

	/**
	 * Ratio of space between markers to width of marker below which marker are not shown.
	 * If not set then 1 is used. 0 means always show markers
	 */
	private static final String GDA_PLOT_TURBO_X_Y_ITEM_RENDERER_TOOLTIP_THRESHOLD = "gda.plot.TurboXYItemRenderer.tooltipThreshold";

	private static int tooltipThreshold = LocalProperties.getInt(
			TurboXYItemRenderer.GDA_PLOT_TURBO_X_Y_ITEM_RENDERER_TOOLTIP_THRESHOLD, 1);

	/**
	 * Constructor.
	 */
	public TurboXYItemRenderer() {
	}

	private boolean drawLines=false;
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
	 * @param crosshairState
	 *            crosshair information for the plot ( <code>null</code> permitted).
	 * @param pass
	 *            the pass index.
	 */
	@Override
	public void drawItem(Graphics2D g2, XYItemRendererState state, Rectangle2D dataArea, PlotRenderingInfo info,
			XYPlot plot, ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset, int series, int _item,
			CrosshairState crosshairState, int pass) {

		if (_item > 0)
			return;
		SimpleXYSeries sxys = (SimpleXYSeries) ((SimpleXYSeriesCollection) dataset).getSeries(series);
		if (!sxys.isVisible()) {
			return;
		}

		PlotOrientation orientation = plot.getOrientation();
		g2.setPaint(sxys.getPaint());
		g2.setStroke(sxys.getStroke());

		RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
		RectangleEdge yAxisLocation = plot.getRangeAxisEdge();

		try {
			int x0 = -1; // the x position in pixels of the previous point
			int y0 = -1; // the y position in pixels of the previous point
			int x1 = -1; // the x position in pixels of the current point
			int y1 = -1; // the y position in pixels of the current point
			int xmin = (int) dataArea.getMinX();
			int xmax = (int) dataArea.getMaxX();
			int ymin = (int) dataArea.getMinY();
			int ymax = (int) dataArea.getMaxY();
			GeneralPath path = null;

			/*
			 * To remove the time spent repeatedly calling domainAxis.valueToJava2D for linear axes use simple linear
			 * maths
			 */
			double xl = 0., mx = Double.NaN, cx = 0.;
			if (domainAxis instanceof SimpleNumberAxis) {
				xl = domainAxis.getRange().getLowerBound();
				mx = dataArea.getWidth() / (domainAxis.getRange().getUpperBound() - xl);
				cx = xmin;
			}
			double yl = 0., my = Double.NaN, cy = 0.;
			if (rangeAxis instanceof SimpleNumberAxis) {
				yl = rangeAxis.getRange().getLowerBound();
				my = -dataArea.getHeight() / (rangeAxis.getRange().getUpperBound() - yl);
				cy = ymax;
			}
			List<XYDataItem> list = sxys.getData();

			boolean MX_MY_NaN = Double.isNaN(mx) || Double.isNaN(my);
			Paint paint = sxys.getPaint();
			Stroke stroke = sxys.getStroke();
			Paint paint_symbol = sxys.getSymbolPaint();
			Stroke stroke_symbol = new BasicStroke();
			drawLines = sxys.isDrawLines();
			boolean filled = sxys.getFilled();
			Shape shape = sxys.getSymbol();
			boolean drawMarkers = sxys.isDrawMarkers() & shape != null;
			int tooltipThresholdCounts = -1; /* number of points to be shown below which markers are also to be drawn */
			if (drawLines && drawMarkers && shape != null && tooltipThreshold != 0) {
				Rectangle shapeBoundingBox = shape.getBounds();
				tooltipThresholdCounts = (int) dataArea.getWidth()
						/ (Math.max(1, shapeBoundingBox.width) * tooltipThreshold);
			}

			java.util.Vector<ddouble> markerPositions = null;
			Shape entityArea = null;
			EntityCollection entities = null;
			if (info != null) {
				entities = info.getOwner().getEntityCollection();
			}
			boolean prevLineAdded = false;
			// In case the iterator does not work then use the TODO comment iterator use and why not always
			// comment variables
			synchronized (list) {
				Iterator<XYDataItem> iter = list.iterator();
				/*
				 * loop over all points calculating X1 and Y1. Store previous points positions into X0 and Y0 The
				 * variable addThis determines if the current point is to be added to the path If previous line was
				 * added that the current must be even if off the screen - but in this case the flag prevLineAdded is
				 * set false so that the next does not have to be added.
				 */
				for (int item = 0; iter.hasNext(); item++, x0 = x1, y0 = y1, x1 = -1, y1 = -1) {
					XYDataItem dataitem = iter.next();
					double x = dataitem.getX().doubleValue();
					double y = dataitem.getY().doubleValue();
					x = xValueTransformer.transformValue(x);

					x1 = MX_MY_NaN ? (int) domainAxis.valueToJava2D(x, dataArea, xAxisLocation)
							: (int) ((x - xl) * mx + cx);
					y1 = MX_MY_NaN ? (int) rangeAxis.valueToJava2D(y, dataArea, yAxisLocation)
							: (int) ((y - yl) * my + cy);

					boolean addThis = true;
					if (item == 0) {
						x0 = x1;
						y0 = y1;
						if ((x1 < xmin) || (x1 > xmax) || (y1 < ymin) || (y1 > ymax)) {
							addThis = false;
						}
					} else {
						if (x1 == x0 && y1 == y0) {
							addThis = false;
						}
						if ((x1 < xmin && x0 < xmin) || (x1 > xmax && x0 > xmax) || (y1 < ymin && y0 < ymin)
								|| (y1 > ymax && y0 > ymax)) {
							if (prevLineAdded) {
								path = addPointToLine(path, orientation, x1, y1);
							}
							addThis = false;
						}
					}
					if (addThis) {
						/*
						 * If the current point is to be added then ensure previous one is as well to prevent lines
						 * not crossing the edge of the screen
						 */
						if (!prevLineAdded) {
							path = addPointToLine(path, orientation, x0, y0);
						}
						path = addPointToLine(path, orientation, x1, y1);
						prevLineAdded = true;
					}
					prevLineAdded = addThis;
					if (addThis && drawMarkers) {
						if (markerPositions == null) {
							markerPositions = new java.util.Vector<ddouble>();
						}
						markerPositions.add(new ddouble(item, x1, y1));
						if (tooltipThresholdCounts != -1 && markerPositions.size() > tooltipThresholdCounts) {
							drawMarkers = false;
							markerPositions = null;
						}
					}
				}
				if (path != null) {
					g2.setStroke(stroke);
					g2.setPaint(paint);
					g2.draw(path);
				}
				if (markerPositions != null) {
					if (drawMarkers) {
						g2.setPaint(paint_symbol);
						g2.setStroke(stroke_symbol);
						for (ddouble dd : markerPositions) {
							Shape shape_item = ShapeUtilities.createTranslatedShape(shape, dd.x, dd.y);
							if (filled) {
								g2.fill(shape_item);
							} else {
								g2.draw(shape_item);
							}
							entityArea = shape_item;
							// add an entity for the item...
							if (entities != null) {
								addEntity(entities, entityArea, dataset, series, dd.item, dd.x, dd.y);
							}
						}
						g2.setPaint(paint);
						g2.setStroke(stroke);
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private GeneralPath addPointToLine(GeneralPath path, PlotOrientation orientation, int x, int y) {
		if (drawLines){
			if (orientation == PlotOrientation.HORIZONTAL) {
				int dummy = x;
				x = y;
				y = dummy;
			}
			if (path == null) {
				path = new GeneralPath();
				path.moveTo(x, y);
			} else {
				path.lineTo(x, y);
			}
		}
		return path;
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
}

final class ddouble {
	double x, y;
	int item;

	ddouble(int item, double x, double y) {
		this.item = item;
		this.x = x;
		this.y = y;
	}
}