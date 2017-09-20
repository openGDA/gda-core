/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package gda.device.detector.pco.collectionstrategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.detector.addetector.collectionstrategy.AbstractADCollectionStrategyDecorator;
import gda.scan.ScanInformation;

/**
 * A PCO camera decorator that stops camera acquiring.
 * Any PCO camera parameter changes will only take effect after PCO has stopped.
 * So this must be the out-most decorators that changes PCO camera settings.
 */
public class PCOStopDecorator extends AbstractADCollectionStrategyDecorator {
	private static final Logger logger = LoggerFactory.getLogger(PCOStopDecorator.class);
	private boolean restoreAcquireState = false;

	private int acquireStateSaved;

	@Override
	protected void rawPrepareForCollection(double collectionTime, int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		//PCO must be stopped before changing parameters to take effect in next acquisition.
		getAdBase().stopAcquiring();
		getDecoratee().prepareForCollection(collectionTime, numberImagesPerCollection, scanInfo); // must be called before detector initialise
	}


	@Override
	public void afterPropertiesSet() throws Exception {
		if (getDecoratee()==null) throw new IllegalStateException("'decoratee' is not set!");
		super.afterPropertiesSet();
	}
	// CollectionStrategyDecoratableInterface interface

	@Override
	public void saveState() throws Exception {
		logger.trace("saveState() called, acquireStateSaved={}", acquireStateSaved);
		getDecoratee().saveState();
		if (isRestoreAcquireState()) {
			acquireStateSaved=getAdBase().getAcquireState();
			logger.debug("Saved State now acquireStateSaved={}", acquireStateSaved);
		}
	}

	@Override
	public void restoreState() throws Exception {
		logger.trace("restoreState() called, restoreAcquireState={}, acquireStateSaved={}", restoreAcquireState, acquireStateSaved);
		if (isRestoreAcquireState()) {
			if (acquireStateSaved==1) {
				getAdBase().startAcquiring();
			} else {
				getAdBase().stopAcquiring();
			}
			logger.debug("Restored state to acquireStateSaved={}", acquireStateSaved);
		}
		getDecoratee().restoreState();
	}

	public boolean isRestoreAcquireState() {
		return restoreAcquireState;
	}

	public void setRestoreAcquireState(boolean restoreAcquireState) {
		this.restoreAcquireState = restoreAcquireState;
	}
}
