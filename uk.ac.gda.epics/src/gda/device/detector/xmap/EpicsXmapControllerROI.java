/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

import gda.device.DeviceException;
import gda.device.detector.xmap.edxd.EDXDMappingController;
import gda.factory.FactoryException;
import gda.factory.Finder;

public class EpicsXmapControllerROI extends EpicsXmapController{
	private EDXDMappingController edxdController;
	private int numberOfElements;
	private String edxdControllerName;
	private double[][][] controlRois;
	
	@Override
	public void configure() throws FactoryException {
		if((edxdController = (EDXDMappingController)Finder.getInstance().find(edxdControllerName) )!= null){
			numberOfElements = edxdController.getNumberOfElements();
			edxdController.addIObserver(this);
			controlRois = new double[numberOfElements][][];
		}
	}

	@Override
	public void setNumberOfElements(int numberOfElements){
		this.numberOfElements = numberOfElements;
		edxdController.setNumberOfElements(numberOfElements);
	}
	
	@Override
	public void deleteROIs(int mcaIndex) throws DeviceException {
		controlRois[mcaIndex] = null;
	}

	/**
	 * @param mcaNumber
	 * @return double array of regions of interest
	 * @throws DeviceException
	 */
	@Override
	public double[] getROIs(int mcaNumber) throws DeviceException {
		int[] mcaData = getData(mcaNumber);
		double[] roiSums = new double[controlRois[mcaNumber].length];
		for (int i = 0; i < controlRois[mcaNumber].length; i++)
			roiSums[i] = calcROICounts((int)controlRois[mcaNumber][i][0], (int)controlRois[mcaNumber][i][1], mcaData);
		return roiSums;
	}
	
	@Override
	public double[] getROIs(int mcaNumber, int[][] data) throws DeviceException {
		int[] mcaData = data[mcaNumber];
		double[] roiSums = new double[controlRois[mcaNumber].length];
		for (int i = 0; i < controlRois[mcaNumber].length; i++) {
			int min = (int)controlRois[mcaNumber][i][0];
			int max = (int)controlRois[mcaNumber][i][1];
			roiSums[i] = calcROICounts(min, max, mcaData);
		}
		return roiSums;
	}
	
	private double calcROICounts(int min, int max, int[] data) {
		double sum = 0;
		for (int i = min; i <= max; i++)
			sum += data[i];
		return sum;
	}

	/**
	 * Set rois the array can be of size [maximum number rois][2] if it is lower for instance
	 * [actual number of rois][2] then the other possible rois will be set to zero.
	 * 
	 * The actual number of rois is also taken from the length of the first dimension of this array
	 * so it should always be passed in with size of the actual number of rois.
	 */
	@Override
	public void setROI(final double[][] actualRois, int mcaIndex) throws DeviceException {
		// The ROIS might not be scaled to the max ROI size, so we ensure that this has been done
		final double[][] rois = new double[edxdController.getMaxAllowedROIs()][2];
		for (int i = 0; i < actualRois.length; i++) {
			rois[i][0] = actualRois[i][0];
			rois[i][1] = actualRois[i][1];
		}
		controlRois[mcaIndex] = rois;
		edxdController.getSubDetector(mcaIndex).setROIs(rois);
		edxdController.activateROI();
		actualNumberOfROIs = actualRois.length;
	}
}