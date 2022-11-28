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
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import gda.util.findableHashtable.FindableHashtable;

/**
 * Test class for {@link FindableHashtable} and {@link JsonMessageConverter} unit test.
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
	@JsonCreator
	public TestClass(
			@JsonProperty("int") final int i,
			@JsonProperty("double") final double d) {
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

	@Override
	public int hashCode() {
		return Objects.hash(d, i);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TestClass other = (TestClass) obj;
		return Double.doubleToLongBits(d) == Double.doubleToLongBits(other.d) && i == other.i;
	}

	@Override
	public String toString() {
		return "TestClass [i=" + i + ", d=" + d + "]";
	}
}
