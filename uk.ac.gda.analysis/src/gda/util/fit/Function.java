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

package gda.util.fit;

/**
 * Caller implement this interface to specify the function to be minimized and its gradient. Optionally return an
 * initial guess and some test data, though the LM.java only uses this in its optional main() test program. Return null
 * if these are not needed.
 */
public interface Function {

	/**
	 * x is a single point, but domain may be multidimensional
	 * 
	 * @param x
	 * @param parms
	 * @return The function value
	 */
	public abstract double val(double x, double... parms);

	/**
	 * @return double[] parameters
	 */
	public abstract double[] getParameters();

	/**
	 * @return boolean[] fixed parameters
	 */
	public abstract boolean[] getFixedParameters();

	/**
	 * @param parameters
	 */
	public abstract void setParameters(double... parameters);

	/**
	 * @param parameters
	 */
	public abstract void setFixedParameters(boolean... parameters);

}
