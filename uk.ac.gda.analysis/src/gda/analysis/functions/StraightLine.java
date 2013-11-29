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
 * This class implements the function y(x) = m*x + c
 * @deprecated Use {@link uk.ac.diamond.scisoft.analysis.fitting.functions.StraightLine}
 */
@Deprecated
public class StraightLine extends uk.ac.diamond.scisoft.analysis.fitting.functions.StraightLine implements IFunction {

	/**
	 * Basic constructor, not advisable to use.
	 */
	public StraightLine() {
		super();
	}

	public StraightLine(IParameter[] params) {
		super(params);
	}

	/**
	 * Constructor allowing for careful construction of bounds
	 * 
	 * @param minM
	 *            Minimum value allowed for m
	 * @param maxM
	 *            Maximum value allowed for m
	 * @param minC
	 *            Minimum value allowed for c
	 * @param maxC
	 *            Maximum value allowed for c
	 */
	public StraightLine(double minM, double maxM, double minC, double maxC) {
		super(minM, maxM, minC, maxC);
	}

	@Override
	public DataSet makeDataSet(DoubleDataset... values) {
		return DataSet.convertToDataSet(makeSerialDataset(values));
	}

	@Override
	public void disp() {
		TerminalPrinter.print(toString());
	}
}
