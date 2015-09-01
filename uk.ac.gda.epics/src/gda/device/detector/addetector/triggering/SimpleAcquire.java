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

import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.ADBase.ImageMode;
import gda.scan.ScanInformation;


public class SimpleAcquire extends AbstractADTriggeringStrategy {

	
	public SimpleAcquire(ADBase adBase, double readoutTime) {
		setAdBase(adBase);
		setReadoutTime(readoutTime);
	}

	@Override
	public void prepareForCollection(double collectionTime, int numImages, ScanInformation scanInfo) throws Exception {
		enableOrDisableCallbacks();
		if (numImages != 1) {
			throw new IllegalArgumentException("This single exposure triggering strategy expects to expose only 1 image");
		}
		configureAcquireAndPeriodTimes(collectionTime);
		getAdBase().setNumImages(numImages);
		getAdBase().setImageMode(ImageMode.SINGLE.ordinal());
	}

	@Override
	public void collectData() throws Exception {
		getAdBase().startAcquiring();
	}

	@Override
	public void stop() throws Exception {
		completeCollection();
	}
	
	@Override
	public void atCommandFailure() throws Exception {
		completeCollection();
	}
	
	@Override
	public void completeCollection() throws Exception {
		getAdBase().stopAcquiring();
	}

	@Override
	public int getNumberImagesPerCollection(double collectionTime) throws Exception {
		return 1;
	}

}
