/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package gda.device.detector.addetector.triggering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gda.device.DeviceException;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.ADBase.StandardTriggerMode;
import gda.device.detector.areadetector.v17.ImageMode;
import gda.device.detector.countertimer.TfgScalerWithFrames;
import gda.factory.Finder;
import gda.scan.ScanInformation;

public class MultipleExposureHardwareTriggeredStrategy extends SimpleAcquire {

	private static final ArrayList<String> EMPTY_LIST = new ArrayList<String>();

	public MultipleExposureHardwareTriggeredStrategy(ADBase adBase, double readoutTime) {
		super(adBase, readoutTime);
	}

	@Override
	public void prepareForCollection(double collectionTime, int numImages, ScanInformation scanInfo) throws Exception {
		getAdBase().stopAcquiring(); // Stop acquiring first (in case live mode is already be running).
		enableOrDisableCallbacks();
		setTimeFrames(scanInfo); // set time frames for I1 and ionchambers
		configureAcquireAndPeriodTimes(collectionTime);
		configureTriggerMode();
		getAdBase().setImageModeWait(ImageMode.MULTIPLE);
		getAdBase().setNumImages(getTotalNumberScanImages(scanInfo));
		getAdBase().startAcquiring();
	}

	/**
	 * Return total number of images in scan (also works for multi-dimensional/nested scans)
	 *
	 * @param scanInfo
	 * @return Total number of images
	 * @since 11/11/2015
	 *
	 */
	private int getTotalNumberScanImages(ScanInformation scanInfo) {
		int dimensions[] = scanInfo.getDimensions();
		int numImages = 1;
		for (int i = 0; i < dimensions.length; i++)
			numImages *= dimensions[i];
		return numImages;
	}

	/**
	 * Set frame times for ionchambers and I1 so that Medipix collection of each frame is triggered correctly. (Important for command line scans when
	 * I1.setTimes has not explicitly been called before scan start).
	 *
	 * @param scanInfo
	 * @throws DeviceException
	 * @since 11/11/2015
	 *
	 */
	private void setTimeFrames(ScanInformation scanInfo) throws DeviceException {
		// Names of tfgScalers we want to try and set the time frames for
		List<String> tfgScalerNameList = Arrays.asList("ionchambers", "I1");

		String detectorNames[] = scanInfo.getDetectorNames();
		for (int i = 0; i < detectorNames.length; i++) {

			// Get ref to TfgScaler object from detector name
			TfgScalerWithFrames tfgScaler = null;
			if (tfgScalerNameList.contains(detectorNames[i])) {
				tfgScaler = (TfgScalerWithFrames) Finder.getInstance().find(detectorNames[i]);
			}

			// Set TfgScaler frame times. NB. Don't do this if frame times have already been
			// set explicitly. e.g. by a script or for scan started from the command queue.
			if (tfgScaler != null && tfgScaler.getTimes() == null) {
				double collectionTime = tfgScaler.getCollectionTime();
				int numImages = getTotalNumberScanImages(scanInfo);
				Double times[] = new Double[numImages];
				Arrays.fill(times, collectionTime); // each frame has same duration...
				tfgScaler.clearFrameSets();
				tfgScaler.setTimes(times);
			}
		}
	}

	@Override
	public void configureAcquireAndPeriodTimes(double collectionTime) throws Exception {
		getAdBase().setAcquireTime(0.1);
		getAdBase().setAcquirePeriod(0);
	}

	@Override
	public void collectData() throws Exception {
		//System.out.println("test");
	//	getAdBase().startAcquiring();
	}

//	@Override
//	public List<String> getInputStreamNames() {
//		return EMPTY_LIST;
//	}
//
//	@Override
//	public List<String> getInputStreamFormats() {
//		return EMPTY_LIST;
//	}
	@Override
	public int getStatus() throws DeviceException {
		return 0;
	}

	@Override
	public void waitWhileBusy() throws InterruptedException, DeviceException {
		// getAdBase().waitWhileStatusBusy();
		return;
	}
	private void configureTriggerMode() throws Exception {
		getAdBase().setTriggerMode(StandardTriggerMode.EXTERNAL.ordinal());
	}

	@Override
	public boolean requiresAsynchronousPlugins() {
		return true;
	}

	/**
	 * @since 6/11/2015
	 */
	@Override
	public void completeCollection() throws Exception {
		getAdBase().stopAcquiring();
		// Trigger mode needs to be set back to internal so that live mode can be started correctly from GUI at end of scan
		getAdBase().setTriggerMode(StandardTriggerMode.INTERNAL.ordinal());
	}
}
