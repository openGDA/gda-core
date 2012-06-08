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

package gda.device.detector.pco4000;

import java.awt.Rectangle;

import gda.device.DeviceException;
import gda.device.detector.DetectorBase;

/**
 * This class is designed to deal with the PCO4000 camera, primarily for the I12 beamline
 * It should once it is finished it should sit on rtop of eiter a simulation or the real
 * device
 */
public class PCO4000 extends DetectorBase {

	/**
	 * As this is as serializable object
	 */
	private static final long serialVersionUID = 3992340275415676318L;

	/**
	 * 
	 */
	public static final String FILENAME = "filename";
	/**
	 * 
	 */
	public static final String PATHNAME = "pathname";
	/**
	 * 
	 */
	public static final String SET_COUNT = "setCount";
	/**
	 * 
	 */
	public static final String SET_ROI = "setROI";
	/**
	 * 
	 */
	public static final String CLEAR_ROI = "clearROI";
	/**
	 * 
	 */
	public static final String SET_EXPOSURE_TIME = "setExposureTime";
	/**
	 * 
	 */
	public static final String SET_DYNAMIC_RANGE = "setDynamicRange";
	/**
	 * 
	 */
	public static final String SET_BINNING = "setBinning";
	/**
	 * 
	 */
	public static final String SET_HARDWARE = "setHardware";

	private int fileNumber = 0;

	private IPCO4000Hardware hardware = null;

	/**
	 * This needs to initialise some parameters to standards just in case they are not set manualy
	 */
	public PCO4000() {
		try {
			setAttribute(SET_EXPOSURE_TIME, new Double(2.5));
		} catch (DeviceException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Triggers the data collection method
	 */
	@Override
	public void collectData() throws DeviceException {

	}

	/**
	 * Setter
	 * @param count
	 */
	public void setCount(int count) {
		fileNumber = count;
	}

	/**
	 * Setter
	 * @param RoI
	 */
	public void setROI(@SuppressWarnings("unused") Rectangle RoI) {

	}

	/**
	 * Clears the ROI
	 */
	public void clearROI() {

	}

	/**
	 * Setter
	 * @param bits
	 */
	public void setDynamicRange(@SuppressWarnings("unused") int bits) {

	}

	/**
	 * Setter
	 * @param Bins
	 */
	public void setBinning(@SuppressWarnings("unused") Rectangle Bins) {

	}

	/**
	 * Setter
	 * @param inputHardware
	 */
	public void setHardware(IPCO4000Hardware inputHardware) {
		hardware = inputHardware;
	}

	/**
	 * Setter
	 * @param time
	 * @throws DeviceException
	 */
	public void setExposureTime(Double time) throws DeviceException {
		super.setAttribute(SET_EXPOSURE_TIME, time);
	}
	
	/**
	 * Setter
	 * @param fileName
	 * @throws DeviceException
	 */
	public void setFileName(String fileName) throws DeviceException {
		super.setAttribute(FILENAME, fileName);
	}
	
	/**
	 * Setter
	 * @param pathName
	 * @throws DeviceException
	 */
	public void setPathName(String pathName) throws DeviceException {
		super.setAttribute(PATHNAME, pathName);
	}

	/**
	 * This will be used to set all the main attributes of the 
	 * camera, as this means that the system is fully corbarized which
	 * may be useful as the camera runs on windows, and may be running
	 * on a seperate server 
	 */
	@Override
	public void setAttribute(String attributeName, Object value) throws DeviceException {

		if (attributeName.equalsIgnoreCase(FILENAME)) {
			if (value instanceof String) {
				setFileName((String)value);
			} else {
				throw new DeviceException("setAttribute(PCO4000.FILENAME) has been passed a non string :" + value);
			}

		} else if (attributeName.equalsIgnoreCase(PATHNAME)) {
			if (value instanceof String) {
				setPathName((String)value);
			} else {
				throw new DeviceException("setAttribute(PCO4000.PATHNAME) has been passed a non string :" + value);
			}

		} else if (attributeName.equalsIgnoreCase(SET_COUNT)) {
			if (value instanceof Integer) {
				setCount((Integer) value);
			} else {
				throw new DeviceException("setAttribute(PCO4000.SET_COUNT) has been passed a non integer :" + value);
			}

		} else if (attributeName.equalsIgnoreCase(SET_ROI)) {
			if (value instanceof Rectangle) {
				setROI((Rectangle) value);
			} else {
				throw new DeviceException("setAttribute(PCO4000.SET_ROI) has been passed a non Rectangle :" + value);
			}

		} else if (attributeName.equalsIgnoreCase(CLEAR_ROI)) {
			clearROI();

		} else if (attributeName.equalsIgnoreCase(SET_EXPOSURE_TIME)) {
			if (value instanceof Double) {
				setExposureTime((Double) value);
			} else {
				throw new DeviceException("setAttribute(PCO4000.SET_EXPOSURE_TIME) has been passed a non Double :"
						+ value);
			}

		} else if (attributeName.equalsIgnoreCase(SET_DYNAMIC_RANGE)) {
			if (value instanceof Integer) {
				setDynamicRange((Integer) value);
			} else {
				throw new DeviceException("setAttribute(PCO4000.SET_DYNAMIC_RANGE) has been passed a non Integer :"
						+ value);
			}

		} else if (attributeName.equalsIgnoreCase(SET_BINNING)) {
			if (value instanceof Rectangle) {
				setBinning((Rectangle) value);
			} else {
				throw new DeviceException("setAttribute(PCO4000.SET_BINNING) has been passed a non Rectangle :" + value);
			}

		} else if (attributeName.equalsIgnoreCase(SET_HARDWARE)) {
			if (value instanceof IPCO4000Hardware) {
				setHardware((IPCO4000Hardware) value);
			} else {
				throw new DeviceException("setAttribute(PCO4000.SET_HARDWARE) has been passed a non IPCO4000Hardware :"
						+ value);
			}
		}
	}

	/**
	 * This should basicaly be set when the readout is occuring, 
	 * this may well need to call on another method
	 * 0 idle
	 * 1 busy
	 */
	@Override
	public int getStatus() throws DeviceException {
		// TODO Needs to be tied into the actual device
		return 0;
	}

	/**
	 *  the data or filename needs to be returned here
	 */
	@Override
	public Object readout() throws DeviceException {
		// set the filename
		String fullname = getAttribute(PATHNAME) + "/" + getAttribute(FILENAME) + String.format("%04d", fileNumber)
				+ ".tif";

		// Call the readout method on the camera.
		hardware.exposeDetector(fullname, (Double) getAttribute(SET_EXPOSURE_TIME));

		// then increment the fileNumber
		fileNumber++;

		return fullname;
	}

	/**
	 * This tag is for nexus to let it know that this will generate a seperate file
	 * @return true in this case
	 * @throws DeviceException 
	 */
	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return true;
	}

	/**
	 * Data collection for Nexus
	 * @return the device description
	 * @throws DeviceException 
	 */
	@Override
	public String getDescription() throws DeviceException {
		return "PCO4000 14bit CCD detector.";
	}

	/**
	 * Data collection for Nexus
	 * @return The ID of the device
	 * @throws DeviceException 
	 */
	@Override
	public String getDetectorID() throws DeviceException {
		return hardware.getDetectorID();
	}

	/**
	 * Data collection for Nexus
	 * @return "CCD"
	 * @throws DeviceException 
	 */
	@Override
	public String getDetectorType() throws DeviceException {
		return "CCD";
	}

	/**
	 * Data collection for Nexus
	 */
	@Override
	public int[] getDataDimensions() throws DeviceException {
		// TODO This is curently set to 1, although it should be obtained by the real equipment
		int[] result = { 1 };
		return result;
	}

}
