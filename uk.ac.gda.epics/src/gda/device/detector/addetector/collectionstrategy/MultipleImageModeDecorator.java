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
 * This class causes the decorated collection strategy to request the detector collects multiple images.
 */
public class MultipleImageModeDecorator extends AbstractADCollectionStrategyDecorator {

	// Instance variables
	private static final Logger logger = LoggerFactory.getLogger(MultipleImageModeDecorator.class);

	// NXCollectionStrategyPlugin interface

	@Override
	protected void rawPrepareForCollection(double collectionTime, int numberImagesPerCollection, ScanInformation scanInfo)
			throws Exception {
		logger.trace("rawPrepareForCollection({}, {}, {})", collectionTime, numberImagesPerCollection, scanInfo);

		// Set number of images
		getAdBase().setNumImages(numberImagesPerCollection);
		// Set mode to multiple
		getAdBase().setImageModeWait(ImageMode.MULTIPLE);

		getDecoratee().prepareForCollection(collectionTime, numberImagesPerCollection, scanInfo);
	}
}
