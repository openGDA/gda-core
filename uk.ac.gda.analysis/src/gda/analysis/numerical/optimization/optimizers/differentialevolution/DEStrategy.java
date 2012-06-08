// unknown copyright

package gda.analysis.numerical.optimization.optimizers.differentialevolution;

/**
 * Parent class for all the different DE-strategies you can choose. Authors: Mikal Keenan, Rainer Storn
 */
public abstract class DEStrategy {
	protected DERandom deRandom;

	protected int i, counter;

	/**
	 * @param F
	 * @param Cr
	 * @param dim
	 * @param x
	 * @param gen_best
	 * @param g0
	 */
	abstract public void apply(double F, double Cr, int dim, double[] x, double[] gen_best, double[][] g0);

	/*******************************************************************************************************************
	 * * Contains the actual strategy which alters your vectors.**
	 * 
	 * @param deRnd
	 ******************************************************************************************************************/

	public void init(DERandom deRnd)
	/*******************************************************************************************************************
	 * * Link to the random number generator. **
	 ******************************************************************************************************************/
	{
		deRandom = deRnd;
	}

	protected final void prepare(int dim)
	/*******************************************************************************************************************
	 * * Fetch a random number ex [0,dim]. **
	 ******************************************************************************************************************/
	{
		i = deRandom.nextValue(dim);
		counter = 0;
	}
}
