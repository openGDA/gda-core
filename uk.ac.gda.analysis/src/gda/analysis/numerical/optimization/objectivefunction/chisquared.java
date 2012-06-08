/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.analysis.numerical.optimization.objectivefunction;

import gda.analysis.datastructure.DataVector;
import gda.analysis.numerical.linefunction.AbstractCompositeFunction;
import gda.analysis.numerical.linefunction.IParameter;
import gda.analysis.numerical.linefunction.Parameter;

import java.util.Vector;

/**
 * A standard chi squared objective function for comparing an x and y data set to a composite function
 */
public class chisquared extends AbstractLSQObjectiveFunction {
	private AbstractCompositeFunction function;

	private DataVector[] dataSets;

	/**
	 * Constructor.
	 * 
	 * @param function
	 * @param dataSets
	 */
	public chisquared(AbstractCompositeFunction function, DataVector... dataSets) {
		this.function = function;
		this.dataSets = dataSets;
		checkDatasets(dataSets);
	}

	private void checkDatasets(DataVector... dataSets) {
		// First check that there are two data sets
		if (dataSets.length != 2) {
			throw new IllegalArgumentException("Must contain an X and Y data set only");
		}
		// Now check that they're 1D
		for (int i = 0; i < dataSets.length; i++) {
			if (dataSets[i].getDimensions().length != 1) {
				throw new IllegalArgumentException("One of these data sets is not 1D...."
						+ "one of these data sets doesn't belong..");
			}
		}
		// Now check they're the same length!
		if (dataSets[0].size() != dataSets[1].size()) {
			throw new IllegalArgumentException("The X and Y datasets aren't the same size");
		}

	}

	@Override
	public double evaluate(double... parameters) {

		DataVector xdata = this.getDatasets()[0];
		DataVector ydata = this.getDatasets()[1];

		setParameters(parameters);
		// Now with these new values calculate the
		// chisqaured
		int noOfDataPoints = xdata.size();
		double sum = 0.0;
		for (int i = 0; i < noOfDataPoints; i++) {
			sum += Math.pow(getCompositeFunction().val(xdata.getIndex(i)) - ydata.getIndex(i), 2);
		}
		return sum;
	}

	/**
	 * A special case for the least sqaure algorithm For the least square code you must return the
	 * 
	 * @param parameters
	 * @return A double array containing the difference between the function and the data AT EACH DATA POINT
	 */
	@Override
	public double[] LMEvaluate(double... parameters) {

		DataVector xdata = this.getDatasets()[0];
		DataVector ydata = this.getDatasets()[1];
		// set the parameters in the function(s)
		setParameters(parameters);
		int noOfDataPoints = xdata.size();
		double[] differences = new double[noOfDataPoints];
		for (int i = 0; i < noOfDataPoints; i++) {
			differences[i] = getCompositeFunction().val(xdata.getIndex(i)) - ydata.getIndex(i);
			// System.out.println("Differences\t"+i+"\t"+differences[i]);
		}
		return differences;

	}

	private void setParameters(double... parameters) {
		AbstractCompositeFunction func = getCompositeFunction();
		int nFunctions = func.getNoOfFunctions();
		// I'm basically findingout which parameters are free
		int pCounter = 0;
		for (int i = 0; i < nFunctions; i++) {
			int nParams = func.getFunction(i).getNoOfParameters();
			for (int j = 0; j < nParams; j++) {
				if (!func.getFunction(i).getParameter(j).isFixed()) {
					func.getFunction(i).getParameter(j).setValue(parameters[pCounter]);
					pCounter++;
				}
			}
		}
	}

	/**
	 * Sets the Datasets.
	 * 
	 * @param dataSets
	 */
	public void setDatasets(DataVector... dataSets) {
		checkDatasets();
		getDatasets()[0] = dataSets[0];
		getDatasets()[1] = dataSets[1];
	}

	/**
	 * Returns the number of data points.
	 * 
	 * @return Number of data points.
	 */
	@Override
	public int getNoOfDataPoints() {
		return getDatasets()[0].size();
	}

	/**
	 * Returns the composite function.
	 * 
	 * @return composite function.
	 */
	public AbstractCompositeFunction getCompositeFunction() {
		return function;
	}

	/**
	 * Sets the composite function.
	 * 
	 * @param function
	 */
	public void setCompositeFunction(AbstractCompositeFunction function) {
		this.function = function;
	}

	/**
	 * @return vector of datasets.
	 */
	public DataVector[] getDatasets() {
		return dataSets;
	}

	@Override
	public IParameter[] getParameters() {

		int nFunctions = function.getNoOfFunctions();
		// I'm basically findingout which parameters are free
		Vector<IParameter> params = new Vector<IParameter>();
		for (int i = 0; i < nFunctions; i++) {
			int nParams = function.getFunction(i).getNoOfParameters();
			for (int j = 0; j < nParams; j++) {
				IParameter inputParameter = function.getFunction(i).getParameter(j);
				inputParameter.setGroup(i);
				params.add(inputParameter);
			}
		}
		// Now I have a list of free parameters....
		// set the relevant values for the codes
		int noOfParameters = params.size();
		// Population size
		IParameter[] parameters = new Parameter[noOfParameters];

		for (int i = 0; i < params.size(); i++) {
			parameters[i] = params.get(i);
		}
		return parameters;
	}

	/**
	 * @return DataVector
	 */
	public DataVector getVector() {

		// make the datavector
		DataVector xdata = this.getDatasets()[0];
		DataVector ydata = this.getDatasets()[1];

		int size[] = new int[2];
		size[0] = xdata.size();
		size[1] = function.getNoOfFunctions() + 3;
		DataVector output = new DataVector(size);

		// add the xaxis
		for (int i = 0; i < xdata.size(); i++) {
			output.add(xdata.get(i));
		}

		// now the raw data
		for (int i = 0; i < xdata.size(); i++) {
			output.add(ydata.get(i));
		}

		// now add the data
		for (int i = 0; i < xdata.size(); i++) {
			double value = function.val(xdata.get(i));
			output.add(value);
		}

		// now add the data for each bit in turn
		for (int j = 0; j < function.getNoOfFunctions(); j++) {
			for (int i = 0; i < xdata.size(); i++) {
				double value = function.getFunction(j).val(xdata.get(i));
				output.add(value);
			}
		}

		return output;

	}

	@Override
	public String toString() {
		String output = "\tArea,\tMean\tSigma\n";
		for (int i = 0; i < function.getNoOfFunctions(); i++) {
			output = output + function.getFunction(i) + "\n";
		}
		return output;
	}

}
