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

package gda.device.detector.addetector.collectionstrategy;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.detector.areadetector.v17.ImageMode;
import gda.device.detector.areadetector.v17.NDFile;
import gda.scan.ScanInformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Collection strategy can be used where the detector continuously acquires images.
 *
 * Since this collection strategy cannot use the status of the detector to determine whether it is busy, we need an external
 * indicator of busyness. Typically this would be a filewriter, which knows when all requested images have been collected.
 *
 * This strategy does not set the Trigger Mode, so should be wrapped with a {@link TriggerModeDecorator} as appropriate (for
 * instance {@link InternalTriggerModeDecorator}).
 *
 * Note, this collection strategy ignores the now deprecated NXCollectionStrategyPlugin.configureAcquireAndPeriodTimes method,
 * so support for AbstractADTriggeringStrategy properties such as accumulation Mode and readoutTime will have to be implemented
 * by decorators.
 */
public class ContinuousAcquisition extends AbstractADCollectionStrategy {

	private static final Logger logger = LoggerFactory.getLogger(ContinuousAcquisition.class);
	private NDFile ndFile=null;

	// NXCollectionStrategyPlugin interface

	@Override
	public void prepareForCollection(double collectionTime, int numberImagesPerCollection, ScanInformation scanInfo)
			throws Exception {
		logger.trace("prepareForCollection({}, {}, {})", collectionTime, numberImagesPerCollection, scanInfo);

		getAdBase().setImageModeWait(ImageMode.CONTINUOUS);
		if (getAdBase().getStatus() != Detector.BUSY) {
			logger.warn("Continuously acquiring detector {} was not busy! Starting...");
			getAdBase().startAcquiring();
		}
	}

	@Override
	public void collectData() throws Exception {
		// Do nothing, the detector should already be running.
	}

	@Override
	public int getStatus() throws DeviceException {
		return ndFile.getStatus();
	}

	@Override
	public int getNumberImagesPerCollection(double collectionTime) throws Exception {
		logger.trace("getNumberImagesPerCollection({}) called, ignoring collectionTime & returning 1.", collectionTime);
		return 1;
	}

	@Override
	public void completeCollection() throws Exception {
		// Do nothing, the detector should not be stopped.
	}

	@Override
	public void atCommandFailure() throws Exception {
		completeCollection();
	}

	@Override
	public void stop() throws Exception {
		completeCollection();
	}

	// InitializingBean interface

	@Override
	public void afterPropertiesSet() throws Exception {
		if (ndFile == null) throw new RuntimeException("ndProcess is not set");
		super.afterPropertiesSet();
	}

	// Class properties

	public NDFile getNdFile() {
		return ndFile;
	}

	public void setNdFile(NDFile ndFile) {
		this.ndFile = ndFile;
	}
}