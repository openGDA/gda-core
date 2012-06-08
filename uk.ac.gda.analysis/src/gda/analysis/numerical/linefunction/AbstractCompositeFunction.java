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

import gda.analysis.utilities.OrderedHashMap;

/**
 * Fitting lineshapes generally involves fitting more than one function This class is used to sum together lineshape
 * functions which are then to be compared to experimental data
 */
public abstract class AbstractCompositeFunction {

	protected OrderedHashMap<String, AbstractFunction> functions = new OrderedHashMap<String, AbstractFunction>();

	/**
	 * Get the value of the multifunction (sum of the individual functions) at positions
	 * 
	 * @param positions
	 * @return function value at positions
	 */
	public abstract double val(double... positions);

	/**
	 * Add a function called name to the multifunction object
	 * 
	 * @param name
	 * @param function
	 */
	public void addFunction(String name, AbstractFunction function) {
		functions.put(name, function);
	}

	/**
	 * Remove the function at index from the multifunction
	 * 
	 * @param index
	 *            Index of function to be removed
	 */
	public void removeFunction(int index) {
		functions.remove(index);
	}

	/**
	 * Remove the function at index from the multifunction
	 * 
	 * @param name
	 *            Name of the function to be removed
	 */
	public void removeFunction(String name) {
		functions.remove(name);
	}

	/**
	 * @return The no of individual functions in the multifunction
	 */
	public int getNoOfFunctions() {
		return functions.size();
	}

	/**
	 * @param index
	 * @return The function at index
	 */
	public AbstractFunction getFunction(int index) {
		return functions.get(index);
	}

	/**
	 * @param name
	 * @return The function with this name
	 */
	public AbstractFunction getFunction(String name) {
		return functions.get(name);
	}
	/*
	 * public int getTotalNoOfFreeParameters() { for(int i=0;i<functions.size();i++) { int noOfParamters =
	 * functions.get(i).getNoOfParameters() } } public int getFreeParameters() { }
	 */

}
