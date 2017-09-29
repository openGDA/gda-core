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

import java.util.StringTokenizer;

/**
 * A class which extends StringTokenizer to provide the ability to concatenate remaining tokens.
 */
class SimpleStringTokenizer extends StringTokenizer {
	/**
	 * Constructor
	 *
	 * @param theString
	 *            the string to tokenize
	 */
	SimpleStringTokenizer(String theString) {
		super(theString);
	}

	/**
	 * Concatenates the rest of the tokens.
	 *
	 * @return any remaining tokens concatenated into a single string.
	 */
	String restOfTokens() {
		// If there are no more tokens then "" will be returned.
		String rtrn = "";

		while (countTokens() > 1)
			rtrn = rtrn + nextToken() + " ";

		// The last token is treated differently to avoid having a space
		// at the end of the string.
		if (countTokens() == 1)
			rtrn = rtrn + nextToken();

		return (rtrn);
	}
}
