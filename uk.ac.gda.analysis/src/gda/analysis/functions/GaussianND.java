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
 * A ND Gaussian function
 * 
 * The parameters are mean peak position coordinates (N), volume (1), diagonal elements of covariance matrix (N)
 * and normalized upper triangle elements of covariance matrix (N*(N-1)/2). The last set of parameters
 * are normalized by the diagonal elements.
 * @deprecated Use {@link uk.ac.diamond.scisoft.analysis.fitting.functions.GaussianND}
 */
@Deprecated
public class GaussianND extends uk.ac.diamond.scisoft.analysis.fitting.functions.GaussianND implements IFunction {

	public GaussianND(IParameter[] params) {
		super(params);
	}

	/**
	 * Constructor which takes the N + 1 + N + N(N-1)/2 properties required, which are volume, elements of the mean vector,
	 * diagonal elements and upper triangle elements of the covariance matrix (as this is symmetric)
	 * @param params
	 */
	public GaussianND(double... params) {
		super(params);
	}

	/**
	 * Create a multi-dimensional Gaussian function
	 * @param maxVol maximum "volume"
	 * @param minPeakPosition array containing minimum peak position
	 * @param maxPeakPosition array containing maximum peak position
	 * @param maxSigma maximum magnitude for any element in covariance matrix
	 */
	public GaussianND(double maxVol, double[] minPeakPosition, double[] maxPeakPosition, double maxSigma) {
		super(maxVol, minPeakPosition, maxPeakPosition, maxSigma);
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
