/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

import gda.device.Detector;
import gda.device.DeviceBase;
import gda.device.DeviceException;

/**
 * Should set in the XML this class to be always local. There is no corba implementation for it.
 */
public class DummyXmapController extends DeviceBase implements XmapController {
	private int numberOfMca = 4;
	private int numberOfBins = 1024;
	private double acquisitionTime;
	private double readRate = 1.0;
	private double realTime = 1000.0;
	private double statusRate;
	private int actualNumberOfROIs;
	private Long acquisitionStartedTimeMillis = null;

	/**
	 * 
	 */
	public DummyXmapController() {
	}

	@Override
	public void configure(){
		// no configuration required
	}

	@Override
	public void setNumberOfMca(int numberOfMca) {
		this.numberOfMca = numberOfMca;
	}

	@Override
	public void clearAndStart() throws DeviceException {
		startDummyAcquisition();
	}

	@Override
	public double getAcquisitionTime() throws DeviceException {
		return this.acquisitionTime;

	}

	@Override
	public int[] getData(int mcaNumber) throws DeviceException {
		int[] dummyData = new int[numberOfBins];
		Random generator = new Random();
		for (int i = 0; i < numberOfBins; i++) {
			// say that for this simulation cannot count more than 10MHz
			dummyData[i] = generator.nextInt(new Double(realTime).intValue() * 10000);
		}
		return dummyData;
	}

	@Override
	public int getNumberOfBins() throws DeviceException {
		return numberOfBins;
	}

	@Override
	public int getNumberOfMca() throws DeviceException {
		return numberOfMca;
	}

	@Override
	public double getReadRate() throws DeviceException {
		return this.readRate;
	}

	@Override
	public double getRealTime() throws DeviceException {
		return this.realTime;
	}

	@Override
	public int getStatus() throws DeviceException {
		return isDummyAcquisitionDone() ? Detector.IDLE : Detector.BUSY;
	}

	@Override
	public double getStatusRate() throws DeviceException {
		return statusRate;
	}

	@Override
	public void setAcquisitionTime(double time) throws DeviceException {
		this.acquisitionTime = time;

	}

	@Override
	public void setNumberOfBins(int numberOfBins) throws DeviceException {
		this.numberOfBins = numberOfBins;

	}

	@Override
	public void setReadRate(double readRate) throws DeviceException {
		this.readRate = readRate;

	}

	@Override
	public void setReadRate(String readRate) throws DeviceException {
		// Do nothing

	}

	@Override
	public void setStatusRate(double statusRate) throws DeviceException {
		this.statusRate = statusRate;

	}

	@Override
	public void setStatusRate(String statusRate) throws DeviceException {
		// Do nothing

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
	public int[][] getData() throws DeviceException {
		int[][] data = new int[numberOfMca][numberOfBins];
		for (int i = 0; i < numberOfMca; i++) {
			data[i] = getData(i);
		}
		return data;
	}

	@Override
	public int getNumberOfROIs() {
		return actualNumberOfROIs;
	}

	@Override
	public double[] getROICounts(int roiIndex) throws DeviceException {
		double[] roiCounts = new double[numberOfMca];
		Random generator = new Random();
		for (int k = 0; k < numberOfMca; k++) {
			roiCounts[k] = generator.nextInt(new Double(realTime).intValue() * 10000);
		}
		return roiCounts;
	}

	@Override
	public double[] getROIs(int mcaNumber) throws DeviceException {
		double[] roiSum = new double[actualNumberOfROIs];

		Random generator = new Random();
		for (int k = 0; k < actualNumberOfROIs; k++) {
			roiSum[k] = generator.nextInt(new Double(realTime).intValue() * 10000);
		}
		return roiSum;
	}
	
	@Override
	public double[] getROIsSum() throws DeviceException {
		double[] roiSum = new double[actualNumberOfROIs];

		Random generator = new Random();
		for (int k = 0; k < actualNumberOfROIs; k++) {
			roiSum[k] = generator.nextInt(new Double(realTime).intValue() * 10000);
		}
		return roiSum;

	}

	@Override
	public void setNthROI(double[][] rois, int roiIndex) throws DeviceException {
//		actualNumberOfROIs = roiIndex + 1;
		return;
	}

	@Override
	public void setNumberOfROIs(int numberOfROIs) {
		actualNumberOfROIs = numberOfROIs;
	}

	@Override
	public void setROI(double[][] rois, int mcaIndex) throws DeviceException {
		actualNumberOfROIs += rois.length;
		return;

	}

	@Override
	public void setROIs(double[][] rois) throws DeviceException {
		actualNumberOfROIs = 0;
		for (int i = 0; i < numberOfMca; i++) {
			setROI(rois, i);
		}

	}

	@Override
	public void deleteROIs(int mcaIndex) throws DeviceException {
		actualNumberOfROIs = 0;
		return;
		
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

	private void startDummyAcquisition() {
		acquisitionStartedTimeMillis = System.currentTimeMillis();
	}
	
	private void stopDummyAcquisition() {
		acquisitionStartedTimeMillis = null;
	}
	
	private boolean isDummyAcquisitionDone() {
		if (acquisitionStartedTimeMillis==null)
			return true;
		return System.currentTimeMillis() >= acquisitionStartedTimeMillis + acquisitionTime*1000;
	}

	@Override
	public double[] getROIs(int mcaNumber, int[][] data) throws DeviceException {
		return getROIs(mcaNumber);
	}

}
