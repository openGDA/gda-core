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

import gda.device.DeviceException;
import gda.jython.IAllScanDataPointsObserver;
import gda.jython.IScanDataPointProvider;
import gda.jython.InterfaceProvider;
import gda.scan.ScanDataPoint;

import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.SDAPlotter;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;

/**
 * Plots a 2D graph of the current scan into an RCP plot window as the scan progresses.
 * <p>
 * To use this scannable, give it the names of the x,y and z columns and the plot view to send the plot to ("Plot 1" by
 * default. Then simply include in the scan command you wish to plot.
 */
public class TwoDScanPlotter extends ScannableBase implements IAllScanDataPointsObserver {

	private static final Logger logger = LoggerFactory.getLogger(TwoDScanPlotter.class);

	private DoubleDataset x;
	private DoubleDataset y;
	private DoubleDataset intensity;

	private String x_colName;
	private String y_colName;
	private String z_colName;
	private String plotViewname = "Plot 1";

	private int pointCounter = 0;

	private Double xStart;
	private Double xStop;
	private Double xStep;
	private Double yStart;
	private Double yStop;
	private Double yStep;

	private Vector<Double> xPoints;
	private Vector<Double> yPoints;

	public TwoDScanPlotter() {
		this.inputNames = new String[] {};
		this.extraNames = new String[] {};
		this.outputFormat = new String[] {};
	}

	@Override
	public void atScanStart() throws DeviceException {
		// clear datasets and re-register with datapoint provider
		x = null;
		y = null;
		intensity = null;
		pointCounter = 0;
		InterfaceProvider.getScanDataPointProvider().addIScanDataPointObserver(this);
	}

	@Override
	public void atScanEnd() throws DeviceException {
		InterfaceProvider.getScanDataPointProvider().deleteIScanDataPointObserver(this);
	}

	@Override
	public void update(Object source, Object arg) {
		if (source instanceof IScanDataPointProvider && arg instanceof ScanDataPoint) {
			try {
				unpackSDP((ScanDataPoint) arg);
				plot();
			} catch (Exception e) {
				logger.warn("exception while plotting 2D data: " + e.getMessage(), e);
			} finally {
				pointCounter++;
			}
		}

	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		return null;
	}

	private void unpackSDP(ScanDataPoint sdp) {
		if (intensity == null) {
			intensity = new DoubleDataset(sdp.getScanDimensions()[0], sdp.getScanDimensions()[1]);
			intensity.setName(getZ_colName());
			int scanSize = sdp.getScanDimensions()[0] * sdp.getScanDimensions()[1];
			x = new DoubleDataset(scanSize);
			x.setName(getX_colName());
			y = new DoubleDataset(scanSize);
			y.setName(getY_colName());

			// pre-populate x and y if we can to improve how the plotting works.
			if (haveAllScanLimits()) {
//				xPoints = new Vector<Double>();
				int xIndex = 0;
				for (int i = 0; i < sdp.getScanDimensions()[0]; i++) {
					Double currentPoint = xStart;
					do {
//						xPoints.add(currentPoint);
						x.set(currentPoint, xIndex);
						xIndex++;
						currentPoint += xStep;
					} while (currentPoint < xStop);
//					xPoints.add(xStop);
					x.set(xStop, xIndex);

				}

//				yPoints = new Vector<Double>();
				int yIndex = 0;
				for (int i = 0; i < sdp.getScanDimensions()[1]; i++) {
					Double currentPoint = yStart;
					do  {
//						yPoints.add(currentPoint);
						y.set(currentPoint, yIndex);
						yIndex++;
						currentPoint += yStep;
					} while (currentPoint < yStop);
//					yPoints.add(yStop);
					y.set(currentPoint, yIndex);
					yIndex++;
				}
			}
		}

		int[] locationInDataSets = getSDPLocation(sdp);

		Double thisX = getE0(sdp);
		Double thisY = getEf(sdp);
		Double inten = getIntensity(sdp);
		int xLoc = locationInDataSets[0];
		int yLoc = locationInDataSets[1];

//		x.set(thisX, pointCounter);
//		y.set(thisY, pointCounter);

		intensity.set(inten, xLoc, yLoc);
	}

	private boolean haveAllScanLimits() {
		return xStart != null & xStop != null && xStep != null && yStart != null && yStop != null && yStep != null;
	}

	private int[] getSDPLocation(ScanDataPoint sdp) {
		int xLoc = 0;
		int yLoc = sdp.getCurrentPointNumber();

		if (sdp.getCurrentPointNumber() >= sdp.getScanDimensions()[1]) {
			xLoc = sdp.getCurrentPointNumber() / sdp.getScanDimensions()[1];
			yLoc = sdp.getCurrentPointNumber() - (xLoc * sdp.getScanDimensions()[1]);
		}

		return new int[] { xLoc, yLoc };
	}

	private Double getE0(ScanDataPoint sdp) {
		return (Double) sdp.getScannablePositions().get(getPositionOfScannable(x_colName, sdp));
	}

	private Double getEf(ScanDataPoint sdp) {
		return (Double) sdp.getScannablePositions().get(getPositionOfScannable(y_colName, sdp));
	}

	private Double getIntensity(ScanDataPoint sdp) {
		return sdp.getDetectorDataAsDoubles()[getPositionOfDetector(z_colName, sdp)];
	}

	private int getPositionOfScannable(String columnName, ScanDataPoint sdp) {
		return org.apache.commons.lang.ArrayUtils.indexOf(sdp.getScannableHeader(), columnName);
	}

	private int getPositionOfDetector(String columnName, ScanDataPoint sdp) {
		Object[] headers = sdp.getDetectorHeader().toArray();
		return org.apache.commons.lang.ArrayUtils.indexOf(headers, columnName);
	}

	public void plot() throws Exception {
		// SDAPlotter.surfacePlot(plotViewname, x, y, intensity);
		SDAPlotter.imagePlot(plotViewname, x, y, intensity);
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return false;
	}

	public String getX_colName() {
		return x_colName;
	}

	public void setX_colName(String xColName) {
		x_colName = xColName;
	}

	public String getY_colName() {
		return y_colName;
	}

	public void setY_colName(String yColName) {
		y_colName = yColName;
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

	public void setxStart(Double xStart) {
		this.xStart = xStart;
	}

	public void setxStop(Double xStop) {
		this.xStop = xStop;
	}

	public void setxStep(Double xStep) {
		this.xStep = xStep;
	}

	public void setyStart(Double yStart) {
		this.yStart = yStart;
	}

	public void setyStop(Double yStop) {
		this.yStop = yStop;
	}

	public void setyStep(Double yStep) {
		this.yStep = yStep;
	}
}
