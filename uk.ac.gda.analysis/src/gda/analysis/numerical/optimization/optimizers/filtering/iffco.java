// unknown copyright

package gda.analysis.numerical.optimization.optimizers.filtering;

import gda.analysis.numerical.linefunction.IParameter;
import gda.analysis.numerical.optimization.objectivefunction.AbstractObjectiveFunction;
import gda.analysis.numerical.optimization.optimizers.IOptimizer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import Jama.Matrix;
import Jama.SingularValueDecomposition;

/**
 * ----------------------------------------------------------------- // IFFCO - Implicit Filtering For Constrained
 * Optimization. // // Code updated 5-18-01 by Alton Patrick. // MPI version added 12-18-00 by Alton Patrick. // Revised
 * 11-20-00 for the IBM SP/2 architecture by Alton Patrick. // Code updated 5-18-99 by Owen Eslinger and Alton Patrick //
 * (ojesling@unity.ncsu.edu and hapatric@unity.ncsu.edu). // Rewritten by Tony Choi (tdchoi@unity.math.ncsu.edu). //
 * Original version by Paul Gilmore (paul@latour.math.ncsu.edu). // North Carolina State University // Dept. of
 * Mathematics // // // IFFCO is a projected quasi-Newton algorithm for constrained // nonlinear minimization. IFFCO is
 * designed to solve problems of the // form: // // min f: Q --> R, // // where f is the function to be minimized // and
 * Q is an n-dimensional hyper-box given by the the following // equation: // // Q={ x : l[i] <= x[i] <= u[i], i =
 * 1,...,n }. // // A brief outline of the algorithm for IFFCO follows. // // The main input variables used by IFFCO
 * are: // 1) An initial point in the hyper-box Q. // 2) An initial finite difference step (scale) used to calculate //
 * the gradient. // 3) A lower bound for the scales used to calculate the gradient. // // IFFCO calculates the function
 * and the gradient of the // function at the given point. It then tests for convergence // at the current point with
 * the current scale. If the convergence criteria // is met, the scale is reduced by a half and the convergence criteria
 * is // again checked. This process continues until either the convergence // criteria is not satisfied or until the
 * scale is reduced to below the // lower bound for scales. // // If the scale has been reduced below the lower bound,
 * the algorithm // terminates. Otherwise, IFFCO calculates a descent direction // and then attempts to find an
 * acceptable new iterate in that direction // using a cutback line search algorithm. // // If a new point cannot be
 * found, the scale is reduced by half and the // process continues. If a new point is found, IFFCO continues the
 * process // of checking for termination and looking for a new acceptable point at // the current point, scale pair. // //
 * IFFCO is designed to solve problems where the function f has the form // // f(x) = g(x) + o(x). // // The function
 * g(x) is a smooth function with a simple form, such as // a convex quadratic. The function o(x) is a low amplitude
 * high frequency // function. IFFCO is especially effective on problems where the amplitude // of o(x) decays near
 * minima of g(x). However, IFFCO has proven to be // effective on more complex problems. // // Input variables: // func -
 * name of subroutine that evaluates f(x). // Declare it external within the routine that calls // IFFCO. e.g. external
 * myfunction. // x - (dble/size n) initial vector. // u - (dble/size n) vector of upper constraints. // l - (dble/size
 * n) vector of lower constraints. // fscale - (dble) approximate maximum value of the function // within the
 * constraints. // minh - (dble) lower bound for the scale (h). // maxh - (dble) upper bound for the scale (h). // n -
 * (int) the dimension of x. // maxIterations[0]- (int) maximum iterations on a given scale. // maxIterations[1]- (int)
 * maximum func. evals for entire run. // restart - (int) # of restarts to be done. // writeLevel - (int) type of output
 * (see statsIF routine). // writeLevel = 0 - suppress all output // writeLevel = 1 - standard info // writeLevel = 2 -
 * standard info + x + #f evals // writeLevel = 3 - standard info, unscaled // writeLevel = 4 - standard info + x + #f
 * evals, unscaled // Add 10 to writeLevel to print output to the screen. // termTol - (dble) tolerence at which to
 * terminate the iteration // for a given scale. // maxcuts - (int) maximum # of cuts allowed in line search. // option -
 * (int/size 7) vector // option[0] = Quasi-Newton update. // = 0 (Gradient projection) // = 1 (SR1) // = 2 (BFGS) //
 * option[1] = minimum strategy. // = 0 (Do nothing) // = 1 (Take min as current point at restart) // = 2 (Take min as
 * current point at new scale) // = 3 (Take min as current point at new step) // option[2] = initial point. // = 0 (Use
 * given initial point) // = 1 (Use center of box as initial point) // option[3] = Quasi-Newton variation. // = 0
 * (Re-initialize B at each new scale) // = 1 (Re-initialize B if active set changes) // = 2 (Update Hessian even if
 * active set changes) // option[4] = Output file // = 0 (Create and use iffco.out (default)) // = 1 (option[5] contains
 * the unit number of the // file to use instead of iffco.out) // option[5] = Output file unit number. // option[6] =
 * Master processor use. // = 0 (Use master processor for function // evaluations when computing the gradient //
 * (default)) // = 1 (Do not use master processor for function // evaluations when computing the gradient) // idata -
 * (int/size ilen) vector of int's that is passed, unaltered, // to the user's objective function code. // ilen - (int)
 * length of vector idata. // ddata - (dble/size dlen) vector of dble's that is passed, unaltered, // to the user's
 * objective function code. // dlen - (int) length of vector ddata. // cdata - (char/size clen) vector of char's that is
 * passed, unaltered, // to the user's objective function code. // // Output variables: // x - IFFCO's candidate for the
 * solution. // f - (dble) f(x). // // Global variables: // Global variables are only used to keep track of the current
 * min and // max of all sampled points, total number of function evaluations, and // machine precision. Suffixed w/
 * 'IF' for less chance of duplication with // other globals. // Found in: iffco, minIF, funcIF, initIF, statsIF,
 * par_gradIF, // ser_linesearchIF, par_linesearchIF, maxIF, quasiIF, restartIF, takeminIF. // // Since FORTRAN does not
 * allow local variable arrays to be dynamic you must // change the parameter mx in every routine if n > mx. Another
 * parameter // you may have to change in many routines is maxprocs. maxprocs should // be >= nprocs. It is currently
 * set to mx*2+1, which should be large enough // unless you allow very many cutbacks in the linesearch. // // fhist
 * (dble/size mx2) is a local array that stores f(x) for every iteration. // We store the unscaled value since fscale
 * can change after restarts. // Parameter mx2 determines the size of fhist. If you expect more then you // must change
 * mx2. // // Subroutines: // Note all subroutines are suffixed w/ 'IF' (for IFFCO). // 1) inputsIF // 2) initIF // 3)
 * statsIF // 4) scaleIF // 5) unscaleIF // 6) funcIF // 7a) ser_gradIF // 7b) par_gradIF // 8) minIF // 9) maxIF // 10)
 * updateIF // 11a)ser_linesearchIF // 11b)par_linesearchIF // 12) eyeIF // 13) evaltolIF // 14) quasiIF // 15) stepIF //
 * 16) restartIF // 17) takeminIF // 18) pointsIF // Parallel communication subroutines (only used by the MPI and PVM
 * versions): // c1) mastersendIF // c2) slaverecvIF // c3) slavesendIF // c4) masterrecvIF // c5) getmytidIF // c6)
 * getnprocsIF // c7) gettidIF // c8) comminitIF // c9) commexitIF
 * //-----------------------------------------------------------------
 */

public class iffco implements IOptimizer {

	private int nevalsIF = 0;

	private double fminIF;

	private double[] xminIF;

	private double mepsIF;

	private double fmaxIF;

	private double maxh = 0.5;

	private double minh = 1.0E-4;

	private double fscale = 100.0;

	// Max no of iterations for step and algorithm
	private int[] maxIterations = new int[] { 0, 0 };

	// Number of restarts
	private int restart = 0;

	private String pointsOutputFilename = "points.out";

	private String runOutputFilename = "run.out";

	// Level of output
	private int writeLevel = 14;

	// Termination tolerance
	private double termTol = 0.001;

	private int maxcuts = 0;

	private int fctr = 0;

	private int ncuts = 0;

	private int stencil = 0;

	// Maximum cuts to take in a linesearch

	// Options
	private int[] option = new int[] { 2, 3, 0, 1, 0 };

	// double[] x_orig;

	double[] x;

	double[] u;

	double[] l;

	private int flag = 0;

	private int quasi = 2;

	// private int option=1;

	private double functionf;

	private int functionflag = 0;

	private double fp;

	private double f;

	private double h;

	private AbstractObjectiveFunction function;

	/**
	 * @param function
	 */
	public iffco(AbstractObjectiveFunction function) {
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
		// Population size
		l = new double[noOfFreeParameters];
		u = new double[noOfFreeParameters];
		x = new double[noOfFreeParameters];
		// x_orig = new double[noOfFreeParameters];
		int count = 0;
		for (int i = 0; i < params.length; i++) {
			if (!params[i].isFixed()) {

				x[count] = params[i].getValue();
				// x_orig[count] = params[i].getValue();
				l[count] = params[i].getLowerLimit();
				u[count] = params[i].getUpperLimit();
				count++;
			}
		}
		for (int i = 0; i < x.length; i++) {
			System.out.println(x[i] + "\t" + l[i] + "\t" + u[i]);
		}
	}

	/**
	 * 
	 */
	public void iffco_run() {

		// reset();

		// Local variables
		int n = x.length;
		int k1;
		int its = 0;
		double tol = 0.0;
		double[] x_orig = new double[n];
		double[] xp = new double[n];
		double[] g = new double[n];
		double[] gp = new double[n];
		double[] p = new double[n];
		double[][] B = new double[n][n];
		Double[] fhist = new Double[1000];
		xminIF = new double[n];

		// INITIALIZE IFFCO AND CHECK FOR ERRORS. TERMINATE IF ANY ARE FOUND.

		// Report the input parameters.
		inputsIF();

		// Initialize, error check parameters, and copy x to x_orig.
		initIF(x, u, l, x_orig);

		// Any errors?
		if (flag > 0) {

			return;
		}

		// -----------------------------------------

		// BEGIN IFFCO BY SETTING X_ORIG AND FINDING INITIAL F.

		// Scale x_orig onto the unit box. Routine returns x.
		scaleIF(x_orig, x);
		// Evaluate function at initial point, x.
		funcIF(x);
		f = functionf;

		// Initialize fhist(unscaled), fminIF(unscaled),
		// xminIF(unscaled), fmaxIF (scaled) to initial point.
		fhist[0] = f * fscale;
		fctr = 1; // changed from 2 PDQ

		fminIF = f * fscale;
		fmaxIF = f;
		for (int i = 0; i < n; i++) {
			xminIF[i] = x_orig[i];
		}

		// Loop over restarts.
		for (k1 = 0; k1 <= restart; k1++) {

			// Initialize quasi-Newton matrix.
			eyeIF(B);

			// Loop over scales.
			while (h >= minh && nevalsIF < maxIterations[1]) {

				// Re-initialize quasi-Newton matrix if appropriate.
				if (option[3] == 0) { // changed from 4 to 3 PDQ
					eyeIF(B);
				}

				its = 0;
				ncuts = 0;

				// Evaluate gradient.

				ser_gradIF(x, f, h, g);

				// Evaluate tolerance.
				tol = evaltolIF(x, g);
				// System.out.println("tolerance\t" + tol);

				// Iterate until: 1) maxIterations exceeded.
				// or 2) iteration has converged for this scale.
				// or 3) line search failure.
				// or 4) Stencil failure

				while (tol > termTol * h && stencil == 0 && ncuts >= 0 && its < maxIterations[0]
						&& nevalsIF < maxIterations[1]) {

					// Form step: p = -H*g.
					stepIF(g, B, p);
					// Line Search.
					// for (int m = 0; m < p.length; m++)
					// {
					// System.out.println("step\t" + p[m]);
					// }
					// System.out.println("Starting linesearch");
					ser_linesearchIF(x, g, p, xp);
					// Take minimum if appropriate.
					if (option[1] >= 3) {
						fp = takeminIF(xp, fp, fhist);
						// Set ncuts = 0 if we take the min.
						if (flag == 1 && ncuts < 0) {
							ncuts = 0;
						}
					}

					// Accept or reject new point (accept if ncuts >= 0).
					if (ncuts >= 0) {
						// try
						// {
						// System.out.println("ncuts");
						// Thread.sleep(1000);
						// }
						// catch (InterruptedException e)
						// {
						// Auto-generated catch block
						// e.printStackTrace();
						// }
						// Get new gradient.

						ser_gradIF(xp, fp, h, gp);

						// Quasi-Newton Update.
						quasiIF(x, xp, g, gp, B);

						// Update x,g,f and increment iteration counter.
						updateIF(xp, gp, x, g);

						its = its + 1;

						// Report Stats (new x,f,g);
						statsIF(g, its);

						// Save f history (unscaled).
						fhist[fctr] = f * fscale;
						fctr = fctr + 1;

						// Evaluate tolerance.
						tol = evaltolIF(x, g);
					}

				}

				// Check if exited loop because of convergence or
				// exceeding maxIterations.
				if (tol <= termTol * h) {
					ncuts = -2;
				} else if (its >= maxIterations[0]) {
					ncuts = -3;
				} else if (stencil == 1) {
					ncuts = -4;
				} else if (nevalsIF >= maxIterations[1]) {
					ncuts = -5;
				}

				// Report Stats (new x,f,g).
				statsIF(g, its);

				// Save f history (unscaled).
				fhist[fctr] = f * fscale;
				fctr = fctr + 1;

				// Take minimum if appropriate.
				if (option[1] >= 2) {
					f = takeminIF(x, f, fhist);

				}

				// Set h-scale value.
				h = h / 2.0;

			}

			// Perform restart if there is a restart to be done.
			if (k1 < restart) {
				restartIF();

				// Take minimum if appropriate.
				if (option[1] >= 1) {
					f = takeminIF(x, f, fhist);
				}
			}

		}

		// Perform concluding rites.

		// Take minimum if appropriate.
		if (option[1] >= 1) {
			f = takeminIF(x, f, fhist);
		}

		// Unscale final x,f.
		unscaleIF(x, x_orig);
		for (int i = 0; i < n; i++) {
			x[i] = x_orig[i];
		}

		f = f * fscale;

		// //write out f history
		if (writeLevel >= 1) {
			// //write(mainout_unum,*) 'f history (unscaled)'
			if (writeLevel > 10) {
				System.out.println("f history (unscaled)");
			}
			for (int i = 0; i < fctr - 1; i++) {
				// //write(mainout_unum,*) fhist[i]
				if (writeLevel > 10) {
					System.out.println(fhist[i]);
				}
			}
		}

		// if (writeLevel>10) {
		// if(option[4]!=1) then
		// close(unit = mainout_unum)
		// }
		// }
		// close(unit = ptsout_unum)

	}

	// -----------------------------------------------------------------
	// 1) inputsIF
	//
	// This routine simply prints out what you've inputed to IFFCO.
	// Useful for a self-check.
	// -----------------------------------------------------------------
	private void inputsIF() {
		// // Arguments
		// integer n, maxIterations[1], restart, writeLevel,
		// // ncuts, nprocs,
		// * option[6] // integer mainout_unum, ptsout_unum // double precision
		// * x(n), u(n), l(n) // double precision fscale, minh, maxh, termTol, f
		// // //
		// Local variables
		int i, writbase;
		// Calculate writbase
		if (writeLevel >= 10) {
			writbase = writeLevel - 10;
		} else {
			writbase = writeLevel;
		}
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(runOutputFilename, true)));

			// * OPEN OUTPUT FILES if((option[4]!=1) && (writbase!=0)) then
			// open(unit =
			// * mainout_unum, file = './iffco.out', status = 'unknown')
			// else
			// * mainout_unum = option[5] end if // Open "points.out" file
			// and
			// create
			// * structure for info open(unit = ptsout_unum, file =
			// './points.out',
			// * status = 'unknown')
			if (writbase > 0) {
				out.println("fscale\t" + fscale);
				out.println("minh\t" + minh);
				out.println("maxh\t" + maxh);
				out.println("maxIterations[0]\t" + maxIterations[0]);
				out.println("maxIterations[1]\t" + maxIterations[1]);
				out.println("restart\t" + restart);
				out.println("writelevel\t" + writeLevel);
				out.println("tolerance\t" + termTol);
				out.println("maxcuts\t" + maxcuts);
				out.println("X AND ITS LOWER AND UPPER BOUNDS");
				for (i = 0; i < x.length; i++) {
					out.println(x[i] + "\t" + l[i] + "\t" + u[i]);
				}
				out.println("OPTIONS");
				for (i = 0; i < option.length; i++) {
					out.println(i + "\t" + option[i]);
				}
			} else {
				System.out.println("fscale\t" + fscale);
				System.out.println("minh\t" + minh);
				System.out.println("maxh\t" + maxh);
				System.out.println("maxIterations[0]\t" + maxIterations[0]);
				System.out.println("maxIterations[1]\t" + maxIterations[1]);
				System.out.println("restart\t" + restart);
				System.out.println("writelevel\t" + writeLevel);
				System.out.println("tolerance\t" + termTol);
				System.out.println("maxcuts\t" + maxcuts);
				System.out.println("X AND ITS LOWER AND UPPER BOUNDS");
				for (i = 0; i < x.length; i++) {
					System.out.println(x[i] + "\t" + l[i] + "\t" + u[i]);
				}
				System.out.println("OPTIONS");
				for (i = 0; i < option.length; i++) {
					System.out.println(i + "\t" + option[i]);
				}
			}
		} catch (IOException e) {
		}

	}

	// -----------------------------------------------------------------
	// 2) initIF
	//
	// This routine initializes various variables and checks that
	// the input variables have reasonable values.
	// Output variables:
	// x_orig
	// h
	// quasi
	// flag: > 0 failure.
	//
	// -----------------------------------------------------------------
	private void initIF(double[] x, double[] u, double[] l, double[] x_orig)

	{
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(runOutputFilename, false)));

			int n = x.length;
			int i, nerrs, writbase;

			// Calculate writbase
			if (writeLevel >= 10) {
				writbase = writeLevel - 10;
			} else {
				writbase = writeLevel;
			}

			// Initialize variables.
			nerrs = 0;
			nevalsIF = 0;
			mepsIF = 1.0E-12;
			quasi = option[0];

			// Check consistency of upper bounds, lower bounds, and x.
			for (i = 0; i < n; i++) {
				if (u[i] <= l[i]) {
					if (writeLevel > 0) {
						out.println("U\t" + i + "is less than L\t" + i);
					}
					System.out.println("U\t" + i + "is less than L\t" + i);
					nerrs = nerrs + 1;
				}
				if (x[i] > u[i] || x[i] < l[i]) {
					if (writeLevel > 0) {
						out.println("X\t" + i + "is out of bounds");
					}
					System.out.println("X\t" + i + "is out of bounds");
					nerrs = nerrs + 1;
				}
			}

			// Check fscale non-negative
			if (fscale < 0.0) {
				if (writeLevel > 0) {
					out.println("fscale negative");
				}
				System.out.println("fscale negative");
				nerrs = nerrs + 1;
			} else if (fscale == 0.0) {
				fscale = 1.0;
			}

			// Check consistency of minh and maxh and that maxh <= .5.
			if (minh > maxh) {
				if (writeLevel > 0) {
					out.println("minh > maxh");
				}
				System.out.println("minh > maxh");
				nerrs = nerrs + 1;
			} else if (maxh > 5.0E-1) {
				if (writeLevel > 0) {
					out.println("maxh > 0.5");
				}
				System.out.println("maxh > 0.5");
				nerrs = nerrs + 1;
			} else {
				h = maxh;
			}

			// Check n > 0.
			if (n <= 0) {
				if (writeLevel > 0) {
					out.println("n <= 0");
				}
				System.out.println("n <= 0");
				nerrs = nerrs + 1;
			}

			// Check maxIterations[0],maxIterations[1] > 0.
			if (maxIterations[0] < 0) {
				if (writeLevel > 0) {
					out.println("maxIterations[0] < 0");
				}
				System.out.println("maxIterations[0] < 0");
				nerrs = nerrs + 1;
			} else if (maxIterations[0] == 0) {
				maxIterations[0] = 100;
			}

			if (maxIterations[1] < 0) {
				if (writeLevel > 0) {
					out.println("maxIterations[1] < 0");
				}
				System.out.println("maxIterations[1] < 0");
				nerrs = nerrs + 1;
			} else if (maxIterations[1] == 0) {
				maxIterations[1] = 100 * n * n;
			}

			if (restart < 0) {
				if (writeLevel > 0) {
					out.println("restart < 0");
				}
				System.out.println("restart < 0");
				nerrs = nerrs + 1;
			}

			// Check termTol > 0.
			if (termTol < 0) {
				if (writeLevel > 0) {
					out.println("termtol < 0");
				}
				System.out.println("termtol < 0");
				nerrs = nerrs + 1;
			} else if (termTol == 0.0) {
				termTol = 1.0;
			}

			// Check ncuts >= 0.
			if (maxcuts < 0) {
				if (writeLevel > 0) {
					out.println("maxcuts < 0");
				}
				System.out.println("maxcuts < 0");
				nerrs = nerrs + 1;
			} else if (maxcuts == 0) {
				maxcuts = 3;
			}

			// Check that option has legal values.
			if (option[0] < 0 || option[0] > 2) {
				if (writeLevel > 0) {
					out.println("Invalid value for option[0]");
				}
				System.out.println("Invalid value for option[0]");
				nerrs = nerrs + 1;
			}

			if (option[1] < 0 || option[1] > 3) {
				if (writeLevel > 0) {
					out.println("Invalid value for option[1]");
				}
				System.out.println("Invalid value for option[1]");
				nerrs = nerrs + 1;
			}

			if (option[2] < 0 || option[2] > 1) {
				if (writeLevel > 0) {
					out.println("Invalid value for option[2]");
				}
				System.out.println("Invalid value for option[1]");
				nerrs = nerrs + 1;
			}

			if (option[3] < 0 || option[3] > 2) {
				if (writeLevel > 0) {
					out.println("Invalid value for option[3]");
				}
				System.out.println("Invalid value for option[2]");
				nerrs = nerrs + 1;
			}

			if (option[4] < 0 || option[4] > 1) {
				if (writeLevel > 0) {
					out.println("Invalid value for option[4]");
				}
				System.out.println("Invalid value for option[3]");
				nerrs = nerrs + 1;
			}

			if (option[2] == 1) {
				// Set x_orig to center of box.
				for (i = 0; i < n; i++) {
					x_orig[i] = (u[i] + l[i]) / 2.0;
				}
			} else {
				// Copy x to x_orig
				for (i = 0; i < n; i++) {
					x_orig[i] = x[i];
				}
			}

			if (writeLevel >= 1) {
				out.println("-----------------------------");
				out.println("No of errors\t" + nerrs);
				out.println("-----------------------------");
			}
			if (writeLevel >= 11) {
				System.out.println("-----------------------------");
				System.out.println("No of errors\t" + nerrs);
				System.out.println("-----------------------------");
			}

			// Since the "standard info" output lines don't have junk (num
			// funcs,
			// current iterate) stuck in between them, the header is printed
			// just
			// once, at the top. This needs to be done after the number of
			// errs
			// line.
			if (writbase == 1 || writbase == 3) {
				out.println(" m ||x|| f ||g||  h cuts");
				if (writeLevel > 10) {
					System.out.println(" m ||x|| f ||g||  h cuts");
				}
			}
			flag = nerrs;
			out.close();
		} catch (IOException e) {
		}
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(pointsOutputFilename, false)));
			out.print("IFFCO RUN");
			out.close();
		} catch (IOException e) {
		}

	}

	// -----------------------------------------------------------------
	// 3) statsIF
	//
	// This routine logs various statistics about the current
	// iterate of IFFCO to iffco.out.
	//
	// writeLevel = 1 - standard info
	// writeLevel = 2 - standard info + x
	// writeLevel = 3 - standard info, unscaled
	// writeLevel = 4 - standard info + x, unscaled
	// Add 10 to print the info to stdout.
	// The user has the option to print out scaled f,x or unscaled f,x.
	// However \| x \| and \| GF \| will always refer to the scaled
	// value.
	// -----------------------------------------------------------------
	private void statsIF(double[] g, int its) {

		// Arguments
		// integer n, writeLevel, ncuts, its, mx, unit
		// double precision x(n), u(n), l(n), fscale
		// double precision h, f, g(n)

		int n = x.length;
		// Local variables
		int i, writbase;
		double f_value, nmx, nmg;
		double[] x_value = new double[n];
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(runOutputFilename, true)));

			// Global variables
			// integer nevalsIF
			// common /globalIF/ nevalsIF

			if (writeLevel >= 10) {
				writbase = writeLevel - 10;
			} else {
				writbase = writeLevel;
			}

			// Copy f,x to f_value,x_value
			f_value = f;
			for (i = 0; i < n; i++) {
				x_value[i] = x[i];
			}

			// Compute norm(g) and norm(x)
			nmx = 0.0;
			nmg = 0.0;
			for (i = 0; i < n; i++) {
				nmx = nmx + x[i] * x[i];
				nmg = nmg + g[i] * g[i];
			}
			nmx = Math.sqrt(nmx / (1.0 * n));
			nmg = Math.sqrt(nmg / (1.0 * n));

			if (writbase == 2 || writbase == 4) {
				out.println("m ||x|| f ||g|| h cuts");
				if (writeLevel > 10) {
					System.out.println("m ||x|| f ||g|| h cuts");
				}
			}

			// Unscale the results.
			if (writbase == 3 || writbase == 4) {
				f_value = f * fscale;
				unscaleIF(x, x_value);
			}

			// Print out the statistics.
			if (writbase >= 1) {
				if (ncuts == -1) {
					out.println(its + "\t" + nmx + "\t" + f_value + "\t" + nmg + "\t" + h + "\t"
							+ "Line Search Failure");
					if (writeLevel > 10) {
						System.out.println(its + "\t" + nmx + "\t" + f_value + "\t" + nmg + "\t" + h + "\t"
								+ "Line Search Failure");
					}
				} else if (ncuts == -2) {
					out.println(its + "\t" + nmx + "\t" + f_value + "\t" + nmg + "\t" + h + "\t" + "Convergence");
					if (writeLevel > 10) {
						System.out.println(its + "\t" + nmx + "\t" + f_value + "\t" + nmg + "\t" + h + "\t"
								+ "Convergence");
					}
				} else if (ncuts == -3) {
					out.println(its + "\t" + nmx + "\t" + f_value + "\t" + nmg + "\t" + h + "\t" + "MAXIT 1 Exceeded");
					if (writeLevel > 10) {
						System.out.println(its + "\t" + nmx + "\t" + f_value + "\t" + nmg + "\t" + h + "\t"
								+ "MAXIT 1 Exceeded");
					}
				} else if (ncuts == -4) {
					out.println(its + "\t" + nmx + "\t" + f_value + "\t" + nmg + "\t" + h + "\t" + "Stencil failure");
					if (writeLevel > 10) {
						System.out.println(its + "\t" + nmx + "\t" + f_value + "\t" + nmg + "\t" + h + "\t"
								+ "Stencil failure");
					}
				} else if (ncuts == -5) {
					out.println(its + "\t" + nmx + "\t" + f_value + "\t" + nmg + "\t" + h + "\t" + "MAXIT 2 Exceeded");
					if (writeLevel > 10) {
						System.out.println(its + "\t" + nmx + "\t" + f_value + "\t" + nmg + "\t" + h + "\t"
								+ "MAXIT 2 Exceeded");
					}
				} else if (ncuts >= 0) {
					out.println(its + "\t" + nmx + "\t" + f_value + "\t" + nmg + "\t" + h);
					if (writeLevel > 10) {
						System.out.println(its + "\t" + nmx + "\t" + f_value + "\t" + nmg + "\t" + h);
					}
				}
			}

			// Print out x components as well.
			if (writbase == 2 || writbase == 4) {
				for (i = 0; i < n; i++) {
					if (writeLevel > 10) {
						System.out.println("X\t" + i + "\t" + x_value[i]);
					} else {
						out.println("X\t" + i + "\t" + x_value[i]);
					}

				}
				if (writeLevel > 10) {
					System.out.println("TOTAL FUNCTION EVALUATIONS\t" + nevalsIF);
				} else {
					out.println("TOTAL FUNCTION EVALUATIONS\t" + nevalsIF);
				}
			}
			out.close();
		} catch (IOException e) {
		}

	}

	// -----------------------------------------------------------------
	// 4) scaleIF
	//
	// This routine scales x_orig from the domain defined by l,u to the
	// unit box.
	//
	// Output variables:
	// x_scal
	// -----------------------------------------------------------------
	private void scaleIF(double[] x_orig, double[] x_scal) {

		for (int i = 0; i < x_orig.length; i++) {
			x_scal[i] = (x_orig[i] - l[i]) / (u[i] - l[i]);
		}
	}

	// -----------------------------------------------------------------
	// 5) unscaleIF
	//
	// This routine unscales x_scal from the unit box to the domain defined
	// by l,u.
	//
	// Output variables:
	// x_orig
	// -----------------------------------------------------------------
	private void unscaleIF(double[] x_scal, double[] x_orig) {
		for (int i = 0; i < x_orig.length; i++) {
			x_orig[i] = (u[i] - l[i]) * x_scal[i] + l[i];
			// System.out.println("unscaled values\t" + x_orig[i]);
		}
	}

	// -----------------------------------------------------------------
	// 6) funcIF
	//
	// This routine evaluates the objective function, 'func'.
	// 'func' is expected to return flag = 0 for success.
	// > 0 for failure.
	//
	// Input variables:
	// x - scaled x value.
	// Ouput variables:
	// f - scaled objective function value at x.
	// -----------------------------------------------------------------
	private void funcIF(double[] x) {

		double[] x_unscal = new double[x.length];

		unscaleIF(x, x_unscal);

		// Initialize flag to success.
		functionflag = 0;

		// to outside function.
		// My function
		functionf = this.function.evaluate(x_unscal);
		functionflag = function.failedToEvaluate();
		if (functionflag > 0) {
			functionf = fscale;
			if (writeLevel > 0) {
				// write(mainout_unum,*) 'function failed to evaluate!'
				// write(mainout_unum,*) 'Setting f := fscale.'
			}
			if (writeLevel > 10) {
				// write(6,*) 'function failed to evaluate!'
				// write(6,*) 'Setting f := fscale.'
			}
		}

		// Check if current point is a minimum so far (unscaled).
		// fminIF unscaled because restarts change fscale.
		minIF(x_unscal);

		// Scale f value
		functionf = functionf / fscale;

		nevalsIF = nevalsIF + 1;

		maxIF();
		pointsIF(x);
		// System.out.println("f at end\t" + functionf);

	}

	// For the serial version, use this gradient routine.
	// -----------------------------------------------------------------
	// 7a) ser_gradIF
	//
	// This routine evaluates the gradient using a finite difference
	// formula with a difference step of h.
	//
	// Ouput variables
	// g - gradient
	// -----------------------------------------------------------------
	private void ser_gradIF(double[] x, double f, double h, double[] g) {

		// Arguments

		// Local variables
		int n = x.length;
		int[] fflagfor = new int[n];
		int[] fflagback = new int[n];
		int[] st = new int[n];
		double[] x2 = new double[n];
		double[] ffor = new double[n];
		double[] fback = new double[n];
		double minf, maxf;

		// Initialize variables
		minf = f;
		maxf = f;
		stencil = 0;
		for (int i = 0; i < n; i++) {
			x2[i] = x[i];
		}

		// See what type of difference gradient we can do for each direction.
		for (int i = 0; i < n; i++) {
			st[i] = 0;
			if (x[i] - h >= 0.0) {
				// Able to do backward differences.
				st[i] = st[i] + 1;
			}
			if (x[i] + h <= 1.0) {
				// Able to do forward differences.
				st[i] = st[i] + 2;
			}
		}

		// Get all function values needed to calculate the gradient.
		for (int i = 0; i < n; i++) {
			// Backward differences
			if (st[i] == 1 || st[i] == 3) {
				x2[i] = x2[i] - h;
				// System.out.println("gradient func back\t" + h);
				// for (int p = 0; p < x2.length; p++)
				// {
				// System.out.println("x2p\t" + x2[p]);
				// }

				funcIF(x2);
				fback[i] = functionf;
				fflagback[i] = functionflag;

				// Reset x2[i]
				x2[i] = x[i];

				// Check for extremal f values
				if (fback[i] < minf && fflagback[i] == 0) {
					minf = fback[i];
				} else if (fback[i] > maxf && fflagback[i] == 0) {
					maxf = fback[i];
				}

			}

			// Forward differences
			if (st[i] == 2 || st[i] == 3) {
				x2[i] = x2[i] + h;
				// System.out.println("gradient func forward\t" + h);
				// for (int p = 0; p < x2.length; p++)
				// {
				// System.out.println("x2p\t" + x2[p]);
				// }

				funcIF(x2);
				ffor[i] = functionf;
				fflagfor[i] = functionflag;

				// Reset x2[i]
				x2[i] = x[i];

				// Check for extremal f values
				if (ffor[i] < minf && fflagfor[i] == 0) {
					minf = ffor[i];
				} else if (ffor[i] > maxf && fflagfor[i] == 0) {
					maxf = ffor[i];
				}

			}
		}

		// Set all infeasible function values to a value slightly
		// larger than the largest feasible value.
		maxf = maxf + 1.0E-6 * Math.abs(maxf);
		for (int i = 0; i < n; i++) {
			if (st[i] == 1 || st[i] == 3) {
				if (fflagback[i] > 0) {
					fback[i] = maxf;
				}
			}
			if (st[i] == 2 || st[i] == 3) {
				if (fflagfor[i] > 0) {
					ffor[i] = maxf;
				}
			}
		}

		// Calculate the gradient
		for (int i = 0; i < n; i++) {
			if (st[i] == 1) {
				g[i] = (f - fback[i]) / h;
			} else if (st[i] == 2) {
				g[i] = (ffor[i] - f) / h;
			} else {
				g[i] = (ffor[i] - fback[i]) / (2 * h);
			}
		}

		// Check for stencil failure.
		if (f <= minf) {
			stencil = 1;
		}
		// System.out.println("gradient\t");
		// for (int p = 0; p < x2.length; p++)
		// {
		// System.out.println("grad\t" + g[p] + "\t" + ffor[p] + "\t" +
		// fback[p]);
		// }

	}

	// -----------------------------------------------------------------
	// 8) minIF
	//
	// This routine checks to see if the point given has a lower function
	// value than the current minimum function value. If so it saves the
	// point and value and returns flag = 1. If not then it does
	// nothing and returns flag = 0.
	// Input variables:
	// n -- number of dimensions
	// x -- point, *unscaled*
	// f -- function value, *unscaled*
	// Output variables:
	// flag
	// -----------------------------------------------------------------
	private void minIF(double[] x) {

		if (functionf < fminIF) {
			fminIF = functionf;
			for (int i = 0; i < x.length; i++) {
				xminIF[i] = x[i];
			}
		}

	}

	// -----------------------------------------------------------------
	// 9) maxIF
	//
	// This routine checks to see if the point given has a greater function
	// value then the current max function value. If so it saves the
	// value and returns flag = 1. If not then it does nothing and
	// returns flag = 0.
	// Output variables:
	// flag
	// -----------------------------------------------------------------
	private void maxIF() {

		if (functionf > fmaxIF) {
			fmaxIF = functionf;
		}
	}

	// -----------------------------------------------------------------
	// 10) updateIF
	//
	// This routine simply copies xp,gp,fp to x,g,f.
	// Output variables:
	// x,g,f
	// -----------------------------------------------------------------
	private void updateIF(double[] xp, double[] gp, double[] x, double[] g) {
		int n = x.length;
		for (int i = 0; i < n; i++) {
			x[i] = xp[i];
			g[i] = gp[i];
		}
		f = fp;
	}

	// For serial versions, use the following linesearch

	// -----------------------------------------------------------------
	// 11a) ser_linesearchIF
	//
	// This routine implements a quadratic and cubic line search.
	// p = direction of step: - H*g.
	// Output variables:
	// ncuts: -1 Failure.
	// fp
	// xp
	// -----------------------------------------------------------------
	private void ser_linesearchIF(double[] x, double[] g, double[] p, double[] xp) {

		// Local Variables
		int active, cubic, flag;
		double alpha, oldalpha = 0, dfz, f2p = 0, nrm;
		double suffdec, lp, l2p, sgma;
		double a11, a21, a12, a22, v1, v2;
		double a, b;
		int n = x.length;
		double[] z = new double[n];
		double[] lastx = new double[n];

		ncuts = 0;
		flag = 0;
		cubic = 0;
		sgma = 1.0E-4;
		alpha = 1.0;

		// Form new trial point: xp = P[x + alpha p]
		for (int i = 0; i < n; i++) {
			z[i] = x[i] + alpha * p[i];
		}

		// Form xp = P[z]. (Project z onto the unit box)
		for (int i = 0; i < n; i++) {
			if (z[i] <= 0.0) {
				xp[i] = 0.0;
			} else if (z[i] >= 1.0) {
				xp[i] = 1.0;
			} else {
				xp[i] = z[i];
			}
		}

		// Evaluate fp = f(xp).
		funcIF(xp);
		// for (int m = 0; m < xp.length; m++)
		// {
		// System.out.println("xp line\t" + xp[m] + "\t" + x[m] + "\t" + p[m]
		// + "\t" + alpha);
		// }

		fp = functionf;

		for (int i = 0; i < n; i++) {
			lastx[i] = xp[i];
		}

		// Form suffdec = sgma * g' * (xp - x) and dfz = g'*p.
		suffdec = 0.0;
		dfz = 0.0;
		for (int i = 0; i < n; i++) {
			suffdec = suffdec + g[i] * (xp[i] - x[i]);
			dfz = dfz + g[i] * p[i];
		}
		suffdec = suffdec * sgma;

		// Iterate until 1) fp is sufficiently smaller than f
		// or 2) ncuts exceeds maxcuts
		// or 3) flag = 1
		while (fp > f + suffdec && ncuts < maxcuts && flag == 0) {

			// Find any new active constraints of xp.
			// active = 1 new active constraints
			// active = 0 no new active constraints

			active = 0;
			for (int i = 0; i < n; i++) {
				z[i] = x[i] + alpha * p[i];
				if ((z[i] < 0.0 && x[i] > mepsIF) || (z[i] > 1.0 && x[i] < 1.0 - mepsIF)) {
					active = 1;
				}
			}

			// If there are new active constraints do not use quadratic
			// model.
			if (active == 1) {
				oldalpha = alpha;
				alpha = 0.5 * alpha;
			} else {
				// Use quadratic model.
				if (cubic == 0) {
					oldalpha = alpha;
					alpha = -alpha * dfz / (2.0 * (fp - f - alpha * dfz));
					alpha = oldalpha * alpha;
					cubic = 1;
					// Use cubic model
				} else if (cubic == 1) {
					lp = alpha;
					l2p = oldalpha;
					a11 = 1 / (lp * lp);
					a21 = -l2p / (lp * lp);
					a12 = -1 / (l2p * l2p);
					a22 = lp / (l2p * l2p);

					v1 = fp - f - dfz * lp;
					v2 = f2p - f - dfz * l2p;

					a = 1 / (lp - l2p) * (a11 * v1 + a12 * v2);
					b = 1 / (lp - l2p) * (a21 * v1 + a22 * v2);

					oldalpha = alpha;

					if (Math.abs(a) > mepsIF) {
						alpha = (-b + Math.sqrt(b * b - 3.0 * a * dfz)) / (3.0 * a);
					} else {
						// Cubic model failed. Use quadratic model
						alpha = -alpha * dfz / (2.0 * (fp - f - alpha * dfz));
						alpha = oldalpha * alpha;
					}
				}
				f2p = fp;
			}

			// Bound alpha between [.1*oldalpha, .5*oldalpha].
			if (alpha > (5.0E-1) * oldalpha) {
				alpha = (5.0E-1) * oldalpha;
			} else if (alpha < (1.0E-1) * oldalpha) {
				alpha = (1.0E-1) * oldalpha;
			}

			// Form new trial point: xp = P[x + alpha p]
			for (int i = 0; i < n; i++) {
				z[i] = x[i] + alpha * p[i];
			}

			// Form xp = P[z]. (Project z onto the unit box)
			for (int i = 0; i < n; i++) {
				if (z[i] <= 0.0) {
					xp[i] = 0.0;
				} else if (z[i] >= 1.0) {
					xp[i] = 1.0;
				} else {
					xp[i] = z[i];
				}
			}

			// Check that the step length (alpha * p) has not
			// been reduced below .01*h.
			// Form nrm
			nrm = 0.0;
			for (int i = 0; i < n; i++) {
				a = alpha * p[i];
				nrm = nrm + a * a;
			}
			nrm = Math.sqrt(nrm / (1.0 * n));

			if (nrm < (1.0E-2) * minh) {
				flag = 1;
			} else {
				// Check to see if line search came up w/ same point as last
				// time
				int k = 0;
				int repcount = 0;
				for (k = 0; k < xp.length; k++) {
					if (Math.abs(xp[k] - lastx[k]) <= mepsIF) {
						repcount++;
					}
				}
				if (repcount < n - 1 || Math.abs(xp[n - 1] - lastx[n - 1]) > mepsIF) {
					// Evaluate fp = f(xp).
					funcIF(xp);
					fp = functionf;

					// Save point.
					for (int i = 0; i < n; i++) {
						lastx[i] = xp[i];
					}
				}
			}

			// Form suffdec = sgma * g' * (xp - x)
			suffdec = 0.0;
			for (int i = 0; i < n; i++) {
				suffdec = suffdec + g[i] * (xp[i] - x[i]);
			}
			suffdec = suffdec * sgma;

			ncuts = ncuts + 1;

		}

		// Linesearch failure. Return ncuts = -1
		if (fp > f + suffdec) {
			ncuts = -1;
			// Set fp = f (used for taking minimums)
			fp = f;
		}

	}

	// -----------------------------------------------------------------
	// 12) eyeIF
	//
	// This routine sets the matrix B = I.
	// Output variables:
	// B
	// -----------------------------------------------------------------
	private void eyeIF(double[][] B) {
		int n = B[0].length;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (i == j) {
					B[i][j] = 1.0;
				} else {
					B[i][j] = 0.0;
				}
			}
		}
	}

	// -----------------------------------------------------------------
	// 13) evaltolIF
	//
	// This routine evaluates the tolerance at the current point x.
	// We define the tolerance by \| x - P[x - \grad_h f(x)] \|
	// Output variables:
	// tol
	// -----------------------------------------------------------------
	private double evaltolIF(double[] x, double[] g) {

		// Local variables

		int n = x.length;
		double tol;
		double[] diff = new double[n];
		double[] proj = new double[n];

		// Form P[x - \grad_h f(x)].
		for (int i = 0; i < n; i++) {
			diff[i] = x[i] - g[i];

			if (diff[i] <= 0.0) {
				proj[i] = 0.0;
			} else if (diff[i] >= 1) {
				proj[i] = 1.0;
			} else {
				proj[i] = diff[i];
			}
		}

		// Compute \| x - P[x - \grad_h f(x)] \|
		tol = 0.0;
		for (int i = 0; i < n; i++) {
			tol = tol + (x[i] - proj[i]) * (x[i] - proj[i]);
		}

		tol = Math.sqrt(tol / (1.0 * n));
		return tol;

	}

	// -----------------------------------------------------------------
	// 14) quasiIF
	//
	// This routine initializes and updates the quasi-Newton matrix.
	// It handles either the SR1 or the BFGS updates.
	// quasi = option[0]
	// quasi = 0: projected gradient alg.
	// quasi = 1: SR1 update
	// quasi = 2: BFGS update
	// Output variables:
	// B
	// -----------------------------------------------------------------
	private void quasiIF(double[] x, double[] xp, double[] g, double[] gp, double[][] B) {

		// Local variables
		int flag = 0;
		// parameter(mx = 24)
		int n = x.length;

		int[] xp_act = new int[n];
		int[] x_act = new int[n];

		double[] y = new double[n];
		double[] s = new double[n];
		double[] t = new double[n];
		double[][] C = new double[n][n];
		double[][] D = new double[n][n];
		double r, r2;

		// Global variables
		// double precision mepsIF,fminIF,xminIF(mx),fmaxIF
		// common /globalIF2/fminIF,xminIF,fmaxIF
		// common /globalIF3/mepsIF

		// Find active sets for x_+ and x.
		for (int i = 0; i < n; i++) {
			if ((xp[i] < mepsIF && gp[i] > 0.0) || (xp[i] > (1.0 - mepsIF) && gp[i] < 0.0)) {
				xp_act[i] = 1;
			} else {
				xp_act[i] = 0;
			}

			if ((x[i] < mepsIF && g[i] > 0.0) || (x[i] > (1.0 - mepsIF) && g[i] < 0.0)) {
				x_act[i] = 1;
			} else {
				x_act[i] = 0;
			}

		}

		// Form y = gf(x_+) - gf(x) and s = x_+ - x
		for (int i = 0; i < n; i++) {
			y[i] = gp[i] - g[i];
			s[i] = xp[i] - x[i];
		}

		// Check to see if xp_act = x_act.
		flag = 0;
		for (int i = 0; i < n; i++) {
			if (xp_act[i] != x_act[i]) {
				flag = 1;
			}
		}

		// Form P_{I_c} B_c P_{I_c}
		// Zero out the rows and columns of B wrt x_act.
		for (int i = 0; i < n; i++) {
			if (x_act[i] == 1) {
				for (int j = 0; j < n; j++) {
					B[i][j] = 0.0;
					B[j][i] = 0.0;
				}
			}
		}

		// Set B = I
		if (quasi == 0 || (flag == 1 && option[3] == 1)) {
			eyeIF(B);

			// SR1 update (B = approximate Hessian)
			// B = B + [(y - B*s) (y-B*s)^T] / (y - B*s)'*s
		} else if (quasi == 1) {

			// Form (y - B*s).
			for (int i = 0; i < n; i++) {

				t[i] = 0.0;
				for (int j = 0; j < n; j++) {
					t[i] = t[i] + B[i][j] * s[j];
				}
				t[i] = y[i] - t[i];
			}

			// Form (y - B*s)'*s.
			r = 0.0;
			for (int i = 0; i < n; i++) {
				r = r + t[i] * s[i];
			}

			// if |(y - B*s)'*s| is too small then skip SR1 update.
			if (Math.abs(r) > mepsIF) {

				// Form [(y - B*s) (y-B*s)^T]
				for (int i = 0; i < n; i++) {
					for (int j = 0; j < n; j++) {
						C[i][j] = t[i] * t[j];
					}
				}

				// Update B
				for (int i = 0; i < n; i++) {
					for (int j = 0; j < n; j++) {
						B[i][j] = B[i][j] + C[i][j] / r;
					}
				}
			}

			// BFGS update (B = approximate inverse Hessian)
			// B = (I-s*y'/y's)*B*(I-s*y'/y's) + s*s'/y'*s
		} else if (quasi == 2) {
			// Form r = (s'*y)
			r = 0.0;
			for (int i = 0; i < n; i++) {
				r = r + s[i] * y[i];
			}

			// Check that r is not <= 0.
			if (r > 0) {
				// Form t = B*y
				for (int i = 0; i < n; i++) {
					t[i] = 0.0;
					for (int j = 0; j < n; j++) {

						t[i] = t[i] + B[i][j] * y[j];
					}
				}

				// Form r2 = (1 + y'*B*y / r )
				r2 = 0.0;
				for (int i = 0; i < n; i++) {
					r2 = r2 + t[i] * y[i];
				}
				r2 = 1.0 + r2 / r;

				// Form // = r2*(s*s') and D = B*y*s'
				for (int i = 0; i < n; i++) {
					for (int j = 0; j < n; j++) {
						C[i][j] = r2 * s[i] * s[j];
						D[i][j] = t[i] * s[j];
					}
				}

				// Update B
				for (int i = 0; i < n; i++) {
					for (int j = 0; j < n; j++) {

						B[i][j] = B[i][j] + (C[i][j] - D[i][j] - D[j][i]) / r;
					}
				}
			} else {
				if (writeLevel > 0) {
					// write(mainout_unum,*) 'B not positive definite'
				}
				if (writeLevel > 10) {
					System.out.println("B not positive definite");
				}
				eyeIF(B);
			}

		}

		// Form P_{I_+} B P_{I_+} + P_{A_+}.
		for (int i = 0; i < n; i++) {
			if (xp_act[i] == 1) {
				for (int j = 0; j < n; j++) {
					if (j != i) {
						B[i][j] = 0.0;
						B[j][i] = 0.0;
					} else {
						B[i][i] = 1.0;
					}
				}
			}
		}
	}

	// -----------------------------------------------------------------
	// 15) stepIF
	//
	// This routine forms the step: p = - H^{-1} g
	// where g is the gradient at x and H is an approximation
	// to the Hessian, \grad^2 f(x).
	//
	// For BFGS, B = H^{-1}
	// For SR1, B = H
	// We also set the condition that \| p / n \| >= 2*h.
	// Output variables:
	// p
	//
	// -----------------------------------------------------------------
	private void stepIF(double[] g, double[][] B, double[] p) {

		// Arguments
		// integer n, quasi, writeLevel, mainout_unum
		// double precision g(n), h, B(n,n), p(n)

		// Local Variables
		// int writbase;
		// int[] jpvt = new int[mx];
		double[][] C = new double[g.length][g.length];
		// double[] work = new double[mx];
		double nrm;
		int n = p.length;
		/*
		 * if (writeLevel >= 10) { writbase = writeLevel - 10; } else { writbase = writeLevel; }
		 */
		if (quasi == 1) {

			// SR1 Case:
			// // := B
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					C[i][j] = B[i][j];
				}
			}

			// Factor B (Note dchdc must use B because C has wrong
			// dimensions)
			// Cholesky decomposition of B
			// dchdc(B,n,n,work,jpvt,0,info);
			Matrix MatB = new Matrix(B);
			SingularValueDecomposition svd = new SingularValueDecomposition(MatB);
			Matrix u = svd.getU();
			Matrix s = svd.getS();
			Matrix v = svd.getV();
			// CholeskyDecomposition cd = new CholeskyDecomposition(MatB);
			// B = cd.getL().getArray();
			// if (MatB.chol().isSPD())
			// {
			// info = n;
			// }
			// else
			// {
			// info = n - 1;
			// System.out.print("Is not positive definite\n");
			// }

			// Set p = -g
			for (int i = 0; i < n; i++) {
				p[i] = -g[i];
			}

			// if (info < n)
			// {
			// // B isn't positive definite, set B = I & p = -g.
			// if (writbase > 0)
			// {
			// // write(mainout_unum,*) 'B not positive definite'
			// if (writeLevel > 10)
			// {
			// // write(6,*) 'B not positive definite'
			// }
			// }
			// eyeIF(B);
			// MatB = new Matrix(B);
			// }
			// else
			// {
			// Set p = -g and dposl (which will compute p = B^{-1} (-g) )
			// Matrix pdq = MatB.chol().solve(new Matrix(p, p.length));
			Matrix pdq = v.times(s).times(u.transpose()).times(new Matrix(p, p.length));
			p = pdq.getRowPackedCopy();
			// B := //
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					B[i][j] = C[i][j];
				}
			}

			// }

		} else if (quasi == 2 || quasi == 0) {

			// BFGS Case:
			// Compute: p = - B * g
			for (int i = 0; i < n; i++) {
				p[i] = 0.0;
				for (int j = 0; j < n; j++) {
					p[i] = p[i] - B[i][j] * g[j];
				}
			}

		}

		// Modify p so that \|p\| / n^1/2 >= 1*h.
		// Compute nrm = \|p\| / n^1/2
		nrm = 0.0;
		for (int i = 0; i < n; i++) {
			nrm = nrm + p[i] * p[i];
		}
		nrm = Math.sqrt(nrm / (n * 1.0));

		// Allows at least one point to check in linesearch
		if (nrm < 1.0 * h) {
			for (int i = 0; i < n; i++) {
				p[i] = 1.0 * h * p[i] / nrm;
			}
		}

	}

	/**
	 * ----------------------------------------------------------------- 16) restartIF This routine is called when a
	 * restart is to be done. It reinitializes h, fscale, and fmaxIF. Input variables: f maxh writeLevel Output
	 * variables (excluding globals): fscale h -----------------------------------------------------------------
	 */

	private void restartIF() {

		// Arguments
		// integer writeLevel, mainout_unum
		// double precision maxh, f, fscale, h

		// Local variables
		int writbase;
		double f_unscal;

		// Global variables
		// double precision fminIF, xminIF(mx), mepsIF, fmaxIF
		// common /globalIF2/fminIF,xminIF,fmaxIF
		// common /globalIF3/mepsIF
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(runOutputFilename, true)));

			if (writeLevel > 10) {
				writbase = writeLevel - 10;
			} else {
				writbase = writeLevel;
			}

			// Printout fminIF
			if (writbase == 1 || writbase == 2) {
				out.println("fminIF = \t" + fminIF / fscale);
				if (writeLevel > 10) {
					System.out.println("fminIF = \t" + fminIF / fscale);
				}
			} else if (writbase == 3 || writbase == 4) {
				out.println("fminIF = \t" + fminIF);
				if (writeLevel > 10) {
					System.out.println("fminIF = \t" + fminIF);
				}
			}

			// Printout fmaxIF
			if (writbase == 1 || writbase == 2) {
				out.println("fmaxIF = \t" + fmaxIF);
				if (writeLevel > 10) {
					System.out.println("fmaxIF = \t" + fmaxIF);
				} else if (writbase == 3 || writbase == 4) {
					out.println("fmaxIF = \t" + fmaxIF * fscale);
					if (writeLevel > 10) {
						System.out.println("fmaxIF = \t" + fmaxIF * fscale);
					}
				}

				// Reinitialize h
				h = maxh;

				// Unscale f
				f_unscal = f * fscale;

				// Set new fscale
				if (fmaxIF > 1.1 || fmaxIF < 1.0 - 1) {
					fscale = fmaxIF * fscale;
					// //write(6,*) 'New fscale = ',fscale;
				}

				// Rescale f
				f = f_unscal / fscale;

				// Reinitialize fmaxIF
				fmaxIF = f;
			}
		} catch (IOException e) {
		}
	}

	/**
	 * ----------------------------------------------------------------- takeminIF This routine replaces the current
	 * point with the point stored in xminIF(n) if fminIF < f_unscaled(x). If so it records the minimim value in fhist.
	 * -----------------------------------------------------------------
	 * 
	 * @param x
	 * @param f
	 * @param fhist
	 * @return f
	 */
	private double takeminIF(double[] x, double f, Double[] fhist) {
		double f_value = f;
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(runOutputFilename, true)));

			// Arguments
			// int n, mx2, writeLevel, fctr, unit, flag
			// double precision u(n), l(n), fscale, x(n), f, fhist(mx2)

			// Local variables
			int writbase;
			double[] x_value = new double[x.length];

			flag = 0;

			// Check if min f is less than unscaled f.
			if (fminIF < f * fscale) {

				flag = 1;

				// Update f,x w/ scaled values of fminIF and xminIF
				f = fminIF / fscale;
				scaleIF(xminIF, x);
				if (writeLevel > 10) {
					writbase = writeLevel - 10;
				} else {
					writbase = writeLevel;
				}

				f_value = f;
				for (int i = 0; i < x.length; i++) {
					x_value[i] = x[i];
				}

				// Unscale the results.
				if (writbase == 3 || writbase == 4) {
					f_value = f * fscale;
					unscaleIF(x, x_value);
				}

				if (writbase >= 1) {
					out.println("Minimum Taken: f = \t" + f_value);
					if (writeLevel > 10) {
						System.out.println("Minimum Taken: f = \t" + f_value);
					}
				}

				if (writbase == 2 || writbase == 4) {
					out.println("unscaled x");
					for (int i = 0; i < x_value.length; i++) {
						out.println(i + "\t" + x_value[i]);
					}
					if (this.writeLevel > 10) {
						System.out.println("unscaled x");
						for (int i = 0; i < x_value.length; i++) {
							System.out.println(i + "\t" + x_value[i]);
						}
					}
				}
				out.close();
				// Save f history
				fhist[fctr] = fminIF;
				// Increment count
				fctr++;

			}
		} catch (IOException e) {
		}
		return f;

	}

	/**
	 * ----------------------------------------------------------------- pointsIF This routine will write out to the
	 * file pointsOutputFilename. The first column will be the function evaluation count, the second will be the
	 * function value, and the next n columns will be the components of x (unscaled).
	 * -----------------------------------------------------------------
	 * 
	 * @param x_orig
	 */
	private void pointsIF(double[] x_orig) {
		// Local variables
		int n = x_orig.length;
		double[] x = new double[n];

		// unscale x_orig to x
		unscaleIF(x_orig, x);
		// Now append the data to the
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(pointsOutputFilename, true)));
			out.print("FUNCTION EVALUATION NO.\t" + nevalsIF + "\t");
			out.print("FUNCTION VALUE \t" + functionf * fscale + "\t");
			out.print("PARAMETERS\t");
			for (int i = 0; i < n; i++) {
				out.print(x[i] + "\t");
			}
			out.print("\n");
			out.close();
		} catch (IOException e) {
		}
	}

	@Override
	public double[] getBest() {
		return xminIF;
	}

	@Override
	public double getMinimum() {
		return this.fminIF;
	}

	@Override
	public boolean isRunning() {
		// 
		return false;
	}

	@Override
	public void optimize() {
		// 
		iffco_run();
	}

	@Override
	public void start() {
		// 

	}

	@Override
	public void stop() {
		// 

	}

	/**
	 * @param filename
	 */
	public void setPointsFile(String filename) {
		this.pointsOutputFilename = filename;
	}

	/**
	 * @param filename
	 */
	public void setRunFile(String filename) {
		this.runOutputFilename = filename;
	}

	/**
	 * @param minh
	 */
	public void setMinStep(double minh) {
		this.minh = minh;
	}

	/**
	 * @param maxh
	 */
	public void setMaxStep(double maxh) {
		this.maxh = maxh;
	}

	/**
	 * @return min step
	 */
	public double getMinStep() {
		return this.minh;
	}

	/**
	 * @return max step
	 */
	public double getMaxStep() {
		return this.maxh;
	}

	/**
	 * @param scale
	 */
	public void setFscale(double scale) {
		this.fscale = scale;
	}

	/**
	 * @return scale
	 */
	public double getFscale() {
		return this.fscale;
	}

	/**
	 * @param restarts
	 */
	public void setNoOfRestarts(int restarts) {
		this.restart = restarts;
	}

	/**
	 * @return number of restarts
	 */
	public int getNoOfRestarts() {
		return this.restart;
	}

	/**
	 * @param newoptions
	 */
	public void setOptions(int[] newoptions) {
		if (newoptions.length == 5) {
			this.option = newoptions;
		} else {
			System.out.println("Only 5 options\n");
		}
	}

	/**
	 * @return options
	 */
	public int[] getOptions() {
		return this.option;
	}
}
