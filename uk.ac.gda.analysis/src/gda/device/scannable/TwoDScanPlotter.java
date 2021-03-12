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

import java.util.Objects;
import java.util.stream.Stream;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DoubleDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.RateLimiter;

import gda.device.DeviceException;
import gda.jython.IScanDataPointObserver;
import gda.jython.IScanDataPointProvider;
import gda.jython.InterfaceProvider;
import gda.scan.ScanDataPoint;
import gda.scan.ScanInformation;
import uk.ac.diamond.daq.server.rcpcontroller.RCPController;
import uk.ac.diamond.scisoft.analysis.SDAPlotter;
import uk.ac.diamond.scisoft.analysis.plotclient.IPlotWindowManager;

/**
 * Plots a 2D graph of the current scan into an RCP plot window as the scan progresses.
 * <p>
 * To use this scannable, give it the names of the x,y and z columns and the plot view to send the plot to ("Plot 1" by
 * default. Then simply include in the scan command you wish to plot.
 */
public class TwoDScanPlotter extends ScannableBase implements IScanDataPointObserver {

	private static final Logger logger = LoggerFactory.getLogger(TwoDScanPlotter.class);

	protected DoubleDataset x;
	protected DoubleDataset y;
	protected DoubleDataset intensity;

	private String z_colName; // Currently, this *must* be a detector as this class looks only in that part of the SDP
	private String xAxisName;
	private String yAxisName;
	private String plotViewname = "Plot 1";

	private Double xStart;
	private Double xStop;
	private Double xStep;

	private Double yStart;
	private Double yStop;
	private Double yStep;
	private Long rate = 1200L; // by default refresh all 2D plotter is 1.2 s
	private RateLimiter limiter = RateLimiter.create(1000.0/rate);
	private RCPController rcpController;
	private boolean openPlotViewAtScanStart = false;

	public TwoDScanPlotter() {
		this.inputNames = new String[] {};
		this.extraNames = new String[] {};
		this.outputFormat = new String[] {};
	}

	@Override
	public void atScanStart() throws DeviceException {
		ScanInformation scanInfo = InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation();
		if (scanInfo.getDimensions().length != 2) {
			logger.warn("Not using 2d plotter - scan does not have 2 dimensions");
			return;
		}

		// re-register with datapoint provider
		InterfaceProvider.getScanDataPointProvider().addIScanDataPointObserver(this);
		logger.debug("{} - registering as SDP listener.", getName());
		limiter  = RateLimiter.create(1000.0/rate);
		if (openPlotViewAtScanStart) {
			openPlotView();
		}
	}

	private DoubleDataset createTwoDset(double start, double stop, double step, boolean reverse) {
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
		try {
			// try to plot any points that have been buffered
			plot();
		} catch (Exception e) {
		}
		deregisterAsScanDataPointObserver();
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
				logger.debug("{} - receiving point {} of {}", getName(), currentPoint, totalPoints);
				unpackSDP((ScanDataPoint) arg);
				if (limiter.tryAcquire()) {
					plot();
				}

				if (currentPoint == (totalPoints - 1)) {
					plot(); //plot last points
					logger.debug("{} - last point received; deregistering as SDP listener.", getName());
					deregisterAsScanDataPointObserver();
				}
			} catch (Exception e) {
				// Quietly deregister as this scan does not match what this is looking for.
				// In this way, this object could be added to the list of defaults and only plot related scans.
				logger.warn("Problem updating 2d plot for {} : {}",getName(), e.getMessage());
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
			if (Stream.of(xStart, xStop, xStep).anyMatch(Objects::isNull)) {
				x = createTwoDset(0.0, sdp.getScanDimensions()[0], 1.0, false);
			} else {
				x = createTwoDset(xStart, xStop, xStep, false);
			}

			if (Stream.of(yStart, yStop, yStep).anyMatch(Objects::isNull)) {
				y = createTwoDset(0.0, sdp.getScanDimensions()[1], 1.0, true);
			} else {
				y = createTwoDset(yStart, yStop, yStep, true);
			}

		}

		Double inten = getIntensity(sdp);
		int[] locationInDataSets = getSDPLocation(sdp);
		int xLoc = locationInDataSets[0];
		// y has to be reversed as otherwise things are plotted from the top left, not bottom left as the plotting
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

	private Double getIntensity(ScanDataPoint sdp) throws IndexOutOfBoundsException {
		int dataIndex = getPositionOfDetector(z_colName, sdp);
		if (dataIndex==-1) {
			throw new IndexOutOfBoundsException("Could not find data called '"+z_colName+" in scan results");
		}
		return sdp.getDetectorDataAsDoubles()[dataIndex];
	}

	private int getPositionOfDetector(String columnName, ScanDataPoint sdp) {
		Object[] headers = sdp.getDetectorHeader().toArray();
		return ArrayUtils.indexOf(headers, columnName);
	}

	public void plot() throws Exception {
		if (Stream.of(plotViewname, x, y, intensity).allMatch(Objects::nonNull)) {
			logger.debug("Plotting to RCP client plot named: {}", getPlotViewname());
			if (xAxisName!=null && yAxisName!=null) {
				SDAPlotter.imagePlot(plotViewname, x, y, intensity, xAxisName, yAxisName);
			} else {
				SDAPlotter.imagePlot(plotViewname, x, y, intensity);
			}
		}
	}

	/**
	 * Use RCPController to try and open the plot view
	 */
	public void openPlotView() {
		if (rcpController != null) {
			logger.debug("Making plot view {} visible", plotViewname);
			rcpController.openView(IPlotWindowManager.PLOT_VIEW_MULTIPLE_ID + ":" + plotViewname);
		} else {
			logger.debug("Cannot make plot view {} visible - RCPController has not been set", plotViewname);
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

	public String getXAxisName() {
		return xAxisName;
	}

	public void setXAxisName(String xAxisName) {
		this.xAxisName = xAxisName;
	}

	public String getYAxisName() {
		return yAxisName;
	}

	public void setYAxisName(String yAxisName) {
		this.yAxisName = yAxisName;
	}

	public RCPController getRcpController() {
		return rcpController;
	}

	/**
	 *
	 * @param rcpController {@link RCPController} object used for opening plot view
	 */
	public void setRcpController(RCPController rcpController) {
		this.rcpController = rcpController;
	}

	public boolean isOpenPlotViewAtScanStart() {
		return openPlotViewAtScanStart;
	}

	/**
	 *
	 * @param openPlotViewAtScanStart Set to 'true' to make the plot view open at the start of the scan
	 */
	public void setOpenPlotViewAtScanStart(boolean openPlotViewAtScanStart) {
		this.openPlotViewAtScanStart = openPlotViewAtScanStart;
	}

}
