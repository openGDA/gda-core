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

package gda.device;

/**
 * An interface for a distributed Serial (RS232) class
 */
public interface Serial extends Device, CharReadableDev, CharWriteableDev {
	/** Number of data bits = 5 */
	public final static int BYTESIZE_5 = 5;

	/** Number of data bits = 6 */
	public final static int BYTESIZE_6 = 6;

	/** Number of data bits = 7 */
	public final static int BYTESIZE_7 = 7;

	/** Number of data bits = 8 */
	public final static int BYTESIZE_8 = 8;

	/** Baud rate = 0 */
	public final static int BAUDRATE_0 = 0;

	/** Baud rate = 50 */
	public final static int BAUDRATE_50 = 50;

	/** Baud rate = 75 */
	public final static int BAUDRATE_75 = 75;

	/** Baud rate = 110 */
	public final static int BAUDRATE_110 = 110;

	/** Baud rate = 134 */
	public final static int BAUDRATE_134 = 134;

	/** Baud rate = 1500 */
	public final static int BAUDRATE_150 = 150;

	/** Baud rate = 200 */
	public final static int BAUDRATE_200 = 200;

	/** Baud rate = 300 */
	public final static int BAUDRATE_300 = 300;

	/** Baud rate = 600 */
	public final static int BAUDRATE_600 = 600;

	/** Baud rate = 1200 */
	public final static int BAUDRATE_1200 = 1200;

	/** Baud rate = 1800 */
	public final static int BAUDRATE_1800 = 1800;

	/** Baud rate = 2000 */
	public final static int BAUDRATE_2000 = 2000;

	/** Baud rate = 2400 */
	public final static int BAUDRATE_2400 = 2400;

	/** Baud rate = 3600 */
	public final static int BAUDRATE_3600 = 3600;

	/** Baud rate = 4800 */
	public final static int BAUDRATE_4800 = 4800;

	/** Baud rate = 7200 */
	public final static int BAUDRATE_7200 = 7200;

	/** Baud rate = 9600 */
	public final static int BAUDRATE_9600 = 9600;

	/** Baud rate = 14400 */
	public final static int BAUDRATE_14400 = 14400;

	/** Baud rate = 19200 */
	public final static int BAUDRATE_19200 = 19200;

	/** Baud rate = 38400 */
	public final static int BAUDRATE_38400 = 38400;

	/** Baud rate = 56000 */
	public final static int BAUDRATE_56000 = 56000;

	/** Baud rate = 57600 */
	public final static int BAUDRATE_57600 = 57600;

	/** Baud rate = 115200 */
	public final static int BAUDRATE_115200 = 115200;

	/** Baud rate = 128000 */
	public final static int BAUDRATE_128000 = 128000;

	/** Baud rate = 256000 */
	public final static int BAUDRATE_256000 = 256000;

	/** Number of stop bits = 1 */
	public final static int STOPBITS_1 = 1;

	/** Number of stop bits = 2 */
	public final static int STOPBITS_2 = 2;

	/** Number of stop bits = 1.5 */
	public final static int STOPBITS_1_5 = 3;

	/** Parity = no parity */
	public final static String PARITY_NONE = "none";

	/** Parity = odd parity */
	public final static String PARITY_ODD = "odd";

	/** Parity = even parity */
	public final static String PARITY_EVEN = "even";

	/** Parity = mark parity */
	public final static String PARITY_MARK = "mark";

	/** Parity = space parity */
	public final static String PARITY_SPACE = "space";

	/** No flow control */
	public final static String FLOWCONTROL_NONE = "none";

	/** xon xoff flow control */
	public final static String FLOWCONTROL_XONXOFF = "xonxoff";

	/** rts cts flow control */
	public final static String FLOWCONTROL_RTSCTS = "rtscts";

	/**
	 * Sets the baud rate to the required value
	 * 
	 * @param baudRate
	 *            number of bits/second
	 * @exception DeviceException
	 */
	public void setBaudRate(int baudRate) throws DeviceException;

	/**
	 * Sets the number of data bits to the required value
	 * 
	 * @param byteSize
	 *            number of data bits
	 * @exception DeviceException
	 */
	public void setByteSize(int byteSize) throws DeviceException;

	/**
	 * Sets the parity to the required value
	 * 
	 * @param parity
	 *            type of parity
	 * @exception DeviceException
	 */
	public void setParity(String parity) throws DeviceException;

	/**
	 * Sets the number of stop bits to the required value
	 * 
	 * @param stopBits
	 *            number of stop bits
	 * @exception DeviceException
	 */
	public void setStopBits(int stopBits) throws DeviceException;

	/**
	 * Gets the timeout value for single character reads
	 * 
	 * @return the timeout in milliseconds
	 * @exception DeviceException
	 */
	public int getReadTimeout() throws DeviceException;

	/**
	 * Close serial connection
	 * 
	 * @throws DeviceException
	 */
	@Override
	public void close() throws DeviceException;

	/**
	 * Sets the flow control
	 * 
	 * @param flowControl
	 * @throws DeviceException
	 */
	public void setFlowControl(String flowControl) throws DeviceException;
}
