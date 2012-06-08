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

package gda.device.digitalio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JniDigitalIO Class
 */
public class JniDigitalIO {
	private static final Logger logger = LoggerFactory.getLogger(JniDigitalIO.class);

	private native int jConfigureDigitalIORead(String channelName);

	private native int jConfigureDigitalIOWrite(String channelName);

	private native int jStartDigitalIO(int taskHandle);

	private native int jStopDigitalIO(int taskHandle);

	private native int jReadPort(int taskHandle, byte data[]);

	private native int jReadChannel(int taskHandle, byte data[], int sizeOfData);

	private native int jWritePort(int taskHandle, byte data[]);

	private native int jWriteChannel(int taskHandle, byte data[], int sizeOfData);

	private String errorMessage = new String("");

	// Load the 'jNIDigitalIO.dll" library which interfaces this Java class
	// to the JNI NI DigitalIO system Windows dll file via the
	// Java native interface.
	static {
		try {
			System.loadLibrary("jNIDigitalIO");
			logger.info("Loaded jNIDigitalIO.dll file");
		} catch (Exception e) {
			logger.error("Error loading jNIDigitalIO.dll file");
		}
	}

	/**
	 * @return error message
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * @param channelName
	 * @return channel handle
	 */
	public int configureDigitalIORead(String channelName) {
		int channelHandle = jConfigureDigitalIORead(channelName);

		return channelHandle;
	}

	/**
	 * @param channelName
	 * @return channel handle
	 */
	public int configureDigitalIOWrite(String channelName) {
		int channelHandle = jConfigureDigitalIOWrite(channelName);

		return channelHandle;
	}

	/**
	 * @param channelHandle
	 * @return int
	 */
	public int start(int channelHandle) {
		return jStartDigitalIO(channelHandle);
	}

	/**
	 * @param channelHandle
	 * @return int
	 */
	public int stop(int channelHandle) {
		return jStopDigitalIO(channelHandle);
	}

	/**
	 * @param channelHandle
	 * @param data
	 * @return int
	 */
	public int readPort(int channelHandle, byte data[]) {
		return jReadPort(channelHandle, data);
	}

	/**
	 * @param channelHandle
	 * @param data
	 * @param sizeOfData
	 * @return int
	 */
	public int readChannel(int channelHandle, byte data[], int sizeOfData) {
		return jReadChannel(channelHandle, data, sizeOfData);
	}

	/**
	 * @param channelHandle
	 * @param data
	 * @return int
	 */
	public int writePort(int channelHandle, byte data[]) {
		return jWritePort(channelHandle, data);
	}

	/**
	 * @param channelHandle
	 * @param data
	 * @param sizeOfData
	 * @return int
	 */
	public int writeChannel(int channelHandle, byte data[], int sizeOfData) {
		return jWriteChannel(channelHandle, data, sizeOfData);
	}
}
