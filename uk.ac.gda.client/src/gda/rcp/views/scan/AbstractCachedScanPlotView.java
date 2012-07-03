/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.rcp.views.scan;

import gda.rcp.util.ScanDataPointEvent;
import gda.scan.IScanDataPoint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.ui.PlatformUI;

import uk.ac.diamond.scisoft.analysis.rcp.views.plot.IPlotData;
import uk.ac.diamond.scisoft.analysis.rcp.views.plot.PlotData;

/**
 * Extend to deal with plots which are simple functions.
 */
public abstract class AbstractCachedScanPlotView extends AbstractScanPlotView implements Runnable {

	protected List<Double> cachedX, cachedY;
	protected String xAxisTitle;

	// attributes to perform calculations in a separate thread
	private ScanDataPointEvent latestEvent;
	private Thread performCalculationsThread = null;
	private volatile boolean continueCalculations = true;
	private String currentScanUniqueName = "";

	@Override
	public void scanStopped() {
		super.scanStopped();
		if (cachedX == null)
			cachedX = new ArrayList<Double>(89);
		if (cachedY == null)
			cachedY = new ArrayList<Double>(89);
		cachedX.clear();
		cachedY.clear();
	}

	@Override
	public void scanStarted() {
		super.scanStarted();
		if (cachedX == null)
			cachedX = new ArrayList<Double>(89);
		if (cachedY == null)
			cachedY = new ArrayList<Double>(89);
	}

	@Override
	public void scanDataPointChanged(ScanDataPointEvent e) {
		if (cachedX == null)
			cachedX = new ArrayList<Double>(89);
		if (cachedY == null)
			cachedY = new ArrayList<Double>(89);
		// don't call the super method, instead add to the cache
		// super.scanDataPointChanged(e);
		latestEvent = e;
		checkLoopRunning();
	}

	protected void checkLoopRunning() {
		if (performCalculationsThread == null || !performCalculationsThread.isAlive()) {
			performCalculationsThread = new Thread(this, this.getClass().getSimpleName());
			performCalculationsThread.start();
		}
	}

	@Override
	public void run() {
		while (continueCalculations) {
			if (updateCachedValues()) {
				y = getY((IScanDataPoint[]) null);
				x = getX((IScanDataPoint[]) null);
				if (y != null && x != null) {
					PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
						@Override
						public void run() {
							rebuildPlot(); // uses x and y
						}
					});
				}
			}
		}
	}

	@Override
	public void dispose() {
		continueCalculations = false;
		super.dispose();
	}

	/**
	 * Using the latestEvent attribute, update the cachedY and cachedX values.
	 * <p>
	 * Implementations of this should be aware that the latestEvent attribute will be updated by a different thread so
	 * should operate with a copy of the attribute.
	 * 
	 * @return boolean - true if the cache was updated
	 */
	private boolean updateCachedValues() {

		// take copies of what we are going to use
		IScanDataPoint sdp = latestEvent.getCurrentPoint();
		Collection<IScanDataPoint> allSDPs = latestEvent.getDataPoints();

		if (currentScanUniqueName == null || currentScanUniqueName.isEmpty()) {
			currentScanUniqueName = sdp.getUniqueName();
		}

		if (currentScanUniqueName != sdp.getUniqueName()) {
			// somehow missed the scan stopped event
			scanStopped();
		}

		if (!scanning) {
			// somehow missed the scan started event
			scanStarted();
		}

		// assume a new entry to Y for every SDP
		int previousCacheSize = cachedY.size();

		if (previousCacheSize == allSDPs.size()) {
			// nothing new
			return false;
		}

		// extract new SDPs and perform calculations
		IScanDataPoint[] sdpArray = allSDPs.toArray(new IScanDataPoint[allSDPs.size()]);
		sdpArray = (IScanDataPoint[]) ArrayUtils.subarray(sdpArray, previousCacheSize,sdpArray.length);
 
//		getY(sdpArray);
		updateCache(sdpArray);
		
		return sdpArray.length > 0;
	}

	/**
	 * Using the latest data points, update a local cache of data.
	 * <p>
	 * It is then expected that the next calls to getX and getY will reflect the latest cache contents.
	 * 
	 * @param sdpArray
	 */
	protected abstract void updateCache(IScanDataPoint[] sdpArray);

	@Override
	protected IPlotData getX(IScanDataPoint... points) {

		if (cachedX == null)
			cachedX = new ArrayList<Double>(89);

//		if (points != null){
//			for (int i = 0; i < points.length; i++) {
//				final IScanDataPoint point = points[i];
//				final Double[] data = point.getAllValuesAsDoubles();
//				cachedX.add(data[0]);
//				xAxisTitle = point.getPositionHeader().get(0);
//			}
//		}
		return new PlotData(xAxisTitle, cachedX);
	}

	@Override
	protected String getXAxis() {
		return xAxisTitle;
	}

	@Override
	protected void plotPointsFromService() throws Exception {

		if (cachedX == null)
			cachedX = new ArrayList<Double>(89);
		if (cachedY == null)
			cachedY = new ArrayList<Double>(89);

		cachedX.clear();
		cachedY.clear();

		super.plotPointsFromService();
	}

	public List<Double> testGetCachedX() {
		return this.cachedX;
	}

	public List<Double> testGetCachedY() {
		return this.cachedY;
	}
}