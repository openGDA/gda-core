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

package gda.plots;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deals with creating java versions of X11 colors.
 */
class XColors {
	private static final Logger logger = LoggerFactory.getLogger(XColors.class);

	private static String solarisName = new String("/usr/openwin/lib/X11/rgb.txt");

	private static String properName = new String("/usr/lib/X11/rgb.txt");

	private static ArrayList<XColorDefinition> theColors;

	private static boolean fileRead = false;

	private static boolean available = false;

	private XColors() {
	}

	/**
	 * Reads the X11 color definition file.
	 */
	private static void readFile() {
		theColors = new ArrayList<XColorDefinition>();
		String line;
		BufferedReader in = null;

		// Try to open the file, first using the name it ought to have,
		// then using the usual Solaris name.
		try {
			in = new BufferedReader(new FileReader(properName));
			available = true;
		} catch (FileNotFoundException fnfe1) {
			try {
				in = new BufferedReader(new FileReader(solarisName));
				available = true;
			} catch (FileNotFoundException fnfe2) {
			}
		}

		// If the file is available then create an array of XColorDefinitions
		// from it.
		if (in != null) {

			try {
				while ((line = in.readLine()) != null) {
					if (!line.startsWith("!")) {
						XColorDefinition next = new XColorDefinition(line);
						theColors.add(next);
					}
				}

				in.close();
			} catch (Exception e) {
				logger.debug("Caught exception " + e + " trying to read XColors");
			}
		}
		fileRead = true;
	}

	/**
	 * Creates a name for a given Color - either by finding it in the list of X11 colour definitions or defaulting to an
	 * rgb string.
	 *
	 * @param color
	 *            the Color
	 * @return a name for Color
	 */
	public static String name(Color color) {
		int red = color.getRed();
		int green = color.getGreen();
		int blue = color.getBlue();

		String name = "(" + red + "," + green + "," + blue + ")";

		if (!fileRead)
			readFile();

		if (!available)
			return (name);

		for (XColorDefinition xd : theColors) {
			if (xd.getRed() == red && xd.getGreen() == green && xd.getBlue() == blue) {
				name = xd.getName();
				break;
			}
		}
		return name;
	}
}
