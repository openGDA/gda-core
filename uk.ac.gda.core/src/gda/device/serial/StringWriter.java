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

import gda.device.CharWriteableDev;
import gda.device.DeviceException;

/**
 * Class to write a String to a character writeable device
 */
public class StringWriter {
	private CharWriteableDev charWritebleDev;

	/**
	 * 
	 */
	public StringProperties stringProps = new StringProperties();

	/**
	 * @param charWritebleDev
	 */
	public StringWriter(CharWriteableDev charWritebleDev) {
		this.charWritebleDev = charWritebleDev;
	}

	/**
	 * @param str
	 * @throws DeviceException
	 */
	public void write(String str) throws DeviceException {
		// call write(int) with maxlen = 0 ...
		write(str, 0);
	}

	/**
	 * @param str
	 * @param maxlen
	 * @throws DeviceException
	 */
	public void write(String str, int maxlen) throws DeviceException {
		/**
		 * FIXME - maxlen not used
		 */
		if (maxlen == 0) {
			maxlen = stringProps.getBufferSize();
		}
		/*
		 * We'll write the whole string only if the length of the string is less than the buffer size
		 */
		int strlen = ((str.length() < stringProps.getBufferSize()) ? str.length() : stringProps.getBufferSize());

		// write the string char by char
		for (int i = 0; i < strlen; i++) {
			charWritebleDev.writeChar(str.charAt(i));
		}

		// if write termination is on, append a termination string
		if (stringProps.getTermination()) {
			String term_str = stringProps.getTerminator();
			int term_len = term_str.length();

			for (int i = 0; i < term_len; i++) {
				charWritebleDev.writeChar(term_str.charAt(i));
			}
		}
	}
}
