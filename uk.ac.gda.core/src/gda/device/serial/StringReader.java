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

import gda.device.CharReadableDev;
import gda.device.DeviceException;

/**
 * Class to read a String from a character readable device
 */
public class StringReader {
	private CharReadableDev charReadableDev;

	private boolean stripTermination = true;

	private char ch = (char) 0;

	private String readString;

	/**
	 * 
	 */
	public StringProperties stringProps = new StringProperties();

	/**
	 * @param charReadableDev
	 */
	public StringReader(CharReadableDev charReadableDev) {
		this.charReadableDev = charReadableDev;
	}

	/**
	 * @return char
	 * @throws DeviceException
	 */
	public char readchar() throws DeviceException {
		try {
			ch = charReadableDev.readChar();
		} catch (DeviceException de) {
			// we've timed out or there's a problem.
			// Store the string so far and throw up.
			throw de;
		}

		return ch;
	}

	/**
	 * @param maxlen
	 * @return String
	 * @throws DeviceException
	 */
	public String read(int maxlen) throws DeviceException {
		int str_end_ind = -1;

		if (maxlen == 0) {
			maxlen = stringProps.getBufferSize();
		}

		// create a 0 length string buffer
		StringBuffer str = new StringBuffer("");

		// read characters one by one from the CharReadableDev object, looking
		// for termination strings

		for (int i = 0; i < maxlen; i++) {
			try {
				ch = charReadableDev.readChar();
				str.append(ch);

				if (stringProps.getTermination() && ((str_end_ind = stringProps.isTerminated(str.toString())) != -1)) {
					// Termination string found.
					break;
				}
			} catch (DeviceException de) {
				// we've timed out or there's a problem.
				// Store the string so far and throw up.
				readString = str.toString();
				throw de;
			}
		}

		readString = str.toString();

		if ((!stripTermination) || (str_end_ind == -1))
			return readString;

		return (readString.substring(0, str_end_ind));
	}

	/**
	 * @return String
	 * @throws DeviceException
	 */
	public String read() throws DeviceException {
		// call read(int) with maxlen = 0 ...
		return read(0);
	}

	/**
	 * @throws DeviceException
	 */
	public void flush() throws DeviceException {
		// route to CharReadableDev object ...
		charReadableDev.flush();
	}

	/**
	 * FIXME - Please specify units for param time
	 * 
	 * @param timeout
	 * @throws DeviceException
	 */
	public void setTimeout(int timeout) throws DeviceException {
		// route to CharReadableDev object ...
		charReadableDev.setReadTimeout(timeout);
	}

	/**
	 * @param stripOn
	 *            Enable or disable removal of terminators from String
	 */
	public void setStripTerminators(boolean stripOn) {
		stripTermination = stripOn;
	}

	/**
	 * @return char
	 */
	public char getLastChar() {
		return ch;
	}

	/**
	 * @return String
	 */
	public String getLastString() {
		return readString;
	}
}
