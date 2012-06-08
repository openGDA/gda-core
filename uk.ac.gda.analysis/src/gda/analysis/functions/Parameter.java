/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.analysis.functions;

import uk.ac.diamond.scisoft.analysis.fitting.functions.IParameter;


public class Parameter extends uk.ac.diamond.scisoft.analysis.fitting.functions.Parameter implements IParameter {
//	private r parameter;
//
//	/**
//	 * Basic Constructor, does not initialise anything
//	 */
//	
	public Parameter() {
		super();
//		parameter = new uk.ac.diamond.scisoft.analysis.fitting.functions.Parameter();
	}
//
//	/**
//	 * This constructor wraps another Parameter
//	 * 
//	 * @param p
//	 *            The parameter to be wrapped
//	 */
	public Parameter(IParameter p) {
		super(p);
//		parameter = (uk.ac.diamond.scisoft.analysis.fitting.functions.Parameter) p;
	}
//
//	/**
//	 * Constructor that sets the value, but leaves everything else set as initialised
//	 * 
//	 * @param value
//	 *            Value that the parameter is set to
//	 */
	public Parameter(double value) {
		super(value);
	}
//
//	/**
//	 * Constructor that sets up the value along with the max and min parameters
//	 * 
//	 * @param value
//	 *            Value of the parameter
//	 * @param lowerLimit
//	 *            Lower limit the parameter is restricted to
//	 * @param upperLimit
//	 *            Upper limit the parameter is restricted to
//	 */
//	public Parameter(double value, double lowerLimit, double upperLimit) {
//		parameter = new uk.ac.diamond.scisoft.analysis.fitting.functions.Parameter(value, lowerLimit, upperLimit);
//	}
//
//	@Override
//	public int hashCode() {
//		return parameter.hashCode();
//	}
//
//	@Override
//	public double getValue() {
//		return parameter.getValue();
//	}
//
//	@Override
//	public double getUpperLimit() {
//		return parameter.getUpperLimit();
//	}
//
//	@Override
//	public double getLowerLimit() {
//		return parameter.getLowerLimit();
//	}
//
//	@Override
//	public boolean isFixed() {
//		return parameter.isFixed();
//	}
//
//	@Override
//	public void setFixed(boolean b) {
//		parameter.setFixed(b);
//	}
//
//	@Override
//	public void setLimits(double newLowerLimit, double newUpperLimit) {
//		parameter.setLimits(newLowerLimit, newUpperLimit);
//	}
//
//	@Override
//	public boolean equals(Object obj) {
//		return parameter.equals(obj);
//	}
//
//	@Override
//	public void setLowerLimit(double lowerLimit) {
//		parameter.setLowerLimit(lowerLimit);
//	}
//
//	@Override
//	public void setUpperLimit(double upperLimit) {
//		parameter.setUpperLimit(upperLimit);
//	}
//
//	@Override
//	public void setValue(double value) {
//		parameter.setValue(value);
//	}
//
//	@Override
//	public String toString() {
//		return parameter.toString();
//	}
//
//	public uk.ac.diamond.scisoft.analysis.fitting.functions.Parameter unwrap() {
//		return parameter;
//	}

//	public static uk.ac.diamond.scisoft.analysis.fitting.functions.Parameter[] unwrapArray(Parameter[] params) {
//		uk.ac.diamond.scisoft.analysis.fitting.functions.Parameter[] unwrapped = new uk.ac.diamond.scisoft.analysis.fitting.functions.Parameter[params.length];
//		for (int i = 0; i < params.length; i++) {
//			unwrapped[i] = params[i].unwrap();
//		}
//		return unwrapped;
//	}
//
//	public static Parameter[] wrapArray(uk.ac.diamond.scisoft.analysis.fitting.functions.Parameter[] params) {
//		Parameter[] wrapped = new Parameter[params.length];
//		for (int i = 0; i < params.length; i++) {
//			wrapped[i] = new Parameter(params[i]);
//		}
//		return wrapped;
//	}
}
