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

package gda.device.monitor;

import static tec.units.indriya.AbstractUnit.ONE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Adc;
import gda.device.DeviceException;
import gda.factory.FactoryException;
import gda.factory.Finder;

/**
 * Class to monitor an ADC
 */

public class AdcMonitor extends MonitorBase {

	private static final Logger logger = LoggerFactory.getLogger(AdcMonitor.class);

	private Adc adc;

	private long pollTime = 1000;

	private int channel = 0;

	private String adcName;

	private double lastValue = 0.0;

	/**
	 * Constructor.
	 */
	public AdcMonitor() {
	}

	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			return;
		}
		this.setInputNames(new String[]{adcName});
		logger.debug("Finding: " + adcName);
		if ((adc = (Adc) Finder.find(adcName)) == null) {
			throw new FactoryException("Adc " + adcName + " not found");
		} else {
			new Thread(this::run, getClass().getName()).start();
		}
		setConfigured(true);
	}

	/**
	 * @param adcName
	 */
	public void setAdcName(String adcName) {
		this.adcName = adcName;
	}

	/**
	 * @return name of the monitored adc
	 */
	public String getAdcName() {
		return adcName;
	}

	/**
	 * @param channel
	 */
	public void setChannel(int channel) {
		this.channel = channel;
	}

	/**
	 * @return name of the monitored channel
	 */
	public int getChannel() {
		return channel;
	}

	/**
	 * @param pollTime
	 *            the poll time in milliseconds
	 */
	public void setPollTime(long pollTime) {
		this.pollTime = pollTime;
	}

	/**
	 * @return The poll time.
	 */
	public long getPollTime() {
		return pollTime;
	}

	private synchronized void run() {
		while (true) {
			try {
				Double value = new Double(adc.getVoltage(channel));
				lastValue = value;
				notifyIObservers(this, value);
				wait(pollTime);
			} catch (DeviceException e) {
				logger.error("Error reading adc voltage", e);
			} catch (InterruptedException ie) {
				// do nothing just try again
			}
		}
	}

	/**
	 * Returns the latest value of the ADC's channel this class is monitoring {@inheritDoc}
	 *
	 * @see gda.device.Scannable#getPosition()
	 */
	@Override
	public Object getPosition() throws DeviceException {
		return lastValue;
	}

	@Override
	public int getElementCount() throws DeviceException {
		return 1;
	}

	@Override
	public String getUnit() throws DeviceException {
		// just a number
		return ONE.toString();
	}
}
