package gda.analysis.numerical.utilities;

import java.util.Vector;

/***********************************************************************************************************************
 * USAGE: Mathematical class that supplements java.lang.Math and contains: the main physical constants trigonemetric
 * functions absent from java.lang.Math some useful additional mathematical functions some conversion functions WRITTEN
 * BY: Dr Michael Thomas Flanagan DATE: June 2002 AMENDED: 6 January 2006, 12 April 2006, 5 May 2006, 28 July 2006, 27
 * December 2006 DOCUMENTATION: See Michael Thomas Flanagan's Java library on-line web page: Utility.html Copyright (c)
 * July 2006 PERMISSION TO COPY: Permission to use, copy and modify this software and its documentation for
 * NON-COMMERCIAL purposes is granted, without fee, provided that an acknowledgement to the author, Michael Thomas
 * Flanagan at www.ee.ucl.ac.uk/~mflanaga, appears in all copies. Dr Michael Thomas Flanagan makes no representations
 * about the suitability or fitness of the software for any or for a particular purpose. Michael Thomas Flanagan shall
 * not be liable for any damages suffered as a result of using, modifying or distributing this software or its
 * derivatives. Modified at Diamond Light Source. (javadocs+additional methods)
 **********************************************************************************************************************/

public class Utility {

	// PHYSICAL CONSTANTS
	/** Avagadros constant */
	public static final double N_AVAGADRO = 6.0221419947e23; /* mol^-1 */

	/** Boltzmann Constant */
	public static final double K_BOLTZMANN = 1.380650324e-23; /* J K^-1 */

	/**
	 * 
	 */
	public static final double H_PLANCK = 6.6260687652e-34; /* J s */

	/**
	 * 
	 */
	public static final double H_PLANCK_RED = H_PLANCK / (2 * Math.PI); /* J s */

	/** Speed of light (m/s) */
	public static final double C_LIGHT = 2.99792458e8; /* m s^-1 */

	/**
	 * 
	 */
	public static final double R_GAS = 8.31447215; /* J K^-1 mol^-1 */

	/**
	 * 
	 */
	public static final double F_FARADAY = 9.6485341539e4; /* C mol^-1 */

	/** Absolute Zero */
	public static final double T_ABS = -273.15; /* Celsius */

	/** Electron Charge */
	public static final double Q_ELECTRON = -1.60217646263e-19; /* C */

	/** Electron Mass */
	public static final double M_ELECTRON = 9.1093818872e-31; /* kg */

	/** Proton Mass */
	public static final double M_PROTON = 1.6726215813e-27; /* kg */

	/** Neutron Mass */
	public static final double M_NEUTRON = 1.6749271613e-27; /* kg */

	/**
	 * 
	 */
	public static final double EPSILON_0 = 8.854187817e-12; /* F m^-1 */

	/**
	 * 
	 */
	public static final double MU_0 = Math.PI * 4e-7; /* H m^-1 (N A^-2) */

	/** MATHEMATICAL CONSTANTS */
	public static final double EULER_CONSTANT_GAMMA = 0.5772156649015627;

	/** Pi */
	public static final double PI = Math.PI; /* 3.141592653589793D */

	/** e */
	public static final double E = Math.E; /* 2.718281828459045D */

	// METHODS

	/** LOGARITHMS */
	/**
	 * @param a
	 * @return Log to base 10 of a double number a
	 */
	public static double log10(double a) {
		return Math.log(a) / Math.log(10.0D);
	}

	/**
	 * @param a
	 * @return Log to base 10 of a float number
	 */
	public static float log10(float a) {
		return (float) (Math.log(a) / Math.log(10.0D));
	}

	/**
	 * @param x
	 * @return Base 10 antilog of a double x
	 */
	public static double antilog10(double x) {
		return Math.pow(10.0D, x);
	}

	/**
	 * @param x
	 * @return Base 10 antilog of a float x
	 */
	public static float antilog10(float x) {
		return (float) Math.pow(10.0D, x);
	}

	/**
	 * @param a
	 * @return Log to base e of a double number
	 */
	public static double log(double a) {
		return Math.log(a);
	}

	/**
	 * @param a
	 * @return Log to base e of a float number
	 */
	public static float log(float a) {
		return (float) Math.log(a);
	}

	/**
	 * @param x
	 * @return Base e antilog of a double
	 */
	public static double antilog(double x) {
		return Math.exp(x);
	}

	/**
	 * @param x
	 * @return Base e antilog of a float
	 */
	public static float antilog(float x) {
		return (float) Math.exp(x);
	}

	/**
	 * @param a
	 * @return Log to base 2 of a double number
	 */
	public static double log2(double a) {
		return Math.log(a) / Math.log(2.0D);
	}

	/**
	 * @param a
	 * @return Log to base 2 of a float number
	 */
	public static float log2(float a) {
		return (float) (Math.log(a) / Math.log(2.0D));
	}

	/**
	 * @param x
	 * @return Base 2 antilog of a double
	 */
	public static double antilog2(double x) {
		return Math.pow(2.0D, x);
	}

	/**
	 * @param x
	 * @return Base 2 antilog of a float
	 */
	public static float antilog2(float x) {
		return (float) Math.pow(2.0D, x);
	}

	/**
	 * @param a
	 * @param b
	 * @return Log to base b of a double number and double base
	 */
	public static double log10(double a, double b) {
		return Math.log(a) / Math.log(b);
	}

	/**
	 * @param a
	 * @param b
	 * @return Log to base b of a double number and int base
	 */
	public static double log10(double a, int b) {
		return Math.log(a) / Math.log(b);
	}

	/**
	 * @param a
	 * @param b
	 * @return Log to base b of a float number and float base
	 */
	public static float log10(float a, float b) {
		return (float) (Math.log(a) / Math.log(b));
	}

	/**
	 * @param a
	 * @param b
	 * @return log a to base b
	 */
	public static float log10(float a, int b) {
		return (float) (Math.log(a) / Math.log(b));
	}

	/**
	 * @param a
	 * @return Square of a double number a
	 */

	public static double square(double a) {
		return a * a;
	}

	/**
	 * @param a
	 * @return Square of a float number a
	 */
	public static float square(float a) {
		return a * a;
	}

	/**
	 * @param a
	 * @return Square of a int number a
	 */
	public static int square(int a) {
		return a * a;
	}

	/**
	 * argument and return are integer, therefore limited to 0<=n<=12 see below for long and double arguments
	 * 
	 * @param n
	 * @return factorial of n
	 */
	public static int factorial(int n) {
		if (n < 0)
			throw new IllegalArgumentException("n must be a positive integer");
		if (n > 12)
			throw new IllegalArgumentException(
					"n must less than 13 to avoid integer overflow\nTry long or double argument");
		int f = 1;
		for (int i = 1; i <= n; i++)
			f *= i;
		return f;
	}

	/**
	 * argument and return are long, therefore limited to 0<=n<=20 see below for double argument
	 * 
	 * @param n
	 * @return factorial of n
	 */
	public static long factorial(long n) {
		if (n < 0)
			throw new IllegalArgumentException("n must be a positive integer");
		if (n > 20)
			throw new IllegalArgumentException(
					"n must less than 21 to avoid long integer overflow\nTry double argument");
		long f = 1;
		long iCount = 0L;
		while (iCount <= n) {
			f *= iCount;
			iCount += 1L;
		}
		return f;
	}

	/**
	 * Argument is of type double but must be, numerically, an integer factorial returned as double but is, numerically,
	 * should be an integer numerical rounding may makes this an approximation after n = 21
	 * 
	 * @param n
	 * @return factorial of n
	 */
	public static double factorial(double n) {
		if (n < 0 || (n - Math.floor(n)) != 0)
			throw new IllegalArgumentException(
					"\nn must be a positive integer\nIs a Gamma funtion [Utility.gamma(x)] more appropriate?");
		double f = 1.0D;
		double iCount = 0.0D;
		while (iCount <= n) {
			f *= iCount;
			iCount += 1.0D;
		}
		return f;
	}

	/**
	 * log[e](factorial) returned as double numerical rounding may makes this an approximation
	 * 
	 * @param n
	 * @return log to base e of the factorial of n
	 */
	public static double logFactorial(int n) {
		if (n < 0)
			throw new IllegalArgumentException(
					"\nn must be a positive integer\nIs a Gamma funtion [Utility.gamma(x)] more appropriate?");
		double f = 0.0D;
		for (int i = 2; i <= n; i++)
			f += Math.log(i);
		return f;
	}

	/**
	 * Argument is of type double but must be, numerically, an integer log[e](factorial) returned as double numerical
	 * rounding may makes this an approximation
	 * 
	 * @param n
	 * @return og to base e of the factorial of n
	 */
	public static double logFactorial(long n) {
		if (n < 0 || (n - Math.floor(n)) != 0)
			throw new IllegalArgumentException(
					"\nn must be a positive integer\nIs a Gamma funtion [Utility.gamma(x)] more appropriate?");
		double f = 0.0D;
		long iCount = 2L;
		while (iCount <= n) {
			f += Math.log(iCount);
			iCount += 1L;
		}
		return f;
	}

	/**
	 * Argument is of type double but must be, numerically, an integer log[e](factorial) returned as double numerical
	 * rounding may makes this an approximation
	 * 
	 * @param n
	 * @return log to base e of the factorial of n
	 */
	public static double logFactorial(double n) {
		if (n < 0 || (n - Math.floor(n)) != 0)
			throw new IllegalArgumentException(
					"\nn must be a positive integer\nIs a Gamma funtion [Utility.gamma(x)] more appropriate?");
		double f = 0.0D;
		double iCount = 2.0D;
		while (iCount <= n) {
			f += Math.log(iCount);
			iCount += 1.0D;
		}
		return f;
	}

	/**
	 * @param aa
	 * @return Maximum of a 1D array of doubles, aa
	 */
	public static double maximum(double[] aa) {
		int n = aa.length;
		double aamax = aa[0];
		for (int i = 1; i < n; i++) {
			if (aa[i] > aamax)
				aamax = aa[i];
		}
		return aamax;
	}

	/**
	 * @param aa
	 * @return Maximum of a 1D array of floats, aa
	 */
	public static float maximum(float[] aa) {
		int n = aa.length;
		float aamax = aa[0];
		for (int i = 1; i < n; i++) {
			if (aa[i] > aamax)
				aamax = aa[i];
		}
		return aamax;
	}

	/**
	 * Maximum of a 1D array of ints, aa
	 * 
	 * @param aa
	 * @return Maximum of a 1D array of ints, aa
	 */
	public static int maximum(int[] aa) {
		int n = aa.length;
		int aamax = aa[0];
		for (int i = 1; i < n; i++) {
			if (aa[i] > aamax)
				aamax = aa[i];
		}
		return aamax;
	}

	/**
	 * Section of a 1D array of ints, aa
	 * 
	 * @param aa
	 * @param start
	 *            start index
	 * @param end
	 *            end index
	 * @return section of array from start to end;
	 */
	public static int[] section(int[] aa, int start, int end) {
		int n = end - start;
		int[] result = new int[n];
		for (int i = start; i < end; i++) {
			result[i - start] = aa[i];
		}
		return result;
	}

	/**
	 * Section of a 1D array of floats, aa
	 * 
	 * @param aa
	 * @param start
	 *            start index
	 * @param end
	 *            end index
	 * @return section of array from start to end;
	 */
	public static float[] section(float[] aa, int start, int end) {
		int n = end - start;
		float[] result = new float[n];
		for (int i = start; i < end; i++) {
			result[i - start] = aa[i];
		}
		return result;
	}

	/**
	 * Section of a 1D array of doubles, aa
	 * 
	 * @param aa
	 * @param start
	 *            start index
	 * @param end
	 *            end index
	 * @return section of array from start to end;
	 */
	public static double[] section(double[] aa, int start, int end) {
		int n = end - start;
		double[] result = new double[n];
		for (int i = start; i < end; i++) {
			result[i - start] = aa[i];
		}
		return result;
	}

	/**
	 * @param aa
	 * @return Maximum of a 1D array of longs, aa
	 */

	public static long maximum(long[] aa) {
		long n = aa.length;
		long aamax = aa[0];
		for (int i = 1; i < n; i++) {
			if (aa[i] > aamax)
				aamax = aa[i];
		}
		return aamax;
	}

	/**
	 * @param aa
	 * @return Minimum of a 1D array of doubles, aa
	 */
	public static double minimum(double[] aa) {
		int n = aa.length;
		double aamin = aa[0];
		for (int i = 1; i < n; i++) {
			if (aa[i] < aamin)
				aamin = aa[i];
		}
		return aamin;
	}

	/**
	 * @param aa
	 * @return Minimum of a 1D array of floats, aa
	 */
	public static float minimum(float[] aa) {
		int n = aa.length;
		float aamin = aa[0];
		for (int i = 1; i < n; i++) {
			if (aa[i] < aamin)
				aamin = aa[i];
		}
		return aamin;
	}

	/**
	 * @param aa
	 * @return Minimum of a 1D array of ints, aa
	 */
	public static int minimum(int[] aa) {
		int n = aa.length;
		int aamin = aa[0];
		for (int i = 1; i < n; i++) {
			if (aa[i] < aamin)
				aamin = aa[i];
		}
		return aamin;
	}

	/**
	 * @param aa
	 * @return Minimum of a 1D array of longs, aa
	 */
	public static long minimum(long[] aa) {
		long n = aa.length;
		long aamin = aa[0];
		for (int i = 1; i < n; i++) {
			if (aa[i] < aamin)
				aamin = aa[i];
		}
		return aamin;
	}

	/**
	 * @param aa
	 * @return Reverse the order of the elements of a 1D array of doubles, aa
	 */
	public static double[] reverseArray(double[] aa) {
		int n = aa.length;
		double[] bb = new double[n];
		for (int i = 0; i < n; i++) {
			bb[i] = aa[n - 1 - i];
		}
		return bb;
	}

	/**
	 * @param aa
	 * @return Reverse the order of the elements of a 1D array of floats, aa
	 */
	public static float[] reverseArray(float[] aa) {
		int n = aa.length;
		float[] bb = new float[n];
		for (int i = 0; i < n; i++) {
			bb[i] = aa[n - 1 - i];
		}
		return bb;
	}

	/**
	 * @param aa
	 * @return Reverse the order of the elements of a 1D array of ints, aa
	 */
	public static int[] reverseArray(int[] aa) {
		int n = aa.length;
		int[] bb = new int[n];
		for (int i = 0; i < n; i++) {
			bb[i] = aa[n - 1 - i];
		}
		return bb;
	}

	/**
	 * @param aa
	 * @return Reverse the order of the elements of a 1D array of longs, aa
	 */
	public static long[] reverseArray(long[] aa) {
		int n = aa.length;
		long[] bb = new long[n];
		for (int i = 0; i < n; i++) {
			bb[i] = aa[n - 1 - i];
		}
		return bb;
	}

	/**
	 * @param aa
	 * @return Reverse the order of the elements of a 1D array of char, aa
	 */
	public static char[] reverseArray(char[] aa) {
		int n = aa.length;
		char[] bb = new char[n];
		for (int i = 0; i < n; i++) {
			bb[i] = aa[n - 1 - i];
		}
		return bb;
	}

	/**
	 * @param aa
	 * @return return absolute values of an array of doubles
	 */
	public static double[] arrayAbs(double[] aa) {
		int n = aa.length;
		double[] bb = new double[n];
		for (int i = 0; i < n; i++) {
			bb[i] = Math.abs(aa[i]);
		}
		return bb;
	}

	/**
	 * @param aa
	 * @return return absolute values of an array of floats
	 */
	public static float[] arrayAbs(float[] aa) {
		int n = aa.length;
		float[] bb = new float[n];
		for (int i = 0; i < n; i++) {
			bb[i] = Math.abs(aa[i]);
		}
		return bb;
	}

	/**
	 * @param aa
	 * @return return absolute values of an array of long
	 */
	public static long[] arrayAbs(long[] aa) {
		int n = aa.length;
		long[] bb = new long[n];
		for (int i = 0; i < n; i++) {
			bb[i] = Math.abs(aa[i]);
		}
		return bb;
	}

	/**
	 * @param aa
	 * @return return absolute values of an array of int
	 */
	public static int[] arrayAbs(int[] aa) {
		int n = aa.length;
		int[] bb = new int[n];
		for (int i = 0; i < n; i++) {
			bb[i] = Math.abs(aa[i]);
		}
		return bb;
	}

	/**
	 * @param aa
	 * @param constant
	 * @return multiple all elements by a constant double[] by double -> double[]
	 */
	public static double[] arrayMultByConstant(double[] aa, double constant) {
		int n = aa.length;
		double[] bb = new double[n];
		for (int i = 0; i < n; i++) {
			bb[i] = aa[i] * constant;
		}
		return bb;
	}

	/**
	 * @param aa
	 * @param constant
	 * @return multiple all elements by a constant int[] by double -> double[]
	 */
	public static double[] arrayMultByConstant(int[] aa, double constant) {
		int n = aa.length;
		double[] bb = new double[n];
		for (int i = 0; i < n; i++) {
			bb[i] = aa[i] * constant;
		}
		return bb;
	}

	/**
	 * @param aa
	 * @param constant
	 * @return multiple all elements by a constant double[] by int -> double[]
	 */
	public static double[] arrayMultByConstant(double[] aa, int constant) {
		int n = aa.length;
		double[] bb = new double[n];
		for (int i = 0; i < n; i++) {
			bb[i] = aa[i] * constant;
		}
		return bb;
	}

	/**
	 * @param aa
	 * @param constant
	 * @return multiple all elements by a constant int[] by int -> double[]
	 */
	public static double[] arrayMultByConstant(int[] aa, int constant) {
		int n = aa.length;
		double[] bb = new double[n];
		for (int i = 0; i < n; i++) {
			bb[i] = (aa[i] * constant);
		}
		return bb;
	}

	/**
	 * @param array
	 * @param value
	 * @return finds the value of nearest element value in array to the argument value
	 */
	public static double nearestElementValue(double[] array, double value) {
		double diff = Math.abs(array[0] - value);
		double nearest = array[0];
		for (int i = 1; i < array.length; i++) {
			if (Math.abs(array[i] - value) < diff) {
				diff = Math.abs(array[i] - value);
				nearest = array[i];
			}
		}
		return nearest;
	}

	/**
	 * @param array
	 * @param value
	 * @return finds the index of nearest element value in array to the argument value
	 */
	public static int nearestElementIndex(double[] array, double value) {
		double diff = Math.abs(array[0] - value);
		int nearest = 0;
		for (int i = 1; i < array.length; i++) {
			if (Math.abs(array[i] - value) < diff) {
				diff = Math.abs(array[i] - value);
				nearest = i;
			}
		}
		return nearest;
	}

	/**
	 * @param array
	 * @param value
	 * @return the value of nearest lower element value in array to the argument value
	 */
	public static double nearestLowerElementValue(double[] array, double value) {
		double diff0 = 0.0D;
		double diff1 = 0.0D;
		double nearest = 0.0D;
		int ii = 0;
		boolean test = true;
		double min = array[0];
		while (test) {
			if (array[ii] < min)
				min = array[ii];
			if ((value - array[ii]) >= 0.0D) {
				diff0 = value - array[ii];
				nearest = array[ii];
				test = false;
			} else {
				ii++;
				if (ii > array.length - 1) {
					nearest = min;
					diff0 = min - value;
					test = false;
				}
			}
		}
		for (int i = 0; i < array.length; i++) {
			diff1 = value - array[i];
			if (diff1 >= 0.0D && diff1 < diff0) {
				diff0 = diff1;
				nearest = array[i];
			}
		}
		return nearest;
	}

	/**
	 * @param array
	 * @param value
	 * @return finds the index of nearest lower element value in array to the argument value
	 */
	public static int nearestLowerElementIndex(double[] array, double value) {
		double diff0 = 0.0D;
		double diff1 = 0.0D;
		int nearest = 0;
		int ii = 0;
		boolean test = true;
		double min = array[0];
		int minI = 0;
		while (test) {
			if (array[ii] < min) {
				min = array[ii];
				minI = ii;
			}
			if ((value - array[ii]) >= 0.0D) {
				diff0 = value - array[ii];
				nearest = ii;
				test = false;
			} else {
				ii++;
				if (ii > array.length - 1) {
					nearest = minI;
					diff0 = min - value;
					test = false;
				}
			}
		}
		for (int i = 0; i < array.length; i++) {
			diff1 = value - array[i];
			if (diff1 >= 0.0D && diff1 < diff0) {
				diff0 = diff1;
				nearest = i;
			}
		}
		return nearest;
	}

	/**
	 * @param array
	 * @param value
	 * @return finds the value of nearest higher element value in array to the argument value
	 */
	public static double nearestHigherElementValue(double[] array, double value) {
		double diff0 = 0.0D;
		double diff1 = 0.0D;
		double nearest = 0.0D;
		int ii = 0;
		boolean test = true;
		double max = array[0];
		while (test) {
			if (array[ii] > max)
				max = array[ii];
			if ((array[ii] - value) >= 0.0D) {
				diff0 = value - array[ii];
				nearest = array[ii];
				test = false;
			} else {
				ii++;
				if (ii > array.length - 1) {
					nearest = max;
					diff0 = value - max;
					test = false;
				}
			}
		}
		for (int i = 0; i < array.length; i++) {
			diff1 = array[i] - value;
			if (diff1 >= 0.0D && diff1 < diff0) {
				diff0 = diff1;
				nearest = array[i];
			}
		}
		return nearest;
	}

	/**
	 * @param array
	 * @param value
	 * @return finds the index of nearest higher element value in array to the argument value
	 */
	public static int nearestHigherElementIndex(double[] array, double value) {
		double diff0 = 0.0D;
		double diff1 = 0.0D;
		int nearest = 0;
		int ii = 0;
		boolean test = true;
		double max = array[0];
		int maxI = 0;
		while (test) {
			if (array[ii] > max) {
				max = array[ii];
				maxI = ii;
			}
			if ((array[ii] - value) >= 0.0D) {
				diff0 = value - array[ii];
				nearest = ii;
				test = false;
			} else {
				ii++;
				if (ii > array.length - 1) {
					nearest = maxI;
					diff0 = value - max;
					test = false;
				}
			}
		}
		for (int i = 0; i < array.length; i++) {
			diff1 = array[i] - value;
			if (diff1 >= 0.0D && diff1 < diff0) {
				diff0 = diff1;
				nearest = i;
			}
		}
		return nearest;
	}

	/**
	 * Finds the value of nearest element value in array to the argument value.
	 * 
	 * @param array
	 * @param value
	 * @return the value of nearest element value in array to the argument value
	 */
	public static int nearestElementValue(int[] array, int value) {
		int diff = Math.abs(array[0] - value);
		int nearest = array[0];
		for (int i = 1; i < array.length; i++) {
			if (Math.abs(array[i] - value) < diff) {
				diff = Math.abs(array[i] - value);
				nearest = array[i];
			}
		}
		return nearest;
	}

	/**
	 * @param array
	 * @param value
	 * @return finds the index of nearest element value in array to the argument value
	 */
	public static int nearestElementIndex(int[] array, int value) {
		int diff = Math.abs(array[0] - value);
		int nearest = 0;
		for (int i = 1; i < array.length; i++) {
			if (Math.abs(array[i] - value) < diff) {
				diff = Math.abs(array[i] - value);
				nearest = i;
			}
		}
		return nearest;
	}

	/**
	 * @param array
	 * @param value
	 * @return finds the value of nearest lower element value in array to the argument value
	 */
	public static int nearestLowerElementValue(int[] array, int value) {
		int diff0 = 0;
		int diff1 = 0;
		int nearest = 0;
		int ii = 0;
		boolean test = true;
		int min = array[0];
		while (test) {
			if (array[ii] < min)
				min = array[ii];
			if ((value - array[ii]) >= 0) {
				diff0 = value - array[ii];
				nearest = array[ii];
				test = false;
			} else {
				ii++;
				if (ii > array.length - 1) {
					nearest = min;
					diff0 = min - value;
					test = false;
				}
			}
		}
		for (int i = 0; i < array.length; i++) {
			diff1 = value - array[i];
			if (diff1 >= 0 && diff1 < diff0) {
				diff0 = diff1;
				nearest = array[i];
			}
		}
		return nearest;
	}

	/**
	 * @param array
	 * @param value
	 * @return finds the index of nearest lower element value in array to the argument value
	 */
	public static int nearestLowerElementIndex(int[] array, int value) {
		int diff0 = 0;
		int diff1 = 0;
		int nearest = 0;
		int ii = 0;
		boolean test = true;
		int min = array[0];
		int minI = 0;
		while (test) {
			if (array[ii] < min) {
				min = array[ii];
				minI = ii;
			}
			if ((value - array[ii]) >= 0) {
				diff0 = value - array[ii];
				nearest = ii;
				test = false;
			} else {
				ii++;
				if (ii > array.length - 1) {
					nearest = minI;
					diff0 = min - value;
					test = false;
				}
			}
		}
		for (int i = 0; i < array.length; i++) {
			diff1 = value - array[i];
			if (diff1 >= 0 && diff1 < diff0) {
				diff0 = diff1;
				nearest = i;
			}
		}
		return nearest;
	}

	/**
	 * @param array
	 * @param value
	 * @return finds the value of nearest higher element value in array to the argument value
	 */
	public static int nearestHigherElementValue(int[] array, int value) {
		int diff0 = 0;
		int diff1 = 0;
		int nearest = 0;
		int ii = 0;
		boolean test = true;
		int max = array[0];
		while (test) {
			if (array[ii] > max)
				max = array[ii];
			if ((array[ii] - value) >= 0) {
				diff0 = value - array[ii];
				nearest = array[ii];
				test = false;
			} else {
				ii++;
				if (ii > array.length - 1) {
					nearest = max;
					diff0 = value - max;
					test = false;
				}
			}
		}
		for (int i = 0; i < array.length; i++) {
			diff1 = array[i] - value;
			if (diff1 >= 0 && diff1 < diff0) {
				diff0 = diff1;
				nearest = array[i];
			}
		}
		return nearest;
	}

	/**
	 * Finds the index of nearest higher element value in array to the argument value.
	 * 
	 * @param array
	 * @param value
	 * @return the index of nearest higher element value in array to the argument value
	 */
	public static int nearestHigherElementIndex(int[] array, int value) {
		int diff0 = 0;
		int diff1 = 0;
		int nearest = 0;
		int ii = 0;
		boolean test = true;
		int max = array[0];
		int maxI = 0;
		while (test) {
			if (array[ii] > max) {
				max = array[ii];
				maxI = ii;
			}
			if ((array[ii] - value) >= 0) {
				diff0 = value - array[ii];
				nearest = ii;
				test = false;
			} else {
				ii++;
				if (ii > array.length - 1) {
					nearest = maxI;
					diff0 = value - max;
					test = false;
				}
			}
		}
		for (int i = 0; i < array.length; i++) {
			diff1 = array[i] - value;
			if (diff1 >= 0 && diff1 < diff0) {
				diff0 = diff1;
				nearest = i;
			}
		}
		return nearest;
	}

	/**
	 * @param array
	 * @return Sum of all array elements - double array
	 */
	public static double arraySum(double[] array) {
		double sum = 0.0D;
		for (double i : array)
			sum += i;
		return sum;
	}

	/**
	 * @param array
	 * @return Sum of all array elements - float array
	 */
	public static float arraySum(float[] array) {
		float sum = 0.0F;
		for (float i : array)
			sum += i;
		return sum;
	}

	/**
	 * @param array
	 * @return Sum of all array elements - int array
	 */
	public static int arraySum(int[] array) {
		int sum = 0;
		for (int i : array)
			sum += i;
		return sum;
	}

	/**
	 * @param array
	 * @return Sum of all array elements - long array
	 */
	public static long arraySum(long[] array) {
		long sum = 0L;
		for (long i : array)
			sum += i;
		return sum;
	}

	/**
	 * @param array
	 * @return Product of all array elements - double array
	 */
	public static double arrayProduct(double[] array) {
		double product = 1.0D;
		for (double i : array)
			product *= i;
		return product;
	}

	/**
	 * @param array
	 * @return Product of all array elements - float array
	 */
	public static float arrayProduct(float[] array) {
		float product = 1.0F;
		for (float i : array)
			product *= i;
		return product;
	}

	/**
	 * @param array
	 * @return Product of all array elements - int array
	 */
	public static int arrayProduct(int[] array) {
		int product = 1;
		for (int i : array)
			product *= i;
		return product;
	}

	/**
	 * @param array
	 * @return Product of all array elements - long array
	 */
	public static long arrayProduct(long[] array) {
		long product = 1L;
		for (long i : array)
			product *= i;
		return product;
	}

	/**
	 * @param aa
	 * @param bb
	 * @return Concatenate two double arrays
	 */
	public static double[] concatenate(double[] aa, double[] bb) {
		int aLen = aa.length;
		int bLen = bb.length;
		int cLen = aLen + bLen;
		double[] cc = new double[cLen];
		for (int i = 0; i < aLen; i++)
			cc[i] = aa[i];
		for (int i = 0; i < bLen; i++)
			cc[i + aLen] = bb[i];

		return cc;
	}

	/**
	 * @param aa
	 * @param bb
	 * @return Concatenate two float arrays
	 */
	public static float[] concatenate(float[] aa, float[] bb) {
		int aLen = aa.length;
		int bLen = bb.length;
		int cLen = aLen + bLen;
		float[] cc = new float[cLen];
		for (int i = 0; i < aLen; i++)
			cc[i] = aa[i];
		for (int i = 0; i < bLen; i++)
			cc[i + aLen] = bb[i];

		return cc;
	}

	/**
	 * @param aa
	 * @param bb
	 * @return Concatenate two float arrays
	 */
	public static int[] concatenate(int[] aa, int[] bb) {
		int aLen = aa.length;
		int bLen = bb.length;
		int cLen = aLen + bLen;
		int[] cc = new int[cLen];
		for (int i = 0; i < aLen; i++)
			cc[i] = aa[i];
		for (int i = 0; i < bLen; i++)
			cc[i + aLen] = bb[i];

		return cc;
	}

	/**
	 * @param aa
	 * @param bb
	 * @return Concatenate two long arrays
	 */
	public static long[] concatenate(long[] aa, long[] bb) {
		int aLen = aa.length;
		int bLen = bb.length;
		int cLen = aLen + bLen;
		long[] cc = new long[cLen];
		for (int i = 0; i < aLen; i++)
			cc[i] = aa[i];
		for (int i = 0; i < bLen; i++)
			cc[i + aLen] = bb[i];

		return cc;
	}

	/**
	 * @param aa
	 * @return recast an array of float as doubles
	 */
	public static double[] floatTOdouble(float[] aa) {
		int n = aa.length;
		double[] bb = new double[n];
		for (int i = 0; i < n; i++) {
			bb[i] = aa[i];
		}
		return bb;
	}

	/**
	 * @param aa
	 * @return recast an array of int as double
	 */
	public static double[] intTOdouble(int[] aa) {
		int n = aa.length;
		double[] bb = new double[n];
		for (int i = 0; i < n; i++) {
			bb[i] = aa[i];
		}
		return bb;
	}

	/**
	 * @param aa
	 * @return recast an array of int as float
	 */
	public static float[] intTOfloat(int[] aa) {
		int n = aa.length;
		float[] bb = new float[n];
		for (int i = 0; i < n; i++) {
			bb[i] = aa[i];
		}
		return bb;
	}

	/**
	 * @param aa
	 * @return recast an array of int as long
	 */
	public static long[] intTOlong(int[] aa) {
		int n = aa.length;
		long[] bb = new long[n];
		for (int i = 0; i < n; i++) {
			bb[i] = aa[i];
		}
		return bb;
	}

	/**
	 * @param aa
	 * @return recast an array of long as double // BEWARE POSSIBLE LOSS OF PRECISION
	 */
	public static double[] longTOdouble(long[] aa) {
		int n = aa.length;
		double[] bb = new double[n];
		for (int i = 0; i < n; i++) {
			bb[i] = aa[i];
		}
		return bb;
	}

	/**
	 * @param aa
	 * @return recast an array of long as float BEWARE POSSIBLE LOSS OF PRECISION
	 */
	public static float[] longTOfloat(long[] aa) {
		int n = aa.length;
		float[] bb = new float[n];
		for (int i = 0; i < n; i++) {
			bb[i] = aa[i];
		}
		return bb;
	}

	/**
	 * @param aa
	 * @return recast an array of short as double
	 */
	public static double[] shortTOdouble(short[] aa) {
		int n = aa.length;
		double[] bb = new double[n];
		for (int i = 0; i < n; i++) {
			bb[i] = aa[i];
		}
		return bb;
	}

	/**
	 * @param aa
	 * @return recast an array of short as float
	 */
	public static float[] shortTOfloat(short[] aa) {
		int n = aa.length;
		float[] bb = new float[n];
		for (int i = 0; i < n; i++) {
			bb[i] = aa[i];
		}
		return bb;
	}

	/**
	 * @param aa
	 * @return recast an array of short as long
	 */
	public static long[] shortTOlong(short[] aa) {
		int n = aa.length;
		long[] bb = new long[n];
		for (int i = 0; i < n; i++) {
			bb[i] = aa[i];
		}
		return bb;
	}

	/**
	 * @param aa
	 * @return recast an array of short as int
	 */
	public static int[] shortTOint(short[] aa) {
		int n = aa.length;
		int[] bb = new int[n];
		for (int i = 0; i < n; i++) {
			bb[i] = aa[i];
		}
		return bb;
	}

	/**
	 * @param aa
	 * @return recast an array of byte as double
	 */
	public static double[] byteTOdouble(byte[] aa) {
		int n = aa.length;
		double[] bb = new double[n];
		for (int i = 0; i < n; i++) {
			bb[i] = aa[i];
		}
		return bb;
	}

	/**
	 * @param aa
	 * @return recast an array of byte as float
	 */
	public static float[] byteTOfloat(byte[] aa) {
		int n = aa.length;
		float[] bb = new float[n];
		for (int i = 0; i < n; i++) {
			bb[i] = aa[i];
		}
		return bb;
	}

	/**
	 * @param aa
	 * @return recast an array of byte as long
	 */
	public static long[] byteTOlong(byte[] aa) {
		int n = aa.length;
		long[] bb = new long[n];
		for (int i = 0; i < n; i++) {
			bb[i] = aa[i];
		}
		return bb;
	}

	/**
	 * @param aa
	 * @return recast an array of byte as int
	 */
	public static int[] byteTOint(byte[] aa) {
		int n = aa.length;
		int[] bb = new int[n];
		for (int i = 0; i < n; i++) {
			bb[i] = aa[i];
		}
		return bb;
	}

	/**
	 * @param aa
	 * @return recast an array of byte as short
	 */
	public static short[] byteTOshort(byte[] aa) {
		int n = aa.length;
		short[] bb = new short[n];
		for (int i = 0; i < n; i++) {
			bb[i] = aa[i];
		}
		return bb;
	}

	/**
	 * @param aa
	 * @return recast an array of double as int BEWARE OF LOSS OF PRECISION
	 */
	public static int[] doubleTOint(double[] aa) {
		int n = aa.length;
		int[] bb = new int[n];
		for (int i = 0; i < n; i++) {
			bb[i] = (int) aa[i];
		}
		return bb;
	}

	/**
	 * @param aa
	 * @return sort elements in an array of doubles into ascending order using selection sort method returns Vector
	 *         containing the original array, the sorted array and an array of the indices of the sorted array
	 */
	public static Vector<Object> selectSortVector(double[] aa) {
		int index = 0;
		int lastIndex = -1;
		int n = aa.length;
		double holdb = 0.0D;
		int holdi = 0;
		double[] bb = new double[n];
		int[] indices = new int[n];
		for (int i = 0; i < n; i++) {
			bb[i] = aa[i];
			indices[i] = i;
		}

		while (lastIndex != n - 1) {
			index = lastIndex + 1;
			for (int i = lastIndex + 2; i < n; i++) {
				if (bb[i] < bb[index]) {
					index = i;
				}
			}
			lastIndex++;
			holdb = bb[index];
			bb[index] = bb[lastIndex];
			bb[lastIndex] = holdb;
			holdi = indices[index];
			indices[index] = indices[lastIndex];
			indices[lastIndex] = holdi;
		}
		Vector<Object> vec = new Vector<Object>();
		vec.addElement(aa);
		vec.addElement(bb);
		vec.addElement(indices);
		return vec;
	}

	/**
	 * @param aa
	 * @return sort elements in an array of doubles into ascending order using selection sort method
	 */
	public static double[] selectionSort(double[] aa) {
		int index = 0;
		int lastIndex = -1;
		int n = aa.length;
		double hold = 0.0D;
		double[] bb = new double[n];
		for (int i = 0; i < n; i++) {
			bb[i] = aa[i];
		}

		while (lastIndex != n - 1) {
			index = lastIndex + 1;
			for (int i = lastIndex + 2; i < n; i++) {
				if (bb[i] < bb[index]) {
					index = i;
				}
			}
			lastIndex++;
			hold = bb[index];
			bb[index] = bb[lastIndex];
			bb[lastIndex] = hold;
		}
		return bb;
	}

	/**
	 * @param aa
	 * @return sort elements in an array of floats into ascending order using selection sort method
	 */
	public static float[] selectionSort(float[] aa) {
		int index = 0;
		int lastIndex = -1;
		int n = aa.length;
		float hold = 0.0F;
		float[] bb = new float[n];
		for (int i = 0; i < n; i++) {
			bb[i] = aa[i];
		}

		while (lastIndex != n - 1) {
			index = lastIndex + 1;
			for (int i = lastIndex + 2; i < n; i++) {
				if (bb[i] < bb[index]) {
					index = i;
				}
			}
			lastIndex++;
			hold = bb[index];
			bb[index] = bb[lastIndex];
			bb[lastIndex] = hold;
		}
		return bb;
	}

	/**
	 * @param aa
	 * @return sort elements in an array of ints into ascending order using selection sort method *
	 */
	public static int[] selectionSort(int[] aa) {
		int index = 0;
		int lastIndex = -1;
		int n = aa.length;
		int hold = 0;
		int[] bb = new int[n];
		for (int i = 0; i < n; i++) {
			bb[i] = aa[i];
		}

		while (lastIndex != n - 1) {
			index = lastIndex + 1;
			for (int i = lastIndex + 2; i < n; i++) {
				if (bb[i] < bb[index]) {
					index = i;
				}
			}
			lastIndex++;
			hold = bb[index];
			bb[index] = bb[lastIndex];
			bb[lastIndex] = hold;
		}
		return bb;
	}

	/**
	 * @param aa
	 * @return sort elements in an array of longs into ascending order using selection sort method
	 */
	public static long[] selectionSort(long[] aa) {
		int index = 0;
		int lastIndex = -1;
		int n = aa.length;
		long hold = 0L;
		long[] bb = new long[n];
		for (int i = 0; i < n; i++) {
			bb[i] = aa[i];
		}

		while (lastIndex != n - 1) {
			index = lastIndex + 1;
			for (int i = lastIndex + 2; i < n; i++) {
				if (bb[i] < bb[index]) {
					index = i;
				}
			}
			lastIndex++;
			hold = bb[index];
			bb[index] = bb[lastIndex];
			bb[lastIndex] = hold;
		}
		return bb;
	}

	/**
	 * sort elements in an array of doubles into ascending order using selection sort method returns Vector containing
	 * the original array, the sorted array and an array of the indices of the sorted array
	 * 
	 * @param aa
	 * @param bb
	 * @param indices
	 */
	public static void selectionSort(double[] aa, double[] bb, int[] indices) {
		int index = 0;
		int lastIndex = -1;
		int n = aa.length;
		double holdb = 0.0D;
		int holdi = 0;
		for (int i = 0; i < n; i++) {
			bb[i] = aa[i];
			indices[i] = i;
		}

		while (lastIndex != n - 1) {
			index = lastIndex + 1;
			for (int i = lastIndex + 2; i < n; i++) {
				if (bb[i] < bb[index]) {
					index = i;
				}
			}
			lastIndex++;
			holdb = bb[index];
			bb[index] = bb[lastIndex];
			bb[lastIndex] = holdb;
			holdi = indices[index];
			indices[index] = indices[lastIndex];
			indices[lastIndex] = holdi;
		}
	}

	/**
	 * sort the elements of an array into ascending order with matching switches in an array of the length using
	 * selection sort method array determining the order is the first argument matching array is the second argument
	 * sorted arrays returned as third and fourth arguments resopectively
	 * 
	 * @param aa
	 * @param bb
	 * @param cc
	 * @param dd
	 */
	public static void selectionSort(double[] aa, double[] bb, double[] cc, double[] dd) {
		int index = 0;
		int lastIndex = -1;
		int n = aa.length;
		int m = bb.length;
		if (n != m)
			throw new IllegalArgumentException("First argument array, aa, (length = " + n
					+ ") and the second argument array, bb, (length = " + m + ") should be the same length");
		int nn = cc.length;
		if (nn < n)
			throw new IllegalArgumentException("The third argument array, cc, (length = " + nn
					+ ") should be at least as long as the first argument array, aa, (length = " + n + ")");
		int mm = dd.length;
		if (mm < m)
			throw new IllegalArgumentException("The fourth argument array, dd, (length = " + mm
					+ ") should be at least as long as the second argument array, bb, (length = " + m + ")");

		double holdx = 0.0D;
		double holdy = 0.0D;

		for (int i = 0; i < n; i++) {
			cc[i] = aa[i];
			dd[i] = bb[i];
		}

		while (lastIndex != n - 1) {
			index = lastIndex + 1;
			for (int i = lastIndex + 2; i < n; i++) {
				if (cc[i] < cc[index]) {
					index = i;
				}
			}
			lastIndex++;
			holdx = cc[index];
			cc[index] = cc[lastIndex];
			cc[lastIndex] = holdx;
			holdy = dd[index];
			dd[index] = dd[lastIndex];
			dd[lastIndex] = holdy;
		}
	}

	/**
	 * @param aa
	 * @param bb
	 * @param cc
	 * @param dd
	 */
	public static void selectionSort(float[] aa, float[] bb, float[] cc, float[] dd) {
		int index = 0;
		int lastIndex = -1;
		int n = aa.length;
		int m = bb.length;
		if (n != m)
			throw new IllegalArgumentException("First argument array, aa, (length = " + n
					+ ") and the second argument array, bb, (length = " + m + ") should be the same length");
		int nn = cc.length;
		if (nn < n)
			throw new IllegalArgumentException("The third argument array, cc, (length = " + nn
					+ ") should be at least as long as the first argument array, aa, (length = " + n + ")");
		int mm = dd.length;
		if (mm < m)
			throw new IllegalArgumentException("The fourth argument array, dd, (length = " + mm
					+ ") should be at least as long as the second argument array, bb, (length = " + m + ")");

		float holdx = 0.0F;
		float holdy = 0.0F;

		for (int i = 0; i < n; i++) {
			cc[i] = aa[i];
			dd[i] = bb[i];
		}

		while (lastIndex != n - 1) {
			index = lastIndex + 1;
			for (int i = lastIndex + 2; i < n; i++) {
				if (cc[i] < cc[index]) {
					index = i;
				}
			}
			lastIndex++;
			holdx = cc[index];
			cc[index] = cc[lastIndex];
			cc[lastIndex] = holdx;
			holdy = dd[index];
			dd[index] = dd[lastIndex];
			dd[lastIndex] = holdy;
		}
	}

	/**
	 * @param aa
	 * @param bb
	 * @param cc
	 * @param dd
	 */
	public static void selectionSort(long[] aa, long[] bb, long[] cc, long[] dd) {
		int index = 0;
		int lastIndex = -1;
		int n = aa.length;
		int m = bb.length;
		if (n != m)
			throw new IllegalArgumentException("First argument array, aa, (length = " + n
					+ ") and the second argument array, bb, (length = " + m + ") should be the same length");
		int nn = cc.length;
		if (nn < n)
			throw new IllegalArgumentException("The third argument array, cc, (length = " + nn
					+ ") should be at least as long as the first argument array, aa, (length = " + n + ")");
		int mm = dd.length;
		if (mm < m)
			throw new IllegalArgumentException("The fourth argument array, dd, (length = " + mm
					+ ") should be at least as long as the second argument array, bb, (length = " + m + ")");

		long holdx = 0L;
		long holdy = 0L;

		for (int i = 0; i < n; i++) {
			cc[i] = aa[i];
			dd[i] = bb[i];
		}

		while (lastIndex != n - 1) {
			index = lastIndex + 1;
			for (int i = lastIndex + 2; i < n; i++) {
				if (cc[i] < cc[index]) {
					index = i;
				}
			}
			lastIndex++;
			holdx = cc[index];
			cc[index] = cc[lastIndex];
			cc[lastIndex] = holdx;
			holdy = dd[index];
			dd[index] = dd[lastIndex];
			dd[lastIndex] = holdy;
		}
	}

	/**
	 * @param aa
	 * @param bb
	 * @param cc
	 * @param dd
	 */
	public static void selectionSort(int[] aa, int[] bb, int[] cc, int[] dd) {
		int index = 0;
		int lastIndex = -1;
		int n = aa.length;
		int m = bb.length;
		if (n != m)
			throw new IllegalArgumentException("First argument array, aa, (length = " + n
					+ ") and the second argument array, bb, (length = " + m + ") should be the same length");
		int nn = cc.length;
		if (nn < n)
			throw new IllegalArgumentException("The third argument array, cc, (length = " + nn
					+ ") should be at least as long as the first argument array, aa, (length = " + n + ")");
		int mm = dd.length;
		if (mm < m)
			throw new IllegalArgumentException("The fourth argument array, dd, (length = " + mm
					+ ") should be at least as long as the second argument array, bb, (length = " + m + ")");

		int holdx = 0;
		int holdy = 0;

		for (int i = 0; i < n; i++) {
			cc[i] = aa[i];
			dd[i] = bb[i];
		}

		while (lastIndex != n - 1) {
			index = lastIndex + 1;
			for (int i = lastIndex + 2; i < n; i++) {
				if (cc[i] < cc[index]) {
					index = i;
				}
			}
			lastIndex++;
			holdx = cc[index];
			cc[index] = cc[lastIndex];
			cc[lastIndex] = holdx;
			holdy = dd[index];
			dd[index] = dd[lastIndex];
			dd[lastIndex] = holdy;
		}
	}

	/**
	 * @param aa
	 * @param bb
	 * @param cc
	 * @param dd
	 */
	public static void selectionSort(double[] aa, long[] bb, double[] cc, long[] dd) {
		int index = 0;
		int lastIndex = -1;
		int n = aa.length;
		int m = bb.length;
		if (n != m)
			throw new IllegalArgumentException("First argument array, aa, (length = " + n
					+ ") and the second argument array, bb, (length = " + m + ") should be the same length");
		int nn = cc.length;
		if (nn < n)
			throw new IllegalArgumentException("The third argument array, cc, (length = " + nn
					+ ") should be at least as long as the first argument array, aa, (length = " + n + ")");
		int mm = dd.length;
		if (mm < m)
			throw new IllegalArgumentException("The fourth argument array, dd, (length = " + mm
					+ ") should be at least as long as the second argument array, bb, (length = " + m + ")");

		double holdx = 0.0D;
		long holdy = 0L;

		for (int i = 0; i < n; i++) {
			cc[i] = aa[i];
			dd[i] = bb[i];
		}

		while (lastIndex != n - 1) {
			index = lastIndex + 1;
			for (int i = lastIndex + 2; i < n; i++) {
				if (cc[i] < cc[index]) {
					index = i;
				}
			}
			lastIndex++;
			holdx = cc[index];
			cc[index] = cc[lastIndex];
			cc[lastIndex] = holdx;
			holdy = dd[index];
			dd[index] = dd[lastIndex];
			dd[lastIndex] = holdy;
		}
	}

	/**
	 * @param aa
	 * @param bb
	 * @param cc
	 * @param dd
	 */
	public static void selectionSort(long[] aa, double[] bb, long[] cc, double[] dd) {
		int index = 0;
		int lastIndex = -1;
		int n = aa.length;
		int m = bb.length;
		if (n != m)
			throw new IllegalArgumentException("First argument array, aa, (length = " + n
					+ ") and the second argument array, bb, (length = " + m + ") should be the same length");
		int nn = cc.length;
		if (nn < n)
			throw new IllegalArgumentException("The third argument array, cc, (length = " + nn
					+ ") should be at least as long as the first argument array, aa, (length = " + n + ")");
		int mm = dd.length;
		if (mm < m)
			throw new IllegalArgumentException("The fourth argument array, dd, (length = " + mm
					+ ") should be at least as long as the second argument array, bb, (length = " + m + ")");

		long holdx = 0L;
		double holdy = 0.0D;

		for (int i = 0; i < n; i++) {
			cc[i] = aa[i];
			dd[i] = bb[i];
		}

		while (lastIndex != n - 1) {
			index = lastIndex + 1;
			for (int i = lastIndex + 2; i < n; i++) {
				if (cc[i] < cc[index]) {
					index = i;
				}
			}
			lastIndex++;
			holdx = cc[index];
			cc[index] = cc[lastIndex];
			cc[lastIndex] = holdx;
			holdy = dd[index];
			dd[index] = dd[lastIndex];
			dd[lastIndex] = holdy;
		}
	}

	/**
	 * @param aa
	 * @param bb
	 * @param cc
	 * @param dd
	 */
	public static void selectionSort(double[] aa, int[] bb, double[] cc, int[] dd) {
		int index = 0;
		int lastIndex = -1;
		int n = aa.length;
		int m = bb.length;
		if (n != m)
			throw new IllegalArgumentException("First argument array, aa, (length = " + n
					+ ") and the second argument array, bb, (length = " + m + ") should be the same length");
		int nn = cc.length;
		if (nn < n)
			throw new IllegalArgumentException("The third argument array, cc, (length = " + nn
					+ ") should be at least as long as the first argument array, aa, (length = " + n + ")");
		int mm = dd.length;
		if (mm < m)
			throw new IllegalArgumentException("The fourth argument array, dd, (length = " + mm
					+ ") should be at least as long as the second argument array, bb, (length = " + m + ")");

		double holdx = 0.0D;
		int holdy = 0;

		for (int i = 0; i < n; i++) {
			cc[i] = aa[i];
			dd[i] = bb[i];
		}

		while (lastIndex != n - 1) {
			index = lastIndex + 1;
			for (int i = lastIndex + 2; i < n; i++) {
				if (cc[i] < cc[index]) {
					index = i;
				}
			}
			lastIndex++;
			holdx = cc[index];
			cc[index] = cc[lastIndex];
			cc[lastIndex] = holdx;
			holdy = dd[index];
			dd[index] = dd[lastIndex];
			dd[lastIndex] = holdy;
		}
	}

	/**
	 * @param aa
	 * @param bb
	 * @param cc
	 * @param dd
	 */
	public static void selectionSort(int[] aa, double[] bb, int[] cc, double[] dd) {
		int index = 0;
		int lastIndex = -1;
		int n = aa.length;
		int m = bb.length;
		if (n != m)
			throw new IllegalArgumentException("First argument array, aa, (length = " + n
					+ ") and the second argument array, bb, (length = " + m + ") should be the same length");
		int nn = cc.length;
		if (nn < n)
			throw new IllegalArgumentException("The third argument array, cc, (length = " + nn
					+ ") should be at least as long as the first argument array, aa, (length = " + n + ")");
		int mm = dd.length;
		if (mm < m)
			throw new IllegalArgumentException("The fourth argument array, dd, (length = " + mm
					+ ") should be at least as long as the second argument array, bb, (length = " + m + ")");

		int holdx = 0;
		double holdy = 0.0D;

		for (int i = 0; i < n; i++) {
			cc[i] = aa[i];
			dd[i] = bb[i];
		}

		while (lastIndex != n - 1) {
			index = lastIndex + 1;
			for (int i = lastIndex + 2; i < n; i++) {
				if (cc[i] < cc[index]) {
					index = i;
				}
			}
			lastIndex++;
			holdx = cc[index];
			cc[index] = cc[lastIndex];
			cc[lastIndex] = holdx;
			holdy = dd[index];
			dd[index] = dd[lastIndex];
			dd[lastIndex] = holdy;
		}
	}

	/**
	 * @param aa
	 * @param bb
	 * @param cc
	 * @param dd
	 */
	public static void selectionSort(long[] aa, int[] bb, long[] cc, int[] dd) {
		int index = 0;
		int lastIndex = -1;
		int n = aa.length;
		int m = bb.length;
		if (n != m)
			throw new IllegalArgumentException("First argument array, aa, (length = " + n
					+ ") and the second argument array, bb, (length = " + m + ") should be the same length");
		int nn = cc.length;
		if (nn < n)
			throw new IllegalArgumentException("The third argument array, cc, (length = " + nn
					+ ") should be at least as long as the first argument array, aa, (length = " + n + ")");
		int mm = dd.length;
		if (mm < m)
			throw new IllegalArgumentException("The fourth argument array, dd, (length = " + mm
					+ ") should be at least as long as the second argument array, bb, (length = " + m + ")");

		long holdx = 0L;
		int holdy = 0;

		for (int i = 0; i < n; i++) {
			cc[i] = aa[i];
			dd[i] = bb[i];
		}

		while (lastIndex != n - 1) {
			index = lastIndex + 1;
			for (int i = lastIndex + 2; i < n; i++) {
				if (cc[i] < cc[index]) {
					index = i;
				}
			}
			lastIndex++;
			holdx = cc[index];
			cc[index] = cc[lastIndex];
			cc[lastIndex] = holdx;
			holdy = dd[index];
			dd[index] = dd[lastIndex];
			dd[lastIndex] = holdy;
		}
	}

	/**
	 * @param aa
	 * @param bb
	 * @param cc
	 * @param dd
	 */
	public static void selectionSort(int[] aa, long[] bb, int[] cc, long[] dd) {
		int index = 0;
		int lastIndex = -1;
		int n = aa.length;
		int m = bb.length;
		if (n != m)
			throw new IllegalArgumentException("First argument array, aa, (length = " + n
					+ ") and the second argument array, bb, (length = " + m + ") should be the same length");
		int nn = cc.length;
		if (nn < n)
			throw new IllegalArgumentException("The third argument array, cc, (length = " + nn
					+ ") should be at least as long as the first argument array, aa, (length = " + n + ")");
		int mm = dd.length;
		if (mm < m)
			throw new IllegalArgumentException("The fourth argument array, dd, (length = " + mm
					+ ") should be at least as long as the second argument array, bb, (length = " + m + ")");

		int holdx = 0;
		long holdy = 0L;

		for (int i = 0; i < n; i++) {
			cc[i] = aa[i];
			dd[i] = bb[i];
		}

		while (lastIndex != n - 1) {
			index = lastIndex + 1;
			for (int i = lastIndex + 2; i < n; i++) {
				if (cc[i] < cc[index]) {
					index = i;
				}
			}
			lastIndex++;
			holdx = cc[index];
			cc[index] = cc[lastIndex];
			cc[lastIndex] = holdx;
			holdy = dd[index];
			dd[index] = dd[lastIndex];
			dd[lastIndex] = holdy;
		}
	}

	/**
	 * @param aa
	 * @param bb
	 * @param indices
	 *            sort elements in an array of doubles (first argument) into ascending order using selection sort method
	 *            returns the sorted array as second argument and an array of the indices of the sorted array as the
	 *            third argument
	 */
	public static void selectSort(double[] aa, double[] bb, int[] indices) {
		int index = 0;
		int lastIndex = -1;
		int n = aa.length;
		int m = bb.length;
		if (m < n)
			throw new IllegalArgumentException("The second argument array, bb, (length = " + m
					+ ") should be at least as long as the first argument array, aa, (length = " + n + ")");
		int k = indices.length;
		if (m < n)
			throw new IllegalArgumentException("The third argument array, indices, (length = " + k
					+ ") should be at least as long as the first argument array, aa, (length = " + n + ")");

		double holdb = 0.0D;
		int holdi = 0;
		for (int i = 0; i < n; i++) {
			bb[i] = aa[i];
			indices[i] = i;
		}

		while (lastIndex != n - 1) {
			index = lastIndex + 1;
			for (int i = lastIndex + 2; i < n; i++) {
				if (bb[i] < bb[index]) {
					index = i;
				}
			}
			lastIndex++;
			holdb = bb[index];
			bb[index] = bb[lastIndex];
			bb[lastIndex] = holdb;
			holdi = indices[index];
			indices[index] = indices[lastIndex];
			indices[lastIndex] = holdi;
		}
	}

	/**
	 * @param x
	 * @return returns -1 if x < 0 else returns 1 double version
	 */
	public static double sign(double x) {
		return (x < 0.0) ? -1.0 : 1.0;
	}

	/**
	 * @param x
	 * @return returns -1 if x < 0 else returns 1 float version
	 */
	public static float sign(float x) {
		return (x < 0.0F) ? -1.0F : 1.0F;
	}

	/**
	 * @param x
	 * @return returns -1 if x < 0 else returns 1 int version
	 */
	public static int sign(int x) {
		return (x < 0) ? -1 : 1;
	}

	/**
	 * @param x
	 * @return returns -1 if x < 0 else returns 1 long version
	 */
	public static long sign(long x) {
		return (x < 0) ? -1 : 1;
	}

	/** UNIT CONVERSIONS */

	/**
	 * @param rad
	 * @return Converts radians to degrees
	 */
	public static double radToDeg(double rad) {
		return rad * 180.0D / Math.PI;
	}

	/**
	 * @param deg
	 * @return Converts degrees to radians
	 */
	public static double degToRad(double deg) {
		return deg * Math.PI / 180.0D;
	}

	/**
	 * @param ev
	 * @return Converts electron volts(eV) to corresponding wavelength in nm
	 */
	public static double evToNm(double ev) {
		return 1e+9 * C_LIGHT / (-ev * Q_ELECTRON / H_PLANCK);
	}

	/**
	 * @param nm
	 * @return Converts wavelength in nm to matching energy in eV
	 */
	public static double nmToEv(double nm) {
		return C_LIGHT / (-nm * 1e-9) * H_PLANCK / Q_ELECTRON;
	}

	/**
	 * @param molar
	 * @param molWeight
	 * @return Converts moles per litre to percentage weight by volume
	 */
	public static double molarToPercentWeightByVol(double molar, double molWeight) {
		return molar * molWeight / 10.0D;
	}

	/**
	 * @param perCent
	 * @param molWeight
	 * @return Converts moles per litre to percentage weight by volume
	 */
	public static double percentWeightByVolToMolar(double perCent, double molWeight) {
		return perCent * 10.0D / molWeight;
	}

	/**
	 * @param cels
	 * @return Converts Celsius to Kelvin
	 */
	public static double celsiusToKelvin(double cels) {
		return cels - T_ABS;
	}

	/**
	 * @param kelv
	 * @return Converts Kelvin to Celsius
	 */
	public static double kelvinToCelsius(double kelv) {
		return kelv + T_ABS;
	}

	/**
	 * @param cels
	 * @return Converts Celsius to Fahrenheit
	 */
	public static double celsiusToFahren(double cels) {
		return cels * (9.0 / 5.0) + 32.0;
	}

	/**
	 * @param fahr
	 * @return Converts Fahrenheit to Celsius
	 */
	public static double fahrenToCelsius(double fahr) {
		return (fahr - 32.0) * 5.0 / 9.0;
	}

	/**
	 * @param cal
	 * @return Converts calories to Joules
	 */
	public static double calorieToJoule(double cal) {
		return cal * 4.1868;
	}

	/**
	 * @param joule
	 * @return Converts Joules to calories
	 */
	public static double jouleToCalorie(double joule) {
		return joule * 0.23884;
	}

	/**
	 * @param gm
	 * @return Converts grams to ounces
	 */
	public static double gramToOunce(double gm) {
		return gm / 28.3459;
	}

	/**
	 * @param oz
	 * @return Converts ounces to grams
	 */
	public static double ounceToGram(double oz) {
		return oz * 28.3459;
	}

	/**
	 * @param kg
	 * @return Converts kilograms to pounds
	 */
	public static double kgToPound(double kg) {
		return kg / 0.4536;
	}

	/**
	 * @param pds
	 * @return Converts pounds to kilograms
	 */
	public static double poundToKg(double pds) {
		return pds * 0.4536;
	}

	/**
	 * @param kg
	 * @return Converts kilograms to tons
	 */
	public static double kgToTon(double kg) {
		return kg / 1016.05;
	}

	/**
	 * @param tons
	 * @return Converts tons to kilograms
	 */
	public static double tonToKg(double tons) {
		return tons * 1016.05;
	}

	/**
	 * @param mm
	 * @return Converts millimetres to inches
	 */
	public static double millimetreToInch(double mm) {
		return mm / 25.4;
	}

	/**
	 * @param in
	 * @return Converts inches to millimetres
	 */
	public static double inchToMillimetre(double in) {
		return in * 25.4;
	}

	/**
	 * @param ft
	 * @return Converts feet to metres
	 */
	public static double footToMetre(double ft) {
		return ft * 0.3048;
	}

	/**
	 * @param metre
	 * @return Converts metres to feet
	 */
	public static double metreToFoot(double metre) {
		return metre / 0.3048;
	}

	/**
	 * @param yd
	 * @return Converts yards to metres
	 */
	public static double yardToMetre(double yd) {
		return yd * 0.9144;
	}

	/**
	 * @param metre
	 * @return Converts metres to yards
	 */
	public static double metreToYard(double metre) {
		return metre / 0.9144;
	}

	/**
	 * @param mile
	 * @return Converts miles to kilometres
	 */
	public static double mileToKm(double mile) {
		return mile * 1.6093;
	}

	/**
	 * @param km
	 * @return Converts kilometres to miles
	 */
	public static double kmToMile(double km) {
		return km / 1.6093;
	}

	/**
	 * @param gall
	 * @return Converts UK gallons to litres
	 */
	public static double gallonToLitre(double gall) {
		return gall * 4.546;
	}

	/**
	 * @param litre
	 * @return Converts litres to UK gallons
	 */
	public static double litreToGallon(double litre) {
		return litre / 4.546;
	}

	/**
	 * @param quart
	 * @return Converts UK quarts to litres
	 */
	public static double quartToLitre(double quart) {
		return quart * 1.137;
	}

	/**
	 * @param litre
	 * @return Converts litres to UK quarts
	 */
	public static double litreToQuart(double litre) {
		return litre / 1.137;
	}

	/**
	 * @param pint
	 * @return Converts UK pints to litres
	 */
	public static double pintToLitre(double pint) {
		return pint * 0.568;
	}

	/**
	 * @param litre
	 * @return Converts litres to UK pints
	 */
	public static double litreToPint(double litre) {
		return litre / 0.568;
	}

	/**
	 * @param gallPmile
	 * @return Converts UK gallons per mile to litres per kilometre
	 */
	public static double gallonPerMileToLitrePerKm(double gallPmile) {
		return gallPmile * 2.825;
	}

	/**
	 * Converts litres per kilometre to UK gallons per mile
	 * 
	 * @param litrePkm
	 * @return UK gallons per mile
	 */
	public static double litrePerKmToGallonPerMile(double litrePkm) {
		return litrePkm / 2.825;
	}

	/**
	 * Converts miles per UK gallons to kilometres per litre
	 * 
	 * @param milePgall
	 * @return kilometres per litre
	 */
	public static double milePerGallonToKmPerLitre(double milePgall) {
		return milePgall * 0.354;
	}

	/**
	 * Converts kilometres per litre to miles per UK gallons
	 * 
	 * @param kmPlitre
	 * @return miles per UK gallons
	 */
	public static double kmPerLitreToMilePerGallon(double kmPlitre) {
		return kmPlitre / 0.354;
	}

	/**
	 * Converts UK fluid ounce to American fluid ounce
	 * 
	 * @param flOzUK
	 * @return American fluid ounce
	 */
	public static double fluidOunceUKtoUS(double flOzUK) {
		return flOzUK * 0.961;
	}

	/**
	 * Converts American fluid ounce to UK fluid ounce
	 * 
	 * @param flOzUS
	 * @return UK fluid ounce
	 */
	public static double fluidOunceUStoUK(double flOzUS) {
		return flOzUS * 1.041;
	}

	/**
	 * Converts UK pint to American liquid pint
	 * 
	 * @param pintUK
	 * @return American pints
	 */
	public static double pintUKtoUS(double pintUK) {
		return pintUK * 1.201;
	}

	/**
	 * Converts American liquid pint to UK pint
	 * 
	 * @param pintUS
	 * @return UK pint
	 */
	public static double pintUStoUK(double pintUS) {
		return pintUS * 0.833;
	}

	/**
	 * Converts UK quart to American liquid quart
	 * 
	 * @param quartUK
	 * @return American liquid quart
	 */
	public static double quartUKtoUS(double quartUK) {
		return quartUK * 1.201;
	}

	/**
	 * Converts American liquid quart to UK quart
	 * 
	 * @param quartUS
	 * @return UK quart
	 */
	public static double quartUStoUK(double quartUS) {
		return quartUS * 0.833;
	}

	/**
	 * Converts UK gallon to American gallon
	 * 
	 * @param gallonUK
	 * @return American gallon
	 */
	public static double gallonUKtoUS(double gallonUK) {
		return gallonUK * 1.201;
	}

	/**
	 * Converts American gallon to UK gallon
	 * 
	 * @param gallonUS
	 * @return UK gallon
	 */
	public static double gallonUStoUK(double gallonUS) {
		return gallonUS * 0.833;
	}

	/**
	 * Converts UK pint to American cup
	 * 
	 * @param pintUK
	 * @return American cup
	 */
	public static double pintUKtoCupUS(double pintUK) {
		return pintUK / 0.417;
	}

	/**
	 * Converts American cup to UK pint
	 * 
	 * @param cupUS
	 * @return UK pint
	 */
	public static double cupUStoPintUK(double cupUS) {
		return cupUS * 0.417;
	}

	/**
	 * Calculates body mass index (BMI) from height (m) and weight (kg)
	 * 
	 * @param height
	 * @param weight
	 * @return body mass index (bmi)
	 */
	public static double calcBMImetric(double height, double weight) {
		return weight / (height * height);
	}

	/**
	 * Calculates body mass index (BMI) from height (ft) and weight (lbs)
	 * 
	 * @param height
	 * @param weight
	 * @return body mass index (bmi)
	 */
	public static double calcBMIimperial(double height, double weight) {
		height = Utility.footToMetre(height);
		weight = Utility.poundToKg(weight);
		return weight / (height * height);
	}

	/**
	 * Calculates weight (kg) to give a specified BMI for a given height (m)
	 * 
	 * @param bmi
	 * @param height
	 * @return weight (kg)
	 */
	public static double calcWeightFromBMImetric(double bmi, double height) {
		return bmi * height * height;
	}

	/**
	 * Calculates weight (lbs) to give a specified BMI for a given height (ft)
	 * 
	 * @param bmi
	 * @param height
	 * @return weight (lbs)
	 */
	public static double calcWeightFromBMIimperial(double bmi, double height) {
		height = Utility.footToMetre(height);
		double weight = bmi * height * height;
		weight = Utility.kgToPound(weight);
		return weight;
	}

	// ADDITIONAL TRIGONOMETRIC FUNCTIONS

	/**
	 * Returns the length of the hypotenuse of a and b i.e. sqrt(a*a+b*b) [without unecessary overflow or underflow]
	 * double version
	 * 
	 * @param aa
	 * @param bb
	 * @return length of the hypotenuse
	 */
	public static double hypot(double aa, double bb) {
		double amod = Math.abs(aa);
		double bmod = Math.abs(bb);
		double cc = 0.0D, ratio = 0.0D;
		if (amod == 0.0) {
			cc = bmod;
		} else {
			if (bmod == 0.0) {
				cc = amod;
			} else {
				if (amod >= bmod) {
					ratio = bmod / amod;
					cc = amod * Math.sqrt(1.0 + ratio * ratio);
				} else {
					ratio = amod / bmod;
					cc = bmod * Math.sqrt(1.0 + ratio * ratio);
				}
			}
		}
		return cc;
	}

	/**
	 * @param aa
	 * @param bb
	 * @return Returns the length of the hypotenuse of a and b i.e. sqrt(a*a+b*b) [without unecessary overflow or
	 *         underflow] float version
	 */
	public static float hypot(float aa, float bb) {
		return (float) hypot((double) aa, (double) bb);
	}

	/**
	 * @param xAtA
	 * @param yAtA
	 * @param xAtB
	 * @param yAtB
	 * @param xAtC
	 * @param yAtC
	 * @return Angle (in radians) subtended at coordinate C given x, y coordinates of all apices, A, B and C, of a
	 *         triangle
	 */
	public static double angle(double xAtA, double yAtA, double xAtB, double yAtB, double xAtC, double yAtC) {

		double ccos = Utility.cos(xAtA, yAtA, xAtB, yAtB, xAtC, yAtC);
		return Math.acos(ccos);
	}

	/**
	 * @param sideAC
	 * @param sideBC
	 * @param sideAB
	 * @return Angle (in radians) between sides sideA and sideB given all side lengths of a triangle
	 */
	public static double angle(double sideAC, double sideBC, double sideAB) {

		double ccos = Utility.cos(sideAC, sideBC, sideAB);
		return Math.acos(ccos);
	}

	/**
	 * @param xAtA
	 * @param yAtA
	 * @param xAtB
	 * @param yAtB
	 * @param xAtC
	 * @param yAtC
	 * @return Sine of angle subtended at coordinate C given x, y coordinates of all apices, A, B and C, of a triangle
	 */
	public static double sin(double xAtA, double yAtA, double xAtB, double yAtB, double xAtC, double yAtC) {
		double angle = Utility.angle(xAtA, yAtA, xAtB, yAtB, xAtC, yAtC);
		return Math.sin(angle);
	}

	/**
	 * @param sideAC
	 * @param sideBC
	 * @param sideAB
	 * @return Sine of angle between sides sideA and sideB given all side lengths of a triangle
	 */
	public static double sin(double sideAC, double sideBC, double sideAB) {
		double angle = Utility.angle(sideAC, sideBC, sideAB);
		return Math.sin(angle);
	}

	/**
	 * @param arg
	 * @return Sine given angle in radians for completion - returns Math.sin(arg)
	 */
	public static double sin(double arg) {
		return Math.sin(arg);
	}

	/**
	 * @param a
	 * @return Inverse sine Utility.asin Checks limits - Java Math.asin returns NaN if without limits
	 */
	public static double asin(double a) {
		if (a < -1.0D && a > 1.0D)
			throw new IllegalArgumentException("Utility.asin argument (" + a + ") must be >= -1.0 and <= 1.0");
		return Math.asin(a);
	}

	/**
	 * @param xAtA
	 * @param yAtA
	 * @param xAtB
	 * @param yAtB
	 * @param xAtC
	 * @param yAtC
	 * @return Cosine of angle subtended at coordinate C given x, y coordinates of all apices, A, B and C, of a triangle
	 */
	public static double cos(double xAtA, double yAtA, double xAtB, double yAtB, double xAtC, double yAtC) {
		double sideAC = Utility.hypot(xAtA - xAtC, yAtA - yAtC);
		double sideBC = Utility.hypot(xAtB - xAtC, yAtB - yAtC);
		double sideAB = Utility.hypot(xAtA - xAtB, yAtA - yAtB);
		return Utility.cos(sideAC, sideBC, sideAB);
	}

	/**
	 * @param sideAC
	 * @param sideBC
	 * @param sideAB
	 * @return Cosine of angle between sides sideA and sideB given all side lengths of a triangle
	 */
	public static double cos(double sideAC, double sideBC, double sideAB) {
		return 0.5D * (sideAC / sideBC + sideBC / sideAC - (sideAB / sideAC) * (sideAB / sideBC));
	}

	/**
	 * Cosine given angle in radians for completion
	 * 
	 * @param arg
	 * @return Math.cos(arg)
	 */
	public static double cos(double arg) {
		return Math.cos(arg);
	}

	/**
	 * Inverse cosine, checks limits
	 * 
	 * @param a
	 * @return Math.acos(a)
	 */
	public static double acos(double a) {
		if (a < -1.0D || a > 1.0D)
			throw new IllegalArgumentException("Utility.acos argument (" + a + ") must be >= -1.0 and <= 1.0");
		return Math.acos(a);
	}

	/**
	 * @param xAtA
	 * @param yAtA
	 * @param xAtB
	 * @param yAtB
	 * @param xAtC
	 * @param yAtC
	 * @return Tangent of angle subtended at coordinate C given x, y coordinates of all apices, A, B and C, of a
	 *         triangle
	 */
	public static double tan(double xAtA, double yAtA, double xAtB, double yAtB, double xAtC, double yAtC) {
		double angle = Utility.angle(xAtA, yAtA, xAtB, yAtB, xAtC, yAtC);
		return Math.tan(angle);
	}

	/**
	 * @param sideAC
	 * @param sideBC
	 * @param sideAB
	 * @return Tangent of angle between sides sideA and sideB given all side lengths of a triangle
	 */
	public static double tan(double sideAC, double sideBC, double sideAB) {
		double angle = Utility.angle(sideAC, sideBC, sideAB);
		return Math.tan(angle);
	}

	/**
	 * Tangent given angle in radians for completion
	 * 
	 * @param arg
	 * @return Math.tan(arg)
	 */
	public static double tan(double arg) {
		return Math.tan(arg);
	}

	/**
	 * Inverse tangent for completion
	 * 
	 * @param a
	 * @return Math.atan(a)
	 */
	public static double atan(double a) {
		return Math.atan(a);
	}

	/**
	 * Inverse tangent - ratio numerator and denominator provided for completion.
	 * 
	 * @param a
	 * @param b
	 * @return Math.atan2(a, b)
	 */
	public static double atan2(double a, double b) {
		return Math.atan2(a, b);
	}

	/**
	 * @param a
	 * @return Cotangent
	 */
	public static double cot(double a) {
		return 1.0D / Math.tan(a);
	}

	/**
	 * @param a
	 * @return Inverse cotangent
	 */
	public static double acot(double a) {
		return Math.atan(1.0D / a);
	}

	/**
	 * @param a
	 * @param b
	 * @return Inverse cotangent - ratio numerator and denominator provided
	 */
	public static double acot2(double a, double b) {
		return Math.atan2(b, a);
	}

	/**
	 * @param a
	 * @return Secant
	 */
	public static double sec(double a) {
		return 1.0 / Math.cos(a);
	}

	/**
	 * @param a
	 * @return Inverse secant
	 */
	public static double asec(double a) {
		if (a < 1.0D && a > -1.0D)
			throw new IllegalArgumentException("asec argument (" + a + ") must be >= 1 or <= -1");
		return Math.acos(1.0 / a);
	}

	/**
	 * @param a
	 * @return Cosecant
	 */
	public static double csc(double a) {
		return 1.0D / Math.sin(a);
	}

	/**
	 * @param a
	 * @return Inverse cosecant
	 */
	public static double acsc(double a) {
		if (a < 1.0D && a > -1.0D)
			throw new IllegalArgumentException("acsc argument (" + a + ") must be >= 1 or <= -1");
		return Math.asin(1.0 / a);
	}

	/**
	 * @param a
	 * @return Exsecant
	 */
	public static double exsec(double a) {
		return (1.0 / Math.cos(a) - 1.0D);
	}

	/**
	 * @param a
	 * @return Inverse exsecant
	 */
	public static double aexsec(double a) {
		if (a < 0.0D && a > -2.0D)
			throw new IllegalArgumentException("aexsec argument (" + a + ") must be >= 0.0 and <= -2");
		return Math.asin(1.0D / (1.0D + a));
	}

	/**
	 * @param a
	 * @return Versine
	 */
	public static double vers(double a) {
		return (1.0D - Math.cos(a));
	}

	/**
	 * @param a
	 * @return Inverse versine
	 */
	public static double avers(double a) {
		if (a < 0.0D && a > 2.0D)
			throw new IllegalArgumentException("avers argument (" + a + ") must be <= 2 and >= 0");
		return Math.acos(1.0D - a);
	}

	/**
	 * @param a
	 * @return Coversine
	 */
	public static double covers(double a) {
		return (1.0D - Math.sin(a));
	}

	/**
	 * @param a
	 * @return Inverse coversine (1 -sin(a))
	 */
	public static double acovers(double a) {
		if (a < 0.0D && a > 2.0D)
			throw new IllegalArgumentException("acovers argument (" + a + ") must be <= 2 and >= 0");
		return Math.asin(1.0D - a);
	}

	/**
	 * @param a
	 * @return Haversine
	 */
	public static double hav(double a) {
		return 0.5D * Utility.vers(a);
	}

	/**
	 * @param a
	 * @return Inverse haversine
	 */
	public static double ahav(double a) {
		if (a < 0.0D && a > 1.0D)
			throw new IllegalArgumentException("ahav argument (" + a + ") must be >= 0 and <= 1");
		return 0.5D * Utility.vers(a);
	}

	/**
	 * @param a
	 * @return Sinc
	 */
	public static double sinc(double a) {
		if (Math.abs(a) < 1e-40) {
			return 1.0D;
		}
		return Math.sin(a) / a;
	}

	/**
	 * @param a
	 * @return Hyperbolic sine of a double number
	 */
	public static double sinh(double a) {
		return 0.5D * (Math.exp(a) - Math.exp(-a));
	}

	/**
	 * @param a
	 * @return Inverse hyperbolic sine of a double number
	 */
	public static double asinh(double a) {
		double sgn = 1.0D;
		if (a < 0.0D) {
			sgn = -1.0D;
			a = -a;
		}
		return sgn * Math.log(a + Math.sqrt(a * a + 1.0D));
	}

	/**
	 * @param a
	 * @return Hyperbolic cosine of a double number
	 */
	public static double cosh(double a) {
		return 0.5D * (Math.exp(a) + Math.exp(-a));
	}

	/**
	 * @param a
	 * @return Inverse hyperbolic cosine of a double number
	 */
	public static double acosh(double a) {
		if (a < 1.0D)
			throw new IllegalArgumentException("acosh real number argument (" + a + ") must be >= 1");
		return Math.log(a + Math.sqrt(a * a - 1.0D));
	}

	/**
	 * @param a
	 * @return Hyperbolic tangent of a double number
	 */
	public static double tanh(double a) {
		return sinh(a) / cosh(a);
	}

	/**
	 * @param a
	 * @return Inverse hyperbolic tangent of a double number
	 */
	public static double atanh(double a) {
		double sgn = 1.0D;
		if (a < 0.0D) {
			sgn = -1.0D;
			a = -a;
		}
		if (a > 1.0D)
			throw new IllegalArgumentException("atanh real number argument (" + sgn * a + ") must be >= -1 and <= 1");
		return 0.5D * sgn * (Math.log(1.0D + a) - Math.log(1.0D - a));
	}

	/**
	 * @param a
	 * @return Hyperbolic cotangent of a double number
	 */
	public static double coth(double a) {
		return 1.0D / tanh(a);
	}

	/**
	 * @param a
	 * @return Inverse hyperbolic cotangent of a double number
	 */
	public static double acoth(double a) {
		double sgn = 1.0D;
		if (a < 0.0D) {
			sgn = -1.0D;
			a = -a;
		}
		if (a < 1.0D)
			throw new IllegalArgumentException("acoth real number argument (" + sgn * a + ") must be <= -1 or >= 1");
		return 0.5D * sgn * (Math.log(1.0D + a) - Math.log(a - 1.0D));
	}

	/**
	 * @param a
	 * @return Hyperbolic secant of a double number
	 */
	public static double sech(double a) {
		return 1.0D / cosh(a);
	}

	/**
	 * @param a
	 * @return Inverse hyperbolic secant of a double number
	 */
	public static double asech(double a) {
		if (a > 1.0D || a < 0.0D)
			throw new IllegalArgumentException("asech real number argument (" + a + ") must be >= 0 and <= 1");
		return 0.5D * (Math.log(1.0D / a + Math.sqrt(1.0D / (a * a) - 1.0D)));
	}

	/**
	 * @param a
	 * @return Hyperbolic cosecant of a double number
	 */
	public static double csch(double a) {
		return 1.0D / sinh(a);
	}

	/**
	 * @param a
	 * @return Inverse hyperbolic cosecant of a double number
	 */
	public static double acsch(double a) {
		double sgn = 1.0D;
		if (a < 0.0D) {
			sgn = -1.0D;
			a = -a;
		}
		return 0.5D * sgn * (Math.log(1.0 / a + Math.sqrt(1.0D / (a * a) + 1.0D)));
	}

	/**
	 * Rounds the mantissa of a double to prec places
	 * 
	 * @param x
	 * @param prec
	 * @return x rounded to prec places
	 */
	public static double truncate(double x, int prec) {

		if (prec < 0)
			throw new IllegalArgumentException("precision less than zero places");

		if (x == 0.0D)
			return x;
		if (Utility.isNaN(x))
			return x;
		if (Utility.isPlusInfinity(x))
			return x;
		if (Utility.isMinusInfinity(x))
			return x;

		char sign = ' ';
		if (x < 0.0D) {
			sign = '-';
			x = -x;
		}

		Double xx = new Double(x);
		String xString = xx.toString();
		String newXstring = stringRound(xString.trim(), prec, sign);

		return Double.parseDouble(newXstring);
	}

	/**
	 * Rounds the mantissa of a float to prec places
	 * 
	 * @param x
	 * @param prec
	 * @return x rounded to prec places
	 */
	public static float truncate(float x, int prec) {

		if (prec < 0)
			throw new IllegalArgumentException("precision less than zero places");

		if (x == 0.0D)
			return x;
		if (Utility.isNaN(x))
			return x;
		if (Utility.isPlusInfinity(x))
			return x;
		if (Utility.isMinusInfinity(x))
			return x;

		char sign = ' ';
		if (x < 0.0D) {
			sign = '-';
			x = -x;
		}

		Float xx = new Float(x);
		String xString = xx.toString();
		String newXstring = stringRound(xString.trim(), prec, sign);
		return Float.parseFloat(newXstring);
	}

	// Method for truncate
	private static String stringRound(String ss, int prec, char sign) {

		String returnString = null; // truncated number as a String
		int posE = ss.indexOf('E'); // Exponent E index
		int posDot = ss.indexOf('.'); // decimal point index

		String decPlaces = null; // number of decimal places
		String exponent = null; // exponent as a String
		String prePoint = ss.substring(0, posDot); // digits before decimal
		// place as a String
		int ppLen = prePoint.length(); // number of digits before the point

		if (posE == -1) {
			decPlaces = ss.substring(posDot + 1);
		} else {
			decPlaces = ss.substring(posDot + 1, posE);
			exponent = ss.substring(posE + 1);
		}
		int dpLen = decPlaces.length();

		// Return for truncation to zero decimal places
		if (prec == 0) {
			returnString = prePoint + ".0";
			if (exponent != null)
				returnString = returnString + "E" + exponent;
			if (sign == '-')
				returnString = "-" + returnString;
			return returnString;
		}

		// Return if number of decimal places required by truncation is greater
		// than the actual number of decimal placess
		if (dpLen <= prec) {
			returnString = ss;
			if (sign == '-')
				returnString = "-" + ss;
			return returnString;
		}

		// Check for 0. and then all nines before and one place after the
		// truncation point
		boolean test9 = true;
		boolean test9end = false;
		if (posE == -1) {
			if (ss.charAt(0) != '0') {
				test9 = false;
			} else {
				if (ss.charAt(1) != '.') {
					test9 = false;
				} else {
					for (int i = 2; i < 2 + prec; i++) {
						if (ss.charAt(i) != '9') {
							test9 = false;
						}
						if (!test9)
							break;
					}
					if (test9 && ss.charAt(prec + 2) > '4')
						test9end = true;
				}
			}
			if (test9) {
				if (test9end) {
					returnString = "1.";
					for (int i = 1; i <= prec; i++)
						returnString += '0';
					if (sign == '-')
						returnString = "-" + returnString;
					return returnString;
				}
				returnString = "0.";
				for (int i = 1; i <= prec; i++)
					returnString += '9';
				if (sign == '-')
					returnString = "-" + returnString;
				return returnString;
			}
		}

		// all other truncations
		int[] iss = new int[dpLen];
		int carry = 0;
		for (int i = dpLen - 1; i >= prec; i--) {
			iss[i] = decPlaces.charAt(i) + carry;
			if (iss[i] < 53) {
				carry = 0;
			} else {
				carry = 1;
			}
			iss[i] = 48;
		}
		boolean test = true;
		int ii = prec - 1;
		while (test) {
			iss[ii] = decPlaces.charAt(ii) + carry;
			if (iss[ii] < 58) {
				test = false;
				for (int i = 0; i < ii; i++) {
					iss[i] = decPlaces.charAt(i);
				}
				carry = 0;
			} else {
				iss[ii] = 48;
				carry = 1;
				ii--;
				if (ii < 0)
					test = false;
			}
		}

		test = true;
		if (carry == 0)
			test = false;
		ii = posDot - 1;
		while (test) {
			iss[ii] = prePoint.charAt(ii) + carry;
			if (iss[ii] < 58) {
				test = false;
				for (int i = 0; i < ii; i++) {
					iss[i] = prePoint.charAt(i);
				}
				carry = 0;
			} else {
				iss[ii] = 48;
				carry = 1;
				ii--;
				if (ii < 0)
					test = false;
			}
		}

		if (carry == 1) {
			prePoint = "1";
			for (int i = 0; i < ppLen; i++)
				prePoint = prePoint + "0";
			returnString = prePoint + ".0";

		} else {
			returnString = prePoint + ".";
			StringBuffer strbuff = new StringBuffer();
			for (int k = 0; k < prec; k++) {
				strbuff.append((char) iss[k]);
			}
			returnString = returnString + strbuff.toString();
		}

		if (exponent != null)
			returnString = returnString + "E" + exponent;
		if (sign == '-')
			returnString = "-" + returnString;

		return returnString;
	}

	/**
	 * @param x
	 * @return true if x is infinite, i.e. is equal to either plus or minus infinity x is double
	 */
	public static boolean isInfinity(double x) {
		boolean test = false;
		if (x == Double.POSITIVE_INFINITY || x == Double.NEGATIVE_INFINITY)
			test = true;
		return test;
	}

	/**
	 * @param x
	 * @return Returns true if x is infinite, i.e. is equal to either plus or minus infinity x is float
	 */
	public static boolean isInfinity(float x) {
		boolean test = false;
		if (x == Float.POSITIVE_INFINITY || x == Float.NEGATIVE_INFINITY)
			test = true;
		return test;
	}

	/**
	 * @param x
	 * @return Returns true if x is plus infinity x is double
	 */
	public static boolean isPlusInfinity(double x) {
		boolean test = false;
		if (x == Double.POSITIVE_INFINITY)
			test = true;
		return test;
	}

	/**
	 * @param x
	 * @return Returns true if x is plus infinity x is float
	 */
	public static boolean isPlusInfinity(float x) {
		boolean test = false;
		if (x == Float.POSITIVE_INFINITY)
			test = true;
		return test;
	}

	/**
	 * @param x
	 * @return Returns true if x is minus infinity x is double
	 */
	public static boolean isMinusInfinity(double x) {
		boolean test = false;
		if (x == Double.NEGATIVE_INFINITY)
			test = true;
		return test;
	}

	/**
	 * @param x
	 * @return Returns true if x is minus infinity x is float
	 */
	public static boolean isMinusInfinity(float x) {
		boolean test = false;
		if (x == Float.NEGATIVE_INFINITY)
			test = true;
		return test;
	}

	/**
	 * @param x
	 * @return Returns true if x is 'Not a Number' (NaN) x is double
	 */
	public static boolean isNaN(double x) {
		return Double.isNaN(x);
	}

	/**
	 * @param x
	 * @return Returns true if x is 'Not a Number' (NaN) x is float
	 */
	public static boolean isNaN(float x) {
		return Float.isNaN(x);
	}

	/**
	 * @param x
	 * @param y
	 * @return Returns true if x equals y x and y are double x may be float within range, PLUS_INFINITY,
	 *         NEGATIVE_INFINITY, or NaN NB!! This method treats two NaNs as equal
	 */
	public static boolean isEqual(double x, double y) {
		boolean test = false;
		if (Utility.isNaN(x)) {
			if (Utility.isNaN(y))
				test = true;
		} else {
			if (Utility.isPlusInfinity(x)) {
				if (Utility.isPlusInfinity(y))
					test = true;
			} else {
				if (Utility.isMinusInfinity(x)) {
					if (Utility.isMinusInfinity(y))
						test = true;
				} else {
					if (x == y)
						test = true;
				}
			}
		}
		return test;
	}

	/**
	 * @param x
	 * @param y
	 * @return Returns true if x equals y x and y are float x may be float within range, PLUS_INFINITY,
	 *         NEGATIVE_INFINITY, or NaN NB!! This method treats two NaNs as equal
	 */
	public static boolean isEqual(float x, float y) {
		boolean test = false;
		if (Utility.isNaN(x)) {
			if (Utility.isNaN(y))
				test = true;
		} else {
			if (Utility.isPlusInfinity(x)) {
				if (Utility.isPlusInfinity(y))
					test = true;
			} else {
				if (Utility.isMinusInfinity(x)) {
					if (Utility.isMinusInfinity(y))
						test = true;
				} else {
					if (x == y)
						test = true;
				}
			}
		}
		return test;
	}

	/**
	 * @param x
	 * @param y
	 * @return Returns true if x equals y x and y are int
	 */
	public static boolean isEqual(int x, int y) {
		boolean test = false;
		if (x == y)
			test = true;
		return test;
	}

	/**
	 * @param x
	 * @param y
	 * @return Returns true if x equals y x and y are char
	 */
	public static boolean isEqual(char x, char y) {
		boolean test = false;
		if (x == y)
			test = true;
		return test;
	}

	/**
	 * @param x
	 * @param y
	 * @return Returns true if x equals y
	 */
	public static boolean isEqual(String x, String y) {
		boolean test = false;
		if (x.equals(y))
			test = true;
		return test;
	}

	/**
	 * @param x
	 * @return Returns true if x is an even number, false if x is an odd number x is int
	 */
	public static boolean isEven(int x) {
		boolean test = false;
		if (x % 2 == 0.0D)
			test = true;
		return test;
	}

	/**
	 * @param x
	 * @param y
	 * @return Returns 0 if x == y Returns -1 if x < y Returns 1 if x > y x and y are double
	 */
	public static int compare(double x, double y) {
		Double X = new Double(x);
		Double Y = new Double(y);
		return X.compareTo(Y);
	}

	/**
	 * @param x
	 * @param y
	 * @return Returns 0 if x == y Returns -1 if x < y Returns 1 if x > y x and y are int
	 */
	public static int compare(int x, int y) {
		Integer X = new Integer(x);
		Integer Y = new Integer(y);
		return X.compareTo(Y);
	}

	/**
	 * @param x
	 * @param y
	 * @return Returns 0 if x == y Returns -1 if x < y Returns 1 if x > y x and y are long
	 */
	public static int compare(long x, long y) {
		Long X = new Long(x);
		Long Y = new Long(y);
		return X.compareTo(Y);
	}

	/**
	 * @param x
	 * @param y
	 * @return Returns 0 if x == y Returns -1 if x < y Returns 1 if x > y x and y are float
	 */
	public static int compare(float x, float y) {
		Float X = new Float(x);
		Float Y = new Float(y);
		return X.compareTo(Y);
	}

	/**
	 * @param x
	 * @param y
	 * @return Returns 0 if x == y Returns -1 if x < y Returns 1 if x > y x and y are short
	 */
	public static int compare(byte x, byte y) {
		Byte X = new Byte(x);
		Byte Y = new Byte(y);
		return X.compareTo(Y);
	}

	/**
	 * @param x
	 * @param y
	 * @return Returns 0 if x == y Returns -1 if x < y Returns 1 if x > y x and y are short
	 */
	public static int compare(short x, short y) {
		Short X = new Short(x);
		Short Y = new Short(y);
		return X.compareTo(Y);
	}

	/**
	 * @param x
	 * @return Returns true if x is an even number, false if x is an odd number x is float but must hold an integer
	 *         value
	 */
	public static boolean isEven(float x) {
		double y = Math.floor(x);
		if ((x - y) != 0.0D)
			throw new IllegalArgumentException("the argument is not an integer");
		boolean test = false;
		y = Math.floor(x / 2.0F);
		if (((x / 2.0F) - y) == 0.0D)
			test = true;
		return test;
	}

	/**
	 * @param x
	 * @return Returns true if x is an even number, false if x is an odd number x is double but must hold an integer
	 *         value
	 */
	public static boolean isEven(double x) {
		double y = Math.floor(x);
		if ((x - y) != 0.0D)
			throw new IllegalArgumentException("the argument is not an integer");
		boolean test = false;
		y = Math.floor(x / 2.0F);
		if ((x / 2.0D - y) == 0.0D)
			test = true;
		return test;
	}

	/**
	 * @param x
	 * @return Returns true if x is an odd number, false if x is an even number x is int
	 */
	public static boolean isOdd(int x) {
		boolean test = true;
		if (x % 2 == 0.0D)
			test = false;
		return test;
	}

	/**
	 * @param x
	 * @return Returns true if x is an odd number, false if x is an even number x is float but must hold an integer
	 *         value
	 */
	public static boolean isOdd(float x) {
		double y = Math.floor(x);
		if ((x - y) != 0.0D) {
			throw new IllegalArgumentException("the argument is not an integer");
		}
		boolean test = true;
		y = Math.floor(x / 2.0F);
		if (((x / 2.0F) - y) == 0.0D) {
			test = false;
		}
		return test;
	}

	/**
	 * @param x
	 * @return Returns true if x is an odd number, false if x is an even number x is double but must hold an integer
	 *         value
	 */
	public static boolean isOdd(double x) {
		double y = Math.floor(x);
		if ((x - y) != 0.0D)
			throw new IllegalArgumentException("the argument is not an integer");
		boolean test = true;
		y = Math.floor(x / 2.0F);
		if ((x / 2.0D - y) == 0.0D)
			test = false;
		return test;
	}

	/**
	 * @param year
	 * @return Returns true if year (argument) is a leap year
	 */
	public static boolean leapYear(int year) {
		boolean test = false;

		if (year % 4 != 0) {
			test = false;
		} else {
			if (year % 400 == 0) {
				test = true;
			} else {
				if (year % 100 == 0) {
					test = false;
				} else {
					test = true;
				}
			}
		}
		return test;
	}

	/**
	 * @param year
	 * @param month
	 * @param day
	 * @param hour
	 * @param min
	 * @param sec
	 * @return Returns milliseconds since 0 hours 0 minutes 0 seconds on 1 Jan 1970
	 */
	public static long dateToJavaMilliS(int year, int month, int day, int hour, int min, int sec) {

		long[] monthDays = { 0L, 31L, 28L, 31L, 30L, 31L, 30L, 31L, 31L, 30L, 31L, 30L, 31L };
		long ms = 0L;

		long yearDiff = 0L;
		int yearTest = year - 1;
		while (yearTest >= 1970) {
			yearDiff += 365;
			if (Utility.leapYear(yearTest))
				yearDiff++;
			yearTest--;
		}
		yearDiff *= 24L * 60L * 60L * 1000L;

		long monthDiff = 0L;
		int monthTest = month - 1;
		while (monthTest > 0) {
			monthDiff += monthDays[monthTest];
			if (Utility.leapYear(year))
				monthDiff++;
			monthTest--;
		}

		monthDiff *= 24L * 60L * 60L * 1000L;

		ms = yearDiff + monthDiff + day * 24L * 60L * 60L * 1000L + hour * 60L * 60L * 1000L + min * 60L * 1000L + sec
				* 1000L;

		return ms;
	}

}
