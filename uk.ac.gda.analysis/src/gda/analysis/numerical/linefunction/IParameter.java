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

package gda.analysis.numerical.linefunction;

/**
 * Parameter interface A parameter has a value,upperlimit,lowerlimit and can be fixed or free
 */
public interface IParameter {
	/**
	 * @return The value of the parameter.
	 */
	double getValue();

	/**
	 * @return The upper limit on the parameter
	 */
	public double getUpperLimit();

	/**
	 * @return The lower limit on the parameter
	 */
	public double getLowerLimit();

	/**
	 * @return Boolean true if the parameter is not to be varied
	 */
	boolean isFixed();

	/**
	 * Set the parameter to be fixed
	 * 
	 * @param b
	 */
	void setFixed(boolean b);

	/**
	 * Set lower limit on this parameter
	 * 
	 * @param value
	 */
	void setLowerLimit(double value);

	/**
	 * Set upper limit on this parameter
	 * 
	 * @param value
	 */
	void setUpperLimit(double value);

	/**
	 * Set the value of the parameter.
	 * 
	 * @param value
	 */
	void setValue(double value);

	/**
	 * @param newGroupNumber
	 */
	public void setGroup(int newGroupNumber);

	/**
	 * @return group
	 */
	public int getGroup();

}