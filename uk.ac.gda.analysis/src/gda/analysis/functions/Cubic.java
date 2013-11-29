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
 * Class that wrappers the equation <br>
 * y(x) = ax^3 + bx^2 + cx + d
 * @deprecated Use {@link uk.ac.diamond.scisoft.analysis.fitting.functions.Cubic}
 */
@Deprecated
public class Cubic extends uk.ac.diamond.scisoft.analysis.fitting.functions.Cubic implements IFunction {

	/**
	 * Basic constructor, not advisable to use
	 */
	public Cubic() {
		super();
	}

	public Cubic(IParameter[] params) {
		super(params);
	}

	/**
	 * Constructor that allows for the positioning of all the parameter bounds
	 * 
	 * @param minA
	 *            minimum boundary for the A parameter
	 * @param maxA
	 *            maximum boundary for the A parameter
	 * @param minB
	 *            minimum boundary for the B parameter
	 * @param maxB
	 *            maximum boundary for the B parameter
	 * @param minC
	 *            minimum boundary for the C parameter
	 * @param maxC
	 *            maximum boundary for the C parameter
	 * @param minD
	 *            minimum boundary for the D parameter
	 * @param maxD
	 *            maximum boundary for the D parameter
	 */
	public Cubic(double minA, double maxA, double minB, double maxB, double minC, double maxC, double minD, double maxD) {
		super(minA, maxA, minB, maxB, minC, maxC, minD, maxD);
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
