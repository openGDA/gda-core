// unknown copyright

package gda.analysis.numerical.optimization.optimizers.differentialevolution;

/**
 * This strategy seems to be one of the best strategies. Try F=0.85 and CR=1. If you get misconvergence try to increase
 * NP. If this doesn't help you should play around with all three control variables. Authors: Mikal Keenan, Rainer Storn
 */
public class DERandToBest1Exp extends DEStrategy {

	@Override
	public void apply(double F, double Cr, int dim, double[] x, double[] gen_best, double[][] g0) {
		prepare(dim);
		do {
			x[i] += F * ((gen_best[i] - x[i]) + (g0[0][i] - g0[1][i]));
			i = ++i % dim;
		} while ((deRandom.nextDouble() < Cr) && (++counter < dim));
	}
}
