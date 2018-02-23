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

package gda.device.detector.xmap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.device.detector.xmap.edxd.EDXDMappingController;
import gda.device.detector.xmap.edxd.IEDXDElement;
import gda.factory.FactoryException;

public class EpicsXmapController extends DeviceBase implements XmapController {

	private static final Logger logger = LoggerFactory.getLogger(EpicsXmapController.class);

	protected EDXDMappingController edxdController;
	protected int numberOfElements;
	protected int actualNumberOfROIs;

	@Override
	public void configure() throws FactoryException {
		if (edxdController == null) {
			throw new FactoryException(String.format("EDXD controller not set in %s", getName()));
		}
		numberOfElements = edxdController.getNumberOfElements();
	}

	@Override
	public void clearAndStart() throws DeviceException {
		edxdController.setResume(false);
		edxdController.start();
	}

	@Override
	public void deleteROIs(int mcaIndex) throws DeviceException {
		throw new UnsupportedOperationException("Cannot delete ROIs");
	}

	@Override
	public double getAcquisitionTime() throws DeviceException {
		return edxdController.getAcquisitionTime();
	}

	@Override
	public int[] getData(int mcaNumber) throws DeviceException {
		final int numberOfBins = getNumberOfBins();
		final int[] returnArray = new int[numberOfBins];
		final int[] replyArray = edxdController.getSubDetector(mcaNumber).readoutInts();
		System.arraycopy(replyArray, 0, returnArray, 0, numberOfBins);
		return returnArray;
	}

	@Override
	public int[][] getData() throws DeviceException {
		// should write data to a file
		// bespoke scan scripts write data at the moment
		final int numberOfBins = getNumberOfBins();
		final int[][] data = new int[numberOfElements][numberOfBins];
		for (int i = 0; i < numberOfElements; i++) {
			data[i] = getData(i);
		}
		return data;
	}

	@Override
	public int getNumberOfBins() throws DeviceException {
		return edxdController.getBins();
	}

	@Override
	public void setNumberOfBins(int numberOfBins) throws DeviceException {
		edxdController.setBins(numberOfBins);

	}

	@Override
	public int getNumberOfElements() throws DeviceException {
		return numberOfElements;
	}

	/**
	 * Returns the roi count if they have been set, otherwise reads the total possible count from EPICS
	 */
	@Override
	public int getNumberOfROIs() {
		if (actualNumberOfROIs > 0)
			return actualNumberOfROIs;
		try {
			return edxdController.getMaxAllowedROIs();
		} catch (DeviceException e) {
			logger.error("Unable to read the max allowed ROIs from the detector", e);
		}
		return 0;
	}

	/**
	 * Returns a count for each mca for a given roi number For instance if roi=0 the first roi
	 */
	@Override
	public double[] getROICounts(int roiIndex) throws DeviceException {
		final double[] roiCounts = new double[numberOfElements];
		for (int j = 0; j < numberOfElements; j++) {
			final double individualMCARois[] = this.getROIs(j);
			roiCounts[j] = individualMCARois[roiIndex];
		}
		return roiCounts;
	}

	/**
	 * @param mcaNumber
	 * @return double array of regions of interest
	 * @throws DeviceException
	 */
	@Override
	public double[] getROIs(int mcaNumber) throws DeviceException {
		return edxdController.getSubDetector(mcaNumber).getROICounts();
	}

	/**
	 * Returns a sum of all rois for each mca channel
	 */
	@Override
	public double[] getROIsSum() throws DeviceException {
		final double[] roiSum = new double[actualNumberOfROIs];
		for (int j = 0; j < numberOfElements; j++) {
			final double individualMCARois[] = this.getROIs(j);
			for (int i = 0; i < actualNumberOfROIs; i++) {
				roiSum[i] = roiSum[i] + individualMCARois[i];
			}
		}
		return roiSum;
	}

	@Override
	public double getReadRate() throws DeviceException {
		throw new UnsupportedOperationException("Cannot get read rate");
	}

	@Override
	public double getRealTime() throws DeviceException {
		return edxdController.getSubDetector(0).getRealTime();
	}

	@Override
	public int getStatus() throws DeviceException {
		return edxdController.getStatus();
	}

	@Override
	public double getStatusRate() throws DeviceException {
		throw new UnsupportedOperationException("Cannot get status rate");
	}

	@Override
	public void setAcquisitionTime(double collectionTime) throws DeviceException {
		edxdController.setAquisitionTime(collectionTime);
	}

	@Override
	public void setNthROI(double[][] rois, int roiIndex) throws DeviceException {
		if (rois.length != numberOfElements) {
			logger.error("ROIs length does not match the Number of MCA");
			return;
		}
		for (int mcaIndex = 0; mcaIndex < numberOfElements; mcaIndex++) {
			this.setNthROI(rois[mcaIndex], roiIndex, mcaIndex);
		}
		actualNumberOfROIs = roiIndex + 1;
	}

	private void setNthROI(double[] roi, int roiIndex, int mcaIndex) throws DeviceException {
		if (roiIndex >= edxdController.getMaxAllowedROIs()) {
			logger.error("Not a valid roi index");
			return;
		}
		final IEDXDElement element = edxdController.getSubDetector(mcaIndex);
		final double[] roiLow = element.getLowROIs();
		final double[] roiHigh = element.getHighROIs();
		if (roi[0] <= roi[1]) {
			roiLow[roiIndex] = roi[0];
			roiHigh[roiIndex] = roi[1];
		} else {
			roiLow[roiIndex] = roi[1];
			roiHigh[roiIndex] = roi[0];
		}
		element.setLowROIs(roiLow);
		element.setHighROIs(roiHigh);
		edxdController.activateROI();
	}

	@Override
	public void setNumberOfElements(int numberOfElements) throws DeviceException {
		throw new UnsupportedOperationException("Cannot set number of elements");
	}

	@Override
	public void setNumberOfROIs(int numberOfROIs) {
		throw new UnsupportedOperationException("Cannot set number of ROIs");
	}

	/**
	 * Set rois the array can be of size [maximum number rois][2] if it is lower for instance [actual number of rois][2]
	 * then the other possible rois will be set to zero. The actual number of rois is also taken from the length of the
	 * first dimension of this array so it should always be passed in with size of the actual number of rois.
	 */
	@Override
	public void setROI(final double[][] actualRois, int mcaIndex) throws DeviceException {
		// The ROIS might not be scaled to the max ROI size, so we ensure that this has been done
		final double[][] rois = new double[edxdController.getMaxAllowedROIs()][2];
		for (int i = 0; i < actualRois.length; i++) {
			rois[i][0] = actualRois[i][0];
			rois[i][1] = actualRois[i][1];
		}
		final IEDXDElement subDetector = edxdController.getSubDetector(mcaIndex);
		if (subDetector != null) {
			subDetector.setROIs(rois);
		}
		edxdController.activateROI();
		actualNumberOfROIs = actualRois.length;
	}

	@Override
	public void setROIs(double[][] rois) throws DeviceException {
		for (int i = 0; i < numberOfElements; i++)
			setROI(rois, i);
	}

	@Override
	public void setReadRate(double readRate) throws DeviceException {
		throw new UnsupportedOperationException("Cannot set read rate");
	}

	@Override
	public void setReadRate(String readRate) throws DeviceException {
		throw new UnsupportedOperationException("Cannot set read rate");
	}

	@Override
	public void setStatusRate(double statusRate) throws DeviceException {
		throw new UnsupportedOperationException("Cannot set status rate");
	}

	@Override
	public void setStatusRate(String statusRate) throws DeviceException {
		throw new UnsupportedOperationException("Cannot set status rate");
	}

	@Override
	public void start() throws DeviceException {
		edxdController.start();
	}

	@Override
	public void stop() throws DeviceException {
		edxdController.stop();
	}

	/**
	 * Returns the total events recorded
	 *
	 * @param mcaNumber
	 * @throws DeviceException
	 */
	@Override
	public int getEvents(final int mcaNumber) throws DeviceException {
		return edxdController.getEvents(mcaNumber);
	}

	@Override
	public double getICR(int mcaNumber) throws DeviceException {
		return edxdController.getICR(mcaNumber);
	}

	@Override
	public double getOCR(int mcaNumber) throws DeviceException {
		return edxdController.getOCR(mcaNumber);
	}

	@Override
	public double[] getROIs(int mcaNumber, int[][] data) throws DeviceException {
		return getROIs(mcaNumber);
	}

	// This should really be called getROIs, as it is the reverse of setROIs
	@Override
	public double[][] getROIParameters(int mcaIndex) throws DeviceException {
		final IEDXDElement element = edxdController.getSubDetector(mcaIndex);
		if (element == null) {
			throw new IndexOutOfBoundsException("No subelement for index " + mcaIndex + " exists.");
		}
		final double[] lows = element.getLowROIs();
		final double[] highs = element.getHighROIs();
		final int length = Math.min(lows.length, highs.length); // these should never be different
		final double[][] rois = new double[length][2];
		for (int i = 0; i < length; i++) {
			rois[i][0] = lows[i];
			rois[i][1] = highs[i];
		}
		return rois;
	}

	public EDXDMappingController getEdxdController() {
		return edxdController;
	}

	public void setEdxdController(EDXDMappingController edxdController) {
		this.edxdController = edxdController;
	}

	public double[] getEnergyBins(int mcaNumber) throws DeviceException {
		return edxdController.getSubDetector(mcaNumber).getEnergyBins();
	}

	public double[][] getEnergyBins() throws DeviceException {
		final int numberOfBins = getNumberOfBins();
		final double[][] data = new double[numberOfElements][numberOfBins];
		for (int i = 0; i < numberOfElements; i++)
			data[i] = getEnergyBins(i);
		return data;
	}

}