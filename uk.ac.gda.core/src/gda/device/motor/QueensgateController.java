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

package gda.device.motor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.device.MotorException;
import gda.device.MotorStatus;
import gda.device.Serial;
import gda.device.serial.StringReader;
import gda.device.serial.StringWriter;
import gda.factory.FactoryException;
import gda.factory.Finder;

/**
 * A controller class for Queensgate piezos. It manages serial communication with individual piezo command modules
 * (there can be up to three in a box), ensuring that replies return to the correct module. The class also manages the
 * tasks common to all piezos - converting to and from module replies.
 */
public class QueensgateController extends DeviceBase implements PiezoController {

	private static final Logger logger = LoggerFactory.getLogger(QueensgateController.class);

	private Serial serial;

	private String serialDeviceName;

	private String parity = Serial.PARITY_EVEN;

	private int baudRate = Serial.BAUDRATE_9600;

	private int stopBits = Serial.STOPBITS_1;

	private int byteSize = Serial.BYTESIZE_7;

	private StringReader reader;

	private StringWriter writer;

	// TODO - does timeout need to be this big?
	private final static int READ_TIMEOUT = 5000;

	private static final String TERMINATOR = "\r\n";

	// private static final String DISABLE = "M01";
	private static final String START_CONVERSION = "N0";

	private static final String ADC_RESULT = "N4";

	private static final String SEND_REPLY = "?";

	private static final String ZERO_MODULES = "#0";

	private static final String SET_FOR_INPUT = "!QT";

	// private static final String POSITION_ZERO = "I000";
	// private static final String WRITE_TO_ALL_MODULES = "M73";

	/**
	 * Constructor
	 */
	public QueensgateController() {
	}

	@Override
	public void configure() {
		logger.debug("QueensgateController: finding: " + serialDeviceName);
		if ((serial = (Serial) Finder.getInstance().find(serialDeviceName)) == null) {
			logger.error("Serial Device " + serialDeviceName + " not found");
		} else {
			try {
				logger.debug("QueensgateController: baudrate= " + baudRate + "stopbits = " + stopBits + "byteSize = "
						+ byteSize + "parity = " + parity + " timeout = " + READ_TIMEOUT);
				serial.setBaudRate(baudRate);
				serial.setStopBits(stopBits);
				serial.setByteSize(byteSize);
				serial.setParity(parity);
				serial.setReadTimeout(READ_TIMEOUT);
				serial.flush();

				reader = new StringReader(serial);
				writer = new StringWriter(serial);
				// set up communicators
				reader.stringProps.setTerminator(TERMINATOR);
				writer.stringProps.setTerminator(TERMINATOR);

				initialiseModules();
			} catch (DeviceException e) {
				logger.error("Queensgate Controller: Exception while connecting the Serial Port", e);
			} catch (FactoryException fe) {
				logger.error("Error configuring {}", serialDeviceName, fe);
			}
		}
	}

	/**
	 * This method is required to setup the initial condition of the piezo. First, module 0 is selected (ie no modules)
	 * then input from the command module is enabled on Ports Q through T. The positions are set to zero on all modules
	 * then the modules are deselected and the input/output gate is disabled.
	 *
	 * @throws FactoryException
	 */
	private void initialiseModules() throws FactoryException {
		try {
			logger.debug("Queensgate Controller: init cmds " + ZERO_MODULES + SET_FOR_INPUT /*
																							 * + POSITION_ZERO +
																							 * WRITE_TO_ALL_MODULES +
																							 * DISABLE
																							 */);

			writer.write(ZERO_MODULES);
			writer.write(SET_FOR_INPUT);
		} catch (DeviceException de) {
			throw new FactoryException("Queensgate Controller: Exception thrown at initialisation.", de);
		}
	}

	/**
	 * @param serialDeviceName
	 */
	public void setSerialDeviceName(String serialDeviceName) {
		this.serialDeviceName = serialDeviceName;
	}

	/**
	 * @return serialDeviceName
	 */
	public String getSerialDeviceName() {
		return serialDeviceName;
	}

	/**
	 * Used to send a command to the piezo module that does not require a reply.
	 *
	 * @param module
	 *            is the module number
	 * @param positionCommand -
	 *            the position to set in command
	 * @throws MotorException
	 */
	@Override
	public synchronized void setPosition(int module, String positionCommand) throws MotorException {
		// README The communication is in binary form, so module 3 must take the
		// binary value of 100, which is 4 in base 10.
		if (module == 3) {
			module = 4;
		}

		String moduleCommand = "M" + module + "1";
		try {
			logger.debug("Queensgate Controller: setposition cmd " + moduleCommand + positionCommand + "N3N1" /*
																												 * +
																												 * DISABLE
																												 */);

			writer.write(moduleCommand);
			Thread.sleep(1000);
			writer.write(positionCommand);
			Thread.sleep(1000);
			writer.write("N3"); // TODO - QG manual & emu say this is needed
			Thread.sleep(1000);
			writer.write("N1"); // TODO - QG manual & emu say this is needed
			Thread.sleep(2000);
		} catch (DeviceException de) {
			logger.error("Queensgate controller: Exception occurred sending the offset command.");
			throw new MotorException(MotorStatus.FAULT, de.toString());
		} catch (InterruptedException ie) {
			// do nothing
		}
	}

	/**
	 * Sends the commands required to return the current rawPosition (offset), converts this value to a position
	 * (offset) and compares with requested position (offset). Notifies when the process is complete.
	 *
	 * @param module
	 *            the module to query the position and status
	 * @return the reply string
	 * @throws MotorException
	 */
	@Override
	public synchronized String getPositionAndStatus(int module) throws MotorException {
		// README The communication is in binary form, so module 3 must take the
		// binary value of 100, which is 4 in base 10.
		if (module == 3) {
			module = 4;
		}

		String moduleCommand = "M" + module + "4";
		String reply = null;
		try {
			logger.debug("Queensgate Controller: getposition cmd " + moduleCommand + START_CONVERSION + ADC_RESULT
					+ SEND_REPLY);

			writer.write(moduleCommand);
			Thread.sleep(100);
			writer.write(START_CONVERSION);
			Thread.sleep(100);
			writer.write(ADC_RESULT);
			Thread.sleep(100);
			writer.write(SEND_REPLY);
			Thread.sleep(100);
			reply = reader.read().trim();
			Thread.sleep(100);

			logger.debug("Queensgate Controller: getposition reply " + module + " " + reply);

		} catch (DeviceException de) {
			logger.error("Queensgate controller: Exception caught getting a reply.");
			throw new MotorException(MotorStatus.FAULT, de.toString());
		} catch (InterruptedException ie) {
			// do nothing
		}

		return reply;
	}
}
