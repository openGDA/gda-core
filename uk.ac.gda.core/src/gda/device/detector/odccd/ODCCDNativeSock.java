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

package gda.device.detector.odccd;

import gda.io.socket.NativeSock;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * <b>Title: </b>Customized <code>NativeSock</code> class for Oxford Diffraction CCDs.
 * </p>
 * <p>
 * <b>Description: </b>This class extends <code>gda.io.socket.NativeSock</code> to provide a cutomized
 * <code>readUntil(String)</code> method. The method performs the same functions as in <code>NativeSock</code>
 * except that it throws an exception if it encounters an error in the messages transmitted from the IS software. This
 * avoids <code>readUntil(String)</code> hanging because it cannot find the string because IS sent an error message
 * instead.
 * </p>
 */
public class ODCCDNativeSock extends NativeSock {
	private static final Logger logger = LoggerFactory.getLogger(ODCCDNativeSock.class);

	/**
	 * Constructor. This sets the socket timeout suitable for use with IS.
	 */
	public ODCCDNativeSock() {
		super.setSocketTimeOut(50000);
	}

	/**
	 * This reads the input stream byte-by-byte searching for a pattern. This only works with ASCII characters (8 bits).
	 * An exception is thrown if the method encounters an error from the IS software. The exception would contain
	 * details of the error.
	 * 
	 * @param pattern
	 *            The pattern to read until.
	 * @return The input stream in string format upto the end of the pattern.
	 * @throws IOException
	 *             This is thrown if the IS software transmits an error.
	 */
	@Override
	public String readUntil(String pattern) throws IOException {

		StringBuffer sb = null;
		String errorMsg = "(ERROR):";
		byte[] errorBytes = errorMsg.getBytes();
		byte last_errorByte = errorBytes[errorBytes.length - 1];
		try {
			char lastChar = pattern.charAt(pattern.length() - 1);
			sb = new StringBuffer();
			byte ch = inputStream.readByte();
			// Loop until finding the pattern. Then return.
			while (true) {
				sb.append((char) ch);

				// Check if what we have read so far contains an error message
				// from
				// IS.
				if ((ch == last_errorByte) && sb.toString().endsWith(errorMsg)) {
					// Read the rest of this line.
					sb.append(super.readLine());
					// Read the next line too, as this contains the error
					// message.
					sb.append(super.readLine());
					// Now return the error message in the exception
					String errorToReturn = "ERROR: IS sent an error." + sb.toString();
					logger.error(errorToReturn);
					throw new IOException(errorToReturn);
				}

				if ((char) ch == lastChar) {
					if (sb.toString().endsWith(pattern)) {
						logger.trace("IS sent: {}", sb.toString());
						return (sb.toString());
					}
				}
				ch = inputStream.readByte();
			}
		} catch (IOException e) {
			logger.warn("Exception caught when trying to readUntil({}) in NativeSock. IS sent: {}", pattern, sb.toString());
			throw e;
		}
	}

}
