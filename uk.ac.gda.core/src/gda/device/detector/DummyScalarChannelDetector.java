/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.detector.countertimer.DummyCounterTimer;
import gda.device.scannable.ScannableUtils;
import gda.factory.FactoryException;

/**
 * A class to represent a single Scalar Channel as a detector. It requires a {@link DummyCounterTimer} to simulate the Scalar.
 */
public class DummyScalarChannelDetector extends DetectorBase {

	private static final Logger logger = LoggerFactory.getLogger(DummyScalarChannelDetector.class);

	private String name;

	private int scalerChannelIndex;

	private DummyCounterTimer scaler;

	@Override
	public void configure() throws FactoryException {
		if (!isConfigured()) {
			setConfigured(true);
		}
	}

	/**
	 * Sets the collection time for the scalers
	 *
	 * @param time
	 *            period to count
	 * @throws DeviceException
	 */
	@Override
	public void setCollectionTime(double time) throws DeviceException {
		getScaler().setCollectionTime(time);
	}

	@Override
	public void collectData() throws DeviceException {
		getScaler().collectData();
	}

	@Override
	public int getStatus() throws DeviceException {
		return getScaler().getStatus();
	}

	@Override
	public Object readout() throws DeviceException {
		return ScannableUtils.objectToArray(getScaler().readout())[scalerChannelIndex];
	}

	@Override
	public int[] getDataDimensions() throws DeviceException {
		return new int[] { 1 };
	}

	@Override
	public void prepareForCollection() throws DeviceException {
		getScaler().prepareForCollection();
	}

	@Override
	public String toString() {
		try {
			// get the current position as an array of doubles
			Object position = getPosition();
			// if position is null then simply return the name
			if (position == null) {
				return getName() + ": Not available.";
			}
			return getName() + " : " + position.toString();
		} catch (Exception e) {
			logger.warn("{}: exception while getting position", getName(), e);
			return valueUnavailableString();
		}
	}

	public void update(Object theObserved, Object changeCode) {
		notifyIObservers(theObserved, changeCode);
	}

	public int getScalerChannelIndex() {
		return scalerChannelIndex;
	}

	public void setScalerChannelIndex(int scalerChannelIndex) {
		this.scalerChannelIndex = scalerChannelIndex;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	@Override
	public String getDescription() throws DeviceException {
		return "Dummy Scalar Channel as Detector";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return getScaler().getName() + " channel " + scalerChannelIndex;
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "Scalar Channel Count";
	}

	public DummyCounterTimer getScaler() {
		return scaler;
	}

	public void setScaler(DummyCounterTimer scaler) {
		this.scaler = scaler;
	}

}
