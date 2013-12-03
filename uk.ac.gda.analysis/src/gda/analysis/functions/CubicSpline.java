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

package gda.analysis.functions;

import gda.analysis.DataSet;
import gda.analysis.TerminalPrinter;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.fitting.functions.IParameter;

/**
 * Basically an implementation of a simple cubic spline calculator
 * @deprecated Use {@link uk.ac.diamond.scisoft.analysis.fitting.functions.CubicSpline}
 */
@Deprecated
public class CubicSpline extends uk.ac.diamond.scisoft.analysis.fitting.functions.CubicSpline implements IFunction {
	public CubicSpline(int numberOfParameters) {
		super(numberOfParameters);
	}
	
	public CubicSpline(IParameter[] params) {
		super(params);
	}

	/**
	 * Constructor for use with Global Optimisers
	 * @param xpoints The x positions for the control points
	 * @param ystartpoints the start y positions for the control points
	 * @param deviation the amount the optimiser can go from the specified y value
	 */
	public CubicSpline(double[] xpoints, double[] ystartpoints, double deviation) {
		super(xpoints, ystartpoints, deviation);
	}
	
	/**
	 * Constructor for normal use
	 * @param xpoints The x positions for the control points
	 * @param ystartpoints the start y positions for the control points
	 */
	public CubicSpline(double[] xpoints, double[] ystartpoints) {
		super(xpoints, ystartpoints);
	}

	@Override
	public DataSet makeDataSet(DoubleDataset... values) {
		return DataSet.convertToDataSet(makeSerialDataset(values));
	}

	@Override
	public void disp() {
		TerminalPrinter.print(toString());
	}

	@Override
	public IFunction getFunction(int index) {
		return this;
	}

	@Override
	public String getParameterName(int index) {
		return getParameter(index).getName();
	}

	@Override
	public void setParameterName(String name, int index) {
		getParameter(index).setName(name);
	}
}
