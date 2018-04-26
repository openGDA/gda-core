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

package gda.device.temperature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.factory.FactoryException;

/**
 * This class is designed to emulate Lake shore 340 temperature controller.
 * <p>
 * Example bean definition in Spring
 * <pre>
 * {@code
 <bean id="lakeshore_controller" class="gda.device.temperature.DummyLakeshore340Controller">
	<property name="local" value="true"/>
	<property name="configureAtStartup" value="true"/>
</bean>
 * }
 */
public class DummyLakeshore340Controller extends DeviceBase implements ILakeshoreController {
	/**
	 *
	 */
	@SuppressWarnings("unused")
	private static final long serialVersionUID = 6189569607134958641L;

	/**
	 * the logger instance
	 */
	private static final Logger logger = LoggerFactory.getLogger(DummyLakeshore340Controller.class);

	/**
	 * Channel to use for read back
	 */
	private int readbackChannel = 0;

	private final static double ZERO_DEGREE_CELSIUS=273.15; //K
	private final static double RAMP_RATE=2; //2K/min
	private final static double MINUTE_IN_NANOSECOND=60.0*1e+9;

	private double demandTemperaure=ZERO_DEGREE_CELSIUS;
	private double currentTemperature=ZERO_DEGREE_CELSIUS;
	private long startTime;

	/**
	 * Constructor
	 */
	public DummyLakeshore340Controller() {

	}

	@Override
	public void configure() throws FactoryException {
		if (!isConfigured()) {
			if (readbackChannel < 0 || readbackChannel > 3) {
				throw new FactoryException("Readback channel must be between 0 and 3 inclusive.");
			}

			setConfigured(true);
		}// end of if (!configured)

	}

	/**
	 * Sets the demand temperature . If ramping is enabled then the actual demand temperature will move
	 * towards this at the ramping rate.
	 *
	 * @param demandTemperature
	 *            The demanded temperature in K
	 * @throws DeviceException
	 */
	@Override
	public void setTargetTemp(double demandTemperature) throws DeviceException {
		try {
			logger.info("Setting demand temperature to {} for ouput {}", demandTemperature);
			this.demandTemperaure = demandTemperature;
			startTime=System.nanoTime();
		} catch (Exception e) {
			logger.error("Error setting the demand temperature to {} for output {}", demandTemperature, e);
			throw new DeviceException("Error setting value in Lakeshore 340 Temperature Controller device", e);
		}
	}

	/**
	 * Gets the current demand temperature in K.
	 *
	 * @return The current demand temperature from the readback value
	 * @throws DeviceException
	 */
	@Override
	public double getTargetTemp() throws DeviceException {
		try {
			return demandTemperaure;
		} catch (Exception e) {
			logger.error("Error getting current demand temperature for output {}", e);
			throw new DeviceException("Error reading from Lakeshore 340 Temperature Controller device", e);
		}
	}

	/**
	 * gets current temperature
	 *
	 * @return temp
	 * @throws DeviceException
	 */
	@Override
	public double getTemp() throws DeviceException {
		switch (readbackChannel) {
		case 0:
			return getChannel0Temp();
		case 1:
			return getChannel1Temp();
		case 2:
			return getChannel2Temp();
		case 3:
			return getChannel3Temp();
		default:
			throw new IllegalStateException("Unknown channel specified");
		}
	}

	/**
	 * gets channel 0 temperature
	 *
	 * @return channel 0 temperature
	 * @throws DeviceException
	 */
	@Override
	public double getChannel0Temp() throws DeviceException {
		try {
			if (getTargetTemp()> currentTemperature) {
				return currentTemperature += scalledTemperatureChange(1);
			} else if (getTargetTemp() < currentTemperature) {
				return currentTemperature -= scalledTemperatureChange(1);
			} else {
				return currentTemperature;
			}
		} catch (Exception e) {
			logger.error("Error trying to get temperature ", e);
			throw new DeviceException("Error reading from Lakeshore 340 Temperature Controller device", e);
		}
	}

	private double scalledTemperatureChange(double scale) {
		return scale*RAMP_RATE*(System.nanoTime()-startTime)/MINUTE_IN_NANOSECOND;
	}

	/**
	 * gets channel 1 temperature
	 *
	 * @return channel 1 temperature
	 * @throws DeviceException
	 */
	@Override
	public double getChannel1Temp() throws DeviceException {
		try {
			if (getTargetTemp()> currentTemperature) {
				return currentTemperature += scalledTemperatureChange(0.9);
			} else if (getTargetTemp() < currentTemperature) {
				return currentTemperature -= scalledTemperatureChange(0.9);
			} else{
				return currentTemperature;
			}
		} catch (Exception e) {
			logger.error("Error trying to get temperature ", e);
			throw new DeviceException("Error reading from Lakeshore 340 Temperature Controller device", e);
		}
	}

	/**
	 * gets channel 2 temperature
	 *
	 * @return channel 2 temperature
	 * @throws DeviceException
	 */
	@Override
	public double getChannel2Temp() throws DeviceException {
		try {
			if (getTargetTemp()> currentTemperature) {
				return currentTemperature += scalledTemperatureChange(0.8);
			} else if (getTargetTemp() < currentTemperature) {
				return currentTemperature -= scalledTemperatureChange(0.8);
			} else {
				return currentTemperature;
			}
		} catch (Exception e) {
			logger.error("Error trying to get temperature ", e);
			throw new DeviceException("Error reading from Lakeshore 340 Temperature Controller device", e);
		}
	}

	/**
	 * gets channel 3 temperature
	 *
	 * @return channel 3 temperature
	 * @throws DeviceException
	 */
	@Override
	public double getChannel3Temp() throws DeviceException {
		try {
			if (getTargetTemp()> currentTemperature) {
				return currentTemperature += scalledTemperatureChange(0.7);
			} else if (getTargetTemp() < currentTemperature) {
				return currentTemperature -= scalledTemperatureChange(0.7);
			} else {
				return currentTemperature;
			}
		} catch (Exception e) {
			logger.error("Error trying to get temperature ", e);
			throw new DeviceException("Error reading from Lakeshore 340 Temperature Controller device", e);
		}
	}

	@Override
	public int getReadbackChannel() {
		return readbackChannel;
	}

	@Override
	public void setReadbackChannel(int readbackChannel) {
		if (readbackChannel < 0 || readbackChannel > 3) {
			logger.error("Temperature channel must be between 0 and 3 inclusive");
			throw new RuntimeException("Temperature channel must be between 0 and 3 inclusive");
		}
		this.readbackChannel = readbackChannel;
	}

}
