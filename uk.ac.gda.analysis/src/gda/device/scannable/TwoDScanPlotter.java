/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package gda.device.scannable;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.jython.IAllScanDataPointsObserver;
import gda.jython.IScanDataPointProvider;
import gda.jython.InterfaceProvider;
import gda.scan.ScanDataPoint;
import uk.ac.diamond.scisoft.analysis.SDAPlotter;

/**
 * Plots a 2D graph of the current scan into an RCP plot window as the scan progresses.
 * <p>
 * To use this scannable, give it the names of the x,y and z columns and the plot view to send the plot to ("Plot 1" by
 * default. Then simply include in the scan command you wish to plot.
 */
public class TwoDScanPlotter extends ScannableBase implements IAllScanDataPointsObserver {

	private static final Logger logger = LoggerFactory.getLogger(TwoDScanPlotter.class);

	protected DoubleDataset x;
	protected DoubleDataset y;
	protected DoubleDataset intensity;

	private String z_colName; // Currently, this *must* be a detector as this class looks only in that part of the SDP
	private String plotViewname = "Plot 1";

	private Double xStart;
	private Double xStop;
	private Double xStep;

	private Double yStart;
	private Double yStop;
	private Double yStep;
	private Long rate = 1200L; // by default refresh all 2D plotter is 1.2 s
	private Long timeElapsed;
	private Long timeInit;



	public TwoDScanPlotter() {
		this.inputNames = new String[] {};
		this.extraNames = new String[] {};
		this.outputFormat = new String[] {};
	}

	@Override
	public void atScanStart() throws DeviceException {
		// re-register with datapoint provider
		InterfaceProvider.getScanDataPointProvider().addIScanDataPointObserver(this);
		logger.debug(getName() + " - registering as SDP listener.");
		timeElapsed = 0L;
		timeInit = System.currentTimeMillis();
	}

	private DoubleDataset createTwoDset(Double start, Double stop, Double step, Boolean reverse) {
		int numPoints = ScannableUtils.getNumberSteps(start, stop, step) + 1; // why + 1?
		double[] values = new double[numPoints];
		Double value = start;
		for (int index = 0; index < numPoints; index++) {
			values[index] = value;
			value += step;
		}
		if (reverse) {
			ArrayUtils.reverse(values);
		}

		return DatasetFactory.createFromObject(DoubleDataset.class, values);
	}

	@Override
	public void atScanEnd() throws DeviceException {
		if (LocalProperties.check("gda.scan.endscan.neworder", false)) {
			try {
				// try to plot any points that have been buffered
				plot();
			} catch (Exception e) {
			}
			deregisterAsScanDataPointObserver();
		} else {
			logger.warn("Cannot safely deregister at scan end if property gda.scan.endscan.neworder is not true.");
		}
	}

	@Override
	public void atCommandFailure() {
		deregisterAsScanDataPointObserver();
	}

	@Override
	public void stop() throws DeviceException {
		deregisterAsScanDataPointObserver();
	}

	private void deregisterAsScanDataPointObserver() {
		intensity = null;
		x = null;
		y = null;
		clearArgs();
		InterfaceProvider.getScanDataPointProvider().deleteIScanDataPointObserver(this);
	}

	@Override
	public void update(Object source, Object arg) {
		if (source instanceof IScanDataPointProvider && arg instanceof ScanDataPoint) {
			int currentPoint = ((ScanDataPoint) arg).getCurrentPointNumber();
			int totalPoints = ((ScanDataPoint) arg).getNumberOfPoints();
			try {
				unpackSDP((ScanDataPoint) arg);
				timeElapsed = System.currentTimeMillis() - timeInit;
				System.out.println("TimeElapsed"+timeElapsed);
				if (timeElapsed > rate){
					plot();
					timeElapsed = 0L;
					timeInit = System.currentTimeMillis();
				}
				logger.debug(getName() + " - Plotting map after receiving point " + currentPoint + " of " + totalPoints);

				if (currentPoint == (totalPoints - 1)) {
					plot(); //plot last points
					logger.debug(getName() + " - last point received; deregistering as SDP listener.");
					deregisterAsScanDataPointObserver();
				}
			} catch (Exception e) {
				// Quietly deregister as this scan does not match what this is looking for.
				// In this way, this object could be added to the list of defaults and only plot related scans.
				deregisterAsScanDataPointObserver();
			}
		}
	}

	private void unpackSDP(ScanDataPoint sdp) {
		// NB: ScanDataPoint scan dimensions work are an array working from outside to inside in the nested scans
		// NB: here we are plotting the inner as the x, and the outer as the y.

		// if the first point, then create empty datasets
		if (intensity == null) {
			intensity = DatasetFactory.zeros(DoubleDataset.class, sdp.getScanDimensions()[0], sdp.getScanDimensions()[1]);
			// Fill with NaN to allow auto histogramming to work. Otherwise values are zero.
			intensity.fill(Double.NaN);

			// if xstart,xstop,xstep values not defined then simply use an index
			if (xStart == null) {
				x = createTwoDset(0.0, (double) sdp.getScanDimensions()[0], 1.0, false);
			} else {
				x = createTwoDset(xStart, xStop, xStep, false);
			}

			if (yStart == null) {
				y = createTwoDset(0.0, (double) sdp.getScanDimensions()[1], 1.0, true);
			} else {
				y = createTwoDset(yStart, yStop, yStep, true);
			}

		}

		Double inten = getIntensity(sdp);
		int[] locationInDataSets = getSDPLocation(sdp);
		int xLoc = locationInDataSets[0];
		// y has to be reversed as otherwise things are plotted from the top left, not bottom right as the plotting
		// system 2d plotting was originally for images which work this way.
		int yLoc = sdp.getScanDimensions()[0] - locationInDataSets[1] - 1;
		intensity.set(inten, yLoc, xLoc);
	}

	private int[] getSDPLocation(ScanDataPoint sdp) {
		int yLoc = 0;
		int xLoc = sdp.getCurrentPointNumber();

		if (sdp.getCurrentPointNumber() >= sdp.getScanDimensions()[1]) {
			yLoc = sdp.getCurrentPointNumber() / sdp.getScanDimensions()[1];
			xLoc = sdp.getCurrentPointNumber() - (yLoc * sdp.getScanDimensions()[1]);
		}

		return new int[] { xLoc, yLoc };
	}

	private Double getIntensity(ScanDataPoint sdp) {
		return sdp.getDetectorDataAsDoubles()[getPositionOfDetector(z_colName, sdp)];
	}

	private int getPositionOfDetector(String columnName, ScanDataPoint sdp) {
		Object[] headers = sdp.getDetectorHeader().toArray();
		return org.apache.commons.lang.ArrayUtils.indexOf(headers, columnName);
	}

	public void plot() throws Exception {
		if (getPlotViewname() != null) {
			logger.debug("Plotting to RCP client plot named:" + getPlotViewname());
			// SDAPlotter.surfacePlot(plotViewname, x, y, intensity);
			SDAPlotter.imagePlot(plotViewname, x, y, intensity);
		}
	}


	/**
	 * Call this before the scan if you want to plot actual motor positions, not just indexes
	 *
	 * @param xStart
	 * @param xStop
	 * @param xStep
	 */
	public void setXArgs(Double xStart, Double xStop, Double xStep) {
		this.xStart = xStart;
		this.xStop = xStop;
		this.xStep = xStep;
	}

	/**
	 * Call this before the scan if you want to plot actual motor positions, not just indexes
	 *
	 * @param yStart
	 * @param yStop
	 * @param yStep
	 */
	public void setYArgs(Double yStart, Double yStop, Double yStep) {
		this.yStart = yStart;
		this.yStop = yStop;
		this.yStep = yStep;
	}

	public void clearArgs() {
		xStart = xStop = xStep = yStart = yStep = yStop = null;
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		return null;
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return false;
	}

	public String getZ_colName() {
		return z_colName;
	}

	public void setZ_colName(String zColName) {
		z_colName = zColName;
	}

	public void setPlotViewname(String plotViewname) {
		this.plotViewname = plotViewname;
	}

	public String getPlotViewname() {
		return plotViewname;
	}
	public Long getRate() {
		return rate;
	}

	public void setRate(Long rate) {
		this.rate = rate;
	}

}
