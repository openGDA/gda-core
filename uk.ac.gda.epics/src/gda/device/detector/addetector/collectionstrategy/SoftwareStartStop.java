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

/**
 * This Collection strategy can be used where the acquisition must be started during collectData() and stopped during
 * completeCollection().
 *
 * This strategy does not set the Trigger Mode, so should be wrapped with a {@link TriggerModeDecorator} as appropriate (use
 * {@link InternalTriggerModeDecorator} for the equivalent of the old SingleExposureStandard with the default internal trigger mode).
 *
 * This strategy does not set the Image Mode, so should be wrapped with an ImageModeDecorator as appropriate (use
 * {@link SingleImageModeDecorator} for the equivalent of the old SimpleAcquire or SingleExposureStandard).
 *
 * Note, this collection strategy ignores the now deprecated NXCollectionStrategyPlugin.configureAcquireAndPeriodTimes method,
 * so support for AbstractADTriggeringStrategy properties such as accumulation Mode and readoutTime will have to be implemented
 * by decorators.
 */
public class SoftwareStartStop extends AbstractADCollectionStrategy {

	private static final Logger logger = LoggerFactory.getLogger(SoftwareStartStop.class);

	private boolean restoreAcquireState = false;

	private int savedAcquireState;

	// NXCollectionStrategyPlugin interface

	@Override
	public void collectData() throws Exception {
		logger.trace("collectData() called, restoreAcquireState={}", restoreAcquireState);
		getAdBase().startAcquiring();
	}

	@Override
	public int getNumberImagesPerCollection(double collectionTime) throws Exception {
		logger.trace("getNumberImagesPerCollection({}) called, ignoring collectionTime & returning 1.", collectionTime);
		return 1;
	}

	@Override
	protected void rawCompleteCollection() throws Exception {
		logger.trace("rawCompleteCollection() called, restoreAcquireState={}", restoreAcquireState);
		getAdBase().stopAcquiring();
	}

	@Override
	public void rawAtCommandFailure() throws Exception {
		logger.trace("rawAtCommandFailure() called, restoreAcquireState={}", restoreAcquireState);
		completeCollection();
	}

	@Override
	public void rawStop() throws Exception {
		logger.trace("rawStop() called, restoreAcquireState={}", restoreAcquireState);
		completeCollection();
	}

	// CollectionStrategyBeanInterface interface

	@Override
	public void saveState() throws Exception {
		logger.trace("saveState() called, restoreAcquireState={}", restoreAcquireState);
		if (restoreAcquireState) {
			savedAcquireState = getAdBase().getAcquireState();
			logger.debug("Saved state now savedAcquireState={}", savedAcquireState);
		}
	}

	@Override
	public void restoreState() throws Exception {
		logger.trace("restoreState() called, restoreAcquireState={}, savedAcquireState={}", restoreAcquireState, savedAcquireState);
		if (restoreAcquireState) {
			if (savedAcquireState == 1) {
				getAdBase().startAcquiring();
			} else {
				getAdBase().stopAcquiring();
			}
			logger.debug("Restored state to savedAcquireState={})", savedAcquireState);
		}
	}

	// Class properties

	public boolean getRestoreAcquireState() {
		return restoreAcquireState;
	}

	public void setRestoreAcquireState(boolean restoreAcquireState) {
		this.restoreAcquireState = restoreAcquireState;
	}
}