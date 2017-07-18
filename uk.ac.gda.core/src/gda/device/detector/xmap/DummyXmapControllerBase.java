/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package gda.device.detector.xmap;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Detector;
import gda.device.DeviceBase;
import gda.device.DeviceException;

public abstract class DummyXmapControllerBase extends DeviceBase implements XmapController {

	private static final Logger logger = LoggerFactory.getLogger(DummyXmapControllerBase.class);

	private int numberOfElements = 4;
	private int numberOfBins = 1024;
	private double acquisitionTime;
	private double readRate = 1.0;
	private double realTime = 1000.0;
	private double statusRate;
	private int numberOfROIs;
	private Long acquisitionStartedTimeMillis = null;

	//---------------------------------------------------------------------
	// Device control
	//---------------------------------------------------------------------
	@Override
	public void clearAndStart() throws DeviceException {
		startDummyAcquisition();
	}

	@Override
	public int getStatus() throws DeviceException {
		return isDummyAcquisitionDone() ? Detector.IDLE : Detector.BUSY;
	}

	@Override
	public void start() throws DeviceException {
		startDummyAcquisition();
	}

	@Override
	public void stop() throws DeviceException {
		stopDummyAcquisition();
	}

	@Override
	public void setNthROI(double[][] rois, int roiIndex) throws DeviceException {
		// Nothing to do
	}

	@Override
	public void setROI(double[][] rois, int mcaIndex) throws DeviceException {
		numberOfROIs += rois.length;
	}

	@Override
	public void setROIs(double[][] rois) throws DeviceException {
		numberOfROIs = 0;
		for (int i = 0; i < numberOfElements; i++)
			setROI(rois, i);
	}

	@Override
	public double[][] getROIParameters(int mcaNumber) throws DeviceException {
		return new double[][] {};
	}

	@Override
	public void deleteROIs(int mcaIndex) throws DeviceException {
		numberOfROIs = 0;
	}

	protected void startDummyAcquisition() {
		acquisitionStartedTimeMillis = System.currentTimeMillis();
	}

	protected void stopDummyAcquisition() {
		acquisitionStartedTimeMillis = null;
	}

	private boolean isDummyAcquisitionDone() {
		if (acquisitionStartedTimeMillis==null)
			return true;
		return System.currentTimeMillis() >= acquisitionStartedTimeMillis + acquisitionTime*1000;
	}

	//---------------------------------------------------------------------
	// Data
	//---------------------------------------------------------------------

	@Override
	public double[] getROICounts(int roiIndex) throws DeviceException {
		double[] roiCounts = new double[numberOfElements];
		Random generator = new Random();
		for (int k = 0; k < numberOfElements; k++)
			roiCounts[k] = generator.nextInt((int) (realTime) * 10000);
		return roiCounts;
	}

	@Override
	public double[] getROIs(int mcaNumber) throws DeviceException {
		double[] roiSum = new double[numberOfROIs];
		Random generator = new Random();
		for (int k = 0; k < numberOfROIs; k++)
			roiSum[k] = generator.nextInt((int) (realTime) * 10000);
		return roiSum;
	}

	@Override
	public double[] getROIsSum() throws DeviceException {
		double[] roiSum = new double[numberOfROIs];
		Random generator = new Random();
		for (int k = 0; k < numberOfROIs; k++)
			roiSum[k] = generator.nextInt((int) (realTime) * 10000);
		return roiSum;
	}

	@Override
	public int getEvents(int element) throws DeviceException {
		return -1000;
	}

	@Override
	public double getICR(int element) throws DeviceException {
		return Math.random()*100d;
	}

	@Override
	public double getOCR(int element) throws DeviceException {
		return Math.random()*100d;
	}

	@Override
	public double[] getROIs(int mcaNumber, int[][] data) throws DeviceException {
		return getROIs(mcaNumber);
	}

	//--------------------------------------------------------------
	// Data member getters & setters
	//--------------------------------------------------------------

	@Override
	public int getNumberOfElements() {
		return numberOfElements;
	}

	@Override
	public void setNumberOfElements(int numberOfElements) {
		this.numberOfElements = numberOfElements;
	}

	@Override
	public int getNumberOfBins() {
		return numberOfBins;
	}

	@Override
	public void setNumberOfBins(int numberOfBins) {
		this.numberOfBins = numberOfBins;
	}

	@Override
	public double getAcquisitionTime() {
		return acquisitionTime;
	}

	@Override
	public void setAcquisitionTime(double acquisitionTime) {
		this.acquisitionTime = acquisitionTime;
	}

	@Override
	public double getReadRate() {
		return readRate;
	}

	@Override
	public void setReadRate(double readRate) {
		this.readRate = readRate;
	}

	@Override
	public void setReadRate(String readRate) throws DeviceException {
		try {
			this.readRate = Double.parseDouble(readRate);
		} catch (Exception e) {
			final String message = "Error setting read rate";
			logger.error(message, e);
			throw new DeviceException(message, e);
		}
	}

	@Override
	public double getStatusRate() {
		return statusRate;
	}

	@Override
	public void setStatusRate(double statusRate) {
		this.statusRate = statusRate;
	}

	@Override
	public void setStatusRate(String statusRate) throws DeviceException {
		try {
			this.statusRate = Double.parseDouble(statusRate);
		} catch (Exception e) {
			final String message = "Error setting status rate";
			logger.error(message, e);
			throw new DeviceException(message, e);
		}
	}

	@Override
	public int getNumberOfROIs() {
		return numberOfROIs;
	}

	@Override
	public void setNumberOfROIs(int numberOfROIs) {
		this.numberOfROIs = numberOfROIs;
	}

	@Override
	public double getRealTime() {
		return realTime;
	}

	@Override
	public String toString() {
		return "DummyXmapControllerBase [numberOfElements=" + numberOfElements + ", numberOfBins=" + numberOfBins
				+ ", acquisitionTime=" + acquisitionTime + ", readRate=" + readRate + ", realTime=" + realTime
				+ ", statusRate=" + statusRate + ", numberOfROIs=" + numberOfROIs + ", acquisitionStartedTimeMillis="
				+ acquisitionStartedTimeMillis + "]";
	}
}