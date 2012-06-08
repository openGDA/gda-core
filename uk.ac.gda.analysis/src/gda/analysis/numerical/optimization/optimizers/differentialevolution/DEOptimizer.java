// unknown copyright

package gda.analysis.numerical.optimization.optimizers.differentialevolution;

import gda.analysis.numerical.linefunction.IParameter;
import gda.analysis.numerical.optimization.objectivefunction.AbstractObjectiveFunction;

/**
 * D I F F E R E N T I A L E V O L U T I O N This is the kernel routine for the DE optimization. Authors: Mikal Keenan
 * Kenneth Price Rainer Storn This program implements some variants of Differential Evolution (DE) as described in part
 * in the techreport tr-95-012.ps of ICSI. You can get this report either via
 * ftp.icsi.berkeley.edu/pub/techreports/1995/tr-95-012.ps.Z or via WWW:
 * http://http.icsi.berkeley.edu/~storn/litera.html A more extended version of tr-95-012.ps has appeared in the Journal
 * of global optimization. You may use this program for any purpose, give it to any person or change it according to
 * your needs as long as you are referring to Rainer Storn and Ken Price as the origi- nators of the the DE idea.
 * Modified by Paul Quinn at Diamond Light Source Removed GUI references Removed complete method from DEProblem Added
 * optimization termination if function doesn't change Altered DEProblem evaluate method Added max no of generations
 * Changed public to private classes Changed DeProblem to AbstractCostFunction
 */
public class DEOptimizer implements Runnable {
	/* ======Public variables====================================== */
	private DERandom deRandom = new DERandom();

	private DEStrategy deStrategy = new DERand1Bin();

	/**
	 * The value below which to stop the optimization
	 */
	private double functionMinimum = 1.0E-8;

	/**
	 * If the minimum doesn't change by this much for toleranceCounter steps
	 */
	private double functionTolerance = 1.0E-8;

	/**
	 * No of counts after which, if the function doesn't change that the optimization will finish
	 */
	private int toleranceCounterMax = 200;

	/**
	 * No of counts after which, if the function doesn't change that the optimization will finish
	 */
	private int toleranceCounter = 0;

	/**
	 * Old minimum
	 */
	private double oldMinimum = Double.MAX_VALUE;

	/**
	 * Maximum no of generations (Used as a stopping criteria)
	 */
	private int maxNoOfGenerations = 5000;

	// public DEProblem deProblem; // prepare for all cost functions

	private AbstractObjectiveFunction function; // prepare for all cost

	// functions
	/**
	 * 
	 */
	private int defaultPopulationFactor = 50;

	/*----Initialize some public variables which will be accessed----*/
	/*----by the container class. The monitor panel wants them.------*/
	private int generation = 0;

	private int evaluation = 0;

	private double mincost = Double.MAX_VALUE;

	/*----These variables will be read from the panels----------------*/
	// public int dim; // this one is not taken from the panels but from
	// the problem.
	private int NP = 100;

	/**
	 * Weighting factor
	 */
	private double F = 0.5;

	/**
	 * Crossover factor
	 */
	private double Cr = 0.5;

	/* ======Protected variables====================================== */

	// int MaxD = 17; // maximum number of parameters
	// int MaxN = 500; // maximum for NP
	// int MaxR = 5; // maximum number of random choices
	private double g0[][] = null; // just some pointers (placeholders)

	private double g1[][] = null;

	private double trial[];// = new double [MaxD]; // the trial vector

	private double best[];// = new double [MaxD]; // the best vector so
	// far p

	private double genbest[];// = new double [MaxD]; // the best vector of
	// the

	// current generation
	private double cost[];// = new double [MaxN];

	private double p1[][];// = new double [MaxN][MaxD]; // array of
	// vectors

	private double p2[][];// = new double [MaxN][MaxD];

	private double rvec[][];// = new double [MaxR][MaxD]; // array of
	// randomly

	// chosen vectors
	private int rnd[];// = new int [MaxR]; // array of random indices

	private Thread action; // the optimizing thread

	private boolean console_out = false; // dummy initializer. The true

	// initialization takes place
	// in PlotScreenPanel
	/**
	 * A store of the starting point
	 */
	private double[] lowerBounds = null;

	/**
	 * A store of the starting point
	 */
	private double[] upperBounds = null;

	/**
	 * Constructor.
	 * 
	 * @param function
	 */
	public DEOptimizer(AbstractObjectiveFunction function) {
		this.function = function;
		reset();
	}

	/**
	 * Reads the parameters from the function setting the upper and lower limits etc.... Will make the function
	 * observable at a later date to do away with this....
	 */
	public void reset() {
		IParameter[] params = function.getParameters();
		/*
		 * 8 AbstractCompositeFunction func = function.getCompositeFunction(); int nFunctions = func.getNoOfFunctions(); //
		 * I'm basically findingout which parameters are free Vector<IParameter> params = new Vector<IParameter>();
		 * for (int i = 0; i < nFunctions; i++) { int nParams = func.getFunction(i).getNoOfParameters(); for (int j = 0;
		 * j < nParams; j++) { if (!func.getFunction(i).getParameter(j).isFixed()) {
		 * params.add(func.getFunction(i).getParameter(j)); } } }
		 */
		// Now I have a list of free parameters....
		// set the relevant values for the codes
		int noOfParameters = params.length;
		int noOfFreeParameters = 0;
		for (int i = 0; i < noOfParameters; i++) {
			if (!params[i].isFixed()) {
				noOfFreeParameters++;
			}
		}
		// Population size
		this.NP = noOfParameters * defaultPopulationFactor;
		lowerBounds = new double[noOfFreeParameters];
		upperBounds = new double[noOfFreeParameters];
		int count = 0;
		for (int i = 0; i < params.length; i++) {
			if (!params[i].isFixed()) {
				lowerBounds[count] = params[i].getLowerLimit();
				upperBounds[count] = params[i].getUpperLimit();
				count++;
			}
		}
		// Set up the working arrays
		trial = new double[noOfFreeParameters]; // the trial vector
		genbest = new double[noOfFreeParameters]; // the best vector of
		// the
		// current
		// generation
		best = new double[noOfFreeParameters]; // the best vector of the
		// current
		// generation
		cost = new double[NP];
		p1 = new double[NP][noOfFreeParameters]; // array of vectors
		p2 = new double[NP][noOfFreeParameters];
		rvec = new double[6][noOfFreeParameters]; // array of randomly
		// chosen
		// vectors
		rnd = new int[6]; // array of random indices
		// Set the strategy to uses
		deStrategy.init(new DERandom());
	}

	/**
	 * Assign array from to
	 * 
	 * @param to
	 * @param from
	 */
	private void assign(double[] to, double[] from) {
		System.arraycopy(from, 0, to, 0, from.length);
	}

	/**
	 * The central component which actually does the DE optimization.
	 * 
	 * @return result
	 */
	public double runDE() {

		int min_index = 0;

		/*---Fetch control variables------------------------*/
		// int dim = deProblem.getLength(); // get size of the problem
		int dim = lowerBounds.length;// deProblem.getLength(); // get size of
		// the
		// problem

		/*--------Optimize----------------------------------*/
		if (evaluation > 0) {// if the initializing evaluation has been done
			// int iterations = Refresh; // how many iterations for this
			// call
			// (defined in scroll panel)
			// while (iterations-- > 0)
			// {
			for (int i = 0; i < NP; i++) {
				assign(trial, g0[i]); // trial vector

				do
					rnd[0] = deRandom.nextValue(NP); // BUG:: Changed next()
				// to
				// nextValue
				while (rnd[0] == i);

				do
					rnd[1] = deRandom.nextValue(NP);// BUG:: Changed next()
				// to
				// nextValue
				while ((rnd[1] == i) || (rnd[1] == rnd[0]));

				do
					rnd[2] = deRandom.nextValue(NP);// BUG:: Changed next()
				// to
				// nextValue
				while ((rnd[2] == i) || (rnd[2] == rnd[1]) || (rnd[2] == rnd[0]));

				do
					rnd[3] = deRandom.nextValue(NP);// BUG:: Changed next()
				// to
				// nextValue
				while ((rnd[3] == i) || (rnd[3] == rnd[2]) || (rnd[3] == rnd[1]) || (rnd[3] == rnd[0]));

				do
					rnd[4] = deRandom.nextValue(NP);// BUG:: Changed next()
				// to
				// nextValue
				while ((rnd[4] == i) || (rnd[4] == rnd[3]) || (rnd[4] == rnd[2]) || (rnd[4] == rnd[1])
						|| (rnd[4] == rnd[0]));

				do
					rnd[5] = deRandom.nextValue(NP);// BUG:: Changed next()
				// to
				// nextValue
				while ((rnd[5] == i) || (rnd[5] == rnd[4]) || (rnd[5] == rnd[3]) || (rnd[5] == rnd[2])
						|| (rnd[5] == rnd[1]) || (rnd[5] == rnd[0]));

				for (int k = 0; k < 6; k++) // select the random vectors
				{
					rvec[k] = g0[rnd[k]];
				}

				/*---Apply the DE strategy of choice-------------------*/
				deStrategy.apply(F, Cr, dim, trial, genbest, rvec);
				for (int tp = 0; tp < trial.length; tp++) {
					if (trial[tp] < lowerBounds[tp])
						trial[tp] = lowerBounds[tp];
					if (trial[tp] > upperBounds[tp])
						trial[tp] = upperBounds[tp];
				}

				/*---cost of trial vector------------------------*/
				double testcost = function.evaluate(trial);
				evaluation++;

				if (testcost <= cost[i]) // Better solution than target
				// vectors
				// cost
				{
					assign(g1[i], trial); // if yes put trial vector in
					// new
					// population
					cost[i] = testcost; // and save the new cost value
					if (testcost < mincost) // if testcost is best ever
					{
						mincost = testcost; // new mincost
						assign(best, trial); // best vector is trial vector
						min_index = i; // save index of best vector
					}
				} else
				// if trial vector is worse than target vector
				{
					assign(g1[i], g0[i]); // Propagate the old
				}
			}// end for (int i = 0; i < NP; i++)

			assign(genbest, best); // Save current generation's best

			double gx[][] = g0; // Swap population pointers
			g0 = g1;
			g1 = gx;

			generation++;
		}// loop
		// }// end while (iterations-- > 0)
		/*--------Initialize----------------------------------*/
		else {
			for (int i = 0; i < NP; i++) {
				double[] x = p1[i];
				int j = dim;
				for (j = 0; j < dim; j++) {
					x[j] = deRandom.nextValue(lowerBounds[j], upperBounds[j]);
				}
				cost[i] = function.evaluate(x);
				evaluation++;
			}

			mincost = cost[0];
			min_index = 0;
			for (int j = 0; j < NP; j++) {
				double x = cost[j];
				if (x < mincost) {
					mincost = x;
					min_index = j;
				}
			}

			assign(best, p1[min_index]);
			assign(genbest, best);

			g0 = p1; // generation t
			g1 = p2; // generation t+1
		}// end else (end of initialization)

		if (console_out == true) {
			for (int i = 0; i < dim; i++) {
				System.out.println("best[" + i + "] = " + best[i]);
			}
			System.out.println(" ");
		}

		// assign (deProblem.getBest(), best);

		return mincost;
	}

	/**
	 * The "Init()" method for the thread.
	 */
	public void start() {
		if (console_out)
			System.out.println("DE Optimizer started");

		if (action == null) // if thread is not running
		{
			action = uk.ac.gda.util.ThreadManager.getThread(this); // Instantiate the new thread
			action.start(); // Start it
		}
	}

	/**
	 * Stop the thread when you leave the application.
	 */
	public void stop() {
		action = null;
	}

	/**
	 * 
	 */
	public void optimize() {
		while (true) {
			runDE();
			/*--Check if optimization is completed.-*/
			if (completed()) {
				System.out.println("completed....");
				break;
			}
		}

	}

	/**
	 * The main method for the thread
	 */
	@Override
	public void run()
	// Let it run! The optimization is taking place here!;
	{
		while (action != null) // as long as there is an active thread
		{
			runDE();
			/*--Check if optimization is completed.-*/
			if (completed()) {
				System.out.println("completed....");
				this.stop();
			}
		}
	}

	/**
	 * Set a strategy See startegy code or documentation for details 0 : Best1Bin 1 : Best1Exp 2 : Best2Bin 3 : Best2Exp
	 * 4 : Best3Bin 5 : Current2Rand 6 : Rand1Bin 7 : Rand1Exp 8 : Rand2Bin 9 : Rand2Exp 10 : RandRandBin 11 :
	 * RandToBest1Bin 12 : RandToBest1BinExp default : 6
	 * 
	 * @param index
	 */
	public void setStrategy(int index) {
		switch (index) {
		case 0:
			deStrategy = new DEBest1Bin();
			break;
		case 1:
			deStrategy = new DEBest1Exp();
			break;
		case 2:
			deStrategy = new DEBest2Bin();
			break;
		case 3:
			deStrategy = new DEBest2Exp();
			break;
		case 4:
			deStrategy = new DEBest3Bin();
			break;
		case 5:
			deStrategy = new DECurrent2Rand();
			break;
		case 6:
			deStrategy = new DERand1Bin();
			break;
		case 7:
			deStrategy = new DERand1Exp();
			break;
		case 8:
			deStrategy = new DERand2Bin();
			break;
		case 9:
			deStrategy = new DERand2Exp();
			break;
		case 10:
			deStrategy = new DERandRandBin();
			break;
		case 11:
			deStrategy = new DERandToBest1Bin();
			break;
		case 12:
			deStrategy = new DERandToBest1Exp();
			break;
		default:
			deStrategy = new DERand1Bin();
			break;
		}
	}

	/**
	 * Set the strategy
	 * 
	 * @param deStrategy
	 */
	public void setStrategy(DEStrategy deStrategy) {
		if (deStrategy != null)
			this.deStrategy = deStrategy;
	}

	// Some helper methods

	/* ====Some methods for the observers to get their input.===== */
	/**
	 * @return Which iteration of the optimization ?
	 */
	public int getGeneration() {
		return generation;
	}

	/**
	 * @return How many evaluations have been done so far ?
	 */
	public int getEvaluation() {
		return evaluation;
	}

	/**
	 * @return The best cost value so far
	 */
	public double getMinimum() {
		return mincost;
	}

	/**
	 * @return Best parameters
	 */
	public final double[] getBest() {
		return best;
	}

	/**
	 * Enable console output trace of optimization parameters.
	 */
	public void consoleEnable() {
		console_out = true;
	}

	/**
	 * Enable console output trace of optimization parameters.
	 */
	public void consoleDisable() {
		console_out = false;
	}

	/**
	 * Allows to update the trial vector from outside.
	 * 
	 * @param x
	 */
	public void updateTrialVector(double x[]) {
		for (int i = 0; i < x.length; i++) {
			trial[i] = x[i];
		}
	}

	/**
	 * Determines if optimization should be terminated
	 * 
	 * @return true if optimzation completed
	 */

	private boolean completed() {
		// Determine change in function minimum
		double deltaMin = Math.abs(mincost - oldMinimum);
		// Update old minimum
		oldMinimum = mincost;

		// Is the change smaller than functionTolerance
		// If so count how many times this occurs
		if (deltaMin <= functionTolerance)
			toleranceCounter++;
		else
			toleranceCounter = 0;
		if (toleranceCounter == toleranceCounterMax) {
			System.out.println("Minimum hasn't changed in\t" + toleranceCounterMax + "\tgenerations");
			return true;
		}
		if (mincost <= functionMinimum)
			return true;
		if (this.generation > this.maxNoOfGenerations)
			return true;
		return false;
	}

	/**
	 * Set the population size Default = 10*noOfParamters
	 * 
	 * @param popsize
	 */
	public void setPopulationSize(int popsize) {
		this.NP = popsize;
	}

	/**
	 * @return The population size
	 */
	public int getPopulationSize() {
		return NP;
	}

	/**
	 * Set the weighting factor F Default : 0.8
	 * 
	 * @param F
	 */
	public void setWeightingFactor(double F) {
		if (F < 0.0 || F > 1.0)
			throw new IllegalArgumentException("The weighting factor, F, must be between 0 and 1");
		this.F = F;
	}

	/**
	 * @return The weighting factor F
	 */
	public double getWeightingFactor() {
		return F;
	}

	/**
	 * Set the crossover factor, Cr Default : 0.9
	 * 
	 * @param Cr
	 */
	public void setCrossoverFactor(double Cr) {
		if (Cr < 0.0 || Cr > 1.0)
			throw new IllegalArgumentException("The crossover factor, CR, must be between 0 and 1");
		this.Cr = Cr;
	}

	/**
	 * @return The crossover factor, Cr
	 */
	public double getCrossoverFactor() {
		return Cr;
	}

	/**
	 * @return Is the thread running or not
	 */
	public boolean isRunning() {
		return (action != null);
	}
}