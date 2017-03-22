/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

import gda.device.detector.pvcam.DetectorInitializer;
import gda.scan.ScanInformation;

public class InitialiseDetectorAfterExposureTimeChangeDecorator extends AbstractADCollectionStrategyDecorator {
	private DetectorInitializer detector;

	@Override
	protected void rawPrepareForCollection(double collectionTime, int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		getDecoratee().prepareForCollection(collectionTime, numberImagesPerCollection, scanInfo); // must be called before detector initialise
		detector.initialiseDetector();
	}

	public DetectorInitializer getDetector() {
		return detector;
	}

	public void setDetector(DetectorInitializer detector) {
		this.detector = detector;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (detector == null) {
			throw new IllegalStateException(getName() + ": requires a DetectorIntializer object.");
		}
		super.afterPropertiesSet();
	}
}
