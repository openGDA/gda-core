/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.server.ncd.optimiser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleValueChecker;
import org.apache.commons.math3.optim.nonlinear.scalar.MultivariateOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.AbstractSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.apache.derby.iapi.services.io.ArrayUtil;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;

/**
 * 	# find base line
	# find excursions from base line and back
	# count if reasonable number, redo with refined criteria if necessary
	# find longest regular spacing and reject points not on it
	# return
 */
public class LadderSampleFinder {
	
	private String plotPanel;
	private int len;
	private double max, min;
	
	public void setPlotPanel(String plotPanel) {
		this.plotPanel = plotPanel;
	}
	
	protected double findBaseLine(final double[] y) {
		MultivariateFunction mf = new MultivariateFunction() {
			
			@Override
			public double value(double[] arg0) {
				double sum = 0, x;
				double base = arg0[0];
				double spread = Math.abs(arg0[1]);
				if (spread < Double.MIN_VALUE*1e10) {
					spread = Double.MIN_VALUE*1e10;
				}
				
				for(double d: y) {
					x = Math.abs(base-d);
					if (x<spread) {
						sum += (spread - x) / spread;
					}
				}
				return sum;
			}
		};
		
		MultivariateOptimizer o = new SimplexOptimizer(new SimpleValueChecker(0, 0, 1000));
		AbstractSimplex worstAPIever = new NelderMeadSimplex(2);
		PointValuePair pair;
		pair = o.optimize(worstAPIever, 
				new ObjectiveFunction(mf), 
				new InitialGuess(new double[] {max, (max-min)/1000}),
				new MaxIter(1000),
				new MaxEval(1000));
		return pair.getKey()[0];
	}
	
	protected void initMaxMin(double[] y) {
		len = y.length;
		max = min = y[0];
		for(double d : y) {
			if (d>max) max = d;
			if (d<min) min = d;
		}
	}
	
	public List<Double> process(double[] x, double[] y) {
		
		initMaxMin(y);
		
		double baseLine = findBaseLine(y);
		
		List<Double> list = new ArrayList<Double>();
		list.add((double) 0);
		list.add((double) 100);
		return list;
	}

}
