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

package gda.util.fit;

import java.util.Vector;

/**
 * MultiFunction abstract class
 */
public abstract class MultiFunction {

	Vector<Function> functions = new Vector<Function>();

	/**
	 * @param x
	 * @param parms
	 * @return double
	 */
	public abstract double val(double x, double... parms);

	/**
	 * @param function
	 */
	public abstract void addFunction(Function function);

	/**
	 * @param index
	 */
	public abstract void removeFunction(int index);

	/**
	 * @return int
	 */
	public abstract int getNoOfFunctions();

	/**
	 * @param index
	 * @return double[]
	 */
	public abstract double[] getFunctionParameters(int index);

	/**
	 * @param index
	 * @param parms
	 */
	public abstract void setFunctionParameters(int index, double... parms);

	/**
	 * @param index
	 * @return boolean[]
	 */
	public abstract boolean[] getFixedParametersForFunction(int index);

	/**
	 * @param index
	 * @param parms
	 */
	public abstract void setFixedParametersForFunction(int index, boolean... parms);

}
