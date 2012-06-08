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

package gda.analysis.numerical.peakfind;

import gda.analysis.datastructure.DataVector;
import gda.analysis.numerical.linefunction.Gaussian1D;

/**
 * Example Class.
 */
public class Example {
	/**
	 * Main method.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		Gaussian1D g1d = new Gaussian1D(5.0, 5.0, 1.0);
		Gaussian1D g2d = new Gaussian1D(2.0, 10.0, 1.0);
		Gaussian1D g3d = new Gaussian1D(5.0, 18.0, 1.0);
		double x = 0.0;
		double range = 20.0;
		int size = 1000;
		double step = range / size;
		DataVector g1 = new DataVector(size);
		DataVector x1 = new DataVector(size);
		for (int i = 0; i < size; i++) {
			x1.set(i, x);
			g1.set(i, 100.0 * (g1d.val(x) + g2d.val(x) + g3d.val(x)));
			x += step;
		}
		double[] res = PeakFind.mariscotti(x1, g1, 1.0E-8);
		System.out.println("No of peaks\t" + res.length);
		for (int i = 0; i < res.length; i++) {
			System.out.println("res\t" + res[i]);
		}

	}

}
