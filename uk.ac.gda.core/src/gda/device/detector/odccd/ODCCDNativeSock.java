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

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.io.socket.NativeSock;

/**
 * <p>
 * <b>Title: </b>Customized <code>NativeSock</code> class for Oxford Diffraction CCDs.
 * </p>
 * <p>
 * <b>Description: </b>This class extends <code>gda.io.socket.NativeSock</code> to provide a customised
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
	 * @return The input stream in string format up to the end of the pattern.
	 * @throws IOException
	 *             This is thrown if the IS software transmits an error.
	 */
	@Override
	public String readUntil(String pattern) throws IOException {
		return readUntil(pattern, null);
	}

	public String readUntil(String pattern, String altError) throws IOException {

		StringBuffer sb = new StringBuffer();
		String errorMsg = "(ERROR):";
		byte[] errorBytes = errorMsg.getBytes();
		byte last_errorByte = errorBytes[errorBytes.length - 1];
		try {
			char lastChar = pattern.charAt(pattern.length() - 1);
			char lastAltErrorChar = (altError == null ? '!' : altError.charAt(altError.length() - 1));
			byte ch = readByte();
			// Loop until finding the pattern. Then return.
			while (true) {
				sb.append((char) ch);
				// Check if what we have read so far contains an error message from IS.
				if ((ch == last_errorByte) && sb.toString().endsWith(errorMsg)) {
					// Read the rest of this line.
					sb.append(super.readLine());
					// Read the next line too, as this contains the error
					// message.
					sb.append(super.readLine());
					// Now return the error message in the exception
					String errorToReturn = "ERROR: IS sent an error: " + errorMsg;
					logger.error(errorToReturn);
					throw new IOException(errorToReturn);
				}
				// Or the alternate error message
				if (altError != null && (char) ch == lastAltErrorChar) {
					if (sb.toString().endsWith(altError)) {
						logger.info("IS sent an alt error: {}", altError);
						sb.append(super.readLine());
						String errorToReturn = "ERROR: IS sent an alt error: " + altError;
						logger.error(errorToReturn);
						throw new IOException(errorToReturn);
					}
				}
				// Or the pattern we are looking for
				if ((char) ch == lastChar) {
					if (sb.toString().endsWith(pattern)) {
						logger.info("IS sent the requested pattern {}", pattern);
						return (sb.toString());
					}
				}
				// Clear the buffer for every whole line we receive
				if (ch == 015) {
					logger.trace("IS sent line: {}", sb.toString());
					sb.setLength(0); // Clear buffer immediately after logging input
				}
				if (ch > 255) {
					logger.warn("IS sent a character > 255: {}", ch);
				}
				ch = readByte();
			}
		} catch (IOException e) {
			logger.error("Exception caught when trying to readUntil({}) in NativeSock: {}", pattern, e.toString());
			throw e;
		} finally {
			logger.trace("IS sent: {}", sb.toString());
		}
	}

}
