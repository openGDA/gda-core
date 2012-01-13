/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.richbeans.components.scalebox;

/**
 * Class used to contain value parsed from box.
 */
public class ValueResult {

	public ValueResult(final double d, final boolean b) {
		numericValue = d;
		matched      = b;
	}
	
	private double  numericValue;
	private boolean matched;
	/**
	 * @return Returns the numericValue.
	 */
	public double getNumericValue() {
		return numericValue;
	}
	/**
	 * @param numericValue The numericValue to set.
	 */
	public void setNumericValue(double numericValue) {
		this.numericValue = numericValue;
	}
	/**
	 * @return Returns the matched.
	 */
	public boolean isMatched() {
		return matched;
	}
	/**
	 * @param matched The matched to set.
	 */
	public void setMatched(boolean matched) {
		this.matched = matched;
	}
}
