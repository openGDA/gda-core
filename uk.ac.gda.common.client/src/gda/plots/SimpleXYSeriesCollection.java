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

import org.jfree.data.Range;
import org.jfree.data.RangeInfo;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Extends XYSeriesCollection in order to restrict the type of XYSeries to SimpleXYSeries and to provide mechanisms to
 * access the series by their lineNumbers instead of their indexes in the XYSeriesCollection. Also implements RangeInfo
 * (XYSeriesCollection only implements DomainInfo) so that Y axes get drawn more sensibly for horizontal lines.
 */

class SimpleXYSeriesCollection extends XYSeriesCollection implements RangeInfo {
	private SimpleValueTransformer xValueTransformer;

	private boolean batching = false;
	private SimplePlot simplePlot;

	/**
	 * Constructor.
	 *
	 * @param simplePlot
	 *            SimplePlot - used to allow calls to get ranges to go back to the plot window
	 */
	SimpleXYSeriesCollection(SimplePlot simplePlot) {
		this.simplePlot = simplePlot;
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
	 * Adds an XYSeries to the collection. Overrides the super class method to ensure that only SimpleXYSeries can be
	 * added.
	 *
	 * @param series
	 *            the SimpleXYSeries to add
	 */
	@Override
	public void addSeries(XYSeries series) {
		if (series instanceof SimpleXYSeries)
			super.addSeries(series);
	}

	/**
	 * Finds a series representing a given lineNumber
	 *
	 * @param lineNumber
	 * @return the series with that lineNumber or null
	 */
	SimpleXYSeries find(int lineNumber) {
		SimpleXYSeries found = null;
		for( Object obj : getSeries()){
			if(obj != null && obj instanceof SimpleXYSeries){
				SimpleXYSeries sxys = (SimpleXYSeries)obj;
				if (sxys.getLineNumber() == lineNumber) {
					found = sxys;
					break;
				}
			}
		}
		return found;
	}

	// The next three methods implement the RangeInfo interface. These
	// are based on the implementation of DomainInfo used by
	// XYSeriesCollection
	// which can be found in the IntervalXYDelegate.

	/**
	 * Returns the maximum y value of the data (part of the RangeInfo interface)
	 *
	 * @param includeInterval
	 * @return the minimum y value
	 */
	@Override
	public double getRangeLowerBound(boolean includeInterval) {
		// This is how IntervalXYDelegate does it for domain
		// However I suspect it is wrong, getValueRange may
		// have been arbitrarily adjusted if all the data has
		// the same value.
		return getRangeBounds(includeInterval).getLowerBound();
	}

	/**
	 * Returns the maximum y value of the data (part of the RangeInfo interface)
	 *
	 * @param includeInterval
	 * @return the maximum y value
	 */
	@Override
	public double getRangeUpperBound(boolean includeInterval) {
		// This is how IntervalXYDelegate does it for domain
		// However I suspect it is wrong, getValueRange may
		// have been arbitrarily adjusted if all the data has
		// the same value.
		return getRangeBounds(includeInterval).getUpperBound();
	}

	/**
	 * Returns the range of y values (part of the RangeInfo interface)
	 *
	 * @param includeInterval
	 * @return a Range representing the range of y values
	 */
	@Override
	public Range getRangeBounds(boolean includeInterval) {

		if (simplePlot != null) {
			Range range = simplePlot.getRangeBounds(this, includeInterval);
			if (range != null)
				return range;

		}
		Range range = iterateXYRangeExtent();
		// A null range indicates no data (this seems to be dealt with
		// somewhere)
		// If the range upper and lower are equal (usually because there is only
		// one data point) then we extend it a bit. NB we have to take care not
		// to produce a range which is the wrong way round if dealing with
		// negative bounds.
		if (range != null && (range.getLowerBound() == range.getUpperBound())) {
			if (range.getLowerBound() < 0.0)
				range = new Range(range.getLowerBound() * 1.1, range.getUpperBound() * 0.9);
			else
				range = new Range(range.getLowerBound() * 0.9, range.getUpperBound() * 1.1);
		}

		return range;
	}

	/**
	 * Iterates over the data item of the xy dataset to find the range extent. This is a modified version of the
	 * DatasetUtilities static method of the same name. It works in the same except that it does not include series
	 * which are invisible. If...then...elses referring to situations impossible for this class have been removed and
	 * the code has been simplified to take advantage of the fact that it is working within an instance.
	 *
	 * @return The range (possibly <code>null</code>).
	 */
	private Range iterateXYRangeExtent() {
		// FIXME: find out how the latest version in DatasetUtilities actually
		// uses the
		// includeInterval and implement it here.
		double minimum = Double.POSITIVE_INFINITY;
		double maximum = Double.NEGATIVE_INFINITY;
		double lvalue;
		double uvalue;

		int seriesCount = getSeriesCount();
		for (int series = 0; series < seriesCount; series++) {
			if (((SimpleXYSeries) getSeries(series)).isVisible()) {
				int itemCount = getItemCount(series);
				for (int item = 0; item < itemCount; item++) {
					lvalue = getStartYValue(series, item);
					uvalue = getEndYValue(series, item);

					if (!Double.isNaN(lvalue)) {
						minimum = Math.min(minimum, lvalue);
					}
					if (!Double.isNaN(uvalue)) {
						maximum = Math.max(maximum, uvalue);
					}
				}
			}
		}
		if (minimum == Double.POSITIVE_INFINITY) {
			return null;
		}
		return new Range(minimum, maximum);
	}

	/**
	 * Overrides the super class method so that we can deal with invisible lines (by not including them). In
	 * XYSeriesCollection getDomainRange is delegated to an XYIntervalDelegate so this method actually is copied from
	 * that class.
	 *
	 * @param includeInterval
	 * @return The range (possibly <code>null</code>).
	 */
	@Override
	public Range getDomainBounds(boolean includeInterval) {
		// Replace the call to the DatasetUtilities iterateDomainExtent with a
		// private one.
		if (simplePlot != null) {
			Range range = simplePlot.getDomainBounds(this, includeInterval);
			if (range != null){
				return new Range(range.getLowerBound(), range.getUpperBound());
			}

		}
		Range range = iterateDomainExtent();

		if (getSeriesCount() == 1 && getItemCount(0) == 1) {
			/*
			 * if there is only one interval value, so add some space to the left and the right - otherwise one bar
			 * looks like a background coloration.
			 */
			range = new Range(range.getLowerBound() - getIntervalWidth(), range.getUpperBound() + getIntervalWidth());
		}

		return range;
	}

	/**
	 * Iterates over the data item of the xy dataset to find the domain extent. This is a modified version of the
	 * DatasetUtilities static method of the same name. It works in the same except that it does not inclued series
	 * which are invisible. If...then...elses referring to situations impossible for this class have been removed and
	 * the code has been simplified to take advantage of the fact that it is working within an instance.
	 *
	 * @return The range (possibly <code>null</code>).
	 */
	private Range iterateDomainExtent() {
		// FIXME: find out how the latest version in DatasetUtilities actually
		// uses the
		// includeInterval and implement it here.
		double minimum = Double.POSITIVE_INFINITY;
		double maximum = Double.NEGATIVE_INFINITY;
		int seriesCount = getSeriesCount();
		for (int series = 0; series < seriesCount; series++) {
			if (((SimpleXYSeries) getSeries(series)).isVisible()) {
				int itemCount = getItemCount(series);
				for (int item = 0; item < itemCount; item++) {
					double lvalue;
					double uvalue;

					lvalue = getStartXValue(series, item);
					uvalue = getEndXValue(series, item);

					// Transform the values
					lvalue = xValueTransformer.transformValue(lvalue);
					uvalue = xValueTransformer.transformValue(uvalue);

					// Because of the transformation we can no longer rely
					// on lvalue being less than uvalue so must do the full
					// comparison to find the minimum and maximum.
					minimum = Math.min(minimum, Math.min(lvalue, uvalue));
					maximum = Math.max(maximum, Math.max(lvalue, uvalue));
				}
			}
		}
		if (minimum > maximum) {
			return null;
		}
		return new Range(minimum, maximum);
	}

	/**
	 * Returns the x value corresponding to the peak y value of a particular series.
	 *
	 * @param text
	 *            the name of the series
	 * @return the x value corresponding to the peak y value of that series
	 */
	double getSeriesXValueOfPeak(String text) {
		for (int i = 0; i < getSeriesCount(); i++)
			if (((SimpleXYSeries) getSeries(i)).getName().equals(text))
				return ((SimpleXYSeries) getSeries(i)).getXValueOfPeak();

		return 0.0;

	}

	/**
	 * Returns the current X value transformer.
	 *
	 * @return the x value transformer.
	 */
	SimpleValueTransformer getXValueTransformer() {
		return xValueTransformer;
	}

	/**
	 * Sets a new xValueTransformer.
	 *
	 * @param valueTransformer
	 *            the new value
	 */
	void setXValueTransformer(SimpleValueTransformer valueTransformer) {
		// The plot does not redraw unless fireDatasetChanged is called.
		xValueTransformer = valueTransformer;
		fireDatasetChanged();
	}

	/**
	 * @return boolean
	 */
	public boolean isBatching() {
		return batching;
	}

	/**
	 * @param batching
	 */
	public void setBatching(boolean batching) {
		this.batching = batching;
		for (int i = 0; i < getSeriesCount(); i++) {
			((SimpleXYSeries) getSeries(i)).setBatching(batching);
		}
	}
}