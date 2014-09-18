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

import org.eclipse.dawnsci.analysis.api.fitting.functions.IParameter;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;

import gda.analysis.DataSet;
import gda.analysis.TerminalPrinter;


/**
 * Class which expands on the AFunction class to give the properties of a pearsonVII. A 1D implementation
 * function derived from Gozzo, F. (2004). 
 * First experiments at the Swiss Light Source Materials Science beamline powder diffractometer.
 * Journal of Alloys and Compounds, 362(1-2), 206-217. doi:10.1016/S0925-8388(03)00585-1
 * @deprecated Use {@link uk.ac.diamond.scisoft.analysis.fitting.functions.PearsonVII}
 */
@Deprecated
public class PearsonVII extends uk.ac.diamond.scisoft.analysis.fitting.functions.PearsonVII implements IFunction {

	/**
	 * Constructor which takes the three properties required, which are
	 * 
	 * <pre>
	 *    position
	 *    FWHM
	 *    area
	 *    power
	 * </pre>
	 * 
	 * @param params
	 */
	public PearsonVII(double[] params) {
		super(params);
	}

	public PearsonVII(IParameter[] params) {
		super(params);
	}

	/**
	 * Constructor which takes more sensible values for the parameters, which also incorporates the limits which they
	 * can be in, reducing the overall complexity of the problem
	 * 
	 * @param minPeakPosition
	 *            The minimum value the peak position of the Pearson VII
	 * @param maxPeakPosition
	 *            The maximum value of the peak position
	 * @param maxFWHM
	 *            The maximum full width half maximum
	 * @param maxArea
	 *            The maximum area under the PDF
	 * 
	 * There is also a power parameter for the Pearson VII distribution. This parameter defines form as
	 * somewhere between a Gaussian and a Lorentzian function. When m = 1 the function is Lorentzian and
	 * m = infinity the function is Gaussian. With this constructor the mixing parameter is set to 2 with
	 * the lower limit set to 1 and the upper limit set to 10.
	 */
	public PearsonVII(double minPeakPosition, double maxPeakPosition, double maxFWHM, double maxArea) {
		super(minPeakPosition, maxPeakPosition, maxFWHM, maxArea);
	}
	
	public PearsonVII(double minPeakPosition, double maxPeakPosition, double maxFWHM, double maxArea, double power) {
		super(minPeakPosition, maxPeakPosition, maxFWHM, maxArea, power);
	}	

	public PearsonVII createPeakFunction(double minPosition, double maxPosition, double maxArea, double maxFWHM) {
		return new PearsonVII(minPosition,maxPosition,maxArea,maxFWHM);
	}
	
	@Override
	public DataSet makeDataSet(DoubleDataset... values) {
		return DataSet.convertToDataSet(calculateValues(values));
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
