/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.analysis.numerical.straightline;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

/**
 * Class to evaluate straightline expressions for a set of x,y values
 */
public class StraightLineFit {

	/**
	 * 
	 * @param data - data[i] contains y values for a line
	 * @param x
	 * @return Array of Result of length equal to data.length
	 */
	public static Result[] fit(double[][] data, double[] x) {
		
		int numLines = data.length;
		int pointsPerLine = x.length;
		for( int i=0; i< numLines; i++){
			if( data[i].length != pointsPerLine)
				throw new IllegalArgumentException("The number of points in each line must be equal to the size of the x axis vector");
			
		}
		double xAverage = getXAverage(x);
		double x1 = getX(x, xAverage);
		Result[] results = new Result[numLines];
		for (int line = 0; line < numLines; line++) {
			double[] y = data[line];
			results[line] = fit2(y, x, xAverage, x1);
		}
		return results;		
	}

	/**
	 * 
	 * @param data Points on a line are the set data.get(i)[m] where i varies from point to point
	 * on the line
	 * @param x
	 * @return Array of Result of length equal to the length of the data arrays
	 */
	public static Results fit(List<double[]> data, long [] dims, double[] x) {
		
		int numLines = data.get(0).length;
		int pointsPerLine = x.length;
		if( data.size() != pointsPerLine)
			throw new IllegalArgumentException("data.size() != pointsPerLine");
			
		for( int i=0; i< pointsPerLine; i++){
			if( data.get(i).length != numLines)
				throw new IllegalArgumentException("data.get(i).length != numLines");
			
		}
		double xAverage = getXAverage(x);
		double x1 = getX(x, xAverage);
		double[] y = new double[pointsPerLine];
		double [] slopes = new double[numLines];
		double [] offsets = new double[numLines];
		short [] fitoks = new short[numLines];
		Arrays.fill(fitoks, (short)0);
		if( pointsPerLine >= 2){
			Arrays.fill(fitoks, (short)1);
			for (int line = 0; line < numLines; line++) {
				for( int point=0; point<pointsPerLine; point++){
					y[point] = data.get(point)[line];
				}
				Result fit2 = fit2(y, x, xAverage, x1);
				slopes[line] = fit2.getSlope();
				offsets[line] = fit2.getOffset();
			}
		}
		return new Results(offsets, slopes, dims, fitoks);		
	}
	public static Results fitInt(List<Object> data, long [] dims, double[] x) {
		
		Object object = data.get(0);
		if( ! object.getClass().isArray()) {
			throw new IllegalArgumentException("fitInt can only accept arrays");
		}
		int numLines = ArrayUtils.getLength(object);
		int pointsPerLine = x.length;
		if( data.size() != pointsPerLine)
			throw new IllegalArgumentException("data.size() != pointsPerLine");
			
		for( int i=0; i< pointsPerLine; i++){
			if( ArrayUtils.getLength(data.get(i)) != numLines)
				throw new IllegalArgumentException("data.get(i).length != numLines");
			
		}
		double xAverage = getXAverage(x);
		double x1 = getX(x, xAverage);
		double[] y = new double[pointsPerLine];
		double [] slopes = new double[numLines];
		double [] offsets = new double[numLines];
		short [] fitoks = new short[numLines];
		Arrays.fill(fitoks, (short)0);		
		if( pointsPerLine >= 2){
			Arrays.fill(fitoks, (short)1);
			for (int line = 0; line < numLines; line++) {
				for( int point=0; point<pointsPerLine; point++){
					y[point] = Array.getDouble(data.get(point),line);
				}
				Result fit2 = fit2(y, x, xAverage, x1);
				slopes[line] = fit2.getSlope();
				offsets[line] = fit2.getOffset();
			}
		}
		return new Results(offsets, slopes, dims, fitoks);		
	}

	
	/**
	 * 
	 * @param data Points on a line are the set data.get(i)[m] where i varies from point to point
	 * on the line
	 * @param x
	 * @return Array of Result of length equal to the length of the data arrays
	 */
	public static Results fitInt(List<Object> data, long[] dims, int[] x) {

		double[] xDouble = new double[x.length];
		for( int point=0; point<x.length; point++){
			xDouble[point] = x[point];
		}
		return fitInt(data, dims, xDouble);
	}
	
	private static double getXAverage(double[] x) {
		double sumx = 0.0;
		int n = 0;
		while (n < x.length) {
			sumx += x[n];
			n++;
		}

		double xbar = sumx / n;
		return xbar;
	}

	private static double getX(double[] x, double xAvg) {
		double xxbar = 0.0;
		for (int i = 0; i < x.length; i++) {
			xxbar += (x[i] - xAvg) * (x[i] - xAvg);
		}
		return xxbar;
	}

	
	private static Result fit2(double[] y, double[] x, double xAvg, double xxBar) {
		int n = 0;
		double sumy = 0.0;
		while (n < x.length) {
			sumy += y[n];
			n++;
		}
		double ybar = sumy / n;
		double xybar = 0.0;
		for (int i = 0; i < n; i++) {
			xybar += (x[i] - xAvg) * (y[i] - ybar);
		}
		double beta1 = xybar / xxBar;
		double beta0 = ybar - beta1 * xAvg;
		return new Result(beta1, beta0);

	}

}
