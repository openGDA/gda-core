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
 * Class that wrappers the Lorentzian function (aka Breit-Wigner or Cauchy distribution) <br>
 * y(x) = A x(half)^2 / ( x(half)^2 + (x-a)^2 ) <br>
 * where : <br>
 * A is the height<br>
 * a is the position of the peak.<br>
 * and <br>
 * x(half) is the half width at half maximum, known as gamma
 * @deprecated Use {@link uk.ac.diamond.scisoft.analysis.fitting.functions.Lorentzian}
 */
@Deprecated
public class Lorentzian extends uk.ac.diamond.scisoft.analysis.fitting.functions.Lorentzian implements IFunction {
	/**
	 * Constructor which takes the three properties required, which are
	 * 
	 * <pre>
	 *     Parameter 1	- Position
	 *     Parameter 2 	- Height
	 *     Parameter 3 	- half width at half maximum
	 * </pre>
	 * 
	 * @param params
	 */
	public Lorentzian(double... params) {
		super(params);
	}

	public Lorentzian(IParameter[] params) {
		super(params);
	}

	/**
	 * Constructor which takes more sensible values for the parameters, which also incorporates the limits which they
	 * can be in, reducing the overall complexity of the problem
	 * 
	 * @param minPeakPosition
	 *            The minimum value of the peak position
	 * @param maxPeakPosition
	 *            The maximum value of the peak position
	 * @param maxHeight
	 *            The maximum height of the peak
	 * @param maxHalfWidth
	 *            The maximum half width at half maximum
	 */
	public Lorentzian(double minPeakPosition, double maxPeakPosition, double maxHeight, double maxHalfWidth) {
		super(minPeakPosition, maxPeakPosition, maxHeight, maxHalfWidth);
	}

	public Lorentzian createPeakFunction(double minPosition, double maxPosition, double maxArea, double maxFWHM) {
		double maxHalfWidth = maxFWHM / 2;
		double maxHeight = 2 * maxArea / maxHalfWidth; // triangular approximation of the max height of the peak.
		return new Lorentzian(minPosition, maxPosition, maxHeight, maxHalfWidth);
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
