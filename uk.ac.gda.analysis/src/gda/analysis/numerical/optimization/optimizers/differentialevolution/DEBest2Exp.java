// unknown copyright

package gda.analysis.numerical.optimization.optimizers.differentialevolution;

/**
 * One of the best strategies. F=0.5 is often appropriate. Authors: Mikal Keenan, Rainer Storn
 */
public class DEBest2Exp extends DEStrategy {

	@Override
	public void apply(double F, double Cr, int dim, double[] x, double[] gen_best, double[][] g0) {
		prepare(dim);
		do {
			x[i] = gen_best[i] + F * (g0[0][i] + g0[1][i] - g0[2][i] - g0[3][i]);
			i = ++i % dim;
		} while ((deRandom.nextDouble() < Cr) && (++counter < dim));
	}
}
