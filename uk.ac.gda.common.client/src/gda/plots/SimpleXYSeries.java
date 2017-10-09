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

import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.List;

import org.jfree.data.Range;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;

/**
 * Extends XYSeries to keep information about line colour, type, style, symbol etc with the data. Also overrides some
 * aspects to provide faster access and synchronization.
 */
public class SimpleXYSeries extends XYSeries {
	/**
	 * Controls if the archive method functions or not.
	 */
	static final String GDA_PLOT_SIMPLEXYSERIES_ARCHIVE_THRESHOLD = "gda.plot.SimpleXYSeries.archiveThreshold";

	private static Integer archiveThreshold;

	private static final Logger logger = LoggerFactory.getLogger(SimpleXYSeries.class);
	private static Paint[] defaultPaints = { new Color(0, 0, 0) ,new Color(0, 0, 255),new Color(255, 0, 0),
		new Color(204, 0, 204), new Color(204, 0, 0), new Color(0, 153, 51), new Color(102, 0, 102),
		new Color(255, 102, 255), new Color(255, 155, 0), new Color(204, 255, 0), new Color(51, 255, 51),
		new Color(102, 255, 255), new Color(102, 102, 255), new Color(153, 153, 0),
		new Color(204, 204, 205), new Color(255, 204, 204)};

	private int lineNumber;
	private int axis;
	private Paint paint;
	private Marker marker;
	private Type type;
	private int symbolSize = 3;
	private Paint symbolPaint;
	private Pattern pattern;
	private int lineWidth = 1;
	private boolean visible = true;
	private boolean visibleInLegend = true;
	private boolean batching = false;
	private boolean includedInData = true;

	private double minX = Double.POSITIVE_INFINITY;
	private double maxX = Double.NEGATIVE_INFINITY;
	private double minY = Double.POSITIVE_INFINITY;
	private double maxY = Double.NEGATIVE_INFINITY;

	/**
	 * Constructor creates a new SimpleXYSeries to represent a line.
	 *
	 * @param name
	 *            the string which will appear in legends
	 * @param lineNumber
	 *            the number of the line
	 * @param axis
	 *            which y axis this line is referred to
	 */
	// Unfortunately until JFreeChart deals with Generics we have to
	// suppress the warning which occurs with the
	// Collections.synchronizedList() call as data
	// is a protected list in the XYSeries class.
	public SimpleXYSeries(String name, int lineNumber, int axis) {
		// autoSort is set to false and allowDuplicateXValues to true
		// though in any case the add method which uses them is overridden.
		super(name, false, true);

		// In all the Simple... code lines are referred to by their lineNumber
		// NOT their position in the XYSeriesCollection. The axis is needed to
		// pass on to LegendItems so that they can be sorted correctly.
		this.lineNumber = lineNumber;
		this.axis = axis;

		// Default colours for the line and symbol are based on lineNumber
		paint = defaultPaints[lineNumber % defaultPaints.length];
		symbolPaint = paint;

		// Default symbol is also based on the lineNumber. Though 10 shapes
		// are available Vertical and Horizontal line are not
		// very visible. In any case using only 8 of them means we can have
		// 72 lines before the defaults start repeating.
		marker = Marker.fromCounter(lineNumber % 8);
		pattern = Pattern.SOLID;
		type = Type.LINEONLY;

		// Change the List of data into a synchronized version. This
		// is needed to cure bug 46 but does lead to some slowing down
		// if there are more than a few thousand points in the data.
		// data = Collections.synchronizedList(data);
	}

	/**
	 * @param name
	 * @param line
	 * @param axis
	 * @param xVals
	 * @param yVals
	 */
	@SuppressWarnings("unchecked")
	public SimpleXYSeries(String name, int line, int axis, double[] xVals, double[] yVals) {
		this(name, line, axis);
		minX = Double.POSITIVE_INFINITY;
		maxX = Double.NEGATIVE_INFINITY;
		minY = Double.POSITIVE_INFINITY;
		maxY = Double.NEGATIVE_INFINITY;
		data = new java.util.Vector<XYDataItem>();
		int l = xVals.length;
		for (int index = 0; index < l; index++) {
			double x = xVals[index];
			double y = yVals[index];
			minX = Math.min(minX, x);
			maxX = Math.max(maxX, x);
			minY = Math.min(minY, y);
			maxY = Math.max(maxY, y);
			data.add(new XYDataItem(x, y));
		}
	}

	/**
	 * Use this method to get the data to ensure it has been unarchived.
	 *
	 * @return list of data
	 */
	@SuppressWarnings("unchecked")
	public List<XYDataItem> getData() {
		unArchive();
		return data;
	}

	/**
	 * @return iterator to data. Ensure you synchronise on getData beforehand.
	 */
	public Iterator<XYDataItem> getIterator() {
		unArchive();
		return getData().iterator();
	}

	@Override
	public XYDataItem addOrUpdate(Number arg0, Number arg1) {
		unArchive();
		return super.addOrUpdate(arg0, arg1);
	}

	/**
	 * Adds an XYDataItem to the data list. This overrides the super class method in order to remove the checking of the
	 * ordering of X values. Note that this method is not directly called by our code but should NOT be removed - all
	 * the add methods of XYSeries eventually call this one.
	 *
	 * @param item
	 *            the XYDataItem to add
	 * @param notify
	 *            whether or not to notify observers of the series.
	 */
	// Unfortunately until JFreeChart deals with Generics we have to
	// suppress the warning which occurs with the data.add(item) call
	// as data is a protected list in the XYSeries class.
	@Override
	@SuppressWarnings("unchecked")
	public void add(XYDataItem item, boolean notify) {

		if (item == null) {
			throw new IllegalArgumentException("Null 'item' argument.");
		}
		minX = Math.min(minX, item.getX().doubleValue());
		maxX = Math.max(maxX, item.getX().doubleValue());
		minY = Math.min(minY, item.getY().doubleValue());
		maxY = Math.max(maxY, item.getY().doubleValue());
		unArchive();
		synchronized (data) {
			data.add(item);
		}
		if (notify && !batching)
			fireSeriesChanged();
		flagAsNeedingToBeArchived();
	}

	// try to stop fireSeriesChanged
	@Override
	public void add(double x, double y) {
		unArchive();
		add(x, y, true);
	}

	private void flagAsNeedingToBeArchived() {
		filename = null;
		dataFilename = null;
	}

	@Override
	public void add(double x, double y, boolean notify) {
		unArchive();
		add(new XYDataItem(x, y), notify);
		flagAsNeedingToBeArchived();

	}

	/**
	 * Returns the lineNumber
	 *
	 * @return the lineNumber
	 */
	public int getLineNumber() {
		return lineNumber;
	}

	/**
	 * @param lineNumber
	 */
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;

		paint = defaultPaints[lineNumber % 9];
		symbolPaint = paint;

		marker = Marker.fromCounter(lineNumber % 8);
	}

	/**
	 * Gets the Paint which should be used to draw the line.
	 *
	 * @return the Paint (which is usually just a Color)
	 */
	public Paint getPaint() {
		return paint;
	}

	/**
	 * Sets the Paint which should be used to draw the line.
	 *
	 * @param paint
	 *            the Paint value to use (which is usually just a Color)
	 */
	public void setPaint(Paint paint) {
		if (paint != null) {
			unArchive();
			this.paint = paint;
			fireSeriesChanged();
		}
	}

	/**
	 * Returns the parameter which specifies whether the marker is filled.
	 *
	 * @return current value of filled.
	 */
	public boolean getFilled() {
		return marker.isFilled();
	}

	/**
	 * Returns the current symbol used to mark points
	 *
	 * @return the symbol
	 */
	public Shape getSymbol() {
		return marker.getShape(symbolSize);
	}

	/**
	 * Sets the Marker
	 *
	 * @param marker
	 *            the marker
	 */
	public void setMarker(Marker marker) {
		unArchive();
		this.marker = marker;
		fireSeriesChanged();
	}

	/**
	 * Gets the symbol size
	 *
	 * @return symbol size
	 */
	public int getSymbolSize() {
		return symbolSize;
	}

	/**
	 * Sets the symbol size
	 *
	 * @param symbolSize
	 *            the new value
	 */
	public void setMarkerSize(int symbolSize) {
		unArchive();
		this.symbolSize = symbolSize;
		fireSeriesChanged();
	}

	/**
	 * Gets the Paint used for marker symbols
	 *
	 * @return the marker symbol Paint
	 */
	public Paint getSymbolPaint() {
		return symbolPaint;
	}

	/**
	 * Sets the Paint used to draw marker symbols
	 *
	 * @param symbolPaint
	 *            the new value
	 */
	public void setSymbolPaint(Paint symbolPaint) {
		unArchive();
		this.symbolPaint = symbolPaint;
		fireSeriesChanged();
	}

	/**
	 * Gets the lineWidth
	 *
	 * @return the line width
	 */
	public int getLineWidth() {
		return lineWidth;
	}

	/**
	 * Sets the line width
	 *
	 * @param lineWidth
	 *            the new value (pixels)
	 */
	public void setLineWidth(int lineWidth) {
		unArchive();
		this.lineWidth = lineWidth;

		// stroke = simpleStroke.getStroke(lineWidth);
		fireSeriesChanged();
	}

	/**
	 * Gets the current Stroke used to draw lines
	 *
	 * @return the stroke.
	 */
	public Stroke getStroke() {
		return pattern.getStroke(lineWidth);
	}

	/**
	 * Sets the line Pattern.
	 *
	 * @param pattern
	 *            the new value
	 */
	public void setPattern(Pattern pattern) {
		unArchive();
		this.pattern = pattern;
		fireSeriesChanged();
	}

	/**
	 * Returns the line Pattern.
	 *
	 * @return the Pattern
	 */
	public Pattern getPattern() {
		return pattern;
	}

	/**
	 * Returns the Marker used for points on the line.
	 *
	 * @return the Marker
	 */
	public Marker getMarker() {
		return marker;
	}

	/**
	 * Sets the line Type.
	 *
	 * @param type
	 *            the new value
	 */
	public void setType(Type type) {
		unArchive();
		this.type = type;
		fireSeriesChanged();
	}

	/**
	 * Returns the line Type.
	 *
	 * @return the Type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Returns whether or not lines are drawn
	 *
	 * @return true if lines are drawn
	 */
	public boolean isDrawLines() {
		return type.getDrawLine();
	}

	/**
	 * Returns whether or not markers are drawn
	 *
	 * @return true if markers are drawn
	 */
	public boolean isDrawMarkers() {
		return type.getDrawPoints();
	}

	/**
	 * Sets all the points at once.
	 *
	 * @param xVals
	 *            array of x values
	 * @param yVals
	 *            array of y values
	 */
	void setPoints(double[] xVals, double[] yVals) {
		unArchive();
		clear();

		// In this case the clear() is enough;
		if (xVals == null || yVals == null)
			return;

		// cuts to same length if needed
		int lastIndex = (xVals.length < yVals.length) ? (xVals.length - 1) : (yVals.length - 1);
		// at least one empty
		if (lastIndex < 0)
			return;

		// Add the points one at a time (it seems to be the only
		// way) but only do fireSeriesChanged on the last point)
		for (int i = 0; i < lastIndex; i++) {
			add(xVals[i], yVals[i], false);
		}
		add(xVals[lastIndex], yVals[lastIndex], true);

	}

	/**
	 * Returns whether or not the line is visible
	 *
	 * @return true if visible
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * Sets whether or not the line is visible
	 *
	 * @param visible
	 *            the new value.
	 */
	public void setVisible(boolean visible) {
		// NB the fireSeriesChanged is essential to get the process
		// started which leads to the axes being recalculated
		if (this.visible != visible && getNotify()) {
			this.visible = visible;
			unArchive();
			fireSeriesChanged();
		}
	}

	/**
	 * Removes all data items from the series.
	 */
	@Override
	public void clear() {
		flagAsNeedingToBeArchived();
		minX = Double.POSITIVE_INFINITY;
		maxX = Double.NEGATIVE_INFINITY;
		minY = Double.POSITIVE_INFINITY;
		maxY = Double.NEGATIVE_INFINITY;
		super.clear();
	}

	/**
	 * Gets whether or not the line is visible in the legend
	 *
	 * @return true if visible
	 */
	public boolean isVisibleInLegend() {
		return visibleInLegend;
	}

	/**
	 * Sets whether or not the line is visible in the legend
	 *
	 * @param visibleInLegend
	 *            the new value
	 */
	public void setVisibleInLegend(boolean visibleInLegend) {
		this.visibleInLegend = visibleInLegend;
	}

	/**
	 * Gets the axis number for this line
	 *
	 * @return the axis number
	 */
	public int getAxis() {
		return axis;
	}

	/**
	 * Returns the X value corresponding to the peak Y value in the series.
	 *
	 * @return the X value of the peak
	 */
	public double getXValueOfPeak() {
		double xValue = 0.0;
		double maximum = Double.MIN_VALUE;
		XYDataItem item;
		synchronized (getData()) {
			for (Iterator<XYDataItem> i = getIterator(); i.hasNext();) {
				item = i.next();
				if (item.getY().doubleValue() > maximum) {
					maximum = item.getY().doubleValue();
					xValue = item.getX().doubleValue();
				}

			}
		}
		return xValue;
	}

	/**
	 * Gets the name. From 1.0.0 onwards the XYSeries has a key and a description instead of just a name. This method
	 * and the corresponding setter hide this from the rest of gda.plots.
	 *
	 * @return the name of the line
	 */
	public String getName() {
		return (String) getKey();
	}

	/**
	 * Sets the name. From 1.0.0 onwards the XYSeries has a key and a description instead of just a name. This method
	 * and the corresponding getter hide this from the rest of gda.plots.
	 *
	 * @param name
	 *            the name
	 */
	public void setName(String name) {
		unArchive();
		setKey(name);
		fireSeriesChanged();
	}

	/**
	 * @return true if batching is on, false if off
	 */
	public boolean isBatching() {
		return batching;
	}

	/**
	 * @param batching
	 */
	public void setBatching(boolean batching) {
		this.batching = batching;
		if (batching == false) {
			unArchive();
			fireSeriesChanged();
		}
	}

	/**
	 * @param xRange
	 * @return The min/max X/Y of the data within the Range of x given in xRange
	 */
	Double[] getBounds(Range xRange) {
		double minX = xRange.getLowerBound();
		double maxX = xRange.getUpperBound();
		double minY = Double.POSITIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		Double[] extents = new Double[4];
		synchronized (getData()) {
			Iterator<XYDataItem> iter = getIterator();
			while (iter.hasNext()) {
				XYDataItem item = iter.next();
				double xval = item.getX().doubleValue();
				double yval = item.getY().doubleValue();
				if (xval >= minX && xval <= maxX) {
					minY = Math.min(minY, yval);
					maxY = Math.max(maxY, yval);
				}
			}
		}
		extents[0] = minX;
		extents[1] = maxX;
		extents[2] = minY;
		extents[3] = maxY;
		return extents;
	}

	/**
	 * @return true if included in data
	 */
	public boolean isIncludedInData() {
		return includedInData;
	}

	/**
	 * @param includedInData
	 */
	public void setIncludedInData(boolean includedInData) {
		unArchive();
		this.includedInData = includedInData;
	}

	private String filename = null;
	private String dataFilename = null; // null if data has not been restored from the archive. else name of file from
										// which data was last unarchived

	synchronized void archive() throws IOException {
		if (archiveThreshold == null) {
			archiveThreshold = LocalProperties.getInt(GDA_PLOT_SIMPLEXYSERIES_ARCHIVE_THRESHOLD, 50);
		}
		if (data.size() > archiveThreshold) {
			if (filename == null) {
				File tempFile = File.createTempFile("SimpleXYSeries_", ".dat");
				tempFile.deleteOnExit();
				FileOutputStream f_out = null;
				ObjectOutputStream obj_out = null;
				try {
					f_out = new FileOutputStream(tempFile);
					obj_out = new ObjectOutputStream(f_out);
					obj_out.writeObject(data);
					obj_out.flush();
					obj_out.reset(); // if not you get an OuOfMemoryException eventually
				} finally {
					if (obj_out != null)
						obj_out.close();
					if (f_out != null)
						f_out.close();
				}
				filename = tempFile.getPath();
				logger.info("SimpleXYSeries.archive to " + filename);
			}
			/* we always need to clear the data and set the flag to indicate so */
			if (data.size() > 0) {
				data.clear();
			}
			dataFilename = null;
		}
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
	}

	@SuppressWarnings("unchecked")
	synchronized void unArchive() {
		if (filename != null && (dataFilename == null || !filename.equals(dataFilename))) {
			try {
				FileInputStream f_in = null;
				ObjectInputStream obj_in = null;
				try {
					f_in = new FileInputStream(filename);
					obj_in = new ObjectInputStream(f_in);
					Object obj = obj_in.readObject();
					data = (List<XYDataItem>) obj;
				} finally {
					if (obj_in != null)
						obj_in.close();
					if (f_in != null)
						f_in.close();
				}

				logger.info("SimpleXYSeries.unArchive from " + filename);
			} catch (Exception e) {
				logger.error("Error getting data from " + filename, e);
			}
			dataFilename = filename;
		}
	}

}
