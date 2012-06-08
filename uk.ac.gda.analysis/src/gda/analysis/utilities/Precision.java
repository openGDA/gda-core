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

package gda.analysis.utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class determines the precisions involved with a double number representation
 */

public final class Precision {
	private static final Logger logger = LoggerFactory.getLogger(Precision.class);

	/** Radix used by floating-point numbers. */
	private final static int radix = computeRadix();

	/** Largest positive value which, when added to 1.0, yields 0 */
	private final static double machinePrecision = computeMachinePrecision();

	/** Typical meaningful precision for numerical calculations. */
	private final static double defaultNumericalPrecision = Math.sqrt(machinePrecision);

	/**
	 * In mathematical numeral systems, the base or radix is usually the number of various unique digits, including
	 * zero, that a positional numeral system uses to represent numbers. For example, the decimal system, the most
	 * common system in use today, uses base ten, hence the maximum number a single digit will ever reach is 9, after
	 * which it is necessary to add another digit to achieve a higher number. For the binary system this is 2 but being
	 * really pedantic most machine precision computing codes go ahead and compute it anyway
	 * 
	 * @return the radix
	 */
	private static int computeRadix() {
		int radix = 0;
		double a = 1.0d;
		double tmp1, tmp2;
		do {
			a += a;
			tmp1 = a + 1.0d;
			tmp2 = tmp1 - a;
		} while (tmp2 - 1.0d != 0.0d);
		double b = 1.0d;
		while (radix == 0) {
			b += b;
			tmp1 = a + b;
			radix = (int) (tmp1 - a);
		}
		return radix;
	}

	/**
	 * Work out the machine precision This basically involves reducing some number to smaller and smaller values to the
	 * point where 1+number - 1 = 0.0. In other words the machine precision is the smallest number that can be added to
	 * a number that changes the value (to the computer anyway) of that number.
	 * 
	 * @return the machine precision
	 */
	private static double computeMachinePrecision() {
		double floatingRadix = getRadix();
		double inverseRadix = 1.0d / floatingRadix;
		double machinePrecision = 1.0d;
		double tmp = 1.0d + machinePrecision;
		while (tmp - 1.0d != 0.0d) {
			machinePrecision *= inverseRadix;
			tmp = 1.0d + machinePrecision;
		}
		return machinePrecision;
	}

	/**
	 * @return The radix...see computeRadix
	 */
	public static int getRadix() {
		return radix;
	}

	/**
	 * @return machine precision
	 */
	public static double getMachinePrecision() {
		return machinePrecision;
	}

	/**
	 * @return default Numerical Precision. This is the sqrt of the machine precision and is a ball park figure for what
	 *         is the minimum precision you should look for in a numerical analysis code For example when doing
	 *         numerical differention (x+h - x)/h or optimization the smallest step the algorithm should take is 10^-7
	 *         or 10^-8 whereas the machine precision is 10^-16
	 */
	public static double defaultNumericalPrecision() {
		return defaultNumericalPrecision;
	}

	/**
	 * Compares to values a and b to see if the difference between them is less than the default numerical precision
	 * 
	 * @param a
	 * @param b
	 * @return true if the difference between a and b is less than the default numerical precision
	 */
	public static boolean equals(double a, double b) {
		return equals(a, b, defaultNumericalPrecision());
	}

	/**
	 * Compares the values a and b to see if the difference between them is less than than precision
	 * 
	 * @param a
	 * @param b
	 * @param precision
	 * @return true if the relative difference between a and b is less than precision
	 */
	public static boolean equals(double a, double b, double precision) {
		double norm = Math.max(Math.abs(a), Math.abs(b));
		return norm < precision || Math.abs(a - b) < precision * norm;
	}

	/**
	 * Test main method.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		logger.debug("Floating-point machine parameters");
		logger.debug("---------------------------------");
		logger.debug("radix = " + Precision.getRadix());
		logger.debug("Machine precision = " + Precision.getMachinePrecision());
		logger.debug("Default numerical precision = " + Precision.defaultNumericalPrecision());
		// Message.debug(Precision.equals(2.71828182845905,
		// (2.71828182845904 + 0.00000000000001)));
		// Message.debug(Precision.equals(2.71828182845905, 2.71828182845904));
		// Message.debug(Precision.equals(2.718281828454, 2.718281828455));
		// Message.debug(Precision.equals(2.7182814, 2.7182815));
	}

}
