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
 * PseudoVoigt Class
 * @deprecated Use {@link uk.ac.diamond.scisoft.analysis.fitting.functions.PseudoVoigt}
 */
@Deprecated
public class PseudoVoigt extends uk.ac.diamond.scisoft.analysis.fitting.functions.PseudoVoigt implements IFunction {
	/**
	 * 
	 * @param position
	 * @param gaussianFWHM
	 * @param lorentzianFWHM
	 * @param area
	 * @param mix
	 */
	public PseudoVoigt(double position, double gaussianFWHM, double lorentzianFWHM, double area, double mix) {
		super(position, gaussianFWHM, lorentzianFWHM, area, mix);
	}
	
	/**
	 * Initialise with set parameters
	 * @param params Position, GaussianFWHM, LorentzianFWHM, Area, Mix(0-1)
	 */
	public PseudoVoigt(IParameter[] params) {
		super(params);
	}

	/**
	 * @param minPos
	 * @param maxPos
	 * @param max_FWHM
	 * @param max_Area
	 */
	public PseudoVoigt(double minPos, double maxPos, double max_FWHM, double max_Area) {
		super(minPos, maxPos, max_FWHM, max_Area);
	}

	public PseudoVoigt createPeakFunction(double minPosition, double maxPosition, double maxArea, double maxFWHM){
		return new PseudoVoigt(minPosition, maxPosition, maxFWHM, maxArea);
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
