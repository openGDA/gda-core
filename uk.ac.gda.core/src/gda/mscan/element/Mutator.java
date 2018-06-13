/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package gda.mscan.element;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import java.util.List;

/**
 * Links the revised mscan syntax to a typed representation of the possible scanpath mutator specifiers.
 * @since GDA 9.9
 */
public enum Mutator implements IMScanElementEnum {
	SNAKE("snak"),
	RANDOM_OFFSET("roff");

	private final String text;

	private Mutator(final String text) {
		this.text = text;
	}

	/**
	 * The default text values that correspond to the instances of Roi
	 *
	 * @return		List of default text for the instances
	 */
	public static List<String> strValues() {
		return stream(values()).map(val -> val.text).collect(toList());
	}
}
