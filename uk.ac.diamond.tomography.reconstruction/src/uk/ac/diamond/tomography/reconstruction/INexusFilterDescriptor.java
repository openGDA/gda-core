/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.diamond.tomography.reconstruction;


public interface INexusFilterDescriptor {

	public enum Operation {
		// @formatter:off
		CONTAINS(0, "exists"),
		DOES_NOT_CONTAIN(0, "does not exist"),

		EQUALS(1, "="),
		NOT_EQUALS(1, "!="),
		GREATER_THAN(1, ">"),
		GREATER_THAN_OR_EQUAL(1, ">="),
		LESS_THAN_OR_EQUAL(1, "<="),
		LESS_THAN(1, "<"),

		OPEN_INTERVAL(2, "[]"),
		CLOSED_INTERVAL(2, "()"),
		LEFT_CLOSED_RIGHT_OPEN_INTERVAL(2, "(]"),
		LEFT_OPEN_RIGHT_CLOSED_INTERVAL(2, "[)");
		// @formatter:on
		final public int NUMBER_OF_OPERANDS;
		final public String DESCRIPTION;

		Operation(int numberOfOperands, String description) {
			this.NUMBER_OF_OPERANDS = numberOfOperands;
			this.DESCRIPTION = description;
		}
	}

	/**
	 * Returns the filter path for filtering nexus metadata
	 *
	 * @return the filter path string (must be non-<code>null</code>)
	 */
	public String getNexusFilterPath();

	/**
	 * Returns the filter operations e.g equals, greater than
	 *
	 * @return the filter operation (must be non-<code>null</code>)
	 */
	public Operation getNexusFilterOperation();

	/**
	 * Returns the filter operands to be applied in conjunction with the filter operation
	 *
	 * @return the filter operands, the number of operands must match getNexusFilterOperation().NUMBER_OF_OPERANDS
	 */
	public String[] getNexusFilterOperands();

	/**
	 * Return a string representation of the descriptor suitable for storing in preferences.
	 *
	 * @return string representation
	 */
	public String getMementoString();

}
