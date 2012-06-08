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

package gda.analysis.numerical.optimization.optimizers.simplex;

/***********************************************************************************************************************
 * The Nelder-Mead method or Simplex method or downhill simplex method is a commonly used nonlinear optimization
 * algorithm. It is due to Nelder & Mead (1965) and is a numerical method for minimising an objective function in a
 * many-dimensional space. The method uses the concept of a simplex, which is a polytope of N + 1 vertices in N
 * dimensions; a line segment on a line, a triangle on a plane, a tetrahedron in three-dimensional space and so forth.
 * In the case of a 2D problem we have 3 vertices, corresponding to 3 starting or "guessed" solutions to the problem.
 * Based on the value of the objective function (what we're trying to minimise) the triangle can 1) Reflect. The worst
 * point is reflected along a direction perpendicular to the best and 2nd best points. 2) If this new point has a lower
 * value, i.e. it minimising we'll try 3) Expansion....so our new point looks good...lets try move it a bit more. If
 * this results in a good answer lets go back to step 1 and find which point is our worst point now and go through the
 * process again. 4) Shrinking. If reflection etc... doesn't produce any improvements maybe our triangle is too big (so
 * our parameters are making big jumps instead of small refinements). Then we need to reduce the size of the triangle
 * and go back to step 1. How do we implement bound contraints ? The easiest way is to use penalty functions. We are
 * normally determing f(x). With penalty functions we are determining f(x) + o(x). o(x) is a function which returns 0 if
 * the x parameters are in bounds but returns a large value if x is outside the bounds. So when the algorithm steps
 * outside the bounds the f(x) will get worse and the algorithm will stay well away. Links Excellent site for applets
 * showing optimization codes at work http://www.cse.uiuc.edu/eot/modules/optimization/ Nelder Mead Interactive Java
 * applet showing how it works step by step...... http://www.cse.uiuc.edu/eot/modules/optimization/NelderMead/
 **********************************************************************************************************************/

import gda.analysis.numerical.linefunction.IParameter;
import gda.analysis.numerical.optimization.objectivefunction.AbstractObjectiveFunction;
import gda.analysis.numerical.optimization.optimizers.IOptimizer;

/**
 * Nelder Mead Minimisation class
 */
public class NelderMeadOptimizer implements Runnable, IOptimizer {
	/**
	 * number of unknown parameters to be estimated
	 */
	private int noOfParameters = 0;

	/**
	 * function parameter values (returned at function minimum)
	 */
	private double[] paramValue = null;

	/**
	 * value of the function to be minimised at the minimum
	 */
	private double minimum = 0.0D;

	/**
	 * number of places to which double variables are truncated on output to text files
	 */
	//TODO this suppresion should be checked if this code is ever used
	@SuppressWarnings("unused")
	private int prec = 4;

	/**
	 * field width on output to text files
	 */
	//TODO this suppresion should be checked if this code is ever used
	@SuppressWarnings("unused")
	private int field = 13;

	/**
	 * Status of minimisation on exiting true - convergence criterion was met false - convergence criterion not met -
	 * current estimates returned
	 */
	private boolean converganceStatus = false;

	/**
	 * Scaling option 0; no scaling of initial estimates 1; initial simplex estimates scaled to unity 2; initial
	 * estimates scaled by user provided values in scaling[]
	 */
	private int scalingOption = 0;

	private int stepOption = 1;

	/**
	 * values used to scaling parameters (see scalingOption)
	 */
	private double[] scaling = null;

	/**
	 * weight for the penalty functions
	 */
	private double penaltyWeight = 1.0e30;

	/**
	 * The maximum number of iterations
	 */
	private int maxNoOfIterations = 3000;

	/**
	 * A counter for the number of iterations
	 */
	private int noOfIterations = 0;

	/**
	 * Max no of restarts
	 */
	private int maxNoOfRestarts = 3;

	/**
	 * Counter of number of restarts
	 */
	private int noOfRestarts = 0;

	/**
	 * Convergence tolerance (stop when f < this)
	 */
	private double fTol = 1e-13;

	/**
	 * Reflection coefficient
	 */
	private double reflectionCoeff = 1.0D;

	/**
	 * Expansion coefficient
	 */
	private double expansionCoeff = 2.0D;

	/**
	 * Contraction/Shrink coefficient
	 */
	private double contractionCoeff = 0.5D;

	/**
	 * A store of the starting point
	 */
	private double[] startingPoint = null;

	/**
	 * A store of the starting point
	 */
	private double[] lowerBounds = null;

	/**
	 * A store of the starting point
	 */
	private double[] upperBounds = null;

	/**
	 * Step values (used in creating the initial n+1 simplex) if not specified defaultStep*start values is used
	 */
	private double[] step = null; // Nelder and Mead simplex step values

	private double[] userSteps = null; // Nelder and Mead simplex step values

	/**
	 * Default step size for parameters
	 */
	private double defaultStep = 0.5D;

	/**
	 * The optimizing thread
	 */
	Thread action;

	/**
	 * The costfunction
	 */
	AbstractObjectiveFunction function = null;

	/**
	 * Setup a neldermead optimization
	 * 
	 * @param function
	 */
	public NelderMeadOptimizer(AbstractObjectiveFunction function) {
		this.function = function;
		reset();
	}

	/**
	 * 
	 */
	public void reset() {
		IParameter[] params = function.getParameters();
		// Now I have a list of free parameters....
		// set the relevant values for the codes
		int noOfFreeParameters = 0;
		for (int i = 0; i < params.length; i++) {
			if (!params[i].isFixed()) {
				noOfFreeParameters++;
			}
		}
		noOfParameters = noOfFreeParameters;
		// Population size
		startingPoint = new double[noOfParameters];
		step = new double[noOfParameters];
		lowerBounds = new double[noOfParameters];
		upperBounds = new double[noOfParameters];
		paramValue = new double[noOfParameters];
		int count = 0;
		for (int i = 0; i < params.length; i++) {
			if (!params[i].isFixed()) {

				startingPoint[count] = params[i].getValue();
				lowerBounds[count] = params[i].getLowerLimit();
				upperBounds[count] = params[i].getUpperLimit();
				count++;
			}
		}
		double norm = 0.0;
		for (int i = 0; i < noOfParameters; i++) {
			norm += startingPoint[i] * startingPoint[i];
		}
		norm = Math.sqrt(norm);
		for (int i = 0; i < noOfParameters; i++) {
			if (stepOption == 0) {
				step[i] = Math.max(1.0, norm);

			} else if (stepOption == 1) {
				step[i] = startingPoint[i] * defaultStep;
			} else {
				step[i] = this.userSteps[i];
			}
		}
	}

	/**
	 * Nelder and Mead Simplex minimisation
	 */
	private void runNelderMead() {

		this.converganceStatus = true;
		int noOfParametersPlus1 = this.noOfParameters + 1;
		double[][] vertices = new double[noOfParametersPlus1][noOfParametersPlus1];
		double[] functionValueAtVertices = new double[noOfParametersPlus1];
		double[] centroid = new double[noOfParametersPlus1];
		double[] reflectionVertex = new double[noOfParametersPlus1];
		double[] expandContractVertex = new double[noOfParametersPlus1];
		double[] pmin = new double[noOfParameters];
		double currentLowestValue = 0.0D;
		double valueAtReflectionPoint = 0.0D;
		double valueAtExpandContractVertex = 0.0D;
		double valueAtLowestVertex = 0.0D;
		double mean = 0.0D, standardDev = 0.0D, zn = 0.0D;
		int indexOfLowerVertex = 0;
		int indexOfHighestVertex = 0;
		int ln = 0;
		boolean test = true;
		double[] start = new double[noOfParameters];
		start = startingPoint.clone();
		// this.step = new double[noOfParameters];
		this.noOfIterations = 0;
		double sho = 0.0D;
		int jcount = this.maxNoOfRestarts; // count of number of restarts
		// still

		if (this.scalingOption < 2)
			this.scaling = new double[noOfParameters];
		if (scalingOption == 2 && scaling.length != noOfParameters)
			throw new IllegalArgumentException("scaling array and initial estimate array are of different lengths");
		if (step.length != noOfParameters)
			throw new IllegalArgumentException("step array length " + step.length
					+ " and initial estimate array length " + start.length + " are of different");

		// check for zero step sizes
		for (int i = 0; i < noOfParameters; i++)
			if (step[i] == 0.0D)
				throw new IllegalArgumentException("step " + i + " size is zero");

		// // Store unscaled start values
		// for (int i = 0; i < noOfParameters; i++)
		// this.startingPoint[i] = start[i];

		// scaling initial estimates and step sizes
		if (this.scalingOption > 0) {
			boolean testzero = false;
			for (int i = 0; i < noOfParameters; i++)
				if (start[i] == 0.0D)
					testzero = true;
			if (testzero) {
				System.out.println("Nelder and Mead Simplex: a start value of zero precludes scaling");
				System.out.println("Regression performed without scaling");
				this.scalingOption = 0;
			}
		}
		switch (this.scalingOption) {
		case 0:
			for (int i = 0; i < noOfParameters; i++)
				scaling[i] = 1.0D;
			break;
		case 1:
			for (int i = 0; i < noOfParameters; i++) {
				scaling[i] = 1.0 / start[i];
				step[i] = step[i] / start[i];
				start[i] = 1.0D;
			}
			break;
		case 2:
			for (int i = 0; i < noOfParameters; i++) {
				step[i] *= scaling[i];
				start[i] *= scaling[i];
			}
			break;
		}

		for (int i = 0; i < noOfParameters; i++) {
			this.step[i] = step[i];
			this.scaling[i] = scaling[i];
		}

		// initial simplex
		for (int i = 0; i < noOfParameters; ++i) {
			sho = start[i];
			reflectionVertex[i] = sho;
			expandContractVertex[i] = sho;
			pmin[i] = sho;
		}

		for (int i = 0; i < noOfParameters; ++i) {
			vertices[i][noOfParametersPlus1 - 1] = start[i];
		}
		functionValueAtVertices[noOfParametersPlus1 - 1] = this.functionValue(start);
		for (int j = 0; j < noOfParameters; ++j) {
			start[j] = start[j] + step[j];
			for (int i = 0; i < noOfParameters; ++i)
				vertices[i][j] = start[i];
			functionValueAtVertices[j] = this.functionValue(start);
			start[j] = start[j] - step[j];
		}

		//
		// PDQADDED
		// The main loop......
		// keep going until converged......
		while (test) {
			// Determine h
			// Determine the lowest point of the simplex (the point on the
			// simplex
			// with the lowest function value)
			valueAtLowestVertex = functionValueAtVertices[0];
			currentLowestValue = valueAtLowestVertex;
			indexOfLowerVertex = 0;
			indexOfHighestVertex = 0;
			for (int i = 1; i < noOfParametersPlus1; ++i) {
				if (functionValueAtVertices[i] < valueAtLowestVertex) {
					valueAtLowestVertex = functionValueAtVertices[i];
					indexOfLowerVertex = i;
				}
				if (functionValueAtVertices[i] > currentLowestValue) {
					currentLowestValue = functionValueAtVertices[i];
					indexOfHighestVertex = i;
				}
			}
			// PDQADDED EXPLANATION
			// Calculate the centroid of the excluding
			// the maximum point
			// Calculate centroid
			for (int i = 0; i < noOfParameters; ++i) {
				zn = 0.0D;
				for (int j = 0; j < noOfParametersPlus1; ++j) {
					zn += vertices[i][j];
				}
				zn -= vertices[i][indexOfHighestVertex];
				centroid[i] = zn / noOfParameters;
			}
			// PDQADDED EXPLANATION
			// REFLECTION
			// REFLECT THE MAXIMUM THROUGH centroid (THE CENTROID)
			// THE RESULTING POINT IS reflectionVertex
			// Calculate p=(1+alpha).centroid-alpha.ph {Reflection}
			for (int i = 0; i < noOfParameters; ++i)
				reflectionVertex[i] = (1.0 + this.reflectionCoeff) * centroid[i] - this.reflectionCoeff
						* vertices[i][indexOfHighestVertex];

			// PDQADDED EXPLANATION
			// DETERMINE THE FUNCTION VALUE AT THIS NEW POINT
			// reflectionVertex
			// Calculate y*
			valueAtReflectionPoint = this.functionValue(reflectionVertex);
			// PDQ
			// increment function evaluation counter
			++this.noOfIterations;

			// check if new point is lower than the lowest
			// function point/value
			if (valueAtReflectionPoint < valueAtLowestVertex) {
				// Form p**=(1+gamma).p*-gamma.centroid {Extension}

				for (int i = 0; i < noOfParameters; ++i)
					expandContractVertex[i] = reflectionVertex[i] * (1.0D + this.expansionCoeff) - this.expansionCoeff
							* centroid[i];
				// Calculate y**
				valueAtExpandContractVertex = this.functionValue(expandContractVertex);
				++this.noOfIterations;

				if (valueAtExpandContractVertex < valueAtLowestVertex) {
					// REPLACE CURRENT MAXIMUM POINT BY expandContractVertex
					// AND
					// UPDATE
					// THE FUNCTION VALUE AT THIS POINT (i.e update vertices
					// and
					// functionValueAtVertices)
					// Replace ph by p**
					for (int i = 0; i < noOfParameters; ++i)
						vertices[i][indexOfHighestVertex] = expandContractVertex[i];
					functionValueAtVertices[indexOfHighestVertex] = valueAtExpandContractVertex;
				} else {
					// Replace ph by p*
					for (int i = 0; i < noOfParameters; ++i)
						vertices[i][indexOfHighestVertex] = reflectionVertex[i];
					functionValueAtVertices[indexOfHighestVertex] = valueAtReflectionPoint;
				}
				// valueAtReflectionPoint IS NOT LESS THAN valueAtLowestVertex
			} else {
				// Check y*>yi, i!=h
				ln = 0;
				for (int i = 0; i < noOfParametersPlus1; ++i)
					if (i != indexOfHighestVertex && valueAtReflectionPoint > functionValueAtVertices[i])
						++ln;
				if (ln == noOfParameters) {
					// y*>= all yi; Check if y*>yh
					if (valueAtReflectionPoint <= functionValueAtVertices[indexOfHighestVertex]) {
						// Replace ph by p*
						for (int i = 0; i < noOfParameters; ++i)
							vertices[i][indexOfHighestVertex] = reflectionVertex[i];
						functionValueAtVertices[indexOfHighestVertex] = valueAtReflectionPoint;
					}

					// Calculate new point p** =beta.ph+(1-beta)centroid
					// {Contraction}
					for (int i = 0; i < noOfParameters; ++i)
						expandContractVertex[i] = this.contractionCoeff * vertices[i][indexOfHighestVertex]
								+ (1.0 - this.contractionCoeff) * centroid[i];
					// Calculate y**
					valueAtExpandContractVertex = this.functionValue(expandContractVertex);
					++this.noOfIterations;
					// Check if function value at this new point is higher
					// than
					// highest value in the simplex
					if (valueAtExpandContractVertex > functionValueAtVertices[indexOfHighestVertex]) {
						// Replace all points(i) by (points(i)+points(lowest))/2
						for (int j = 0; j < noOfParametersPlus1; ++j) {
							for (int i = 0; i < noOfParameters; ++i) {
								vertices[i][j] = 0.5 * (vertices[i][j] + vertices[i][indexOfLowerVertex]);
								pmin[i] = vertices[i][j];
							}
							functionValueAtVertices[j] = this.functionValue(pmin);
						}
						this.noOfIterations += noOfParametersPlus1;
					} else {
						// Replace highest point with the expanded or contract
						// relfection point
						for (int i = 0; i < noOfParameters; ++i)
							vertices[i][indexOfHighestVertex] = expandContractVertex[i];
						functionValueAtVertices[indexOfHighestVertex] = valueAtExpandContractVertex;
					}
				} else {
					// replace highest point with reflection point
					for (int i = 0; i < noOfParameters; ++i)
						vertices[i][indexOfHighestVertex] = reflectionVertex[i];
					functionValueAtVertices[indexOfHighestVertex] = valueAtReflectionPoint;
				}
			}

			mean = 0.0;
			currentLowestValue = functionValueAtVertices[0];
			indexOfLowerVertex = 0;
			for (int i = 0; i < noOfParametersPlus1; ++i) {
				mean += functionValueAtVertices[i];
				if (currentLowestValue > functionValueAtVertices[i]) {
					currentLowestValue = functionValueAtVertices[i];
					indexOfLowerVertex = i;
				}
			}
			// mean value : mean
			mean /= (noOfParametersPlus1);
			// Now get standard deviation
			standardDev = 0.0;
			for (int i = 0; i < noOfParametersPlus1; ++i) {
				zn = functionValueAtVertices[i] - mean;
				standardDev += zn * zn;
			}
			// Standard dev....
			standardDev = Math.sqrt(standardDev / noOfParameters);

			// test for convergence
			if (standardDev < fTol)
				test = false;

			// this.minimum = currentLowestValue;
			// PDQ
			// We've converged but we want to make sure
			// so its good practice to restart the search
			// from this optimised position
			if (!test) {
				// store parameter values
				for (int i = 0; i < noOfParameters; ++i) {
					pmin[i] = vertices[i][indexOfLowerVertex];
					paramValue[i] = pmin[i] / this.scaling[i];
				}
				functionValueAtVertices[noOfParametersPlus1 - 1] = currentLowestValue;
				// test for restart
				--jcount;
				if (jcount > 0) {
					test = true;
					// Starting at min point setup up vertices
					// again using original step sizes
					for (int j = 0; j < noOfParameters; ++j) {
						pmin[j] = pmin[j] + step[j];
						for (int i = 0; i < noOfParameters; ++i)
							vertices[i][j] = pmin[i];
						functionValueAtVertices[j] = this.functionValue(pmin);
						pmin[j] = pmin[j] - step[j];
					}
				}
			}
			// Have we excedded the max number of iterations
			if (test && this.noOfIterations > this.maxNoOfIterations) {
				System.out.println("Maximum iteration number reached, in Minimisation.simplex(...)");
				System.out.println("without the convergence criterion being satisfied");
				System.out.println("Current parameter estimates and sfunction value returned");
				this.converganceStatus = false;
				// store current estimates
				for (int i = 0; i < noOfParameters; ++i)
					pmin[i] = vertices[i][indexOfLowerVertex];
				functionValueAtVertices[noOfParametersPlus1 - 1] = currentLowestValue;
				test = false;
			}

		}
		// The min positions are unscaled and stored in
		// paramValue
		for (int i = 0; i < noOfParameters; ++i) {
			pmin[i] = vertices[i][indexOfHighestVertex];
			paramValue[i] = pmin[i] / this.scaling[i];
		}
		this.minimum = currentLowestValue;
		// System.out.println("paramvalue\t" + paramValue.length);
		// System.out.println("nvalues\t" + paramValue.length);
		// System.out.println("1 Value of x at the minimum = " + paramValue[0]);
		// System.out.println("1 Value of y at the minimum = " + paramValue[1]);
		// System.out.println("1 Value of y at the minimum = " + paramValue[2]);
		this.noOfRestarts = this.maxNoOfRestarts - jcount;
		this.stop();
	}

	/**
	 * Nelder and Mead simplex Default maximum iterations public void nelderMead(NelderMeadFunction g, double[] start,
	 * double[] step) { this.noOfParameters = start.length; double[] lowerBound = new double[noOfParameters]; double[]
	 * upperBound = new double[noOfParameters]; for(int i=0;i<noOfParameters;i++) { lowerBound[i] = Double.MIN_VALUE;
	 * upperBound[i] = Double.MAX_VALUE; } this.nelderMead(g, start, lowerBound,upperBound,step, this.fTol,
	 * this.maxNoOfIterations); }
	 */
	/*
	 * Nelder and Mead simplex Default maximum iterations public void nelderMead(NelderMeadFunction g, double[] start) {
	 * this.noOfParameters = start.length; double[] lowerBound = new double[noOfParameters]; double[] upperBound = new
	 * double[noOfParameters]; double[] step = new double[noOfParameters]; for(int i=0;i<noOfParameters;i++) {
	 * lowerBound[i] = Double.MIN_VALUE; upperBound[i] = Double.MAX_VALUE; step[i] = start[i]*defaultStep; }
	 * this.nelderMead(g, start, lowerBound,upperBound,step, this.fTol, this.maxNoOfIterations); }
	 */
	/**
	 * Nelder and Mead simplex Default tolerance Default maximum iterations public void nelderMead(NelderMeadFunction g,
	 * double[] start, double[] lowerBound, double[] upperBound,double[] step) { this.nelderMead(g, start, step,
	 * lowerBound,upperBound,this.fTol, this.maxNoOfIterations); }
	 */
	/*
	 * Nelder and Mead simplex Default tolerance Default maximum iterations public void nelderMead(NelderMeadFunction g,
	 * double[] start, double[] lowerBound, double[] upperBound) { double[] step = new double[start.length]; for(int
	 * i=0;i<noOfParameters;i++) { step[i] = start[i]*defaultStep; } this.nelderMead(g, start, step,
	 * lowerBound,upperBound,this.fTol, this.maxNoOfIterations); }
	 */

	/**
	 * Calculate the function value for minimisation
	 * 
	 * @param x
	 *            The parameter values
	 * @return The function value with parameters x
	 */
	private double functionValue(double[] x) {
		double funcVal = -3.0D;
		double[] param = new double[this.noOfParameters];
		// rescale
		for (int i = 0; i < this.noOfParameters; i++)
			param[i] = x[i] / scaling[i];
		// A penalty value is added to the function if the values
		// are outside the bounds
		double tempFunctVal = 0.0D;
		for (int i = 0; i < this.noOfParameters; i++) {
			if (param[i] <= this.lowerBounds[i]) {
				tempFunctVal += this.penaltyWeight * Math.pow((param[i] - lowerBounds[i]), 2.0D);
			} else if (param[i] >= upperBounds[i]) {
				tempFunctVal += this.penaltyWeight * Math.pow((param[i] - upperBounds[i]), 2.0D);
			}
		}

		funcVal = tempFunctVal + function.evaluate(param);
		return funcVal;
	}


	/**
	 * Get the minimisation status
	 * 
	 * @return true if convergence was achieved false if convergence not achieved before maximum number of iterations
	 *         current values then returned
	 */
	public boolean getConverganceStatus() {
		return this.converganceStatus;
	}

	/**
	 * Reset scaling factors (scalingOption 0 and 1, see below for scalingOption 2)
	 * 
	 * @param n
	 *            The scaling option 0 no scaling 1 intial estimates all scaled to unity
	 */
	public void setScalingOption(int n) {
		if (n < 0 || n > 1)
			throw new IllegalArgumentException(
					"The argument must be 0 (no scaling) 1(initial estimates all scaled to unity) or the array of scaling factors");
		this.scalingOption = n;
	}

	/**
	 * @param n
	 */
	public void setStepOption(int n) {
		this.stepOption = n;
	}

	/**
	 * @return stepOption
	 */
	public int getStepOption() {
		return this.stepOption;
	}

	/**
	 * Set default step size the initial points of the simplex are defaultStep*startingpoint
	 * 
	 * @param steps
	 */
	public void setSteps(double[] steps) {
		this.userSteps = steps;
	}

	/**
	 * Set default step size the initial points of the simplex are defaultStep*startingpoint
	 * 
	 * @return the step size
	 */
	public double[] getSteps() {
		return this.step;
	}

	/**
	 * Set default step size the initial points of the simplex are defaultStep*startingpoint
	 * 
	 * @param stepSize
	 */
	public void setDefaultStep(double stepSize) {
		this.defaultStep = stepSize;
	}

	/**
	 * Set default step size the initial points of the simplex are defaultStep*startingpoint
	 * 
	 * @return the step size
	 */
	public double getDefaultStep() {
		return this.defaultStep;
	}

	/**
	 * Set scaling factors (scalingOption 2, see above for scalingOption 0 and 1)
	 * 
	 * @param sc
	 */
	public void setScaling(double[] sc) {
		this.scaling = sc;
		this.scalingOption = 2;
	}

	/**
	 * @return scaling factors
	 */
	public double[] getScaling() {
		return this.scaling;
	}

	/**
	 * @return double array containing the values of the parameters at the minimum
	 */
	@Override
	public double[] getBest() {
		return this.paramValue;
	}

	/**
	 * @return Get the function value at minimum
	 */
	@Override
	public double getMinimum() {
		return this.minimum;
	}

	/**
	 * @return The number of iterations in Nelder and Mead
	 */
	public int getNoOfIterations() {
		return this.noOfIterations;
	}

	/**
	 * Set the maximum number of iterations allowed in Nelder and Mead
	 * 
	 * @param maxNoOfIterations
	 */
	public void setMaxNoOfIterations(int maxNoOfIterations) {
		this.maxNoOfIterations = maxNoOfIterations;
	}

	/**
	 * @return The maximum number of iterations allowed in Nelder and Mead
	 */
	public int getMaxNoOfIterations() {
		return this.maxNoOfIterations;
	}

	/**
	 * @return The number of restarts in Nelder and Mead
	 */
	public int getNoOfRestarts() {
		return this.noOfRestarts;
	}

	/**
	 * Set the maximum number of restarts allowed in Nelder and Mead
	 * 
	 * @param nrs
	 *            no of restarts
	 */
	public void setMaxNoOfRestarts(int nrs) {
		this.maxNoOfRestarts = nrs;
	}

	/**
	 * @return The maximum number of restarts allowed in Nelder amd Mead
	 */
	public int getMaxNoOfRestarts() {
		return this.maxNoOfRestarts;
	}

	/**
	 * Set the Nelder and Mead reflection coefficient [alpha]
	 * 
	 * @param refl
	 */
	public void setReflectionCoefficient(double refl) {
		this.reflectionCoeff = refl;
	}

	/**
	 * @return The Nelder and Mead reflection coefficient [alpha]
	 */
	public double getReflectionCoefficient() {
		return this.reflectionCoeff;
	}

	/**
	 * Set the Nelder and Mead extension coefficient [beta]
	 * 
	 * @param ext
	 */
	public void setExpansionCoefficient(double ext) {
		this.expansionCoeff = ext;
	}

	/**
	 * @return The Nelder and Mead extension coefficient [beta]
	 */
	public double getExpansionCoefficient() {
		return this.expansionCoeff;
	}

	/**
	 * Set the Nelder and Mead contraction coefficient [gamma]
	 * 
	 * @param con
	 */
	public void setContractionCoeff(double con) {
		this.contractionCoeff = con;
	}

	/**
	 * @return The Nelder and Mead contraction coefficient [gamma]
	 */
	public double getContractionCoeff() {
		return contractionCoeff;
	}

	/**
	 * Set the minimisation tolerance to tol
	 * 
	 * @param tol
	 */
	public void setTolerance(double tol) {
		this.fTol = tol;
	}

	/**
	 * @return The minimisation tolerance
	 */
	public double getTolerance() {
		return this.fTol;
	}

	/**
	 * Starts the optimization process.... Creates a new thread etc........
	 */
	@Override
	public void start() {
		if (action == null) // if thread is not running
		{
			System.out.println("Nelder Mead Optimizer started");
			action = uk.ac.gda.util.ThreadManager.getThread(this); // Instantiate the new thread
			action.start(); // Start it
		} else {
			System.out.println("A Nelder Mead Optimization is already running");
			System.out.println("use the stop() method to terminate the current process");
		}
	}

	/**
	 * Set the starting point...overriding the values set in the AbstractCostFunction and AbstractMultiFunction
	 * 
	 * @param startingPoint
	 */
	public void setStartPoint(double[] startingPoint) {
		if (startingPoint.length != this.noOfParameters)
			throw new IllegalArgumentException("Length of array does not match noOfParameters");
		this.startingPoint = startingPoint;
	}

	/**
	 * Stops the optimization process.... Creates a new thread etc........
	 */
	@Override
	public void stop() {
		if (action != null) {
			System.out.println("Stopping Nelder Mead Optimizer");
			action = null;
		} else {
			System.out.println("There is no Nelder Mead Optimization running");
		}
	}

	@Override
	public void run() {
		Thread thisThread = Thread.currentThread();
		while (action == thisThread) {
			runNelderMead();
		}
		action = null;
	}

	@Override
	public boolean isRunning() {
		return (action != null);
	}

	@Override
	public void optimize() {
		runNelderMead();
	}
}
