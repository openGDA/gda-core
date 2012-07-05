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
import java.util.List;

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
	private ScanDataPointEvent lastEventUsedInACalculation;
	private Thread performCalculationsThread = null;
	private volatile boolean continueCalculations = true;
	private String currentScanUniqueName = "";
	private volatile boolean waitingForRefresh = false;

	@Override
	public void scanStopped() {
		
		//do nothing as this will intefere with the last points coming through
//		super.scanStopped();
//		if (cachedX == null)
//			cachedX = new ArrayList<Double>(89);
//		if (cachedY == null)
//			cachedY = new ArrayList<Double>(89);
//		cachedX.clear();
//		cachedY.clear();
	}

	@Override
	public void scanStarted() {
		super.scanStarted();
		if (cachedX == null)
			cachedX = new ArrayList<Double>(89);
		if (cachedY == null)
			cachedY = new ArrayList<Double>(89);
		cachedX.clear();
		cachedY.clear();
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
			continueCalculations = true;
			performCalculationsThread.start();
		}
	}

	@Override
	public void run() {
		while (continueCalculations) {
			if (!waitingForRefresh && updateCachedValues()) {
				y = getY((IScanDataPoint[]) null);
				x = getX((IScanDataPoint[]) null); 
				if (y != null && x != null) {
					waitingForRefresh = true;
					PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
						@Override
						public void run() {
							rebuildPlot(); // uses x and y
							waitingForRefresh = false;
						}
					});
				}
			}
			if (!waitingForRefresh){
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					continueCalculations = false;
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
		
		if (lastEventUsedInACalculation!= null && latestEvent == lastEventUsedInACalculation){
			return false;
		}

		// take copies of what we are going to use
		IScanDataPoint sdp = latestEvent.getCurrentPoint();
		ArrayList<IScanDataPoint> allSDPs = (ArrayList<IScanDataPoint>) latestEvent.getDataPoints();

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
		lastEventUsedInACalculation = latestEvent;

		// assume a new entry to Y for every SDP
		int previousCacheSize = cachedY.size();
		int newCacheSize = allSDPs.size();

		if (previousCacheSize == newCacheSize) {
			// nothing new
			return false;
		}
		
//		ArrayList<IScanDataPoint> al = (ArrayList<IScanDataPoint>) allSDPs;
		
		int startIndex = 0;
		if (previousCacheSize > 0){
			startIndex = previousCacheSize - 1;
		}
		
		if (startIndex == newCacheSize -1){
			return false;
		}
		
//		int endIndex = al.size();
		
//		List<IScanDataPoint> sublist  = ((AbstractList<IScanDataPoint>) latestEvent.getDataPoints()).subList(startIndex,newCacheSize);

		// extract new SDPs and perform calculations
//		IScanDataPoint[] sdpArray = allSDPs.toArray(new IScanDataPoint[allSDPs.size()]);
//		sdpArray = (IScanDataPoint[]) ArrayUtils.subarray(sdpArray, previousCacheSize,sdpArray.length);
 
//		getY(sdpArray);
		updateCache(allSDPs,startIndex);
		return true;
	}

	/**
	 * Using the latest data points, update a local cache of data.
	 * <p>
	 * It is then expected that the next calls to getX and getY will reflect the latest cache contents.
	 * 
	 * @param collection
	 */
	protected abstract void updateCache(ArrayList<IScanDataPoint> collection, int startIndex);

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