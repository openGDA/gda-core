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
import gda.factory.FactoryException;
import gda.observable.IObserver;

/**
 * Based on EpicsXmapController2. This is a temporary solution for GDA v8.16 and should not be comitted to thr trunk.
 * <p>
 * Currently the I20 Xmap is running a very old Epics IOC version which has problems with the ROIs. Rather than fixing
 * this old version, the ROI calculations will be done at the GDA level. Once the Epics IOC has been upgraded to the
 * latest version then I20 can switch to EpicsXmapController3 and this clas may be dropped.
 * <p>
 * RJW 5th October 2011
 */
public class EpicsXmapControllerI20 extends EpicsXmapController2 implements XmapController, IObserver {

	int[][][] rois = null; // element, roi index, {min,max}

	@Override
	public void configure() throws FactoryException {
		super.configure();
		rois = new int[1][0][2];
	}

	@Override
	public void setROI(double[][] actualRois, int mcaIndex) throws DeviceException {
		int [][] roiToSet = new int[actualRois.length][2];
		for (int roiIndex = 0; roiIndex < actualRois.length; roiIndex++) {
			int min = (int) Math.round(actualRois[roiIndex][0]);
			int max = (int) Math.round(actualRois[roiIndex][1]);
			roiToSet[roiIndex] = new int[] { min, max };
		}
		rois[mcaIndex] = roiToSet;
	}

	@Override
	public void setROIs(double[][] rois) throws DeviceException {
		for (int element = 0; element < this.rois.length; element++) {
			setROI(rois, element);
		}
	}

	@Override
	public void setNthROI(double[][] rois, int roiIndex) throws DeviceException {
		// here rois = element,{min,max}
		for (int i = 0; i < rois.length; i++) {
			int min = (int) Math.round(rois[i][0]);
			int max = (int) Math.round(rois[i][1]);
			this.rois[i][roiIndex] = new int[]{min,max};
		}
	}

	@Override
	public double[] getROIs(int mcaNumber) throws DeviceException {
		int[][] theROIs = this.rois[mcaNumber];
		int[] data = getData(mcaNumber);

		double[] roiSums = new double[theROIs.length];

		for (int i = 0; i < theROIs.length; i++) {
			roiSums[i] = calcROICounts(theROIs[i][0], theROIs[i][1], data);
		}
		return roiSums;
	}
	
	@Override
	public int getNumberOfROIs() {
		return this.rois[0].length;
	}
	
//	@Override
//	public double[] getROICounts(int roiIndex) throws DeviceException {
//		double[] roiCounts = new double[numberOfMca];
//		for (int j = 0; j < numberOfMca; j++) {
//			double individualMCARois[] = this.getROIs(j);
//			roiCounts[j] = individualMCARois[roiIndex];
//		}
//		return roiCounts;
//	}
	
//	protected double[] getROIs(int mcaNumber) throws DeviceException {
//		return edxdController.getSubDetector(mcaNumber).getROICounts();
//	}

//	/**
//	 * Returns a sum of all rois for each mca channel
//	 */
//	@Override
//	public double[] getROIsSum() throws DeviceException {
//		double[] roiSum = new double[actualNumberOfROIs];
//		for (int j = 0; j < numberOfMca; j++) {
//			double individualMCARois[] = this.getROIs(j);
//			for (int i = 0; i < actualNumberOfROIs; i++) {
//				roiSum[i] = roiSum[i] + individualMCARois[i];
//			}
//		}
//		return roiSum;
//	}
	
	@Override
	public void deleteROIs(int mcaIndex) throws DeviceException {
		// TODO Auto-generated method stub
		
	}


	private double calcROICounts(int min, int max, int[] data) {
		double sum = 0;
		for (int i = min; i <= max; i++) {
			sum += data[i];
		}
		return sum;
	}

}
