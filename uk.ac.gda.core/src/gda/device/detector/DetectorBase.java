/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.device.detector;

import gda.data.fileregistrar.FileRegistrarHelper;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.scannable.ScannableBase;
import gda.device.scannable.ScannableUtils;
import gda.factory.Configurable;

import java.io.Serializable;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for all Detectors.
 */
public abstract class DetectorBase extends ScannableBase implements Serializable, Configurable, Detector {

	private static final Logger logger = LoggerFactory.getLogger(DetectorBase.class);

	// time in seconds to collect data for
	protected double collectionTime;

	public DetectorBase() {
		level = 100; // Change default level set in ScannableBase from 5
	}


	@Override
	public double getCollectionTime() throws DeviceException {
		logger.trace("getCollectionTime()={}, stack trace {}", collectionTime, Arrays.toString(Thread.currentThread().getStackTrace()));
		return collectionTime;
	}

	@Override
	public void setCollectionTime(double collectionTime) throws DeviceException {
		logger.trace("setCollectionTime({}) stack trace {}", collectionTime, Arrays.toString(Thread.currentThread().getStackTrace()));
		this.collectionTime = collectionTime;
	}

	/**
	 * Default which should be overridden by subclasses.
	 *
	 * @see gda.device.Detector#getDataDimensions()
	 */
	@Override
	public int[] getDataDimensions() throws DeviceException {
		return new int[] { 1 };
	}

	/**
	 * By default, the position will be the collection time. So test to see if the supplied object can be converted to a
	 * double.
	 *
	 *@see gda.device.Scannable#checkPositionValid(Object)
	 */
	@Override
	public String checkPositionValid(Object position) {

		Double[] convertedPosition = ScannableUtils.objectToArray(position);

		if (convertedPosition == null) {
			return "requested collection time could not be converted to a Double";
		}
		if (convertedPosition.length > 1) {
			return "collection time must be a single number";
		}

		return null;
	}

	/**
	 * Default which should be overridden by subclasses.
	 *
	 * @see gda.device.Detector#endCollection()
	 */
	@Override
	public void endCollection() throws DeviceException {
	}

	/**
	 * Default which should be overridden by subclasses.
	 *
	 * @see gda.device.Detector#prepareForCollection()
	 */
	@Override
	public void prepareForCollection() throws DeviceException {
		logger.trace("prepareForCollection()");
	}

	/**
	 * Default implementation is to set the collection time and to call the collectData method
	 *
	 * @see gda.device.Scannable#asynchronousMoveTo(java.lang.Object)
	 */
	@Override
	public void asynchronousMoveTo(Object collectionTime) throws DeviceException {
		throwExceptionIfInvalidTarget(collectionTime);
		Double[] collectionTimeArray = ScannableUtils.objectToArray(collectionTime);
		setCollectionTime(collectionTimeArray[0]);
		collectData();
	}

	/**
	 * {@inheritDoc} Also, if createsOwnFiles attempts to register the file with all FileRegistrars (primarily for data
	 * archiving). Note that this method will be called if the detector is triggered from a 'pos' command but not if the
	 * detector is triggered by the scan command (which ignores the detector's scannable interface).
	 *
	 * @see gda.device.Scannable#getPosition()
	 */
	@Override
	public Object getPosition() throws DeviceException {
		Object ob = this.readout();
		if (createsOwnFiles()) {
			try {
				String filepath = (String) ob;
				FileRegistrarHelper.registerFile(filepath);
			} catch (ClassCastException e) {
				logger
						.error(getName()
								+ ": exception in getPosition. This detector indicates that it createsOwnFiles, but its readout method does not return a path: "
								+ e.getMessage());
			}
		}
		return ob;
	}

	/**
	 * {@inheritDoc} default implementation is to call the getStatus method
	 *
	 * @see gda.device.Scannable#isBusy()
	 */
	@Override
	public boolean isBusy()  throws DeviceException{
			return getStatus() == BUSY;
	}

	@Override
	public String toFormattedString() {
		String message = getName();

		try {
			int status = this.getStatus();
			if (status == IDLE) {
				message += " : status=IDLE";
			} else if (status == BUSY) {
				message += " : status=BUSY";

			} else if (status == PAUSED) {
				message += " : status=PAUSED";

			} else if (status == STANDBY) {
				message += " : status=STANDBY";

			} else if (status == FAULT) {
				message += " : status=FAULT";

			} else if (status == MONITORING) {
				message += " : status=MONITORING";
			}
		} catch (DeviceException e) {
			message += " : Error reading status";
			logger.error("Failed to get status of detector " + getName() + " :" + e.getMessage(),e);
		}

		return message;
	}

	@Override
	public String getDescription() throws DeviceException {
		return "DetectorBase";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return "detectorbase-1";
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "DetectorBase";
	}

}