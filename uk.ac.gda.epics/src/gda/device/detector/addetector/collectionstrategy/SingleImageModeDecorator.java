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

import gda.device.detector.areadetector.v17.ImageMode;
import gda.scan.ScanInformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configure image mode as Single.
 */
public class SingleImageModeDecorator extends AbstractADCollectionStrategyDecorator {

	private static final Logger logger = LoggerFactory.getLogger(SingleImageModeDecorator.class);

	private boolean restoreNumImagesAndImageMode = false;

	private int savedNumImages;
	private short savedImageMode;

	// CollectionStrategyBeanInterface interface

	@Override
	public void saveState() throws Exception {
		logger.trace("saveState() called, restoreNumImagesAndImageMode={}", restoreNumImagesAndImageMode);
		getDecoratee().saveState();
		if (restoreNumImagesAndImageMode) {
			savedNumImages = getAdBase().getNumImages();
			savedImageMode = getAdBase().getImageMode();
			logger.debug("Saved State now savedNumImages={}, savedImageMode={}", savedNumImages, savedImageMode);
		}
	}

	@Override
	public void restoreState() throws Exception {
		logger.trace("restoreState() called, restoreNumImagesAndImageMode={}", restoreNumImagesAndImageMode);
		if (restoreNumImagesAndImageMode) {
			final int acquireStatus = getAdBase().getAcquireState(); // TODO: Not all detectors need detector to be stopped to set NumImages or ImageMode
			getAdBase().stopAcquiring();
			getAdBase().setNumImages(savedNumImages);
			getAdBase().setImageMode(savedImageMode);
			if (acquireStatus == 1) {
				getAdBase().startAcquiring();
			}
			logger.debug("Restored state to savedNumImages={}, savedImageMode={} (stop/restart={})", savedNumImages, savedImageMode, acquireStatus);
		}
		getDecoratee().restoreState();
	}

	// NXCollectionStrategyPlugin interface

	@Override
	protected void rawPrepareForCollection(double collectionTime, int numberImagesPerCollection, ScanInformation scanInfo)
			throws Exception {
		logger.trace("rawPrepareForCollection({}, {}, {}) called", collectionTime, numberImagesPerCollection, scanInfo);
		if (numberImagesPerCollection != 1) {
			throw new IllegalArgumentException("This single exposure triggering strategy expects to expose only 1 image");
		}
		getAdBase().setNumImages(numberImagesPerCollection);
		getAdBase().setImageMode(ImageMode.SINGLE);
		getDecoratee().prepareForCollection(collectionTime, numberImagesPerCollection, scanInfo);
	}

	@Override
	public int getNumberImagesPerCollection(double collectionTime) throws Exception {
		return 1;
	}

	// Class properties

	public boolean getRestoreNumImagesAndImageMode() {
		return restoreNumImagesAndImageMode;
	}

	public void setRestoreNumImagesAndImageMode(boolean restoreNumImagesAndImageMode) {
		this.restoreNumImagesAndImageMode = restoreNumImagesAndImageMode;
	}
}
