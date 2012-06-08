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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a Color from a String. The String may be a hexadecimal string of the form "#rrggbb", the name of a
 * pre-defined Color in the Color class or the name of any X11 color.
 */
public class ColorFactory {
	private static final Logger logger = LoggerFactory.getLogger(ColorFactory.class);

	/**
	 * Constructor.
	 */
	public ColorFactory() {
	}

	/**
	 * Tries to find a static field in the Color class with the given name.
	 * 
	 * @param colorName
	 *            the name
	 * @return the Color (if found)
	 */
	private static Color getJavaColor(String colorName) {
		// The Color class has some static fields e.g. Color.red which
		// are Color instances. We can find one of these using reflection
		// Color.class.getField("red") will return a Field with the Color.red
		// object as its object. Field.get(null) will return this as an
		// Object (null because it is a static field of class Color) and
		// we can cast it back to be a Color

		Color rtrn = null;

		try {
			logger.debug("Trying to find " + colorName + " in java.awt.Color");
			rtrn = (Color) Color.class.getField(colorName).get(null);
		} catch (NoSuchFieldException e) {
			logger.debug("Color class does not contain \"" + colorName + "\"");
		} catch (IllegalAccessException e) {
			logger.error("Color class has become secretive");
		}
		logger.debug("Found java.awt.Color " + rtrn);

		return (rtrn);
	}

	/**
	 * Tries to construct a Color from a String assumed to be in one of the hexadecimal forms which Color.decode()
	 * understands
	 * 
	 * @param colorName
	 *            the hexadecimal specification
	 * @return the Color (if the specification was valid)
	 */
	private static Color getHexadecimalColor(String colorName) {
		Color rtrn = null;
		try {
			logger.debug("trying to translate: " + colorName);
			rtrn = Color.decode(colorName);
		} catch (NumberFormatException e) {
			logger.error("Not a valid numerical color representation");
		}

		return (rtrn);
	}

	/**
	 * Tries to find a Color in the XColors class assuming colorName is an X11 color name.
	 * 
	 * @param colorName
	 *            the color name
	 * @return the Color (if found)
	 */
	private static Color getNamedColor(String colorName) {
		return (XColors.getNamedColor(colorName));
	}

	/**
	 * Tries to translate a String into a Color by various means.
	 * 
	 * @param colorString
	 *            the String
	 * @return the Color (if one of the translations succeeds)
	 */
	public static Color createColor(String colorString) {
		Color rtrn = null;

		// If the string starts with # it is assumed to be hexadecimal.
		if (colorString.startsWith("#")) {
			logger.debug("hexadecimal specification");
			rtrn = getHexadecimalColor(colorString);
		}
		// Otherwise it is assumed to be either a Java Color name or an
		// X11 name.
		else {
			logger.debug("trying Java color specification");
			rtrn = getJavaColor(colorString);
			if (rtrn == null) {
				logger.debug("Not a Java color");
				logger.debug("trying X11 named color specification");
				rtrn = getNamedColor(colorString);
				if (rtrn == null)
					logger.debug("Not an X11 named color");
			}
		}
		return (rtrn);
	}
}
