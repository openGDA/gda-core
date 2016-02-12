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

import gda.scan.ScanInformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigureAcquireTimeDecorator extends AbstractADCollectionStrategyDecorator {

	private static final Logger logger = LoggerFactory.getLogger(ConfigureAcquireTimeDecorator.class);

	private boolean restoreAcquireTime = false;

	private double acquireTime;

	// NXCollectionStrategyPlugin interface

	@Override
	protected void rawPrepareForCollection(double collectionTime, int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		logger.trace("rawPrepareForCollection({}, {}, {}) called", collectionTime, numberImagesPerCollection, scanInfo);
		getAdBase().setAcquireTime(collectionTime);

		getDecoratee().prepareForCollection(collectionTime, numberImagesPerCollection, scanInfo);
	}

	// CollectionStrategyDecoratableInterface interface

	@Override
	public void saveState() throws Exception {
		logger.trace("saveState() called, restoreAcquireTime={}", restoreAcquireTime);
		getDecoratee().saveState();
		if (restoreAcquireTime) {
			acquireTime = getAdBase().getAcquireTime();
			logger.debug("Saved State now acquireTime={}", acquireTime);
		}
	}

	@Override
	public void restoreState() throws Exception {
		logger.trace("restoreState() called, restoreAcquireTime={}, acquireTime={}", restoreAcquireTime, acquireTime);
		if (restoreAcquireTime) {
			final int acquireStatus = getAdBase().getAcquireState(); // TODO: Not all detectors need detector to be stopped to set time
			getAdBase().stopAcquiring();
			getAdBase().setAcquireTime(acquireTime);
			if (acquireStatus == 1) {
				getAdBase().startAcquiring();
			}
			logger.debug("Restored state to acquireTime={} (stop/restart={})", acquireTime, acquireStatus);
		}
		getDecoratee().restoreState();
	}

	// Class properties

	public boolean getRestoreAcquireTime() {
		return restoreAcquireTime;
	}

	public void setRestoreAcquireTime(boolean restoreAcquireTime) {
		this.restoreAcquireTime = restoreAcquireTime;
	}
}
