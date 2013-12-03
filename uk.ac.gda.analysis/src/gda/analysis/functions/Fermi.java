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
 * Class that wrappers the Fermi function from Fermi-Dirac distribution
 * y(x) = scale / (exp((x - mu)/kT) + 1) + C
 * @deprecated Use {@link uk.ac.diamond.scisoft.analysis.fitting.functions.Fermi}
 */
@Deprecated
public class Fermi extends uk.ac.diamond.scisoft.analysis.fitting.functions.Fermi implements IFunction {

	public Fermi(double... params) {
		super(params);
	}

	public Fermi(IParameter[] params) {
		super(params);
	}

	/**
	 * Constructor that allows for the positioning of all the parameter bounds
	 * 
	 * @param minMu
	 *            minimum Mu value
	 * @param maxMu
	 *            maximum Mu value
	 * @param minkT
	 *            minimum kT value
	 * @param maxkT
	 *            maximum kT value
	 * @param minScale
	 *            minimum scale value
	 * @param maxScale
	 *            maximum scale value
	 * @param minC
	 *            minimum C value
	 * @param maxC
	 *            maximum C value
	 */
	public Fermi(double minMu, double maxMu, double minkT, double maxkT,
					double minScale, double maxScale, double minC, double maxC) {
		super(minMu, maxMu, minkT, maxkT, minScale, maxScale, minC, maxC);
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
