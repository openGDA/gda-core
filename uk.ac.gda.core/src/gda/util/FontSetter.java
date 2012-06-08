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

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Vector;

/**
 * Provides a mechanism to obtain fonts by family name, style and size.
 */
public class FontSetter {
	private Vector<String> vector = null;

	String envfonts[];

	/**
	 * Constructor
	 */
	public FontSetter() {
		vector = new Vector<String>();
		GraphicsEnvironment gEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
		envfonts = gEnv.getAvailableFontFamilyNames();

		for (int i = 1; i < envfonts.length; i++) {
			vector.addElement(envfonts[i]);
		}
	}

	/**
	 * Gets a list of all available fonts in the current graphics environment.
	 * 
	 * @return Vector of all available fonts in the current graphics environment.
	 */
	public Vector<String> getAvailableFonts() {
		return (vector);
	}

	/**
	 * Constructs a font based on name, style and point size.
	 * 
	 * @param fontName
	 *            The font name.
	 * @param fontStyle
	 *            The style constant for the font.
	 * @param fontSize
	 *            The point size of the font.
	 * @return Constructed font.
	 * @see java.awt.Font
	 */
	public Font newFont(String fontName, int fontStyle, int fontSize) {
		int i = 0;
		Font newFont = null;

		while (i < envfonts.length)
			if (fontName.equals(envfonts[i])) {
				newFont = new Font(fontName, fontStyle, fontSize);
				i = envfonts.length;
			} else
				i++;

		return (newFont);
	}
}
