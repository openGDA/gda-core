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

import java.util.Arrays;
import java.util.Vector;

import junit.framework.Assert;

import org.junit.Test;

public class StraightLineFitTest {

	@Test
	public void testSimple1LineFit() {
		//supply a single line from with 2 points 0,0 and 1,1
		double [] line1 = new double[]{0,1}; 
		Result[] results = StraightLineFit.fit(new double[][]{line1}, new double[]{0,1});
		Assert.assertEquals(1, results.length);
		Assert.assertEquals(1.0, results[0].getSlope(), 1e-6);
		Assert.assertEquals(0.0, results[0].getOffset(), 1e-6);
	}

	@Test
	public void testSimple1LineFit2() {
		
		double slope=23;
		double offset = -12.2;
		int numPoints = 200;
		double [] y = new double[numPoints];
		double [] x = new double[numPoints];
		for(int i=0; i< numPoints; i++){
			x[i] = i*1.1;
			y[i] = offset + slope*x[i];
		}
		Result[] results = StraightLineFit.fit(new double[][]{y}, x);
		Assert.assertEquals(1, results.length);
		Assert.assertEquals(slope, results[0].getSlope(), 1e-6);
		Assert.assertEquals(offset, results[0].getOffset(), 1e-6);
	}

	@Test
	public void testSimple1LineFit3() {
		
		double slope=23;
		double offset = -12.2;
		int numPoints = 20;
		int numPixels = 200;
		Vector<double[]> yarrays = new Vector<double[]>();
		double [] x = new double[numPoints];
		for( int i=0; i< numPoints; i++){
			x[i] = i*1.1;
			double yval = offset + slope*x[i];
			double [] y = new double[numPixels];
			Arrays.fill(y, yval);
			yarrays.add(y);
		}
		Results results = StraightLineFit.fit(yarrays, new long[]{numPoints},x);
		Assert.assertEquals(numPixels, results.getSlopes().length);
		Assert.assertEquals(slope, results.getSlopes()[0], 1e-6);
		Assert.assertEquals(offset,results.getOffsets()[0], 1e-6);
	}

}
