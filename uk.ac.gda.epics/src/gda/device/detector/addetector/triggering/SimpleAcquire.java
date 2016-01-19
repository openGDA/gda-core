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
import gda.device.detector.areadetector.v17.ImageMode;
import gda.scan.ScanInformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleAcquire extends AbstractADTriggeringStrategy {

	private static final Logger logger = LoggerFactory.getLogger(SimpleAcquire.class);

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
		final short detectorState = getAdBase().getDetectorStateLastMonitoredValue();
		logger.debug("Detector state before triggering acquisition: " + detectorState);
		if (detectorState == 9) { // Disconnected
			throw new IllegalStateException("Epics reports detector is disconnected. Cannot not trigger acquisition.");
		}
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
