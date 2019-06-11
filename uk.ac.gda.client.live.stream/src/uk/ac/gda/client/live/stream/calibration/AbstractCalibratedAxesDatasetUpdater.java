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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;

public abstract class AbstractCalibratedAxesDatasetUpdater implements CalibratedAxesDatasetUpdater {

	protected static final Logger logger = LoggerFactory.getLogger(AbstractCalibratedAxesDatasetUpdater.class);
	protected final Scannable scannable;
	protected int numberOfPixels;
	private volatile IDataset dataset;

	protected AbstractCalibratedAxesDatasetUpdater(Scannable scannable) {
		this.scannable = scannable;
	}

	@Override
	public void update(Object source, Object arg) {
		if (numberOfPixels == 0) { // uninitialised
			return;
		}

		boolean moving = true;
		while (moving) {
			try {
				moving = scannable.isBusy();
			} catch (DeviceException e) {
				logger.error("Error while determining whether scannable {} is busy",
						scannable.getName(), e);
			}
			try {
				dataset = createDataSet();
			} catch (DeviceException e) {
				logger.error("Error reading position of axis {} so calibration was not updated", scannable.getName(), e);
			}
			try {
				Thread.sleep(100); // limit loop rate to 10 Hz
			} catch (InterruptedException e) {
				logger.error("Thread was interrupted", e);
				Thread.currentThread().interrupt();
			}
		}
	}

	@Override
	public IDataset getDataset() {
		return dataset;
	}

	@Override
	public void setNumberOfPixels(int numberOfPixels) {
		this.numberOfPixels = numberOfPixels;
	}

	/**
	 * Creates and returns a dataset for an axis.
	 * @throws DeviceException if the scannable position cannot be read
	 */
	protected abstract IDataset createDataSet() throws DeviceException;
}