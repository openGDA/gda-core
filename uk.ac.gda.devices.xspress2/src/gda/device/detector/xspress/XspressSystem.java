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

package gda.device.detector.xspress;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.detector.DetectorBase;
import gda.device.detector.NexusDetector;
import gda.factory.FactoryException;
import uk.ac.gda.beans.vortex.DetectorElement;
import uk.ac.gda.beans.xspress.XspressDetector;
import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.util.beans.xml.XMLHelpers;

public abstract class XspressSystem extends DetectorBase implements NexusDetector, XspressDetector {
	private static final Logger logger = LoggerFactory.getLogger(XspressSystem.class);

	public static final String ONLY_DISPLAY_FF_ATTR = "ff_only";
	// Full path to config file
	protected String configFileName = null;
	protected String dtcConfigFileName;
	protected Boolean addDTScalerValuesToAscii = false;
	protected Boolean onlyDisplayFF = false;
	protected Boolean saveRawSpectrum = false;
	protected Double deadtimeEnergy = null;
	protected XspressParameters xspressParameters;
	protected Integer maxNumberOfFrames = 0; // the number of frames which TFG has space for, based on the current config in TFG

	@Override
	public abstract void configure() throws FactoryException;
//	public String getConfigFileName();
//	public void setOnlyDisplayFF(boolean onlyDisplayFF);
//	public void setAddDTScalerValuesToAscii(Boolean addDTScalerValuesToAscii);
//	public void setSaveRawSpectrum(Boolean saveRawSpectrum);

	public String getConfigFileName() {
		return configFileName;
	}

	public void setConfigFileName(String configFileName) {
		this.configFileName = configFileName;
	}

	public String getDtcConfigFileName() {
		return dtcConfigFileName;
	}

	public void setDtcConfigFileName(String dtcConfigFileName) {
		this.dtcConfigFileName = dtcConfigFileName;
	}

	public Boolean getAddDTScalerValuesToAscii() {
		return addDTScalerValuesToAscii;
	}

	public void setAddDTScalerValuesToAscii(Boolean addDTScalerValuesToAscii) {
		this.addDTScalerValuesToAscii = addDTScalerValuesToAscii;
	}

	public boolean isOnlyDisplayFF() {
		return onlyDisplayFF;
	}

	public void setOnlyDisplayFF(boolean onlyDisplayFF) {
		this.onlyDisplayFF = onlyDisplayFF;
	}

	public void setSaveRawSpectrum(Boolean saveRawSpectrum) {
		this.saveRawSpectrum = saveRawSpectrum;

	}
	public Boolean getSaveRawSpectrum() {
		return saveRawSpectrum;
	}

	@Override
	public void setDeadtimeCalculationEnergy(Double energy) throws DeviceException {
		deadtimeEnergy = energy;
	}

	@Override
	public Double getDeadtimeCalculationEnergy() throws DeviceException {
		return deadtimeEnergy;
	}
	/**
	 * @param which
	 * @return true if detectorElement is excluded.
	 */
	public boolean isDetectorExcluded(int which) {
		return xspressParameters.getDetector(which).isExcluded();
	}

	public void setDetectorExcluded(int which, boolean excluded) {
		xspressParameters.getDetector(which).setExcluded(excluded);
	}

	public List<DetectorElement> getDetectorList() {
		return xspressParameters.getDetectorList();
	}

	/**
	 * @return the maximum number of time frames possible based on the result of the last format-run command. This will
	 *         be 0 when using DummyDaServer.
	 */
	public int getMaxNumberOfFrames() {
		return maxNumberOfFrames;
	}

	public void setMaxNumberOfFrames(int maxNumberOfFrames) {
		this.maxNumberOfFrames = maxNumberOfFrames;
	}

	@Override
	public DetectorElement getDetector(int which) throws DeviceException {
		return xspressParameters.getDetector(which);
	}

	/**
	 * Sets the window of the given detector.
	 *
	 * @param number the detector element number
	 * @param lower the start of the window
	 * @param upper the end of the window
	 */
	@Override
	public void setDetectorWindow(int number, int lower, int upper) {
		xspressParameters.getDetector(number).setWindow(lower, upper);
	}

	/**
	 * Saves the detector windows, gains etc to file
	 *
	 * @param filename the filename to write detector setup in.
	 */
	@Override
	public void saveDetectors(String filename) {
		try {
			XMLHelpers.writeToXML(XspressParameters.mappingURL, xspressParameters, filename);
		} catch (Exception e) {
			logger.error("Exception in saveDetectors: " + e.getMessage());
		}
	}

	@Override
	public int[] getDataDimensions() throws DeviceException {
		// this returns a Nexus Tree as it implements NexusDetector
		return null;
	}

}
