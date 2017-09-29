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

import java.awt.BasicStroke;
import java.awt.Stroke;

/**
 * An enum of the various line patterns allowed in SimplePlots
 */
enum Pattern {
	SOLID("Solid");

	private float[] pattern = null;

	private String bestName;

	private Pattern(String bestName, float... pattern) {
		this.bestName = bestName;

		// For the SOLID case the passed in pattern will be an array of
		// length 0. We want to retain the value null for pattern because
		// a null dash_array works in BasicStroke but an empty dash_array
		// causes an error.
		if (pattern.length > 0) {
			this.pattern = pattern;
		}
	}

	/**
	 * Returns the actual array of floats which represents the pattern
	 *
	 * @return the array of floats
	 */
	public float[] getPattern() {
		return pattern;
	}

	/**
	 * Returns the Stroke corresponding to the Pattern for a given lineWidth
	 *
	 * @param lineWidth
	 *            the line width in pixels
	 * @return a Stroke of the correct pattern
	 */
	Stroke getStroke(int lineWidth) {
		return new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 0, pattern, 0);
	}

	/**
	 * Returns the display name (which may be different from the Enum name)
	 *
	 * @return the display name
	 */
	@Override
	public String toString() {
		return bestName;
	}
}
