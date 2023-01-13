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
import gda.device.detector.addetector.collectionstrategy.SoftwareStartStop;
import gda.device.detector.areadetector.v17.ImageMode;
import gda.scan.ScanInformation;

/**
 * Configure PCO image mode.
 *
 * PCO Image mode change only take effect when acquisition is stopped.
 * Thus it requires a decoratee of {@link SoftwareStartStop} in its configuration.
 * The decoratee of {@link SoftwareStartStop} must be the inner-most decoratee,
 * which stops PCO camera before applying image mode set here.
 */
public class PCOImageModeDecorator extends AbstractADCollectionStrategyDecorator {

	private static final Logger logger = LoggerFactory.getLogger(PCOImageModeDecorator.class);

	private ImageMode imageMode;
	private boolean restoreNumImagesAndImageMode = false;
	private int numImagesSaved;
	private short imageModeSaved;

	@Override
	protected void rawPrepareForCollection(double collectionTime, int numberImagesPerCollection, ScanInformation scanInfo)
			throws Exception {
		logger.trace("rawPrepareForCollection({}, {}, {}) called", collectionTime, numberImagesPerCollection, scanInfo);
		getAdBase().setNumImages(numberImagesPerCollection);
		getAdBase().setImageMode(getImageMode());
		getDecoratee().prepareForCollection(collectionTime, numberImagesPerCollection, scanInfo);
	}

	// CollectionStrategyBeanInterface interface

	@Override
	public void saveState() throws Exception {
		logger.trace("saveState() called, restoreNumImagesAndImageMode={}", restoreNumImagesAndImageMode);
		getDecoratee().saveState();
		if (restoreNumImagesAndImageMode) {
			numImagesSaved = getAdBase().getNumImages();
			imageModeSaved = getAdBase().getImageMode();
			logger.debug("Saved State now numImagesSaved={}, imageModeSaved={}", numImagesSaved, imageModeSaved);
		}
	}

	@Override
	public void restoreState() throws Exception {
		logger.trace("restoreState() called, restoreNumImagesAndImageMode={}", restoreNumImagesAndImageMode);
		if (restoreNumImagesAndImageMode) {
			getAdBase().setNumImages(numImagesSaved);
			getAdBase().setImageMode(imageModeSaved);
			logger.debug("Restored state to numImagesSaved={}, imageModeSaved={}", numImagesSaved, imageModeSaved);
		}
		getDecoratee().restoreState();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (imageMode==null) throw new IllegalStateException("'imageMode' is not set!");
		if (getDecoratee()==null) throw new IllegalStateException("'decoratee' is not set!");
		super.afterPropertiesSet();
	}

	public boolean getRestoreNumImagesAndImageMode() {
		return restoreNumImagesAndImageMode;
	}

	public void setRestoreNumImagesAndImageMode(boolean restoreNumImagesAndImageMode) {
		this.restoreNumImagesAndImageMode = restoreNumImagesAndImageMode;
	}

	public ImageMode getImageMode() {
		return imageMode;
	}

	public void setImageMode(ImageMode imageMode) {
		this.imageMode = imageMode;
	}
}
