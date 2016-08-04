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

package gda.device.detector.xmap.edxd;

import gda.data.nexus.INeXusInfoWriteable;
import gda.device.DeviceException;
import gda.device.detector.analyser.EpicsMCARegionOfInterest;
import gda.device.detector.analyser.EpicsMCASimple;
import gda.device.epicsdevice.FindableEpicsDevice;
import gda.factory.FactoryException;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Each EDXDMappingElement wraps an EpicsMCASimple instance.
 */
public class EDXDMappingElement extends EDXDElement implements INeXusInfoWriteable {
	private static final String MCA = "MCA";
	private int maxROIs = 32;
	private EpicsMCASimple simpleMca;
	transient private static final Logger logger = LoggerFactory.getLogger(EDXDMappingElement.class);

	/**
	 * @param xmapDevice the device where the element is connected to
	 * @param elementNumber the number of the element in the xmap
	 */
	public EDXDMappingElement(FindableEpicsDevice xmapDevice, int elementNumber ) {
		super(xmapDevice, elementNumber);
		simpleMca = new EpicsMCASimple();
		simpleMca.setName(MCA + elementNumber);
		simpleMca.setMcaPV(xmap.getRecordPVs().get(MCA + elementNumber));
		try {
			simpleMca.configure();
		} catch (FactoryException e) {
			logger.error("Exception configuring the ROI mca s in Xmap" , e);
			e.printStackTrace();
		}
	}

	/**
	 * Set rois the array must be of size [maximum number rois][2]
	 * @param rois
	 * @throws DeviceException
	 */
	@Override
	public void setROIs(double[][] rois) throws DeviceException {
		double [] roiLow  = getLowROIs();
		mergeRois(roiLow, rois, 0);
		setLowROIs(roiLow);
		double [] roiHigh = getHighROIs();
		mergeRois(roiHigh, rois, 1);
		setHighROIs(roiHigh);
		final double[] curLow = getLowROIs();
		if (!Arrays.equals(roiLow, curLow))
			throw new DeviceException("Did not set low rois!");
		final double[] curHi = getHighROIs();
		if (!Arrays.equals(curHi, roiHigh))
			throw new DeviceException("Did not set high rois!");
	}

	private void mergeRois(double[] curRois, double[][] rois, int i) {
		for (int j = 0; j < curRois.length; j++)
			curRois[j] = rois[j][i];
	}

	/**
	 * Sets the ROI low limit
	 * @param roiLow
	 * @throws DeviceException
	 */
	@Override
	public void setLowROIs(double[] roiLow) throws DeviceException{
		if(simpleMca.isConfigured()){
			if(roiLow.length > maxROIs)
				throw new DeviceException("Number of ROI's is larger than the maximum allowed value");
			EpicsMCARegionOfInterest[] roiLowObject = new EpicsMCARegionOfInterest[roiLow.length];
			for(int i =0 ; i < roiLow.length ; i++){
				EpicsMCARegionOfInterest roi = simpleMca.getNthRegionOfInterest(i);
				roi.setRegionLow(roiLow[i]);
				roiLowObject[i] = roi;
			}
			simpleMca.setRegionsOfInterest(roiLowObject);
		}
	}

	/**
	 * get the roi low limit
	 * @return roi low limit array
	 * @throws DeviceException
	 */
	@Override
	public double[] getLowROIs() throws DeviceException{
		if(simpleMca.isConfigured()){
			double[] lowRois = new double[maxROIs];
			EpicsMCARegionOfInterest[] roiObject = (EpicsMCARegionOfInterest[])simpleMca.getRegionsOfInterest();
			for(int i =0 ; i< roiObject.length; i++){
				lowRois[i] = roiObject[i].getRegionLow();
			}
			return lowRois;
		}
		return null;
	}

	/**
	 * Set the roi high limit
	 * @param roiHigh
	 * @throws DeviceException
	 */
	@Override
	public void setHighROIs(double[] roiHigh) throws DeviceException{
		if(roiHigh.length > maxROIs)
		throw new DeviceException("Number of ROI's is larger than the maximum allowed value");
		EpicsMCARegionOfInterest[] roiHighObject = new EpicsMCARegionOfInterest[roiHigh.length];
		for(int i =0 ; i < roiHigh.length ; i++){
			EpicsMCARegionOfInterest roi = simpleMca.getNthRegionOfInterest(i);
			roi.setRegionHigh(roiHigh[i]);
			roiHighObject[i] = roi;
		}
		simpleMca.setRegionsOfInterest(roiHighObject);
	}

	/**
	 * get the roi High limit
	 * @return high limit array
	 * @throws DeviceException
	 */
	@Override
	public double[] getHighROIs() throws DeviceException{
		if(simpleMca.isConfigured()){
			double[] highRois = new double[maxROIs];
			EpicsMCARegionOfInterest[] roiObject = (EpicsMCARegionOfInterest[])simpleMca.getRegionsOfInterest();
			for(int i =0 ; i< roiObject.length; i++){
				highRois[i] = roiObject[i].getRegionHigh();
			}
			return highRois;
		}
		return null;
	}

	/**
	 * get the counts for all rois set
	 * @return counts
	 * @throws DeviceException
	 */
	@Override
	public double[] getROICounts() throws DeviceException {
		if(simpleMca.isConfigured()){
			double[][] bothCounts = simpleMca.getRegionsOfInterestCount();
			double[] counts = new double[bothCounts.length];
			for(int i =0 ; i< bothCounts.length; i++){
				counts[i] = bothCounts[i][0];
			}
			return counts;
		}
		return null;
	}

}