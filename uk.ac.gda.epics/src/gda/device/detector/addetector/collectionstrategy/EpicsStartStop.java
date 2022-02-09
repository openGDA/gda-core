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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.scan.ScanInformation;

/**
 * This Collection strategy can be used where the detector acquisition is started and stopped in EPICS, such as zacscan in I06.
 *
 * Since this collection strategy lets EPICS to control detector start and stop, the status of the detector should always return IDEL, and
 * the waitWhileBusy should do nothing, so GDA scan will not wait for this detector.
 *
 * This strategy can be used in place of {@link SoftwareStartStop} to give control of the camera to EPICS.
 *
 * @since 9.25
 */
public class EpicsStartStop extends AbstractADCollectionStrategy {

	private static final Logger logger = LoggerFactory.getLogger(EpicsStartStop.class);

	// NXCollectionStrategyPlugin interface

	@Override
	protected void rawPrepareForCollection(double collectionTime, int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		logger.trace("rawPrepareForCollection({}, {}, {})", collectionTime, numberImagesPerCollection, scanInfo);
		super.rawPrepareForCollection(collectionTime, numberImagesPerCollection, scanInfo);
	}

	@Override
	public void collectData() throws Exception {
		// Do nothing, the detector is controlled by EPICS
	}

	@Override
	public int getStatus() throws DeviceException {
		// override parent impl as in this case EPICS controls this detector
		return Detector.IDLE;
	}

	@Override
	public void waitWhileBusy() throws InterruptedException, Exception {
		// Do nothing, GDA scan don't need to wait for detector as this detector is controlled by EPICS
	}
	@Override
	public int getNumberImagesPerCollection(double collectionTime) throws Exception {
		logger.trace("getNumberImagesPerCollection({}) called, ignoring collectionTime & returning 1.", collectionTime);
		return 1;
	}

	@Override
	protected void rawCompleteCollection() throws Exception {
		// Do nothing, the detector should not be stopped here.
	}

	@Override
	public void rawAtCommandFailure() throws Exception {
		completeCollection();
	}

	@Override
	public void rawStop() throws Exception {
		completeCollection();
	}
}