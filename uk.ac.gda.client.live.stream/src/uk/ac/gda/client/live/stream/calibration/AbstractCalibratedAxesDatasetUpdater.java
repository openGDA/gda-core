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

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.device.Scannable;
import uk.ac.diamond.daq.concurrent.Async;

public abstract class AbstractCalibratedAxesDatasetUpdater implements CalibratedAxesDatasetUpdater {

	private static final Logger logger = LoggerFactory.getLogger(AbstractCalibratedAxesDatasetUpdater.class);
	protected final Scannable scannable;
	protected int numberOfPixels;
	private volatile IDataset dataset;
	private volatile boolean updateRunning = false;

	private static final int AXES_DATASET_UPDATER_SLEEP_TIME_MS = LocalProperties.getAsInt("gda.client.live.stream.datasetUpdaterSleepMillis", 100);

	protected AbstractCalibratedAxesDatasetUpdater(Scannable scannable) {
		this.scannable = scannable;
		updateRunning = false;
	}

	@Override
	public void update(Object source, Object arg) {
		if (numberOfPixels == 0) { // uninitialised
			return;
		}

		if (updateRunning) {
			logger.trace("Return early - already updating");
		} else {
			Async.execute(this::runUpdate);
		}
	}

	private void runUpdate() {
		boolean moving = true;
		updateRunning = true;
		while (moving) {
			try {
				moving = scannable.isBusy();
			} catch (DeviceException e) {
				logger.error("Error while determining whether scannable {} is busy",
						scannable.getName(), e);
				moving = false;
			}

			try {
				dataset = createDataSet();
			} catch (DeviceException e) {
				logger.error("Error reading position of axis {} so calibration was not updated", scannable.getName(), e);
			}

			try {
				Thread.sleep(AXES_DATASET_UPDATER_SLEEP_TIME_MS); // limit loop rate to avoid CPU hogging.
			} catch (InterruptedException e) {
				logger.error("Thread was interrupted", e);
				Thread.currentThread().interrupt();
			}
		}
		updateRunning = false;
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