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

package gda.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stdio Class
 */
public class Stdio {
	private static final Logger logger = LoggerFactory.getLogger(Stdio.class);

	/**
	 * @param prompt
	 * @return string
	 */
	public static String get(String prompt) {
		String str = "";

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			logger.debug(prompt);
			str = in.readLine();
		} catch (IOException e) {
		}

		return str;
	}

	/**
	 * @return string
	 */
	public static String get() {
		String str = "";

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			str = in.readLine();
		} catch (IOException e) {
		}

		return str;
	}
}
