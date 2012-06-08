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

package gda.epics.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * JCAUtils Class
 */
public class JCAUtils {
	/**
	 * 
	 */
	public static final SimpleDateFormat defaultFormatter = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss.SSS");

	/**
	 * Default constructor
	 */
	public JCAUtils() {
	}

	/**
	 * Generates a timestamp
	 * 
	 * @return String timestamp with the current time
	 */
	public static String timeStamp() {
		Date now = new Date();
		return defaultFormatter.format(now);
	}

	/**
	 * Generates a timestamp given a pattern
	 * 
	 * @param pattern
	 *            appropriate for SimpleDateFormat
	 * @return String timestamp with the current time
	 */
	public static String timeStamp(String pattern) {
		SimpleDateFormat dateFormatter = new SimpleDateFormat(pattern);
		Date now = new Date();
		return dateFormatter.format(now);
	}

	/**
	 * @param inputString a string
	 * @return an array that can be put into caput as int[]
	 */
	@SuppressWarnings("cast")
	public static int[] getIntArrayFromWaveform(String inputString) {
		int[] waveform = new int[inputString.length()];
		for (int i = 0; i < inputString.length(); i++) {
			char c = inputString.charAt(i);
			waveform[i] = (int) c;
		}
		return waveform;
	}
}