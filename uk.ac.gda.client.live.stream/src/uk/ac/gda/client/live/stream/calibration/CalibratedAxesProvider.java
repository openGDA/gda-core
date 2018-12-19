/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

/**
 * Provides calibration for data streams
 */
public interface CalibratedAxesProvider {


	/**
	 * Called when the stream connection has been established.
	 * There is no need to do work until this point
	 */
	void connect();


	/**
	 * Called when the stream connection has been closed.
	 * No more work is needed, listeners can be disposed, etc.
	 */
	void disconnect();


	/**
	 * Returns a calibrated horizontal axis.
	 * Called with each new frame.
	 */
	IDataset getXAxisDataset();


	/**
	 * Returns a calibrated vertical axis.
	 * Called with each new frame.
	 */
	IDataset getYAxisDataset();


	/**
	 * Called when the stream data changes shape ([y, x])
	 */
	void resizeStream(int[] newShape);

}
