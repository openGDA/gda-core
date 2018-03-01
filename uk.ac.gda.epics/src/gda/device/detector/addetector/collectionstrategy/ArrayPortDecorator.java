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

import gda.device.detector.areadetector.v17.NDArray;
import gda.scan.ScanInformation;

/**
 * Configure Array Input Port name.
 *
 * This will allow a collection strategy to be configured so the Live Stream View display images that are collected in data file
 * weather it is summed in processing plug in or not during a scan.
 */
public class ArrayPortDecorator extends AbstractADCollectionStrategyDecorator {

	private static final Logger logger = LoggerFactory.getLogger(ArrayPortDecorator.class);

	private NDArray ndArray=null;
	private String arrayPortName;

	private boolean restoreArrayPort = false;

	private String savedArrayPort;

	// CollectionStrategyBeanInterface interface

	@Override
	public void saveState() throws Exception {
		logger.trace("saveState() called, restoreArrayPort={}", restoreArrayPort);
		getDecoratee().saveState();
		if (restoreArrayPort) {
			savedArrayPort = getNdArray().getPluginBase().getNDArrayPort_RBV();
			logger.debug("Saved State now savedArrayPort={}", savedArrayPort);
		}
	}

	@Override
	public void restoreState() throws Exception {
		logger.trace("restoreState() called, restoreArrayPort={}", restoreArrayPort);
		if (restoreArrayPort) {
			getNdArray().getPluginBase().setNDArrayPort(savedArrayPort);
			logger.debug("Restored state to savedArrayPort={}", savedArrayPort);
		}
		getDecoratee().restoreState();
	}

	// NXCollectionStrategyPlugin interface

	@Override
	protected void rawPrepareForCollection(double collectionTime, int numberImagesPerCollection, ScanInformation scanInfo)
			throws Exception {
		logger.trace("rawPrepareForCollection({}, {}, {}) called", collectionTime, numberImagesPerCollection, scanInfo);
		getNdArray().getPluginBase().setNDArrayPort(getArrayPortName());
		getDecoratee().prepareForCollection(collectionTime, numberImagesPerCollection, scanInfo);
	}

	// Class properties

	public NDArray getNdArray() {
		return ndArray;
	}

	public void setNdArray(NDArray ndArray) {
		this.ndArray = ndArray;
	}

	public String getArrayPortName() {
		return arrayPortName;
	}

	public void setArrayPortName(String arrayPortName) {
		this.arrayPortName = arrayPortName;
	}
}
