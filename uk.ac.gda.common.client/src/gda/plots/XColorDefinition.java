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

/**
 * A class which represents an X11 style color definition.
 */
class XColorDefinition {
	private int red;

	private int green;

	private int blue;

	private String name;

	/**
	 * Constructor.
	 */
	public XColorDefinition() {
	}

	/**
	 * Constructor which makes an XColorDefinition from a string.
	 *
	 * @param fromFile
	 *            the string
	 */
	public XColorDefinition(String fromFile) {
		SimpleStringTokenizer strtok = new SimpleStringTokenizer(fromFile);

		// The String fromFile should have the form "255 255 255 white"
		red = Integer.parseInt(strtok.nextToken());
		green = Integer.parseInt(strtok.nextToken());
		blue = Integer.parseInt(strtok.nextToken());
		name = new String(strtok.restOfTokens());
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the red component.
	 *
	 * @return the red component
	 */
	public int getRed() {
		return red;
	}

	/**
	 * Gets the green component.
	 *
	 * @return the green component
	 */
	public int getGreen() {
		return green;
	}

	/**
	 * Gets the blue component.
	 *
	 * @return the blue component
	 */
	public int getBlue() {
		return blue;
	}

	/**
	 * Returns a String to represent the XColorDefinition.
	 *
	 * @return a String (same format as found in rgb.txt files
	 */
	@Override
	public String toString() {
		return ("red " + red + " green " + green + " blue " + blue + " " + name);
	}
}
