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

import java.io.Serializable;

/**
 * Test class for FindableHashtable unit test.
 */
public class TestClass implements Serializable {
	private int i;

	private double d;

	/**
	 * @param i
	 *            Internally stored int instance variable.
	 * @param d
	 *            Internally stored double instance variable.
	 */
	public TestClass(final int i, final double d) {
		this.i = i;
		this.d = d;
	}

	/**
	 * @return Contents of double instance variable.
	 */
	public double getDouble() {
		return d;
	}

	/**
	 * @return Contents of int instance variable.
	 */
	public int getInt() {
		return i;
	}
}
