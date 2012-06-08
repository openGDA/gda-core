package gda.analysis.numerical.optimization.optimizers.leastsquares;

/*
 * Minpack.java copyright claim: This software is based on the public domain MINPACK routines. It was translated from
 * FORTRAN to Java by a US government employee on official time. Thus this software is also in the public domain. The
 * translator's mail address is: Steve Verrill USDA Forest Products Laboratory 1 Gifford Pinchot Drive Madison,
 * Wisconsin 53705 The translator's e-mail address is: steve@www1.fpl.fs.fed.us
 * ********************************************************************** DISCLAIMER OF WARRANTIES: THIS SOFTWARE IS
 * PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND. THE TRANSLATOR DOES NOT WARRANT, GUARANTEE OR MAKE ANY REPRESENTATIONS
 * REGARDING THE SOFTWARE OR DOCUMENTATION IN TERMS OF THEIR CORRECTNESS, RELIABILITY, CURRENCY, OR OTHERWISE. THE
 * ENTIRE RISK AS TO THE RESULTS AND PERFORMANCE OF THE SOFTWARE IS ASSUMED BY YOU. IN NO CASE WILL ANY PARTY INVOLVED
 * WITH THE CREATION OR DISTRIBUTION OF THE SOFTWARE BE LIABLE FOR ANY DAMAGE THAT MAY RESULT FROM THE USE OF THIS
 * SOFTWARE. Sorry about that. ********************************************************************** History: Date
 * Translator Changes 11/3/00 Steve Verrill Translated
 */

import gda.analysis.numerical.linefunction.IParameter;
import gda.analysis.numerical.optimization.objectivefunction.AbstractLSQObjectiveFunction;
import gda.analysis.numerical.optimization.objectivefunction.chisquared;
import gda.analysis.numerical.optimization.optimizers.IOptimizer;
import gda.analysis.utilities.Precision;

/**
 * <p>
 * This class contains Java translations of the MINPACK nonlinear least squares routines. As of November 2000,
 * <p>
 * The original FORTRAN MINPACK package was produced by Burton S. Garbow, Kenneth E. Hillstrom, and Jorge J. More as
 * part of the Argonne National Laboratory MINPACK project, March 1980.
 * 
 * @author (translator)Steve Verrill
 * @version .5 --- November 3, 2000 Modified by Paul Quinn : Diamond Light Source 2006 Fixed arrays so the indexing no
 *          longer starts at 1 (Fortran leftover) Removed lmder and simplified lmdif calling method Added LSQFunction
 *          object and machine precision calcs...
 */
public class minpackOptimizer implements IOptimizer, Runnable {
	/**
	 * machine precision
	 */
	private static final double epsmch = Precision.getMachinePrecision();

	/**
	 * minmag is the smallest magnitude
	 */
	private static final double minmag = Double.MIN_VALUE;

	/**
	 * Flag determining if the function contains a its own differentiation method
	 */
	// private boolean autoDerivative = false;
	/**
	 * 
	 */
	/**
	 * Max step size
	 */
	// private double maxStepSize;
	/**
	 * Min step size
	 */
	// private double minStepSize;
	/**
	 * 
	 */
	private double xTolerance = 1.0E-16;

	/**
	 * v
	 */
	private double fTolerance = 1.0E-16;

	/**
	 * v
	 */
	private double gTolerance = 1.0E-16;

	/**
	 * 
	 */
	private int maxNoOfFunctionEvaluations = 10000;

	/**
	 * 
	 */
	private double factor = 10.0;

	/**
	 * 
	 */
	private double epsfcn = 0.0;

	/**
	 * 
	 */
	private int nprint = 0;

	private double[] functionAtDataPoints;

	/**
	 * 
	 */
	private int mode = 1;

	/**
	 * A scaling factor
	 */
	// private double[] scaling=null;
	/**
	 * 
	 */
	private int noOfParameters;

	/**
	 * 
	 */
	private int noOfObservations;

	/**
	 * this times the no of parameters is the max no of function evaluations
	 */
	private int defaultMaxEvalFactor = 500;

	/**
	 * A class that implements the Lmdif_fcn interface (see the definition in Lmdif_fcn.java). See LmdifTest.java for an
	 * example of such a class. The class must define a method, fcn, that must have the form public static void fcn(int
	 * noOfObservations, int noOfParameters, double x[], double functionAtDataPoints[], int iflag[]) The value of
	 * iflag[0] should not be changed by fcn unless the user wants to terminate execution of lmdif. In this case set
	 * iflag[0] to a negative integer.
	 */
	private AbstractLSQObjectiveFunction function;

	/**
	 * info An integer output variable. info is set as follows.
	 * <P>
	 * info = 0 improper input parameters
	 * <P>
	 * <P>
	 * info = 1 algorithm estimates that the relative error in the sum of squares is at most tol.
	 * <P>
	 * <P>
	 * info = 2 algorithm estimates that the relative error between x and the solution is at most tol.
	 * <P>
	 * <P>
	 * info = 3 conditions for info = 1 and info = 2 both hold
	 * <P>
	 * <P>
	 * info = 4 functionAtDataPoints is orthogonal to the columns of the Jacobian to machine precision.
	 * <P>
	 * <P>
	 * info = 5 number of calls to fcn has reached or exceeded 200*(noOfParameters+1)
	 * <P>
	 * <P>
	 * info = 6 tol is too small. No further reduction in the sum of squares is possible.
	 * <P>
	 * <P>
	 * info = 7 tol is too small. No further improvement in the approximate solution x is possible.
	 * <P>
	 * <P>
	 * info = 9 Thread stopped
	 */
	private int[] info;

	/**
	 * The parameters
	 */
	private double[] parameters;

	/**
	 * The lower bounds on the parameters
	 */
	private double[] lowerBounds;

	/**
	 * The upper bounds on the parameters
	 */
	private double[] upperBounds;

	private Thread action;

	/**
	 * @param function
	 */

	public minpackOptimizer(chisquared function) {
		this.function = function;
		reset();

	}

	/**
	 * 
	 */
	public void reset() {
		noOfObservations = function.getNoOfDataPoints();
		info = new int[2];
		info[0] = 0;
		info[0] = 0;

		// Check the input parameters for errors.
		maxNoOfFunctionEvaluations = defaultMaxEvalFactor * (noOfParameters + 1);
		IParameter[] params = function.getParameters();
		// Now I have a list of free parameters....
		// set the relevant values for the codes
		int noOfFreeParameters = 0;
		for (int i = 0; i < params.length; i++) {
			if (!params[i].isFixed()) {
				noOfFreeParameters++;
			}
		}
		this.noOfParameters = noOfFreeParameters;
		// Population size
		parameters = new double[noOfParameters];
		lowerBounds = new double[noOfParameters];
		upperBounds = new double[noOfParameters];
		// x_orig = new double[noOfFreeParameters];
		int count = 0;
		for (int i = 0; i < params.length; i++) {
			if (!params[i].isFixed()) {

				parameters[count] = params[i].getValue();
				// x_orig[count] = params[i].getValue();
				lowerBounds[count] = params[i].getLowerLimit();
				upperBounds[count] = params[i].getUpperLimit();
				count++;
			}
		}
		for (int i = 0; i < parameters.length; i++) {
			System.out.println(parameters[i] + "\t" + lowerBounds[i] + "\t" + upperBounds[i]);
		}
	}

	// private double[] LSQFunction(double... params){

	// }
	/**
	 * <p>
	 * The enorm method calculates the Euclidean norm of a vector.
	 * <p>
	 * Translated by Steve Verrill on November 14, 2000 from the FORTRAN MINPACK source produced by Garbow, Hillstrom,
	 * and More.
	 * <p>
	 * 
	 * @param noOfParameters
	 *            The length of the vector, x.
	 * @param x
	 *            The vector whose Euclidean norm is to be calculated.
	 * @return calculates the Euclidean norm of a vector
	 */

	private double enorm(int noOfParameters, double x[]) {

		int i;
		double agiant, floatn, s1, s2, s3, xabs, x1max, x3max;
		double enorm;
		double rdwarf = 3.834e-20;
		double rgiant = 1.304e+19;

		s1 = 0.0;
		s2 = 0.0;
		s3 = 0.0;
		x1max = 0.0;
		x3max = 0.0;
		floatn = noOfParameters;
		agiant = rgiant / floatn;

		for (i = 0; i < noOfParameters; i++) {
			xabs = Math.abs(x[i]);
			if (xabs <= rdwarf || xabs >= agiant) {
				if (xabs > rdwarf) {
					// Sum for large components.
					if (xabs > x1max) {
						s1 = 1.0 + s1 * (x1max / xabs) * (x1max / xabs);
						x1max = xabs;
					} else {
						s1 += (xabs / x1max) * (xabs / x1max);
					}
				} else {
					// Sum for small components.
					if (xabs > x3max) {
						s3 = 1.0 + s3 * (x3max / xabs) * (x3max / xabs);
						x3max = xabs;
					} else {
						if (xabs != 0.0)
							s3 += (xabs / x3max) * (xabs / x3max);
					}
				}
			} else {
				// Sum for intermediate components.
				s2 += xabs * xabs;
			}
		}

		// Calculation of norm.

		if (s1 != 0.0) {

			enorm = x1max * Math.sqrt(s1 + (s2 / x1max) / x1max);

		} else {

			if (s2 != 0.0) {

				if (s2 >= x3max) {

					enorm = Math.sqrt(s2 * (1.0 + (x3max / s2) * (x3max * s3)));

				} else {

					enorm = Math.sqrt(x3max * ((s2 / x3max) + (x3max * s3)));

				}

			} else {

				enorm = x3max * Math.sqrt(s3);

			}

		}

		return enorm;

	}

	/**
	 * This method calculates the Euclidean norm of the vector stored in x[ ] with storage increment incx. It is a
	 * translation from FORTRAN to Java of the LINPACK function DNRM2. In the LINPACK listing DNRM2 is attributed to
	 * C.L. Lawson with a date of January 8, 1978. The routine below is based on a more recent DNRM2 version that is
	 * attributed in LAPACK documentation to Sven Hammarling. Translated by Steve Verrill, June 3, 1997.
	 * 
	 * @param noOfParameters
	 *            The order of the vector x[ ]
	 * @param x
	 * @param incx
	 *            The subscript increment for x[ ]
	 * @return The euclidean norm of the vector stored in x[ ] with storage increment incx
	 */
	/*
	 * private double dnrm2_j(int noOfParameters, double x[], int incx) { double absxi, norm, scale, ssq, fac; int ix,
	 * limit; if (noOfParameters < 1 || incx < 1) { norm = 0.0; } else if (noOfParameters == 1) { norm = Math.abs(x[0]); }
	 * else { scale = 0.0; ssq = 1.0; limit = (noOfParameters - 1) * incx; for (ix = 0; ix <= limit; ix += incx) { if
	 * (x[ix] != 0.0) { absxi = Math.abs(x[ix]); if (scale < absxi) { fac = scale / absxi; ssq = 1.0 + ssq * fac * fac;
	 * scale = absxi; } else { fac = absxi / scale; ssq += fac * fac; } } } norm = scale * Math.sqrt(ssq); } return
	 * norm; }
	 */

	/**
	 * <p>
	 * The qrfac method uses Householder transformations with column pivoting (optional) to compute a QR factorization
	 * of the noOfObservations by noOfParameters matrix A. That is, qrfac determines an orthogonal matrix Q, a
	 * permutation matrix P, and an upper trapezoidal matrix R with diagonal elements of nonincreasing magnitude, such
	 * that AP = QR.
	 * <p>
	 * Translated by Steve Verrill on November 17, 2000 from the FORTRAN MINPACK source produced by Garbow, Hillstrom,
	 * and More.
	 * <p>
	 * 
	 * @param noOfObservations
	 *            The number of rows of A.
	 * @param noOfParameters
	 *            The number of columns of A.
	 * @param a
	 *            A is an noOfObservations by noOfParameters array. On input A contains the matrix for which the QR
	 *            factorization is to be computed. On output the strict upper trapezoidal part of A contains the strict
	 *            upper trapezoidal part of R, and the lower trapezoidal part of A contains a factored form of Q.
	 * @param pivot
	 *            pivot is a logical input variable. If pivot is set true, then column pivoting is enforced. If pivot is
	 *            set false, then no column pivoting is done.
	 * @param ipvt
	 *            ipvt is an integer output array. ipvt defines the permutation matrix P such that A*P = Q*R. Column j
	 *            of P is column ipvt[j] of the identity matrix. If pivot is false, ipvt is not referenced.
	 * @param rdiag
	 *            rdiag is an output array of length noOfParameters which contains the diagonal elements of R.
	 * @param acnorm
	 *            acnorm is an output array of length noOfParameters which contains the norms of the corresponding
	 *            columns of the input matrix A.
	 * @param workingArray
	 *            workingArray is a work array of length noOfParameters.
	 */

	private void qrfac(int noOfObservations, int noOfParameters, double a[][], boolean pivot, int ipvt[],
			double rdiag[], double acnorm[], double workingArray[]) {

		int i, j, jp1, k, kmax, minmn;
		double ajnorm, sum, temp;
		double fac;

		double tempvec[] = new double[noOfObservations + 1];

		// Compute the initial column norms and initialize several arrays.

		for (j = 0; j < noOfParameters; j++) {
			for (i = 0; i < noOfObservations; i++) {
				tempvec[i] = a[i][j];
			}
			acnorm[j] = enorm(noOfObservations, tempvec);
			rdiag[j] = acnorm[j];
			workingArray[j] = rdiag[j];
			if (pivot)
				ipvt[j] = j;
		}

		// Reduce A to R with Householder transformations.
		minmn = Math.min(noOfObservations, noOfParameters);
		for (j = 0; j < minmn; j++) {
			if (pivot) {
				// Bring the column of largest norm into the pivot position.
				kmax = j;
				for (k = j; k < noOfParameters; k++) {
					if (rdiag[k] > rdiag[kmax])
						kmax = k;
				}

				if (kmax != j) {
					for (i = 0; i < noOfObservations; i++) {
						temp = a[i][j];
						a[i][j] = a[i][kmax];
						a[i][kmax] = temp;
					}
					rdiag[kmax] = rdiag[j];
					workingArray[kmax] = workingArray[j];
					k = ipvt[j];
					ipvt[j] = ipvt[kmax];
					ipvt[kmax] = k;
				}
			}

			// Compute the Householder transformation to reduce the
			// j-th column of A to a multiple of the j-th unit vector.

			for (i = j; i < noOfObservations; i++)
				tempvec[i - j] = a[i][j];
			// tempvec[i - j + 1] = a[i][j];

			// PDQCOMEBACK
			ajnorm = enorm(noOfObservations - j + 1, tempvec);

			if (ajnorm != 0.0) {
				if (a[j][j] < 0.0)
					ajnorm = -ajnorm;
				for (i = j; i < noOfObservations; i++)
					a[i][j] /= ajnorm;

				a[j][j] += 1.0;

				// Apply the transformation to the remaining columns
				// and update the norms.

				jp1 = j + 1;
				if (noOfParameters >= jp1) {
					for (k = jp1; k < noOfParameters; k++) {
						sum = 0.0;
						for (i = j; i < noOfObservations; i++)
							sum += a[i][j] * a[i][k];

						temp = sum / a[j][j];
						for (i = j; i < noOfObservations; i++)
							a[i][k] -= temp * a[i][j];

						if (pivot && rdiag[k] != 0.0) {
							temp = a[j][k] / rdiag[k];
							rdiag[k] *= Math.sqrt(Math.max(0.0, 1.0 - temp * temp));

							fac = rdiag[k] / workingArray[k];
							if (0.05 * fac * fac <= epsmch) {
								for (i = jp1; i < noOfObservations; i++)
									tempvec[i - j] = a[i][k];
								rdiag[k] = enorm(noOfObservations - j, tempvec);
								workingArray[k] = rdiag[k];
							}
						}
					}
				}
			}
			rdiag[j] = -ajnorm;
		}
		return;
	}

	/**
	 * <p>
	 * Given an noOfObservations by noOfParameters matrix A, an noOfParameters by noOfParameters diagonal matrix D, and
	 * an noOfObservations-vector b, the problem is to determine an x which solves the system
	 * 
	 * <pre>
	 *         Ax = b ,     Dx = 0 ,
	 * </pre>
	 * 
	 * in the least squares sense.
	 * <p>
	 * This method completes the solution of the problem if it is provided with the necessary information from the QR
	 * factorization, with column pivoting, of A. That is, if AP = QR, where P is a permutation matrix, Q has orthogonal
	 * columns, and R is an upper triangular matrix with diagonal elements of nonincreasing magnitude, then qrsolv
	 * expects the full upper triangle of R, the permutation matrix P, and the first noOfParameters components of (Q
	 * transpose)b. The system
	 * 
	 * <pre>
	 *                Ax = b, Dx = 0, is then equivalent to
	 *     
	 *                      t     t
	 *                Rz = Q b,  P DPz = 0 ,
	 * </pre>
	 * 
	 * where x = Pz. If this system does not have full rank, then a least squares solution is obtained. On output qrsolv
	 * also provides an upper triangular matrix S such that
	 * 
	 * <pre>
	 *                 t  t              t
	 *                P (A A + DD)P = S S .
	 * </pre>
	 * 
	 * S is computed within qrsolv and may be of separate interest.
	 * <p>
	 * Translated by Steve Verrill on November 17, 2000 from the FORTRAN MINPACK source produced by Garbow, Hillstrom,
	 * and More.
	 * <p>
	 * 
	 * @param noOfParameters
	 *            The order of r.
	 * @param r
	 *            r is an noOfParameters by noOfParameters array. On input the full upper triangle must contain the full
	 *            upper triangle of the matrix R. On output the full upper triangle is unaltered, and the strict lower
	 *            triangle contains the strict upper triangle (transposed) of the upper triangular matrix S.
	 * @param ipvt
	 *            ipvt is an integer input array of length noOfParameters which defines the permutation matrix P such
	 *            that AP = QR. Column j of P is column ipvt[j] of the identity matrix.
	 * @param diag
	 *            diag is an input array of length noOfParameters which must contain the diagonal elements of the matrix
	 *            D.
	 * @param qtb
	 *            qtb is an input array of length noOfParameters which must contain the first noOfParameters elements of
	 *            the vector (Q transpose)b.
	 * @param x
	 *            x is an output array of length noOfParameters which contains the least squares solution of the system
	 *            Ax = b, Dx = 0.
	 * @param sdiag
	 *            sdiag is an output array of length noOfParameters which contains the diagonal elements of the upper
	 *            triangular matrix S.
	 * @param workingArray
	 *            workingArray is a work array of length noOfParameters.
	 */

	private void qrsolv(int noOfParameters, double r[][], int ipvt[], double diag[], double qtb[], double x[],
			double sdiag[], double workingArray[]) {

		int i, j, jp1, k, kp1, l, nsing;
		double cos, cotan, qtbpj, sin, sum, tan, temp;

		// Copy R and (Q transpose)b to preserve input and initialize S.
		// In particular, save the diagonal elements of R in x.

		for (j = 0; j < noOfParameters; j++) {
			for (i = j; i < noOfParameters; i++)
				r[i][j] = r[j][i];

			x[j] = r[j][j];
			workingArray[j] = qtb[j];
		}

		// Eliminate the diagonal matrix D using a Givens rotation.
		for (j = 0; j < noOfParameters; j++) {
			// Prepare the row of D to be eliminated, locating the
			// diagonal element using P from the QR factorization.

			l = ipvt[j];
			if (diag[l] != 0.0) {
				for (k = j; k < noOfParameters; k++)
					sdiag[k] = 0.0;

				sdiag[j] = diag[l];

				// The transformations to eliminate the row of D
				// modify only a single element of (Q transpose)b
				// beyond the first noOfParameters, which is initially 0.0.
				// ??????

				qtbpj = 0.0;

				for (k = j; k < noOfParameters; k++) {

					// Determine a Givens rotation which eliminates the
					// appropriate element in the current row of D.

					if (sdiag[k] != 0.0) {

						if (Math.abs(r[k][k]) < Math.abs(sdiag[k])) {

							cotan = r[k][k] / sdiag[k];
							sin = 0.5 / Math.sqrt(0.25 + 0.25 * cotan * cotan);
							cos = sin * cotan;

						} else {

							tan = sdiag[k] / r[k][k];
							cos = 0.5 / Math.sqrt(0.25 + 0.25 * tan * tan);
							sin = cos * tan;

						}

						// Compute the modified diagonal element of R and
						// the modified element of ((Q transpose)b,0).

						r[k][k] = cos * r[k][k] + sin * sdiag[k];
						temp = cos * workingArray[k] + sin * qtbpj;
						qtbpj = -sin * workingArray[k] + cos * qtbpj;
						workingArray[k] = temp;

						// Accumulate the tranformation in the row of S.
						kp1 = k + 1;

						for (i = kp1; i < noOfParameters; i++) {
							temp = cos * r[i][k] + sin * sdiag[i];
							sdiag[i] = -sin * r[i][k] + cos * sdiag[i];
							r[i][k] = temp;

						}
					}
				}
			}
			// Store the diagonal element of S and restore
			// the corresponding diagonal element of R.
			sdiag[j] = r[j][j];
			r[j][j] = x[j];
		}

		// Solve the triangular system for z. if the system is
		// singular, then obtain a least squares solution.

		nsing = noOfParameters;
		for (j = 0; j < noOfParameters; j++) {
			if (sdiag[j] == 0.0 && nsing == noOfParameters)
				nsing = j - 1;
			if (nsing < noOfParameters)
				workingArray[j] = 0.0;

		}

		for (k = 0; k < nsing; k++) {
			// PDQCOMEBACK
			// j = nsing - k + 1; //PDQ CHANGED
			j = nsing - k - 1;
			sum = 0.0;
			jp1 = j + 1;
			for (i = jp1; i < nsing; i++)
				sum += r[i][j] * workingArray[i];

			workingArray[j] = (workingArray[j] - sum) / sdiag[j];
		}

		// Permute the components of z back to components of x.

		for (j = 0; j < noOfParameters; j++) {
			l = ipvt[j];
			x[l] = workingArray[j];
		}

		return;

	}

	/**
	 * <p>
	 * Given an noOfObservations by noOfParameters matrix A, an noOfParameters by noOfParameters nonsingular diagonal
	 * matrix D, an noOfObservations-vector b, and a positive number delta, the problem is to determine a value for the
	 * parameter par such that if x solves the system
	 * 
	 * <pre>
	 *                A*x = b ,     sqrt(par)*D*x = 0
	 * </pre>
	 * 
	 * in the least squares sense, and dxnorm is the Euclidean norm of D*x, then either par is 0.0 and
	 * 
	 * <pre>
	 *                (dxnorm-delta) &lt;= 0.1*delta ,
	 * </pre>
	 * 
	 * or par is positive and
	 * 
	 * <pre>
	 *                abs(dxnorm-delta) &lt;= 0.1*delta .
	 * </pre>
	 * 
	 * This method (lmpar) completes the solution of the problem if it is provided with the necessary information from
	 * the QR factorization, with column pivoting, of A. That is, if AP = QR, where P is a permutation matrix, Q has
	 * orthogonal columns, and R is an upper triangular matrix with diagonal elements of nonincreasing magnitude, then
	 * lmpar expects the full upper triangle of R, the permutation matrix P, and the first noOfParameters components of
	 * (Q transpose)b. On output lmpar also provides an upper triangular matrix S such that
	 * 
	 * <pre>
	 *                 t  t                t
	 *                P (A A + par*DD)P = S S .
	 * </pre>
	 * 
	 * S is employed within lmpar and may be of separate interest.
	 * <p>
	 * Only a few iterations are generally needed for convergence of the algorithm. If, however, the limit of 10
	 * iterations is reached, then the output par will contain the best value obtained so far.
	 * <p>
	 * Translated by Steve Verrill on November 17, 2000 from the FORTRAN MINPACK source produced by Garbow, Hillstrom,
	 * and More.
	 * <p>
	 * 
	 * @param noOfParameters
	 *            The order of r.
	 * @param r
	 *            r is an noOfParameters by noOfParameters array. On input the full upper triangle must contain the full
	 *            upper triangle of the matrix R. On output the full upper triangle is unaltered, and the strict lower
	 *            triangle contains the strict upper triangle (transposed) of the upper triangular matrix S.
	 * @param ipvt
	 *            ipvt is an integer input array of length noOfParameters which defines the permutation matrix P such
	 *            that AP = QR. Column j of P is column ipvt[j] of the identity matrix.
	 * @param diag
	 *            diag is an input array of length noOfParameters which must contain the diagonal elements of the matrix
	 *            D.
	 * @param qtb
	 *            qtb is an input array of length noOfParameters which must contain the first noOfParameters elements of
	 *            the vector (Q transpose)b.
	 * @param delta
	 *            delta is a positive input variable which specifies an upper bound on the Euclidean norm of Dx.
	 * @param par
	 *            par is a nonnegative variable. On input par contains an initial estimate of the Levenberg-Marquardt
	 *            parameter. On output par contains the final estimate.
	 * @param x
	 *            x is an output array of length noOfParameters which contains the least squares solution of the system
	 *            Ax = b, sqrt(par)*Dx = 0, for the output par.
	 * @param sdiag
	 *            sdiag is an output array of length noOfParameters which contains the diagonal elements of the upper
	 *            triangular matrix S.
	 * @param wa1
	 *            wa1 is a work array of length noOfParameters.
	 * @param wa2
	 *            wa2 is a work array of length noOfParameters.
	 */

	private void lmpar(int noOfParameters, double r[][], int ipvt[], double diag[], double qtb[], double delta,
			double par[], double x[], double sdiag[], double wa1[], double wa2[]) {

		int i, iter, j, jm1, jp1, k, l, nsing;
		double dxnorm, dwarf, fp, gnorm, parc, parl, paru, sum, temp;
		boolean loop;

		// dwarf is the smallest positive magnitude.
		dwarf = minmag;

		// Compute and store in x the Gauss-Newton direction. If the
		// Jacobian is rank-deficient, obtain a least squares solution.

		nsing = noOfParameters;
		for (j = 0; j < noOfParameters; j++) {
			wa1[j] = qtb[j];
			if (r[j][j] == 0.0 && nsing == noOfParameters)
				nsing = j - 1;
			if (nsing < noOfParameters)
				wa1[j] = 0.0;
		}

		for (k = 0; k < nsing; k++) {
			// PDQCOMEBACK
			j = nsing - k - 1;
			// j = nsing - k + 1;

			wa1[j] /= r[j][j];
			temp = wa1[j];
			jm1 = j - 1;
			if (jm1 < 0)
				continue;
			for (i = 0; i <= jm1; i++)
				wa1[i] -= r[i][j] * temp;
		}

		for (j = 0; j < noOfParameters; j++) {

			l = ipvt[j];
			x[l] = wa1[j];

		}

		// Initialize the iteration counter.
		// Evaluate the function at the origin, and test
		// for acceptance of the Gauss-Newton direction.

		iter = 0;

		for (j = 0; j < noOfParameters; j++) {

			wa2[j] = diag[j] * x[j];

		}

		dxnorm = enorm(noOfParameters, wa2);

		fp = dxnorm - delta;

		if (fp <= 0.1 * delta) {

			par[0] = 0.0;
			return;

		}

		// If the Jacobian is not rank deficient, the Newton
		// step provides a lower bound, parl, for the 0.0 of
		// the function. Otherwise set this bound to 0.0.

		parl = 0.0;

		if (nsing >= noOfParameters) {

			for (j = 0; j < noOfParameters; j++) {

				l = ipvt[j];
				wa1[j] = diag[l] * (wa2[l] / dxnorm);

			}

			for (j = 0; j < noOfParameters; j++) {

				sum = 0.0;
				jm1 = j - 1;

				if (jm1 < 0)
					continue;

				for (i = 0; i < jm1; i++) {

					sum += r[i][j] * wa1[i];

				}

				// }

				wa1[j] = (wa1[j] - sum) / r[j][j];

			}

			temp = enorm(noOfParameters, wa1);
			parl = ((fp / delta) / temp) / temp;

		}

		// Calculate an upper bound, paru, for the 0.0 of the function.

		for (j = 0; j < noOfParameters; j++) {

			sum = 0.0;

			for (i = 0; i < j; i++) {

				sum += r[i][j] * qtb[i];

			}

			l = ipvt[j];
			wa1[j] = sum / diag[l];

		}

		gnorm = enorm(noOfParameters, wa1);
		paru = gnorm / delta;

		if (paru == 0.0)
			paru = dwarf / Math.min(delta, 0.1);

		// If the input par lies outside of the interval (parl,paru),
		// set par to the closer endpoint.

		par[0] = Math.max(par[0], parl);
		par[0] = Math.min(par[0], paru);

		if (par[0] == 0.0)
			par[0] = gnorm / dxnorm;

		// Beginning of an iteration.

		loop = true;

		while (loop) {

			iter++;

			// Evaluate the function at the current value of par.

			if (par[0] == 0.0)
				par[0] = Math.max(dwarf, 0.001 * paru);
			temp = Math.sqrt(par[0]);

			for (j = 0; j < noOfParameters; j++) {

				wa1[j] = temp * diag[j];

			}

			qrsolv(noOfParameters, r, ipvt, wa1, qtb, x, sdiag, wa2);

			for (j = 0; j < noOfParameters; j++) {

				wa2[j] = diag[j] * x[j];

			}

			dxnorm = enorm(noOfParameters, wa2);
			temp = fp;
			fp = dxnorm - delta;

			// If the function is small enough, accept the current value
			// of par. Also test for the exceptional cases where parl
			// is 0.0 or the number of iterations has reached 10.

			if (Math.abs(fp) <= 0.1 * delta || parl == 0.0 && fp <= temp && temp < 0.0 || iter == 10) {

				// Termination

				if (iter == 0)
					par[0] = 0.0;
				return;

			}

			// Compute the Newton correction.

			for (j = 0; j < noOfParameters; j++) {

				l = ipvt[j];
				wa1[j] = diag[l] * (wa2[l] / dxnorm);

			}

			for (j = 0; j < noOfParameters; j++) {

				wa1[j] /= sdiag[j];
				temp = wa1[j];
				jp1 = j + 1;

				for (i = jp1; i < noOfParameters; i++) {

					wa1[i] -= r[i][j] * temp;

				}

			}

			temp = enorm(noOfParameters, wa1);
			parc = ((fp / delta) / temp) / temp;

			// Depending on the sign of the function, update parl or paru.

			if (fp > 0.0)
				parl = Math.max(parl, par[0]);
			if (fp < 0.0)
				paru = Math.min(paru, par[0]);

			// Compute an improved estimate for par[0].

			par[0] = Math.max(parl, par[0] + parc);

			// End of an iteration.

		}

	}

	/**
	 * <p>
	 * The lmdif1 method minimizes the sum of the squares of noOfObservations nonlinear functions in noOfParameters
	 * variables by a modification of the Levenberg-Marquardt algorithm. This is done by using the more general
	 * least-squares solver lmdif. The user must provide a method that calculates the functions. The Jacobian is then
	 * calculated by a forward-difference approximation.
	 * <p>
	 * Translated by Steve Verrill on November 24, 2000 from the FORTRAN MINPACK source produced by Garbow, Hillstrom,
	 * and More.
	 * <p>
	 * 
	 * @param noOfObservations
	 *            A positive integer set to the number of functions [number of observations]
	 * @param noOfParameters
	 *            A positive integer set to the number of variables [number of parameters]. noOfParameters must not
	 *            exceed noOfObservations.
	 * @param x
	 *            On input, it contains the initial estimate of the solution vector [the least squares parameters]. On
	 *            output it contains the final estimate of the solution vector.
	 * @param lowerLimit
	 * @param upperLimit
	 * @param functionAtDataPoints
	 *            An output vector that contains the noOfObservations functions [residuals] evaluated at x.
	 * @param tol
	 *            tol is a nonnegative input variable. Termination occurs when the algorithm estimates either that the
	 *            relative error in the sum of squares is at most tol or that the relative error between x and the
	 *            solution is at most tol.
	 */
	/*
	 * private void lmdif(int noOfObservations, int noOfParameters, double x[],double[] lowerLimit,double[] upperLimit,
	 * double functionAtDataPoints[], double tol) { int maxNoOfFunctionEvaluations, mode, nprint; double epsfcn, factor,
	 * fTolerance, gTolerance, xTolerance; double diag[] = new double[noOfParameters]; int nfev[] = new int[2]; double
	 * fjac[][] = new double[noOfObservations][noOfParameters]; int ipvt[] = new int[noOfParameters]; double qtf[] = new
	 * double[noOfParameters]; factor = 10.0; info[0] = 0; // Check the input parameters for errors. if (noOfParameters <=
	 * 0 || noOfObservations < noOfParameters || tol < 0.0) { return; } maxNoOfFunctionEvaluations = 500 *
	 * (noOfParameters + 1); fTolerance = tol; xTolerance = tol; gTolerance = 0.0; epsfcn = 0.0; mode = 1; nprint = 0;
	 * lmdif(noOfObservations, noOfParameters, x, lowerLimit,upperLimit,functionAtDataPoints, fTolerance, xTolerance,
	 * gTolerance, maxNoOfFunctionEvaluations, epsfcn, diag, mode, factor, nprint, nfev, fjac, ipvt, qtf); if (info[0] ==
	 * 8) info[0] = 4; return; }
	 */

	/**
	 * <p>
	 * The lmdif method minimizes the sum of the squares of noOfObservations nonlinear functions in noOfParameters
	 * variables by a modification of the Levenberg-Marquardt algorithm. The user must provide a method that calculates
	 * the functions. The Jacobian is then calculated by a forward-difference approximation.
	 * <p>
	 * Translated by Steve Verrill on November 20, 2000 from the FORTRAN MINPACK source produced by Garbow, Hillstrom,
	 * and More.
	 * <p>
	 * Modified by paul quinn to allow bound constraints
	 * <p>
	 * 
	 * @param noOfObservations
	 *            A positive integer set to the number of functions [number of observations]
	 * @param noOfParameters
	 *            A positive integer set to the number of variables [number of parameters]. noOfParameters must not
	 *            exceed noOfObservations.
	 * @param x
	 *            On input, it contains the initial estimate of the solution vector [the least squares parameters]. On
	 *            output it contains the final estimate of the solution vector.
	 * @param lowerLimit
	 * @param upperLimit
	 * @param functionAtDataPoints
	 *            An output vector that contains the noOfObservations functions [residuals] evaluated at x.
	 * @param fTolerance
	 *            A nonnegative input variable. Termination occurs when both the actual and predicted relative
	 *            reductions in the sum of squares are at most fTolerance. Therefore, fTolerance measures the relative
	 *            error desired in the sum of squares.
	 * @param xTolerance
	 *            A nonnegative input variable. Termination occurs when the relative error between two consecutive
	 *            iterates is at most xTolerance. Therefore, xTolerance measures the relative error desired in the
	 *            approximate solution.
	 * @param gTolerance
	 *            A nonnegative input variable. Termination occurs when the cosine of the angle between
	 *            functionAtDataPoints and any column of the Jacobian is at most gTolerance in absolute value.
	 *            Therefore, gTolerance measures the orthogonality desired between the function vector and the columns
	 *            of the Jacobian.
	 * @param maxNoOfFunctionEvaluations
	 *            A positive integer input variable. Termination occurs when the number of calls to fcn is at least
	 *            maxNoOfFunctionEvaluations by the end of an iteration.
	 * @param epsfcn
	 *            An input variable used in determining a suitable step length for the forward-difference approximation.
	 *            This approximation assumes that the relative errors in the functions are of the order of epsfcn. If
	 *            epsfcn is less than the machine precision, it is assumed that the relative errors in the functions are
	 *            of the order of the machine precision.
	 * @param diag
	 *            An vector of length noOfParameters. If mode = 1 (see below), diag is internally set. If mode = 2, diag
	 *            must contain positive entries that serve as multiplicative scale factors for the variables.
	 * @param mode
	 *            If mode = 1, the variables will be scaled internally. If mode = 2, the scaling is specified by the
	 *            input diag. Other values of mode are equivalent to mode = 1.
	 * @param factor
	 *            A positive input variable used in determining the initial step bound. This bound is set to the product
	 *            of factor and the euclidean norm of diag*x if non0.0, or else to factor itself. In most cases factor
	 *            should lie in the interval (.1,100). 100 is a generally recommended value.
	 * @param nprint
	 *            An integer input variable that enables controlled printing of iterates if it is positive. In this
	 *            case, fcn is called with iflag[0] = 0 at the beginning of the first iteration and every nprint
	 *            iterations thereafter and immediately prior to return, with x and functionAtDataPoints available for
	 *            printing. If nprint is not positive, no special calls of fcn with iflag[0] = 0 are made.
	 * @param nfev
	 *            An integer output variable set to the number of calls to fcn.
	 * @param fjac
	 *            An output noOfObservations by noOfParameters array. The upper noOfParameters by noOfParameters
	 *            submatrix of fjac contains an upper triangular matrix R with diagonal elements of nonincreasing
	 *            magnitude such that t t t P (jac *jac)P = R R, where P is a permutation matrix and jac is the final
	 *            calculated Jacobian. Column j of P is column ipvt[j] (see below) of the identity matrix. The lower
	 *            trapezoidal part of fjac contains information generated during the computation of R.
	 * @param ipvt
	 *            An integer output array of length noOfParameters. ipvt defines a permutation matrix P such that jac*P =
	 *            QR, where jac is the final calculated Jacobian, Q is orthogonal (not stored), and R is upper
	 *            triangular with diagonal elements of nonincreasing magnitude. column j of P is column ipvt[j] of the
	 *            identity matrix.
	 * @param qtf
	 *            An output array of length noOfParameters which contains the first noOfParameters elements of the
	 *            vector (Q transpose)functionAtDataPoints.
	 */

	private void lmdif(int noOfObservations, int noOfParameters, double[] x, double[] lowerLimit, double[] upperLimit,
			double functionAtDataPoints[], double fTolerance, double xTolerance, double gTolerance,
			int maxNoOfFunctionEvaluations, double epsfcn, double[] diag, int mode, double factor, int nprint,
			int nfev[], double[][] fjac, int[] ipvt, double[] qtf) {

		int i, iter, j, l;

		// double actred,delta,dirder,fnorm,fnorm1,gnorm,
		// 1.0,pnorm,prered,0.1,0.5,0.25,0.75,p0001,ratio,
		// sum,temp,temp1,temp2,xnorm,0.0;
		double actred, delta, dirder, fnorm, fnorm1, gnorm, pnorm, prered, ratio, sum, temp, temp1, temp2, xnorm;

		double[] par = new double[2];

		boolean doneout, donein;

		int[] iflag = new int[2];
		double[] wa1 = new double[noOfParameters];
		double[] wa2 = new double[noOfParameters];
		double[] wa3 = new double[noOfParameters];
		double[] wa4 = new double[noOfObservations];

		delta = 0.0;
		xnorm = 0.0;

		info[0] = 0;
		iflag[0] = 0;
		nfev[0] = 0;
		// Check the input parameters for errors.

		if (noOfParameters <= 0 || noOfObservations < noOfParameters || fTolerance < 0.0 || xTolerance < 0.0
				|| gTolerance < 0.0 || maxNoOfFunctionEvaluations <= 0 || factor <= 0.0) {

			// Termination

			if (nprint > 0) {

				// nlls.fcn(noOfObservations, noOfParameters, x,
				// functionAtDataPoints, iflag);
				functionAtDataPoints = function.LMEvaluate(x);

			}
			return;

		}

		if (mode == 2) {

			for (j = 0; j < noOfParameters; j++) {

				if (diag[j] <= 0.0) {

					// Termination

					if (nprint > 0) {

						// nlls.fcn(noOfObservations, noOfParameters, x,
						// functionAtDataPoints, iflag);
						functionAtDataPoints = function.LMEvaluate(x);
					}
					System.out.println("here 2");
					return;

				}

			}

		}

		// Evaluate the function at the starting point
		// and calculate its norm.

		iflag[0] = 1;

		// nlls.fcn(noOfObservations, noOfParameters, x, functionAtDataPoints,
		// iflag);
		functionAtDataPoints = function.LMEvaluate(x);
		nfev[0] = 1;

		if (iflag[0] < 0) {

			// Termination

			info[0] = iflag[0];
			iflag[0] = 0;

			if (nprint > 0) {

				// nlls.fcn(noOfObservations, noOfParameters, x,
				// functionAtDataPoints, iflag);
				functionAtDataPoints = function.LMEvaluate(x);
			}
			return;

		}

		fnorm = enorm(noOfObservations, functionAtDataPoints);
		// Initialize Levenberg-Marquardt parameter and iteration counter.

		par[0] = 0.0;
		iter = 1;

		// Beginning of the outer loop.

		doneout = false;

		while (!doneout) {

			// Calculate the Jacobian matrix.

			iflag[0] = 2;
			fdjac2(noOfObservations, noOfParameters, x, functionAtDataPoints, fjac, iflag, epsfcn, wa4);

			nfev[0] += noOfParameters;

			if (iflag[0] < 0) {

				// Termination

				info[0] = iflag[0];
				iflag[0] = 0;

				if (nprint > 0) {

					// nlls.fcn(noOfObservations, noOfParameters, x,
					// functionAtDataPoints, iflag);
					functionAtDataPoints = function.LMEvaluate(x);

				}
				return;

			}

			// If requested, call fcn to enable printing of iterates.

			if (nprint > 0) {

				iflag[0] = 0;

				if ((iter - 1) % nprint == 0) {

					// nlls.fcn(noOfObservations, noOfParameters, x,
					// functionAtDataPoints, iflag);
					functionAtDataPoints = function.LMEvaluate(x);
				}

				if (iflag[0] < 0) {

					// Termination

					info[0] = iflag[0];
					iflag[0] = 0;

					// nlls.fcn(noOfObservations, noOfParameters, x,
					// functionAtDataPoints, iflag);
					functionAtDataPoints = function.LMEvaluate(x);
					return;

				}

			}
			// PDQADDED
			// Determine if any of the parameters are pegged at the limits
			int[] whLowerPeg = new int[noOfParameters];
			int[] whUpperPeg = new int[noOfParameters];
			for (i = 0; i < noOfParameters; i++) {
				if (x[i] == lowerLimit[i])
					whLowerPeg[i] = i;
				else
					whLowerPeg[i] = 0;

				if (x[i] == upperLimit[i])
					whUpperPeg[i] = i;
				else
					whUpperPeg[i] = 0;

			}
			for (i = 0; i < noOfParameters; i++) {
				double pdqsum = 0.0;
				if (whLowerPeg[i] != 0) {
					for (j = 0; j < noOfObservations; j++) {
						pdqsum += functionAtDataPoints[j] * fjac[j][i];
					}
					if (pdqsum > 0.0) {
						for (j = 0; j < noOfObservations; j++) {
							fjac[j][i] = 0.0;
						}
					}
				}
				if (whUpperPeg[i] != 0) {
					for (j = 0; j < noOfObservations; j++) {
						pdqsum += functionAtDataPoints[j] * fjac[j][i];
					}
					if (pdqsum < 0.0) {
						for (j = 0; j < noOfObservations; j++) {
							fjac[j][i] = 0.0;
						}
					}
				}

			}

			// Compute the qr factorization of the Jacobian.

			qrfac(noOfObservations, noOfParameters, fjac, true, ipvt, wa1, wa2, wa3);

			// On the first iteration and if mode is 1, scale according
			// to the norms of the columns of the initial Jacobian.

			if (iter == 1) {

				if (mode != 2) {

					for (j = 0; j < noOfParameters; j++) {

						diag[j] = wa2[j];

						if (wa2[j] == 0.0)
							diag[j] = 1.0;

					}

				}

				// On the first iteration, calculate the norm of the scaled x
				// and initialize the step bound delta.

				for (j = 0; j < noOfParameters; j++) {

					wa3[j] = diag[j] * x[j];

				}

				xnorm = enorm(noOfParameters, wa3);

				delta = factor * xnorm;

				if (delta == 0.0)
					delta = factor;

			}

			// Form (q transpose)*functionAtDataPoints and store the first
			// noOfParameters components in
			// qtf.

			for (i = 0; i < noOfObservations; i++)
				wa4[i] = functionAtDataPoints[i];

			for (j = 0; j < noOfParameters; j++) {

				if (fjac[j][j] != 0.0) {

					sum = 0.0;

					for (i = j; i < noOfObservations; i++)
						sum += fjac[i][j] * wa4[i];

					temp = -sum / fjac[j][j];

					for (i = j; i < noOfObservations; i++)
						wa4[i] += fjac[i][j] * temp;

				}

				fjac[j][j] = wa1[j];
				qtf[j] = wa4[j];

			}

			// Compute the norm of the scaled gradient.

			gnorm = 0.0;
			if (fnorm != 0.0) {
				for (j = 0; j < noOfParameters; j++) {
					l = ipvt[j];

					if (wa2[l] != 0.0) {
						sum = 0.0;

						for (i = 0; i <= j; i++)
							sum += fjac[i][j] * (qtf[i] / fnorm);

						gnorm = Math.max(gnorm, Math.abs(sum / wa2[l]));

					}

				}

			}

			// Test for convergence of the gradient norm.

			if (gnorm <= gTolerance)
				info[0] = 4;

			if (info[0] != 0) {

				// Termination

				if (iflag[0] < 0)
					info[0] = iflag[0];
				iflag[0] = 0;

				if (nprint > 0) {

					// nlls.fcn(noOfObservations, noOfParameters, x,
					// functionAtDataPoints, iflag);
					functionAtDataPoints = function.LMEvaluate(x);
				}
				return;

			}

			// Rescale if necessary.

			if (mode != 2) {

				for (j = 0; j < noOfParameters; j++) {

					diag[j] = Math.max(diag[j], wa2[j]);

				}

			}

			// Beginning of the inner loop.

			donein = false;

			while (!donein) {

				// Determine the Levenberg-Marquardt parameter.

				lmpar(noOfParameters, fjac, ipvt, diag, qtf, delta, par, wa1, wa2, wa3, wa4);

				// Store the direction p and x + p. Calculate the norm of p.

				for (j = 0; j < noOfParameters; j++) {
					wa1[j] = -wa1[j];
					// wa2[j] = x[j] + wa1[j];
					// wa3[j] = diag[j] * wa1[j];
				}

				// Respect the limits. If a step were to go out of bounds,
				// then
				// we should take a step in the same direction but shorter
				// distance.
				// The step should take us right to the limit in that case.
				double alpha = 1.0;
				double maxWA1 = wa1[0];
				double minWA1 = wa1[0];
				for (i = 0; i < noOfParameters; i++) {
					maxWA1 = Math.max(wa1[i], maxWA1);
					minWA1 = Math.min(wa1[i], minWA1);
				}
				for (i = 0; i < noOfParameters; i++) {
					if (whLowerPeg[i] != 0) {
						wa1[i] = Math.min(wa1[i], maxWA1);
						wa1[i] = Math.max(0.0, wa1[i]);
					}
				}
				for (i = 0; i < noOfParameters; i++) {
					if (whUpperPeg[i] != 0) {
						wa1[i] = Math.max(wa1[i], minWA1);
						wa1[i] = Math.min(0.0, wa1[i]);
					}
				}
				double[] dwa1 = new double[noOfParameters];
				for (i = 0; i < noOfParameters; i++) {
					if (wa1[i] > epsmch)
						dwa1[i] = wa1[i];
					else
						dwa1[i] = 0.0;
				}

				for (i = 0; i < noOfParameters; i++) {
					if (dwa1[i] != 0.0 && ((x[i] + wa1[i]) < lowerLimit[i])) {
						double t = (lowerLimit[i] - x[i]) / wa1[i];
						alpha = Math.min(alpha, t);
					}
					if (dwa1[i] != 0.0 && ((x[i] + wa1[i]) > upperLimit[i])) {
						double t = (lowerLimit[i] - x[i]) / wa1[i];
						alpha = Math.min(alpha, t);
					}

				}

				// Scale the resulting vector
				for (j = 0; j < noOfParameters; j++) {
					wa1[j] = wa1[j] * alpha;
					wa2[j] = wa1[j] + x[j];
				}

				// Adjust the final output values. If the step put us
				// exactly
				// on a boundary, make sure it is exact.
				for (i = 0; i < noOfParameters; i++) {
					if (wa2[i] >= upperLimit[i] * (1 - epsmch)) {
						wa2[i] = upperLimit[i];
					}
					if (wa2[i] <= lowerLimit[i] * (1 + epsmch)) {
						wa2[i] = lowerLimit[i];
					}

				}

				for (j = 0; j < noOfParameters; j++) {
					wa3[j] = diag[j] * wa1[j];
				}

				pnorm = enorm(noOfParameters, wa3);

				// On the first iteration, adjust the initial step bound.

				if (iter == 1)
					delta = Math.min(delta, pnorm);

				// Evaluate the function at x + p and calculate its norm.

				iflag[0] = 1;

				// nlls.fcn(noOfObservations, noOfParameters, wa2, wa4, iflag);
				wa4 = function.LMEvaluate(wa2);
				nfev[0]++;

				if (iflag[0] < 0) {

					// Termination

					info[0] = iflag[0];
					iflag[0] = 0;

					if (nprint > 0) {

						// nlls.fcn(noOfObservations, noOfParameters, x,
						// functionAtDataPoints, iflag);
						functionAtDataPoints = function.LMEvaluate(x);
					}
					return;

				}

				fnorm1 = enorm(noOfObservations, wa4);

				// Compute the scaled actual reduction.

				actred = -1.0;

				if (0.1 * fnorm1 < fnorm)
					actred = 1.0 - (fnorm1 / fnorm) * (fnorm1 / fnorm);

				// Compute the scaled predicted reduction and
				// the scaled directional derivative.

				for (j = 0; j < noOfParameters; j++) {

					wa3[j] = 0.0;
					l = ipvt[j];
					temp = wa1[l];

					for (i = 0; i <= j; i++)
						wa3[i] += fjac[i][j] * temp;

				}

				temp1 = enorm(noOfParameters, wa3) / fnorm;
				temp2 = (Math.sqrt(par[0]) * pnorm) / fnorm;

				prered = temp1 * temp1 + temp2 * temp2 / 0.5;
				dirder = -(temp1 * temp1 + temp2 * temp2);

				// Compute the ratio of the actual to the predicted
				// reduction.

				ratio = 0.0;
				if (prered != 0.0)
					ratio = actred / prered;

				// Update the step bound.

				if (ratio <= 0.25) {

					if (actred >= 0.0) {

						temp = 0.5;

					} else {

						temp = 0.5 * dirder / (dirder + 0.5 * actred);

					}

					if (0.1 * fnorm1 >= fnorm || temp < 0.1)
						temp = 0.1;

					delta = temp * Math.min(delta, pnorm / 0.1);

					par[0] /= temp;

				} else {

					if (par[0] == 0.0 || ratio >= 0.75) {

						delta = pnorm / 0.5;
						par[0] *= 0.5;

					}

				}

				// Test for successful iteration.

				if (ratio >= 0.0001) {

					// Successful iteration. Update x, functionAtDataPoints,
					// and
					// their norms.

					for (j = 0; j < noOfParameters; j++) {

						x[j] = wa2[j];
						wa2[j] = diag[j] * x[j];

					}

					for (i = 0; i < noOfObservations; i++)
						functionAtDataPoints[i] = wa4[i];

					xnorm = enorm(noOfParameters, wa2);

					fnorm = fnorm1;

					iter++;

				}

				// Tests for convergence.

				if (Math.abs(actred) <= fTolerance && prered <= fTolerance && 0.5 * ratio <= 1.0)
					info[0] = 1;

				if (delta <= xTolerance * xnorm)
					info[0] = 2;

				if (Math.abs(actred) <= fTolerance && prered <= fTolerance && 0.5 * ratio <= 1.0 && info[0] == 2)
					info[0] = 3;

				if (info[0] != 0) {

					// Termination

					if (iflag[0] < 0)
						info[0] = iflag[0];
					iflag[0] = 0;

					if (nprint > 0) {

						// nlls.fcn(noOfObservations, noOfParameters, x,
						// functionAtDataPoints, iflag);
						functionAtDataPoints = function.LMEvaluate(x);

					}
					return;

				}

				// Tests for termination and stringent tolerances.

				if (nfev[0] >= maxNoOfFunctionEvaluations)
					info[0] = 5;

				if (Math.abs(actred) <= epsmch && prered <= epsmch && 0.5 * ratio <= 1.0)
					info[0] = 6;

				if (delta <= epsmch * xnorm)
					info[0] = 7;

				if (gnorm <= epsmch)
					info[0] = 8;

				if (info[0] != 0) {

					// Termination

					if (iflag[0] < 0)
						info[0] = iflag[0];
					iflag[0] = 0;

					if (nprint > 0) {

						// nlls.fcn(noOfObservations, noOfParameters, x,
						// functionAtDataPoints, iflag);
						functionAtDataPoints = function.LMEvaluate(x);
					}
					return;

				}

				// End of the inner loop. Repeat if iteration unsuccessful.

				if (ratio >= 0.0001)
					donein = true;

			}

			// End of the outer loop.

		}

	}

	/**
	 * The fdjac2 method computes a forward-difference approximation to the noOfObservations by noOfParameters Jacobian
	 * matrix associated with a specified problem of noOfObservations functions in noOfParameters variables. Translated
	 * by Steve Verrill on November 24, 2000 from the FORTRAN MINPACK source produced by Garbow, Hillstrom, and More.
	 * PDQ ADDED CENTRED DIFFERENCE DIFFERENTIATION...seems faster and more reliable with it...
	 * 
	 * @param noOfObservations
	 *            A positive integer set to the number of functions [number of observations]
	 * @param noOfParameters
	 *            A positive integer set to the number of variables [number of parameters]. noOfParameters must not
	 *            exceed noOfObservations.
	 * @param x
	 *            An input array.
	 * @param functionAtDataPoints
	 *            An input array that contains the functions evaluated at x.
	 * @param fjac
	 *            An output noOfObservations by noOfParameters array that contains the approximation to the Jacobian
	 *            matrix evaluated at x.
	 * @param iflag
	 *            An integer variable that can be used to terminate the execution of fdjac2. See the description of
	 *            nlls.
	 * @param epsfcn
	 *            An input variable used in determining a suitable step length for the forward-difference approximation.
	 *            This approximation assumes that the relative errors in the functions are of the order of epsfcn. If
	 *            epsfcn is less than the machine precision, it is assumed that the relative errors in the functions are
	 *            of the order of the machine precision.
	 * @param workingArray
	 *            A work array.
	 */
	//TODO although not curently used, this suppressed warning should be looked into if the code is ever used
	private void fdjac2(int noOfObservations, int noOfParameters, double x[], @SuppressWarnings("unused") double functionAtDataPoints[],
			double fjac[][], int iflag[], double epsfcn, double workingArray[]) {

		int i, j;
		double eps, h, temp;
		double[] temp1 = new double[noOfObservations];
		// Loop over the parameters
		eps = Math.sqrt(Math.max(epsfcn, epsmch));
		for (j = 0; j < noOfParameters; j++) {
			temp = x[j];
			// Determine a reasonable step size for differentiation.....
			h = eps * Math.abs(temp);
			if (h == 0.0)
				h = eps;
			//
			x[j] = temp + h;
			// nlls.fcn(noOfObservations, noOfParameters, x, workingArray,
			// iflag);
			workingArray = function.LMEvaluate(x);
			if (iflag[0] < 0) {
				return;
			}

			x[j] = temp;

			temp = x[j];
			// Determine a reasonable step size for differentiation.....
			h = eps * Math.abs(temp);
			if (h == 0.0)
				h = eps;
			//
			x[j] = temp - h;
			// nlls.fcn(noOfObservations, noOfParameters, x, temp1, iflag);
			temp1 = function.LMEvaluate(x);
			if (iflag[0] < 0) {
				return;
			}

			x[j] = temp;

			// Forward difference differentiation
			for (i = 0; i < noOfObservations; i++) {
				fjac[i][j] = (workingArray[i] - temp1[i]) / (2.0 * h);
			}
		}
	}

	/**
	 * @param maxNoOfFunctionEvaluations
	 */
	public void setMaxNoOfEvaluations(int maxNoOfFunctionEvaluations) {
		this.maxNoOfFunctionEvaluations = maxNoOfFunctionEvaluations;
	}

	/**
	 * @return Max no of function evaluations allowed
	 */
	public int getMaxNoOfEvaluations() {
		return this.maxNoOfFunctionEvaluations;
	}

	/**
	 * @param xTolerance
	 */
	public void setxTolerance(double xTolerance) {
		this.xTolerance = xTolerance;
	}

	/**
	 * @return Termination occurs when the relative error between two consecutive iterates is at most xTolerance.
	 */
	public double getxTolerance() {
		return this.xTolerance;
	}

	/**
	 * Termination occurs when both the actual and predicted relative reductions in the sum of squares are at most
	 * fTolerance. Therefore, fTolerance measures the relative error desired in the sum of squares.
	 * 
	 * @param fTolerance
	 */
	public void setfTolerance(double fTolerance) {
		this.gTolerance = fTolerance;
	}

	/**
	 * @return Termination occurs when both the actual and predicted relative reductions in the sum of squares are at
	 *         most fTolerance. Therefore, fTolerance measures the relative error desired in the sum of squares.
	 */
	public double getfTolerance() {
		return this.fTolerance;
	}

	/**
	 * @param gTolerance
	 *            Termination occurs when the cosine of the angle between functionAtDataPoints and any column of the
	 *            Jacobian is at most gTolerance in absolute value. Therefore, gTolerance measures the orthogonality
	 *            desired between the function vector and the columns of the Jacobian.
	 */
	public void setgTolerance(double gTolerance) {
		this.gTolerance = gTolerance;
	}

	/**
	 * Termination occurs when the cosine of the angle between functionAtDataPoints and any column of the Jacobian is at
	 * most gTolerance in absolute value. Therefore, gTolerance measures the orthogonality desired between the function
	 * vector and the columns of the Jacobian.
	 * 
	 * @return gTolerance
	 */
	public double getgTolerance() {
		return this.gTolerance;
	}

	/**
	 * @param factor
	 *            A positive input variable used in determining the initial step bound. This bound is set to the product
	 *            of factor and the euclidean norm of diag*x if non 0.0, or else to factor itself. In most cases factor
	 *            should lie in the interval (.1,100). 100 is a generally recommended value.
	 */
	public void setFactor(double factor) {
		this.factor = factor;
	}

	/**
	 * A positive input variable used in determining the initial step bound. This bound is set to the product of factor
	 * and the euclidean norm of diag*x if non 0.0, or else to factor itself. In most cases factor should lie in the
	 * interval (.1,100). 100 is a generally recommended value.
	 * 
	 * @return factor
	 */
	public double getFactor() {
		return this.getFactor();
	}

	/**
	 * @param scaling
	 */
	public void setScaling(double[] scaling) {
		if (scaling.length != this.noOfParameters) {
			throw new IllegalArgumentException("Scaling array is not the "
					+ "correct length : Must be the same as the number of parameters");
		}
		this.mode = 2;
	}

	/**
	 * 
	 * 
	 */
	public void unsetScaling() {
		this.mode = 1;
	}

	@Override
	public void optimize() {

		double diag[] = new double[noOfParameters];
		int nfev[] = new int[2];
		double fjac[][] = new double[noOfObservations][noOfParameters];
		int ipvt[] = new int[noOfParameters];
		double qtf[] = new double[noOfParameters];

		lmdif(noOfObservations, noOfParameters, parameters, lowerBounds, upperBounds, functionAtDataPoints, fTolerance,
				xTolerance, gTolerance, maxNoOfFunctionEvaluations, epsfcn, diag, mode, factor, nprint, nfev, fjac,
				ipvt, qtf);

	}

	/**
	 * Starts the optimization process.... Creates a new thread etc........
	 */
	@Override
	public void start() {
		if (action == null) // if thread is not running
		{
			System.out.println("LM Optimizer started");
			action = uk.ac.gda.util.ThreadManager.getThread(this); // Instantiate the new thread
			action.start(); // Start it
		} else {
			System.out.println("A Levenberg Marquardt Optimization is already running");
			System.out.println("use the stop() method to terminate the current process");
		}
	}

	/**
	 * Stop the thread....
	 */
	@Override
	public void stop() {
		info[0] = 9;
	}

	/**
	 * Thread
	 */
	@Override
	public void run() {
		optimize();
		action = null;
	}

	/**
	 * @return the best parameters
	 */
	@Override
	public double[] getBest() {
		return parameters;
	}

	/**
	 * @return the current minimum
	 */
	@Override
	public double getMinimum() {
		return function.evaluate(parameters);
	}

	/**
	 * @return info An integer output variable. info is set as follows.
	 *         <P>
	 *         info = 0 improper input parameters
	 *         <P>
	 *         <P>
	 *         info = 1 algorithm estimates that the relative error in the sum of squares is at most tol.
	 *         <P>
	 *         <P>
	 *         info = 2 algorithm estimates that the relative error between x and the solution is at most tol.
	 *         <P>
	 *         <P>
	 *         info = 3 conditions for info = 1 and info = 2 both hold
	 *         <P>
	 *         <P>
	 *         info = 4 functionAtDataPoints is orthogonal to the columns of the Jacobian to machine precision.
	 *         <P>
	 *         <P>
	 *         info = 5 number of calls to fcn has reached or exceeded 200*(noOfParameters+1)
	 *         <P>
	 *         <P>
	 *         info = 6 tol is too small. No further reduction in the sum of squares is possible.
	 *         <P>
	 *         <P>
	 *         info = 7 tol is too small. No further improvement in the approximate solution x is possible.
	 *         <P>
	 *         <P>
	 *         info = 9 Thread stopped
	 */
	public int getInfo() {
		return info[0];
	}

	/**
	 * @return No of parameters...just a consistency check
	 */
	public int getNoOfParameters() {
		return noOfParameters;
	}

	/**
	 * @return No of observations or data points...just a consistency check
	 */
	public int getNoOfObservations() {
		return noOfObservations;
	}

	@Override
	public boolean isRunning() {
		return (action != null);
	}

}
