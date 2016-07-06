/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.ui.PlatformUI;

import gda.rcp.util.ScanDataPointEvent;
import gda.scan.IScanDataPoint;
import uk.ac.diamond.scisoft.analysis.rcp.views.plot.DataSetPlotData;
import uk.ac.diamond.scisoft.analysis.rcp.views.plot.IPlotData;

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
		//do nothing as this will interfere with the last points coming through
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
			if (updateCachedValues() && !waitingForRefresh) {
				y = getY((IScanDataPoint[]) null);
				x = getX((IScanDataPoint[]) null);
				if (y != null && x != null) {
					waitingForRefresh = true;
					PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
						@Override
						public void run() {
							showPlotter();
							rebuildPlot(); // uses x and y
							waitingForRefresh = false;
						}
					});
				} else if (y == null){
					stack.topControl = lblNoDataMessage;
				}
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				break;
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
			scanNumber = sdp.getScanIdentifier();
		}

		if (currentScanUniqueName != sdp.getUniqueName()) {
			// only clear at this point - when points from a new scan are coming in
			system.clear();
			cachedX.clear();
			cachedY.clear();
			currentScanUniqueName = sdp.getUniqueName();
			scanNumber = sdp.getScanIdentifier();
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

		int startIndex = 0;
		if (previousCacheSize > 0){
			startIndex = previousCacheSize - 1;
		}

		if (startIndex == newCacheSize -1){
			return false;
		}

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

		Double[] values = cachedX.toArray(new Double[]{});
		double[] primitiveValues = ArrayUtils.toPrimitive(values, values.length);
		Dataset xValues = DatasetFactory.createFromObject(primitiveValues);
		xValues.setName(getXAxisName());
		return new DataSetPlotData(getXAxisName(), xValues);
	}

	@Override
	protected String getXAxisName() {
		return xAxisTitle;
	}

	@Override
	protected void plotPointsFromService() throws Exception {
		if (cachedX == null)
			cachedX = new ArrayList<Double>(89);
		if (cachedY == null)
			cachedY = new ArrayList<Double>(89);
		super.plotPointsFromService();
	}

	public List<Double> testGetCachedX() {
		return this.cachedX;
	}

	public List<Double> testGetCachedY() {
		return this.cachedY;
	}
}