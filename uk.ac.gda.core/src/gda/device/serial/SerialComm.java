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

package gda.device.serial;

import gda.device.DeviceException;
import gda.device.Serial;
import gda.factory.FactoryException;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of the Serial class
 */
public class SerialComm extends SerialBase {

	private static final Logger logger = LoggerFactory.getLogger(SerialComm.class);

	private SerialPort serialPort;

	private OutputStream outputStream;

	private InputStream inputStream;

	private String portName;

	private String flowControl;

	private int flowControlMode;

	private boolean opened = false;

	// These are the SerialComm defaults
	private int baudRate = 9600;

	private int byteSize = SerialPort.DATABITS_8;

	private int parity = SerialPort.PARITY_NONE;

	private int stopBits = SerialPort.STOPBITS_1;

	private int flowControlIn = SerialPort.FLOWCONTROL_NONE;

	private int flowControlOut = SerialPort.FLOWCONTROL_NONE;

	/**
	 * Constructor
	 */
	public SerialComm() {
	}

	@Override
	public void configure() {
		try {
			openSerialPort(portName);
			if (opened) {
				serialPort.setSerialPortParams(baudRate, byteSize, stopBits, parity);
				logger.debug("Config Baud " + baudRate + " Bytesize " + byteSize + " Stops " + getStopBits()
						+ " Parity " + getParity());

				serialPort.setFlowControlMode(flowControlIn | flowControlOut);
				flowControlMode = serialPort.getFlowControlMode();
				logger.debug("Flowcontrol setting: " + flowControlMode);
			}
			configured = true;
		} catch (UnsupportedCommOperationException e) {
			logger.debug("Comm error " + e.toString());
		}
	}

	/**
	 * @param portName
	 *            String name of the serial port
	 */
	public void setPortName(String portName) {
		this.portName = portName;
	}

	/**
	 * @return String name of the serial port
	 */
	public String getPortName() {
		return portName;
	}

	/**
	 * Open the named serial port
	 *
	 * @param name
	 *            the name of the port to open
	 */
	private void openSerialPort(String name) {
		CommPortIdentifier portId;

		try {
			portId = CommPortIdentifier.getPortIdentifier(name);
			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				logger.debug("Opening port " + name);
				serialPort = (SerialPort) portId.open("SerialComm", 2000);
				inputStream = serialPort.getInputStream();
				outputStream = serialPort.getOutputStream();
				opened = true;
			}
		} catch (NoSuchPortException e1) {
			logger.error("Error : serial port " + name + " not found");
		} catch (PortInUseException e) {
			logger.error("Error : serial port " + name + " in use");
		} catch (IOException e) {
			logger.error("Error : accessing serial port " + name);
		}
	}

	/**
	 * set the baudrate
	 *
	 * @param baudRate
	 * @throws DeviceException
	 */
	@Override
	public void setBaudRate(int baudRate) throws DeviceException {
		switch (baudRate) {
		case Serial.BAUDRATE_0:
		case Serial.BAUDRATE_50:
		case Serial.BAUDRATE_75:
		case Serial.BAUDRATE_110:
		case Serial.BAUDRATE_134:
		case Serial.BAUDRATE_150:
		case Serial.BAUDRATE_200:
		case Serial.BAUDRATE_300:
		case Serial.BAUDRATE_600:
		case Serial.BAUDRATE_1200:
		case Serial.BAUDRATE_1800:
		case Serial.BAUDRATE_2400:
		case Serial.BAUDRATE_4800:
		case Serial.BAUDRATE_9600:
		case Serial.BAUDRATE_19200:
		case Serial.BAUDRATE_38400:
		case Serial.BAUDRATE_115200:
			this.baudRate = baudRate;
			break;
		default:
			throw new DeviceException("Unsupported baud rate setting: Using default");
		}
		if (opened) {
			try {
				serialPort.setSerialPortParams(baudRate, byteSize, stopBits, parity);
				logger.debug("Setting baudRate {}", baudRate);
			} catch (UnsupportedCommOperationException e) {
				throw new DeviceException("Error setting baudRate to " + baudRate,
						e);
			}
		}
	}

	/**
	 * @return int the baud rate
	 */
	public int getBaudRate() {
		return baudRate;
	}

	/**
	 * Set the number of data bits for data transfer
	 *
	 * @param byteSize
	 * @throws DeviceException
	 */
	@Override
	public void setByteSize(int byteSize) throws DeviceException {
		switch (byteSize) {
		case Serial.BYTESIZE_5:
			this.byteSize = SerialPort.DATABITS_5;
			break;
		case Serial.BYTESIZE_6:
			this.byteSize = SerialPort.DATABITS_6;
			break;
		case Serial.BYTESIZE_7:
			this.byteSize = SerialPort.DATABITS_7;
			break;
		case Serial.BYTESIZE_8:
			this.byteSize = SerialPort.DATABITS_8;
			break;
		default:
			throw new DeviceException("Invalid byte size");
		}
		if (opened) {
			try {
				serialPort.setSerialPortParams(baudRate, this.byteSize, stopBits, parity);
				logger.debug("Setting byteSize {}", byteSize);
			} catch (UnsupportedCommOperationException e) {
				throw new DeviceException("Error setting bytesize " + this.byteSize, e);
			}
		}
	}

	/**
	 * @return the no. data bits for data transfer
	 */
	public int getByteSize() {
		return this.byteSize;
	}

	/**
	 * This method only exists for castor instantiation, it does not physically set the parity
	 *
	 * @param parity
	 * @throws DeviceException
	 */
	@Override
	public void setParity(String parity) throws DeviceException {
		if (Serial.PARITY_NONE.equals(parity)) {
			this.parity = SerialPort.PARITY_NONE;
		} else if (Serial.PARITY_ODD.equals(parity)) {
			this.parity = SerialPort.PARITY_ODD;
		} else if (Serial.PARITY_EVEN.equals(parity)) {
			this.parity = SerialPort.PARITY_EVEN;
		} else if (Serial.PARITY_MARK.equals(parity)) {
			this.parity = SerialPort.PARITY_MARK;
		} else if (Serial.PARITY_SPACE.equals(parity)) {
			this.parity = SerialPort.PARITY_SPACE;
		} else {
			throw new DeviceException("Unsupported Parity: Using default");
		}
		if (opened) {
			try {
				serialPort.setSerialPortParams(baudRate, byteSize, stopBits, this.parity);
				logger.debug("Setting parity " + getParity());
			} catch (UnsupportedCommOperationException e) {
				throw new DeviceException("Error setting parity {}", parity, e);
			}
		}
	}

	/**
	 * @return String parity of serial port
	 */
	public String getParity() {
		String parityStr = Serial.PARITY_NONE;
		switch (this.parity) {
		case SerialPort.PARITY_NONE:
			parityStr = Serial.PARITY_NONE;
			break;
		case SerialPort.PARITY_ODD:
			parityStr = Serial.PARITY_ODD;
			break;
		case SerialPort.PARITY_EVEN:
			parityStr = Serial.PARITY_EVEN;
			break;
		case SerialPort.PARITY_MARK:
			parityStr = Serial.PARITY_MARK;
			break;
		case SerialPort.PARITY_SPACE:
			parityStr = Serial.PARITY_SPACE;
			break;
		}
		return parityStr;
	}

	/**
	 * set the number of stop bits to be used in data transfer
	 *
	 * @param stopBits
	 *            int the no. stop bits
	 * @throws DeviceException
	 */
	@Override
	public void setStopBits(int stopBits) throws DeviceException {
		switch (stopBits) {
		case Serial.STOPBITS_1:
			this.stopBits = SerialPort.STOPBITS_1;
			break;
		case Serial.STOPBITS_2:
			this.stopBits = SerialPort.STOPBITS_2;
			break;
		case Serial.STOPBITS_1_5:
			this.stopBits = SerialPort.STOPBITS_1_5;
			break;
		default:
			throw new DeviceException("Invalid stop bits value: Using default");
		}

		if (opened) {
			try {
				serialPort.setSerialPortParams(baudRate, byteSize, this.stopBits, parity);
				logger.debug("Setting stopBits " + getStopBits());
			} catch (UnsupportedCommOperationException e) {
				throw new DeviceException("Error setting stop bits to " + stopBits, e);
			}
		}
	}

	/**
	 * @return int no. stop bits
	 */
	public int getStopBits() {
		return stopBits;
	}

	@Override
	public void setFlowControl(String flowControl) throws DeviceException {
		this.flowControl = flowControl;
		if (flowControl.equals("xonxoff")) {
			flowControlIn = SerialPort.FLOWCONTROL_XONXOFF_IN;
			flowControlOut = SerialPort.FLOWCONTROL_XONXOFF_OUT;
		} else if (flowControl.equals("rtscts")) {
			flowControlIn = SerialPort.FLOWCONTROL_RTSCTS_IN;
			flowControlOut = SerialPort.FLOWCONTROL_RTSCTS_OUT;
		} else if (flowControl.equals("none")) {
			flowControlIn = SerialPort.FLOWCONTROL_NONE;
			flowControlOut = SerialPort.FLOWCONTROL_NONE;
		} else {
			throw new DeviceException("Invalid flow control value: Using default");
		}

		if (opened) {
			try {
				serialPort.setFlowControlMode(flowControlOut | flowControlIn);
				flowControlMode = serialPort.getFlowControlMode();
				logger.debug(" flowcontrol setting is: " + flowControlMode);
			} catch (UnsupportedCommOperationException e) {
				flowControlMode = serialPort.getFlowControlMode();
				logger.debug("There was an error setting the flow control");
				logger.debug("The current setting is" + flowControlMode);
			}
		}
	}

	/**
	 * @return String flow control
	 */
	public String getFlowControl() {
		return flowControl;
	}

	@Override
	public int getReadTimeout() throws DeviceException {
		return (serialPort.isReceiveTimeoutEnabled()) ? serialPort.getReceiveTimeout() : 0;
	}

	@Override
	public void close() {
		serialPort.close();
		opened = false;
		configured = false;
	}

	@Override
	public void reconfigure() throws FactoryException {
		if (!configured)
			configure();
	}

	// CharReadableDev methods ...

	@Override
	public void setReadTimeout(int time) throws DeviceException {
		if (opened) {
			try {
				if (time != 0)
					serialPort.enableReceiveTimeout(time);
				else
					serialPort.disableReceiveTimeout();
			} catch (UnsupportedCommOperationException e) {
				throw new DeviceException("Error setting read timeout to " + time, e);
			}
		}
	}

	@Override
	public char readChar() throws DeviceException {
		byte b[] = { 0 };

		if (opened) {
			try {
				if (inputStream.read(b, 0, 1) <= 0) {
					// We mostly should end up here when the read does not block
					// which is a bug. It seems to happen with gnu.io.SerialPort
					// rxtx 2.1-7 on Windows. See trac #1174
					throw new DeviceException("Device timeout or null reply");
				}
			} catch (IOException e) {
				throw new DeviceException("Error in readChar", e);
			}
		}

		// README to prevent data conversion error for chars > 255
		return (char) (b[0] & 0xff);
	}

	@Override
	public void flush() throws DeviceException {
		byte b[] = { 0 };

		if (opened) {
			try {
				while (true) {
					if (inputStream.available() == 0) {
						break;
					}
					inputStream.read(b, 0, 1);
				}
			} catch (IOException e) {
			}
		}
	}

	// CharWritableDev methods ...

	@Override
	public void writeChar(char c) throws DeviceException {
		if (opened) {
			try {
				outputStream.write((byte) c);
			} catch (IOException e) {
				throw new DeviceException("Error in writeChar", e);
			}
		}
	}
}
