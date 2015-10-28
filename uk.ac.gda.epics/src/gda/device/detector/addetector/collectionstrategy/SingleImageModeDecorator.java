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

/**
 * Configure image mode as Single.
 */
public class SingleImageModeDecorator extends AbstractADCollectionStrategyDecorator {

	// NXCollectionStrategyPlugin interface

	@Override
	public void prepareForCollection(double collectionTime, int numberImagesPerCollection, ScanInformation scanInfo)
			throws Exception {
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
}
