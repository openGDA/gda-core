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

package uk.ac.diamond.daq.devices.keithley;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;

public class Keithley2600SeriesDwellBased extends Keithley2600Series {

	private static final Logger logger = LoggerFactory.getLogger(Keithley2600Series.class);

	public static final String ACQUIRE = "MeasOlapIVStart";
	public static final String MEAN_V = "MeanV";
	public static final String MEAN_I = "MeanI";

	@Override
	public Object rawGetPosition() throws DeviceException {
		try {
			epicsController.caputWait(getChannel(ACQUIRE), "Acquire");
		} catch (TimeoutException exception) {
			throw new DeviceException("Timed-out while waiting for acquisition.", exception);
		} catch (CAException exception) {
			throw new DeviceException("An error occured while initiating acquisition.", exception);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new DeviceException("Acquisition was interrupted.", exception);
		}

		double demand = getDemand();
		double meanVoltage = getMeanVoltage();
		double meanCurrent = getMeanCurrent();

		return new double[] { demand, meanVoltage, meanCurrent};
	}

	public double getMeanVoltage() throws DeviceException {
		try {
			return epicsController.cagetDouble(getChannel(MEAN_V));
		} catch (Exception e) {
			throw new DeviceException("Failed to get mean voltage", e);
		}
	}

	public double getMeanCurrent() throws DeviceException {
		try {
			return epicsController.cagetDouble(getChannel(MEAN_I));
		} catch (Exception e) {
			throw new DeviceException("Failed to get mean current", e);
		}
	}

	@Override
	protected void setupNamesAndFormat() {
		setInputNames(new String[] { "demand" });
		setExtraNames(new String[] { "mean voltage", "mean current" });
		setOutputFormat(new String[] { "%5.5g", "%5.5g", "%5.5g"  });
	}
}
