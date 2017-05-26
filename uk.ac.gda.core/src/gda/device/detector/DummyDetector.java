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

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.scannable.DummyScannable;
import gda.factory.FactoryException;
import uk.ac.diamond.daq.concurrent.Async;

/**
 * <p>
 * A Dummy detector that will create random data at each point. It supports collection time so it will "acquire" for as
 * long as requested.
 * </p>
 * <p>
 * It is intended to be the detector equivalent of {@link DummyScannable} used for testing scanning. e.g
 * <pre>
 *   ds = DummyScannable('ds')
 *   dd = DummyDetector('dd')
 *   scan ds 0 10 1 dd 0.1
 * </pre>
 */
public class DummyDetector extends DetectorBase {

	private double maxDataValue = 10.0;
	private int status;
	private final Random random = new Random();
	private transient Future<?> future;

	/**
	 * No arg constructor for subclasses and Spring. Does not configure.
	 */
	public DummyDetector() {
		setInputNames(new String[0]);
	}

	/**
	 * Constructor for easy use from Jython e.g.
	 *
	 * <pre> dd = DummyDetector('dd')</pre>
	 *
	 * Constructs and configures the detector
	 *
	 * @param name
	 */
	public DummyDetector(String name) {
		setName(name);
		setInputNames(null);
		try {
			configure();
		} catch (FactoryException e) {
			throw new RuntimeException("Failed to configure: " + getName(), e);
		}
	}


	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}


	@Override
	public void reconfigure() throws FactoryException {
		configure();
	}

	@Override
	public void configure() throws FactoryException {
		// If extra names are not set
		if(getExtraNames() == null || getExtraNames().length == 0) {
			setExtraNames(new String[]{getName()}); // Set extra name as device name
		}

		status = Detector.IDLE;
	}

	@Override
	public void collectData() throws DeviceException {
		status = Detector.BUSY;

		// Calculate the collection time in ms to simulate acquiring
		final long collectionTimeMillis = (long) (collectionTime * 1000);

		// Start a task generating the data so this returns instantly
		future = Async.schedule(this::acquireData, collectionTimeMillis, MILLISECONDS);
	}

	private Object acquireData() {
		// Generate data. Return an array of size extraNames.length containing random doubles between 0 and maxDataValue
		return random.doubles(extraNames.length, 0, maxDataValue).toArray();
	}

	@Override
	public void waitWhileBusy() throws DeviceException, InterruptedException {
		try {
			future.get(); // blocks while data is acquired
		} catch (ExecutionException e) {
			throw new DeviceException("Exception while acquiring the data", e);
		}
		status = Detector.IDLE;
	}

	@Override
	public int getStatus() throws DeviceException {
		return status;
	}

	@Override
	public Object readout() throws DeviceException {
		try {
			return future.get();
		} catch (ExecutionException | InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException("Exception during readout", e);
		}
	}

	@Override
	public String getDescription() throws DeviceException {
		return "Dummy Detector";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return "dumbdumb-1";
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "Dummy";
	}

	public double getMaxDataValue() {
		return maxDataValue;
	}

	/**
	 * Sets the max data value. The data returned will be between 0 and this value
	 *
	 * @param maxDataValue
	 */
	public void setMaxDataValue(double maxDataValue) {
		this.maxDataValue = maxDataValue;
	}

	/**
	 * Allows the random seed to be set for reproducible testing.
	 *
	 * @param seed
	 */
	public void setRandomSeed(long seed) {
		random.setSeed(seed);
	}

}