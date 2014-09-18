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

package gda.analysis.utils.optimisation;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IOperator;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IParameter;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;

import gda.analysis.DataSet;
import gda.analysis.functions.IFunction;
import gda.analysis.functions.Parameter;

public class ProblemFunction implements IFunction {

	ProblemDefinition def = null;
	private double[] parameters;

	public ProblemFunction(final ProblemDefinition definition) {
		def = definition;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public void setName(String newName) {
	}

	@Override
	public double val(double... values) {
		return 0;
	}

	@Override
	public Parameter getParameter(int index) {
		return parameters == null ? null : new Parameter(parameters[index]);
	}

	@Override
	public Parameter[] getParameters() {
		return null;
	}

	@Override
	public int getNoOfParameters() {
		return def.getNumberOfParameters();
	}

	@Override
	public double getParameterValue(int index) {
		return parameters == null ? 0 : parameters[index];
	}

	@Override
	public double[] getParameterValues() {
		return parameters;
	}

	@Override
	public void setParameter(int index, IParameter parameter) {
		parameters[index] = parameter.getValue();
	}

	@Override
	public void setParameterValues(double... params) {
		parameters = params;
	}

	@Override
	public double partialDeriv(int Parameter, double... position) {
		return 0;
	}

	@Override
	public double partialDeriv(IParameter param, double... values) {
		return 0;
	}

	@Override
	public DataSet makeDataSet(DoubleDataset... values) {
		return null;
	}

	@Override
	public DoubleDataset makeDataset(IDataset... values) {
		return null;
	}

	@Override
	public double residual(boolean allValues, IDataset data, IDataset... values) {
		try {
			return def.eval(parameters);
		} catch (Exception e) {
			return 0;
		}
	}

	@Override
	public double residual(boolean allValues, IDataset data, IDataset weight, IDataset... values) {
		return residual(allValues, data, values);
	}

	@Override
	public void disp() {
//		TerminalPrinter.print(toString());
	}

	String description= "default";
	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String newDescription) {
		this.description = newDescription;
		
	}

	@Override
	public String getParameterName(int index) {
		return getParameter(index).getName();
	}

	@Override
	public void setParameterName(String name, int index) {
		getParameter(index).setName(name);
		
	}

	@Override
	public void setMonitor(IMonitor monitor) {
	}

	@Override
	public IMonitor getMonitor() {
		return null;
	}

	@Override
	public IFunction copy() throws Exception {
		return null;
	}

	@Override
	public void setDirty(boolean isDirty) {
	}

	@Override
	public IFunction getFunction(int index) {
		return this;
	}

	@Override
	public IDataset calculateValues(IDataset... coords) {
		return null;
	}

	@Override
	public IDataset calculatePartialDerivativeValues(IParameter param, IDataset... coords) {
		return null;
	}

	@Override
	public boolean isValid() {
		return def != null;
	}

	@Override
	public IOperator getParentOperator() {
		return null;
	}

	@Override
	public void setParentOperator(IOperator parent) {
	}
}
