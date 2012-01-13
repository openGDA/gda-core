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

package uk.ac.gda.util.number;

public class DoubleUtils {

	/**
	 * Test if two numbers are equal.
	 * @param foo
	 * @param bar
	 * @param tolerance
	 * @return true if foo equals bar within tolerance
	 */
	public static boolean equalsWithinTolerance(Number foo, Number bar, Number tolerance) {
		final double a = foo.doubleValue();
		final double b = bar.doubleValue();
		final double t = tolerance.doubleValue();	
		return t>=Math.abs(a-b);
	}
	
	/**
	 * Test if two numbers are equal within an absolute or relative tolerance whichever is larger.
	 * The relative tolerance is given by a percentage and calculated from the absolute maximum of the input numbers.
	 * @param foo
	 * @param bar
	 * @param tolerance
	 * @param percentage
	 * @return true if foo equals bar within tolerance
	 */
	public static boolean equalsWithinTolerances(Number foo, Number bar, Number tolerance, Number percentage) {
		final double a = foo.doubleValue();
		final double b = bar.doubleValue();
		final double t = tolerance.doubleValue();
		final double p = percentage.doubleValue();

		double r = p * Math.max(Math.abs(a), Math.abs(b)) / 100.; // relative tolerance
		if (r > t)
			return r >= Math.abs(a - b);
		return t >= Math.abs(a - b);
	}

	public static void main(String[] args) {
		System.out.println(DoubleUtils.equalsWithinTolerance(10,11,2));
		System.out.println(DoubleUtils.equalsWithinTolerance(10,11,1));
		System.out.println(DoubleUtils.equalsWithinTolerance(10,10.9,1));
		System.out.println(DoubleUtils.equalsWithinTolerance(10.99,10.98,0.02));
		System.out.println(DoubleUtils.equalsWithinTolerance(10.99,10.97,0.02));
		System.out.println(DoubleUtils.equalsWithinTolerance(10.99,10.96,0.02));

		System.out.println(DoubleUtils.equalsWithinTolerances(10.99, 10.96, 0.02, 2.));
		System.out.println(DoubleUtils.equalsWithinTolerances(10.99, 10.96, 0.02, 0.1));

	}
}
