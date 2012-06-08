/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

import gda.device.DeviceException;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.ADBase.ImageMode;
import gda.device.detector.areadetector.v17.ADDriverPco;
import gda.device.detector.areadetector.v17.ADDriverPco.PcoTriggerMode;
import gda.device.detector.areadetector.v17.NDProcess.FilterTypeEnum;
import gda.device.timer.Etfg;

/*
 * Class of detector used to take multiple exposures that are then added together to make a single collection file
 * 
 * How to use:
 * 
 * Connect Process Plugin to camera to allow the callback every nth array to the tif file plugin
 * The camera should be running iin continuous node with the exposure set in software
 * in collect Data the Process plugin is enabled to sum up n images. On reaching the n images it then stops acting on any more
 * is busy looks for the number of images summed up to match the number required 
 * 
 */
public class PCOMultipleExposureHardwareTrigger extends MultipleExposureSoftwareTriggerAutoMode {

	private final ADDriverPco adDriverPco;

	public PCOMultipleExposureHardwareTrigger(ADBase adBase, double maxExposureTime, ADDriverPco adDriverPco) {
		super(adBase, maxExposureTime);
		this.adDriverPco = adDriverPco;
	}

	public ADDriverPco getAdDriverPco() {
		return adDriverPco;
	}

	@Override
	public void prepareForCollection(double collectionTime, int numImagesIgnored) throws Exception {
		getAdBase().stopAcquiring();

		numberImagesPerCollection = calcNumberImagesPerCollection(collectionTime);
		getAdBase().setAcquireTime(numberImagesPerCollection > 1 ? maxExposureTime : collectionTime);

		if (ndProcess != null) {

			ndProcess.setFilterType(FilterTypeEnum.RecursiveAve);// FilterTypeEnum.RecursiveSum);
			ndProcess.setNumFilter(numberImagesPerCollection);
			ndProcess.setEnableFilter(0); // enable in collectData
			// ndProcess.setResetFilter(1);
			ndProcess.getPluginBase().enableCallbacks();
			ndProcess.getPluginBase().setArrayCounter(0);
			ndProcess.getPluginBase().setDroppedArrays(0);
			ndProcess.setDataTypeOut(5); // UINT32
			// do noy use autoreset - rather reset for every collection
			// need to set Callbacks Array N only - version 1.8 of AD
			/*
			 * if tfg is present we can use autoreset
			 */
		}
		// we want 1 image per trigger - there will be multiple triggers per collection
		getAdBase().setNumImages(1);
		getAdBase().setImageModeWait(ImageMode.SINGLE);
		// getAdBase().setAcquirePeriod(0.0); //this is needed for PCO to make sure delay=0 - do not use as it effects
		// delay
		getAdBase().setTriggerMode(PcoTriggerMode.EXTERNAL_AND_SOFTWARE.ordinal()); // exposure time set by camera
																					// rather than trigger
		adDriverPco.getArmModePV().putCallback(true);
	}

	@Override
	public void endCollection() throws Exception {
		getAdBase().stopAcquiring();
		if (ndProcess != null) {
			ndProcess.setEnableFilter(0);
			ndProcess.getPluginBase().disableCallbacks();
			// ndProcess.setFilterType(FilterTypeEnum.RecursiveSum);
			// ndProcess.setNumFilter(1);
		}
		getAdBase().setImageModeWait(ImageMode.SINGLE);
		getAdBase().setNumImages(1);
		adDriverPco.getArmModePV().putCallback(false);

	}

	@Override
	public void collectData() throws Exception {
		if (ndProcess != null) {
			ndProcess.setResetFilter(1);
			ndProcess.setEnableFilter(1);
			while (ndProcess.getEnableFilter_RBV() == 0) {
				Thread.sleep(50); // should use wait in setFilter
			}
		}

	}

	private boolean isBusy() throws DeviceException {
		try {
			if (ndProcess != null) {
				return (ndProcess.getEnableFilter_RBV() == 1)
						&& (ndProcess.getResetFilter_RBV() != 0 || ndProcess.getNumFiltered_RBV() != numberImagesPerCollection);
			}
			return false;
		} catch (DeviceException e) {
			throw e;
		} catch (Exception e) {
			throw new DeviceException("Error in isBusy", e);
		}
	}

	@Override
	public void waitWhileBusy() throws InterruptedException, DeviceException {
		while (isBusy()) {
			Thread.sleep(50);
		}
	}

}
