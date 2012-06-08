package gda.util.fit;

// levenberg-marquardt in java
//
// To use this, implement the functions in the LMfunc interface.
//
// This library uses simple matrix routines from the JAMA java matrix package,
// which is in the public domain. Reference:
// http://math.nist.gov/javanumerics/jama/
// (JAMA has a matrix object class. An earlier library JNL, which is no longer
// available, represented matrices as low-level arrays. Several years
// ago the performance of JNL matrix code was better than that of JAMA,
// though improvements in java compilers may have fixed this by now.)
//
// One further recommendation would be to use an inverse based
// on Choleski decomposition, which is easy to implement and
// suitable for the symmetric inverse required here. There is a choleski
// routine at idiom.com/~zilla.
//
// If you make an improved version, please consider adding your
// name to it ("modified by ...") and send it back to me
// (and put it on the web).
//
// ----------------------------------------------------------------
// 
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Library General Public
// License as published by the Free Software Foundation; either
// version 2 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Library General Public License for more details.
// 
// You should have received a copy of the GNU Library General Public
// License along with this library; if not, write to the
// Free Software Foundation, Inc., 59 Temple Place - Suite 330,
// Boston, MA 02111-1307, USA.
//
// initial author contact info:
// jplewis www.idiom.com/~zilla zilla # computer.org, #=at
//
// Improvements by:
// dscherba www.ncsa.uiuc.edu/~dscherba
// Jonathan Jackson j.jackson # ucl.ac.uk

// see comment above
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Jama.Matrix;

/**
 * Levenberg-Marquardt, implemented from the general description in Numerical Recipes (NR), then tweaked slightly to
 * mostly match the results of their code. Use for nonlinear least squares assuming Gaussian errors. This holds some
 * parameters fixed by simply not updating them. this may be ok if the number if fixed parameters is small, but if the
 * number of varying parameters is larger it would be more efficient to make a smaller hessian involving only the
 * variables. The NR code assumes a statistical context, e.g. returns covariance of parameter errors; we do not do this.
 */
public final class LM {
	
	private static final Logger logger = LoggerFactory.getLogger(LM.class);

	/**
	 * calculate the current sum-squared-error (Chi-squared is the distribution of squared Gaussian errors, thus the
	 * name)
	 * 
	 * @param x
	 * @param y
	 * @param a
	 * @param s
	 * @param f
	 * @return chi-squared
	 */
	static double chiSquared(double[] x, double[] y, double[] a, double[] s, MultiFunction f) {
		int npts = y.length;
		double sum = 0.;
		for (int i = 0; i < npts; i++) {
			double d = y[i] - f.val(x[i], a);
			d = d / s[i];
			sum = sum + (d * d);
		}

		return sum;
	} // chiSquared

	/**
	 * Minimize E = sum {(y[k] - f(x[k],a)) / s[k]}^2 The individual errors are optionally scaled by s[k]. Note that
	 * LMfunc implements the value and gradient of f(x,a), NOT the value and gradient of E with respect to a!
	 * 
	 * @param x
	 *            array of domain points, each may be multidimensional
	 * @param y
	 *            corresponding array of values
	 * @param a
	 *            the parameters/state of the model
	 * @param s
	 * @param vary
	 *            false to indicate the corresponding a[k] is to be held fixed
	 * @param f
	 * @param h
	 * @param lambda
	 *            blend between steepest descent (lambda high) and jump to bottom of quadratic (lambda zero). Start with
	 *            0.001.
	 * @param termepsilon
	 *            termination accuracy (0.01)
	 * @param maxiter
	 *            stop and return after this many iterations if not done
	 * @param verbose
	 *            set to zero (no prints), 1, 2
	 * @return the new lambda for future iterations. Can use this and maxiter to interleave the LM descent with some
	 *         other task, setting maxiter to something small.
	 */
	public static double solve(double[] x, double[] a, double[] y, double[] s, boolean[] vary, MultiFunction f,
			double h, double lambda, double termepsilon, int maxiter, int verbose)
	// throws Exception
	{
		int npts = y.length;
		int nparm = a.length;

		assert s.length == npts;
		assert x.length == npts;
		if (verbose > 0) {
			logger.debug("solve x[" + x.length + "]");
			logger.debug(" a[" + a.length + "]");
			logger.debug(" y[" + y.length + "]");
		}
		double e0 = chiSquared(x, y, a, s, f);
		// double lambda = 0.001;
		boolean done = false;
		// g = gradient, H = hessian, d = step to minimum
		// H d = -g, solve for d
		double[][] H = new double[nparm][nparm];
		double[] g = new double[nparm];
		// double[] d = new double[nparm];

		double[] oos2 = new double[s.length];
		for (int i = 0; i < npts; i++)
			oos2[i] = 1. / (s[i] * s[i]);
		int iter = 0;
		int term = 0; // termination count test
		do {
			++iter;
			// hessian approximation
			for (int r = 0; r < nparm; r++) {
				for (int c = 0; c < nparm; c++) {
					for (int i = 0; i < npts; i++) {
						if (i == 0)
							H[r][c] = 0.;
						double xi = x[i];
						H[r][c] += (oos2[i] * gradient(xi, a, h, r, f) * gradient(xi, a, h, c, f));
					} // npts
				} // c
			} // r
			// boost diagonal towards gradient descent
			for (int r = 0; r < nparm; r++)
				H[r][r] *= (1. + lambda);

			// gradient
			for (int r = 0; r < nparm; r++) {
				for (int i = 0; i < npts; i++) {
					if (i == 0)
						g[r] = 0.;
					double xi = x[i];
					g[r] += (oos2[i] * (y[i] - f.val(xi, a)) * gradient(xi, a, h, r, f));
				}
			} // npts

			// scale (for consistency with NR, not necessary)
//			if (false) {
//				for (int r = 0; r < nparm; r++) {
//					g[r] = -0.5 * g[r];
//					for (int c = 0; c < nparm; c++) {
//						H[r][c] *= 0.5;
//					}
//				}
//			}

			// solve H d = -g, evaluate error at new location
			// double[] d = DoubleMatrix.solve(H, g);
			double[] d = (new Matrix(H)).lu().solve(new Matrix(g, nparm)).getRowPackedCopy();
			// double[] na = DoubleVector.add(a, d);
			double[] na = (new Matrix(a, nparm)).plus(new Matrix(d, nparm)).getRowPackedCopy();

			double e1 = chiSquared(x, y, na, s, f);

			if (verbose > 0) {
				logger.debug("\n\niteration " + iter + " lambda = " + lambda);
				System.out.print("a = ");
				(new Matrix(a, nparm)).print(10, 2);
				if (verbose > 1) {
					System.out.print("H = ");
					(new Matrix(H)).print(10, 2);
					System.out.print("g = ");
					(new Matrix(g, nparm)).print(10, 2);
					System.out.print("d = ");
					(new Matrix(d, nparm)).print(10, 2);
				}
				System.out.print("e0 = " + e0 + ": ");
				System.out.print("moved from ");
				(new Matrix(a, nparm)).print(10, 2);
				System.out.print("e1 = " + e1 + ": ");
				if (e1 < e0) {
					System.out.print("to ");
					(new Matrix(na, nparm)).print(10, 2);
				} else {
					logger.debug("move rejected");
				}
			}

			// termination test (slightly different than NR)
			if (Math.abs(e1 - e0) > termepsilon) {
				term = 0;
			} else {
				term++;
				if (term == 4) {
					logger.debug("terminating after " + iter + " iterations");
					done = true;
				}
			}
			if (iter >= maxiter)
				done = true;

			// in the C++ version, found that changing this to e1 >= e0
			// was not a good idea. See comment there.
			//
			if (e1 > e0 || Double.isNaN(e1)) { // new location worse than
				// before
				lambda *= 10.;
			} else { // new location better, accept new parameters
				lambda *= 0.1;
				e0 = e1;
				// simply assigning a = na will not get results copied back to
				// caller
				for (int i = 0; i < nparm; i++) {
					if (vary[i])
						a[i] = na[i];
				}
			}

		} while (!done);

		return lambda;
	} // solve

	// ----------------------------------------------------------------
	private static double gradient(double xi, double[] a, double h, int index, MultiFunction f) {
		double temp = a[index];
		a[index] = a[index] - h;
		double f1 = f.val(xi, a);
		a[index] = a[index] + 2.0 * h;
		double f2 = f.val(xi, a);
		a[index] = temp;
		return ((f2 - f1) / (2.0 * h));
	}

} // LM

