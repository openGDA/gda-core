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

package gda.analysis;

import gda.analysis.functions.CompositeFunction;
import gda.analysis.functions.FunctionOutput;
import gda.analysis.utils.IOptimizer;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.fitting.functions.AFunction;

public class DataSetFunctionFitter {
	public FitterDataSetFunctionFitterResult fit(DoubleDataset xAxis, DoubleDataset yAxis,
			 IOptimizer Optimizer, AFunction... functions) throws Exception {
		CompositeFunction comp = new CompositeFunction();
		DoubleDataset[] coords = new DoubleDataset[] {xAxis};

		for (int i = 0; i < functions.length; i++) {
			comp.addFunction(functions[i]);
		}

		// call the optimisation routine
		Optimizer.Optimize(coords, yAxis, comp);


		FunctionOutput result = new FunctionOutput(comp);

		result.setChiSquared(comp.residual(true, yAxis, coords));

		result.setAreaUnderFit((Double) comp.makeDataSet(xAxis).sum()
				* ((xAxis.max().doubleValue() - xAxis.min().doubleValue()) / xAxis.getSize()));

		return new FitterDataSetFunctionFitterResult(result, comp.display(xAxis, yAxis));
	}
}
