// unknown copyright

package gda.analysis.numerical.optimization.optimizers.differentialevolution;

/**
 * Strangely enough this strategy is often not very successful. Authors: Mikal Keenan, Rainer Storn
 */
public class DERand2Exp extends DEStrategy {

	@Override
	public void apply(double F, double Cr, int dim, double[] x, double[] gen_best, double[][] g0) {
		prepare(dim);
		do {
			x[i] = g0[0][i] + F * (g0[1][i] + g0[2][i] - g0[3][i] - g0[4][i]);
			i = ++i % dim;
		} while ((deRandom.nextDouble() < Cr) && (++counter < dim));
	}
}
