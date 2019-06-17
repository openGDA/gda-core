/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.gda.client.live.stream.calibration;

import org.eclipse.january.dataset.IDataset;

import gda.observable.IObserver;

/**
 * Used by {@link CalibratedAxesProvider}, calculates calibrated axes values,
 * based on an observed scannable. See {@link AbstractCalibratedAxesDatasetUpdater}
 * for an example implementation.
 */
public interface CalibratedAxesDatasetUpdater extends IObserver {

	/**
	 * Creates and returns a dataset for the axis scannable this is observing.
	 * @return the dataset;
	 */
	IDataset getDataset();

	/**
	 * Sets the number of pixels in the camera feed dimension corresponding
	 * to the observed scannable.
	 * @param numberOfPixels
	 */
	void setNumberOfPixels(int numberOfPixels);
}
