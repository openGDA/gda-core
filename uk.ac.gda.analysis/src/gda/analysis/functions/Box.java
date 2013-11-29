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

import gda.analysis.DataSet;
import gda.analysis.TerminalPrinter;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.fitting.functions.IParameter;


/**
 * Class that wrappers the box function calculated as a difference of two Fermi functions
 * y(x) = Fermi(mu1, kT1, scale) - Fermi(mu2,kT2, scale)
 * @deprecated Use {@link uk.ac.diamond.scisoft.analysis.fitting.functions.Box}
 */
@Deprecated
public class Box extends uk.ac.diamond.scisoft.analysis.fitting.functions.Box implements IFunction {

	public Box(double... params) {
		super(params);
	}

	public Box(IParameter[] params) {
		super(params);
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
