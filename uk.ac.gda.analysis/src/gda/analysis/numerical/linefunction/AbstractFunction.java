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
 * Implement this interface to specify the line function
 */
public abstract class AbstractFunction {
	/**
	 * A list of parameters
	 */
	private OrderedHashMap<String, IParameter> parameters;

	/**
	 * Setup the parameter list
	 * 
	 * @param names
	 */
	public AbstractFunction(String[] names) {
		parameters = new OrderedHashMap<String, IParameter>();
		for (int i = 0; i < names.length; i++) {
			parameters.put(names[i], new Parameter());
		}
	}

	/**
	 * return the value of the function at position
	 * 
	 * @param position
	 * @return value of the function at position
	 */
	public abstract double val(double... position);

	/**
	 * @param index
	 * @return Paramter at index
	 */
	public IParameter getParameter(int index) {
		return parameters.get(index);
	}

	/**
	 * @param name
	 * @return Parameter corresponding to name
	 */
	public IParameter getParameter(String name) {
		return parameters.get(name.toLowerCase());
	}

	/**
	 * Get the parameters
	 * 
	 * @return Array of IParameters
	 */
	public IParameter[] getParameters() {
		IParameter[] params = new IParameter[parameters.size()];
		for (int i = 0; i < parameters.size(); i++) {
			params[i] = parameters.get(i);
		}
		return params;
	}

	/**
	 * @return The no of parameters in the function
	 */
	public int getNoOfParameters() {
		return parameters.size();
	}

	/**
	 * Set the parameter values The number of parameters specified must equal the no of parameters of the function You
	 * can get the inidividual parameters and set the values but this is just a convenient method to set them all at
	 * once...
	 * 
	 * @param params
	 */
	public void setParameterValues(double... params) {
		if (params.length != getNoOfParameters()) {
			System.out
					.println("setParameterValues: No of parameters specified do not equal the no of parameters in this function");
			return;
		}
		for (int i = 0; i < getNoOfParameters(); i++) {
			parameters.get(i).setValue(params[i]);
		}

	}

	/**
	 * crude method for printing out the parameters of a function useful for testing and the jython environment
	 */
	public void printParameterList() {
		for (int i = 0; i < getNoOfParameters(); i++) {
			System.out.println("Parameter no. " + i + " with name " + parameters.getKey(i) + " has value :"
					+ getParameter(i).getValue() + " and limits :" + getParameter(i).getLowerLimit() + " "
					+ getParameter(i).getUpperLimit());
		}
	}
}