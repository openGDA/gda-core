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

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.detector.analyser.EpicsMCARegionOfInterest;
import gda.device.detector.analyser.EpicsMCASimple;
import gda.device.epicsdevice.XmapEpicsDevice;
import gda.factory.FactoryException;

/**
 * Each EDXDMappingElement wraps an EpicsMCASimple instance.
 */
public class EDXDMappingElement extends EDXDElement {
	private static final String MCA = "MCA";
	private static final int MAX_ROIS = 32;
	private final EpicsMCASimple simpleMca;
	private static final Logger logger = LoggerFactory.getLogger(EDXDMappingElement.class);

	/**
	 * @param xmapDevice the device where the element is connected to
	 * @param elementNumber the number of the element in the xmap
	 */
	public EDXDMappingElement(XmapEpicsDevice xmapDevice, int elementNumber, EpicsMCASimple simpleMca) {
		super(xmapDevice, elementNumber);

		final String mcaName = MCA + elementNumber;
		final String pvName = xmap.getRecordPV(mcaName);

		this.simpleMca = simpleMca;
		this.simpleMca.setName(mcaName);
		this.simpleMca.setMcaPV(pvName);
		try {
			this.simpleMca.configure();
		} catch (FactoryException e) {
			logger.error("Exception configuring the ROI mca {} : PV {} in Xmap", mcaName, pvName, e);
		}
	}

	/**
	 * Set rois the array must be of size [maximum number rois][2]
	 * @param rois
	 * @throws DeviceException
	 */
	@Override
	public void setROIs(double[][] rois) throws DeviceException {
		final double[] roiLow = getLowROIs();
		mergeRois(roiLow, rois, 0);
		setLowROIs(roiLow);
		final double[] roiHigh = getHighROIs();
		mergeRois(roiHigh, rois, 1);
		setHighROIs(roiHigh);
		final double[] curLow = getLowROIs();
		if (!Arrays.equals(roiLow, curLow)) {
			throw new DeviceException("Did not set low rois!");
		}
		final double[] curHi = getHighROIs();
		if (!Arrays.equals(curHi, roiHigh)) {
			throw new DeviceException("Did not set high rois!");
		}
	}

	/**
	 * Sets the ROI low limit
	 * @param roiLow
	 * @throws DeviceException
	 */
	@Override
	public void setLowROIs(double[] roiLow) throws DeviceException {
		if (simpleMca.isConfigured()) {
			if (roiLow.length > MAX_ROIS) {
				throw new DeviceException("Number of ROI's is larger than the maximum allowed value");
			}
			final EpicsMCARegionOfInterest[] roiLowObject = new EpicsMCARegionOfInterest[roiLow.length];
			for (int i = 0; i < roiLow.length; i++) {
				final EpicsMCARegionOfInterest roi = simpleMca.getNthRegionOfInterest(i);
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
		if (simpleMca.isConfigured()) {
			final double[] lowRois = new double[MAX_ROIS];
			final EpicsMCARegionOfInterest[] roiObject = (EpicsMCARegionOfInterest[]) simpleMca.getRegionsOfInterest();
			for (int i = 0; i < roiObject.length; i++) {
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
	public void setHighROIs(double[] roiHigh) throws DeviceException {
		if (roiHigh.length > MAX_ROIS) {
			throw new DeviceException("Number of ROI's is larger than the maximum allowed value");
		}
		final EpicsMCARegionOfInterest[] roiHighObject = new EpicsMCARegionOfInterest[roiHigh.length];
		for (int i = 0; i < roiHigh.length; i++) {
			final EpicsMCARegionOfInterest roi = simpleMca.getNthRegionOfInterest(i);
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
	public double[] getHighROIs() throws DeviceException {
		if (simpleMca.isConfigured()) {
			final double[] highRois = new double[MAX_ROIS];
			final EpicsMCARegionOfInterest[] roiObject = (EpicsMCARegionOfInterest[]) simpleMca.getRegionsOfInterest();
			for (int i = 0; i < roiObject.length; i++) {
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
		if (simpleMca.isConfigured()) {
			final double[][] bothCounts = simpleMca.getRegionsOfInterestCount();
			final double[] counts = new double[bothCounts.length];
			for (int i = 0; i < bothCounts.length; i++) {
				counts[i] = bothCounts[i][0];
			}
			return counts;
		}
		return null;
	}

}