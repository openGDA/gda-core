// unknown copyright

package gda.analysis.numerical.optimization.optimizers.differentialevolution;

import java.util.Random;

/**
 * Random number generator. Certainly not the best one around. So if you are not satisfied, implement your own one.
 * Authors: Mikal Keenan, Rainer Storn
 */
public class DERandom extends Random
/***********************************************************************************************************************
 * * ** * ** * **
 **********************************************************************************************************************/
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor initializes the generator.
	 */
	public DERandom() {
		setMySeed(0);
	}

	/**
	 * Random initialization. Hence your optimization results may differ from run to run.
	 * 
	 * @param seed
	 */
	public void setMySeed(long seed) {
		if (seed == 0)
			seed = System.currentTimeMillis();
		setSeed(seed);
	}

	/**
	 * @param max
	 * @return Fetch the next integer random number ex [0,max].
	 */
	public final int nextValue(int max) // BUG:: infinite loop if this method is
	// called next
	{
		return (int) (nextDouble() * max);
	}

	/**
	 * @param range
	 * @return Fetch the next double random number ex [-range,+range].
	 */
	public final double nextValue(double range) // BUG:: infinite loop if this
	// method is called next
	{
		return range * (1.0 - 2.0 * nextDouble());
	}

	/**
	 * @param lowerRange
	 * @param upperRange
	 * @return Fetch the next double random number ex [-range,+range].
	 */
	public final double nextValue(double lowerRange, double upperRange) // BUG::
	// infinite
	// loop
	// if
	// this
	// method
	// is
	// called
	// next
	{
		return lowerRange + ((upperRange - lowerRange) * nextDouble());
	}

}
