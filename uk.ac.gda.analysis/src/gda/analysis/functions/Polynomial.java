/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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
 * y(x) = a_0 x^n + a_1 x^(n-1) + a_2 x^(n-2) + ... + a_(n-1) x + a_n
 * @deprecated Use {@link uk.ac.diamond.scisoft.analysis.fitting.functions.Polynomial}
 */
@Deprecated
public class Polynomial extends uk.ac.diamond.scisoft.analysis.fitting.functions.Polynomial implements IFunction {
	/**
	 * Basic constructor, not advisable to use
	 */
	public Polynomial() {
		super();
	}

	/**
	 * Make a polynomial of given degree (0 - constant, 1 - linear, 2 - quadratic, etc)
	 * @param degree
	 */
	public Polynomial(final int degree) {
		super(degree);
	}

	/**
	 * Make a polynomial with given parameters
	 * @param params
	 */
	public Polynomial(double[] params) {
		super(params);
	}

	/**
	 * Make a polynomial with given parameters
	 * @param params
	 */
	public Polynomial(IParameter[] params) {
		super(params);
	}

	/**
	 * Constructor that allows for the positioning of all the parameter bounds
	 * 
	 * @param min
	 *            minimum boundaries
	 * @param max
	 *            maximum boundaries
	 */
	public Polynomial(double[] min, double[] max) {
		super(min, max);
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
