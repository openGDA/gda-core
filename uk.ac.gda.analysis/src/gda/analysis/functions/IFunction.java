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
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;

/**
 * @deprecated {@link uk.ac.diamond.scisoft.analysis.fitting.functions.IFunction}
 */
@Deprecated
public interface IFunction extends uk.ac.diamond.scisoft.analysis.fitting.functions.IFunction {

	/**
	 * Function that makes a dataset from the Function
	 * 
	 * The function can be evaluated in one of two possible modes. In general, the function has
	 * <tt>m</tt> independent variables and the output dataset has <tt>n</tt> dimensions.
	 * The simplest mode has the restriction <tt>m = n</tt> and all <tt>n</tt> input datasets must
	 * be 1D and the function is evaluated on a nD hypergrid.
	 * 
	 * The general mode requires <tt>m</tt> nD datasets.
	 * 
	 * @param values
	 *            The values at which to evaluate the function
	 * @return The dataset of the whole function
	 */
	@Deprecated
	public DataSet makeDataSet(DoubleDataset... values);

	/**
	 * Displays the content of the Function
	 */
	@Deprecated
	public void disp();
}
