/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package gda.device.detector.nxdetector.andor.proc;

import gda.device.DeviceException;
import gda.device.detector.areadetector.v17.NDProcess;
import gda.device.detector.nxdetector.plugin.NullNXPlugin;
import gda.scan.ScanInformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This plugin is used to obtain dark and flat fields using EPICs AD plugin. Two scans are needed: ne
 * to save the dark field image and the other one to save the flat field image. Then, this class is used to setup
 * the corrected images during the scan.
 * Three scan types: DARK_FIELD, FLAT_FIELD, CORRECTED_IMAGES
 *
 */

public class FlatAndDarkFieldPlugin extends NullNXPlugin {
	private NDProcess ndProcess;

	public enum ScanType {
		DARK_FIELD, FLAT_FIELD, CORRECTED_IMAGES, NO_CORRECTED_IMAGES
	}

	private enum Enable {
		DISABLE, ENABLE
	}

	private ScanType scanType = ScanType.NO_CORRECTED_IMAGES;
	private static final Logger logger = LoggerFactory
			.getLogger(FlatAndDarkFieldPlugin.class);

	public FlatAndDarkFieldPlugin(NDProcess ndProcess) {
		this.ndProcess = ndProcess;
	}

	@Override
	public boolean willRequireCallbacks() {
		return false;
	}

	@Override
	public void prepareForCollection(int numberImagesPerCollection,
			ScanInformation scanInfo) throws Exception {
		ndProcess.getPluginBase().enableCallbacks();
		ndProcess.setEnableFilter(Enable.DISABLE.ordinal()); // disable recursive filter

		switch (scanType) {
			case DARK_FIELD: case FLAT_FIELD:
				enableBackgroundAndFlatFieldAndScale(Enable.DISABLE.ordinal());
				break;
			case CORRECTED_IMAGES:
				enableBackgroundAndFlatFieldAndScale(Enable.ENABLE.ordinal());
				ndProcess.setAutoOffsetScale(Enable.ENABLE.ordinal());
				break;
			case NO_CORRECTED_IMAGES:
				enableBackgroundAndFlatField(Enable.DISABLE.ordinal());
				enableScaleOffsetClipping(Enable.ENABLE.ordinal());
				ndProcess.setAutoOffsetScale(Enable.ENABLE.ordinal());
				break;
		}
	}

	@Override
	public void completeCollection() throws Exception {
		switch (scanType) {
			case DARK_FIELD:
				ndProcess.setSaveBackground(Enable.ENABLE.ordinal());
				checkValidBackground();
				break;
			case FLAT_FIELD:
				ndProcess.setSaveFlatField(Enable.ENABLE.ordinal());
				checkValidFlatField();
				break;
			default:
				break;
		}
		enableBackgroundAndFlatField(Enable.DISABLE.ordinal());
		enableScaleOffsetClipping(Enable.ENABLE.ordinal());
		setScanType(ScanType.NO_CORRECTED_IMAGES);
	}

	public void setScanType(ScanType scanType) {
		this.scanType = scanType;
	}

	public ScanType getScanType() {
		return scanType;
	}

	public void checkValidBackground() throws Exception {
		if (ndProcess.getValidBackground_RBV() == 0) {
			logger.error("Acquired Background not Valid");
			throw new DeviceException("Acquired Background not Valid");
		}
	}

	public void checkValidFlatField() throws Exception {
		if (ndProcess.getValidFlatField_RBV() == 0) {
			logger.error("Acquired Flat Field not Valid");
			throw new DeviceException("Acquired Flat Field not Valid");
		}
	}

	public void enableBackground(int enable) throws Exception {
		if (enable == 1)
			checkValidBackground();
		ndProcess.setEnableBackground(enable);
	}

	public void enableFlatField(int enable) throws Exception {
		if (enable == 1)
			checkValidFlatField();
		ndProcess.setEnableFlatField(enable);
	}

	public void enableBackgroundAndFlatField(int enable) throws Exception {
		enableBackground(enable);
		enableFlatField(enable);
	}

	public void enableScaleOffsetClipping(int enable) throws Exception {
		ndProcess.setEnableOffsetScale(enable);
		ndProcess.setEnableHighClip(enable);
		ndProcess.setEnableLowClip(enable);
	}

	/*When acquiring dark field and flat field Background subtraction, Flat field and Scale,
	Offset and Clipping should be disable
	During the scan enable all background subtraction, Flat field and Scale,
	Offset and Clipping in order to have corrected images.
	@param enable 0 for disable and 1 for enable
	*/

	public void enableBackgroundAndFlatFieldAndScale(int enable)
			throws Exception {
		enableBackgroundAndFlatField(enable);
		enableScaleOffsetClipping(enable);
	}

	public NDProcess getNdProcess() {
		return ndProcess;
	}

}
