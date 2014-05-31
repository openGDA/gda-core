/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

import java.text.MessageFormat;

import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.ADBase.ImageMode;
import gda.device.detector.areadetector.v17.ADBase.StandardTriggerMode;
import gda.scan.ScanInformation;

public class MultipleExposurePerCollectionStandard extends SimpleAcquire {

	private int numberOfImagesPerCollection = 1;
	
	public MultipleExposurePerCollectionStandard(ADBase adBase, double readoutTime) {
		super(adBase, readoutTime);
	}

	@Override
	public void prepareForCollection(double collectionTime, int numImages, ScanInformation scanInfo) throws Exception {
		if (numImages != numberOfImagesPerCollection) {
			// NXDetector calls #getNumberImagesPerCollection() and then passes the result back in through numImages.
			// Assert that this is happening, as it is a little unexpected.
			throw new AssertionError(MessageFormat.format("numImages ({0}) != numberOfImagesPerCollection ({1})",
					numImages, numberOfImagesPerCollection));
		}
	
		enableOrDisableCallbacks();
		configureAcquireAndPeriodTimes(collectionTime);
		
		configureTriggerMode();
		getAdBase().setImageModeWait(ImageMode.MULTIPLE); 
		getAdBase().setNumImages(numberOfImagesPerCollection);
	}

	protected void configureTriggerMode() throws Exception {
		getAdBase().setTriggerMode(StandardTriggerMode.INTERNAL.ordinal());
	}
	
	@Override
	public int getNumberImagesPerCollection(double unusedCollectionTime) throws Exception {
		return getNumberOfImagesPerCollection();
	}

	public int getNumberOfImagesPerCollection() {
		return numberOfImagesPerCollection;
	}

	public void setNumberOfImagesPerCollection(int numberOfImagesPerCollection) {
		this.numberOfImagesPerCollection = numberOfImagesPerCollection;
	}
	
	@Override
	public String toString() {
		return MessageFormat.format("driver.numberOfImagesPerCollection = {0}, driver.readoutTime = {1} (acquisition_period = acquisition_time + driver.readoutTime)", numberOfImagesPerCollection, getReadoutTime()); 
	}

}