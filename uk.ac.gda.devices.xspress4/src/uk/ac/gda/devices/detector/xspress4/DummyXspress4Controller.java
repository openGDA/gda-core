/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.detector.xspress4;

import java.util.Arrays;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import gda.device.DeviceException;
import gda.factory.FindableBase;

public class DummyXspress4Controller extends FindableBase implements Xspress4Controller, InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(DummyXspress4Controller.class);

	private int numElements = 64;
	private int numScalers = 8;
	private int numMcaChannels = 128;
	private int numResGrades = 16;

	private double deadtimeCorrectionEnergy = 0;
	private int triggerMode = 0;
	private boolean saveResGradeData = false;
	private int totalNumFramesAvailable = 0;
	private int timeSeriesNumPoints = 0;

	@Override
	public void afterPropertiesSet() throws Exception {
	}

	@Override
	public double getScalerValue(int element, int scalerNumber) {
		return scalerNumber + element*100.0;
	}

	@Override
	public double[] getScalerArray(int element) {
		double[] vals = new double[numScalers];
		IntStream.range(0,numScalers).forEach(scaler -> vals[scaler] = getScalerValue(element, scaler));
		return vals;
	}

	@Override
	public double[][] getScalerTimeseries(int element, int startFrame, int finalFrame) {
		double[][] values = new double[numScalers][];
		int numValues = finalFrame - startFrame + 1;
		for(int i=0; i<numScalers; i++) {
			double[] vals = new double[numValues];
			Arrays.fill(vals, getScalerValue(element, i));
			values[i] = vals;
		}
		return values;
	}

	@Override
	public void startTimeSeries() throws DeviceException {
		logger.debug("startTimeSeries called");
	}

	@Override
	public void stopTimeSeries() throws DeviceException {
		logger.debug("stopTimeSeries called");
	}

	@Override
	public int getTimeSeriesNumPoints() throws DeviceException {
		return timeSeriesNumPoints;
	}

	public void setTimeSeriesNumPoints(int timeSeriesNumPoints) throws DeviceException {
		this.timeSeriesNumPoints = timeSeriesNumPoints;
	}

	@Override
	public double[] getResGradeArrays(int element, int window) {
		double[] vals = new double[numResGrades];
		IntStream.range(0,numResGrades).forEach(index -> {
			// In window counts weighted by gaussian function
			double weight = Math.exp( -Math.pow((double)(index-numResGrades)/numResGrades, 2.0) );
			vals[index] = weight * getScalerValue(element, 5);
		});
		return vals;
	}

	@Override
	public double[] getMcaData(int element) {
		double[] vals = new double[numMcaChannels];
		IntStream.range(0, numMcaChannels).forEach(channel -> vals[channel] = (double) element * numMcaChannels + channel);
		return vals;
	}

	@Override
	public double[][] getMcaData() throws DeviceException {
		double[][] vals = new double[numElements][];
		IntStream.range(0,numElements).forEach(element -> vals[element] = getMcaData(element));
		return vals;
	}

	@Override
	public double[] getDeadtimeCorrectionFactors() throws DeviceException {
		double[] vals = new double[numElements];
		IntStream.range(0, numElements).forEach(element -> vals[element] = 1 + 0.001*element);
		return vals;
	}

	@Override
	public boolean setSaveResolutionGradeData(boolean saveResGradeData) throws DeviceException {
		this.saveResGradeData = saveResGradeData;
		return true;
	}

	@Override
	public void setDeadtimeCorrectionEnergy(double energyKev) throws DeviceException {
		deadtimeCorrectionEnergy = energyKev;
	}

	@Override
	public double getDeadtimeCorrectionEnergy() throws DeviceException {
		return deadtimeCorrectionEnergy;
	}

	@Override
	public void resetFramesReadOut() throws DeviceException {
	}

	@Override
	public int getTotalFramesAvailable() throws DeviceException {
		return totalNumFramesAvailable;
	}

	/**
	 * Set total number of frames available to readout (ArrayCounter_RBV)
	 * @param totalNumFramesAvailable
	 */
	public void setTotalNumFramesAvailable(int totalNumFramesAvailable) {
		this.totalNumFramesAvailable = totalNumFramesAvailable;
	}

	@Override
	public void setAcquireTime(double time) throws DeviceException {
	}

	@Override
	public void setTriggerMode(int triggerMode) throws DeviceException {
		this.triggerMode = triggerMode;
	}

	@Override
	public int getTriggerMode() throws DeviceException {
		return triggerMode;
	}

	@Override
	public int getNumElements() {
		return numElements;
	}

	@Override
	public void setNumElements(int numElements) {
		this.numElements = numElements;
	}

	@Override
	public int getNumScalers() {
		return numScalers;
	}

	@Override
	public void setNumScalers(int numScalers) {
		this.numScalers = numScalers;
	}

	@Override
	public void waitForCounterToIncrement(int currentCount, long timeoutMillis)
			throws DeviceException, InterruptedException {
		//return immediately
	}

	public int getNumResGrades() {
		return numResGrades;
	}

	public void setNumResGrades(int numResGrades) {
		this.numResGrades = numResGrades;
	}

	@Override
	public int getNumMcaChannels() {
		return numMcaChannels;
	}

	@Override
	public void setNumMcaChannels(int numMcaChannels) {
		this.numMcaChannels = numMcaChannels;
	}

}
