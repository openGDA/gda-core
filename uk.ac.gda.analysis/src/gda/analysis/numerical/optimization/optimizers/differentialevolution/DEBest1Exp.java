// unknown copyright

package gda.analysis.numerical.optimization.optimizers.differentialevolution;

/**
 * Ken's classic strategy. However, we have found several optimization problems where misconvergence occurs. Authors:
 * Mikal Keenan, Rainer Storn
 */
public class DEBest1Exp extends DEStrategy {

	@Override
	public void apply(double F, double Cr, int dim, double[] x, double[] gen_best, double[][] g0) {
		prepare(dim);
		do {
			x[i] = gen_best[i] + F * (g0[0][i] - g0[1][i]);
			i = ++i % dim;
		} while ((deRandom.nextDouble() < Cr) && (++counter < dim));
	}
}
