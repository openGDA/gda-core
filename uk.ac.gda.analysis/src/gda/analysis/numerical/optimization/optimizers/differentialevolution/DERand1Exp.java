// unknown copyright

package gda.analysis.numerical.optimization.optimizers.differentialevolution;

/**
 * Perhaps the most universally applicaple strategy, but not always the fastest one. Still this is one of my favourite
 * strategies. It works especially well when the "Best"-schemes experience mis-convergence. Try e.g. F=0.7 and CR=0.5 as
 * a first guess. Authors: Mikal Keenan, Rainer Storn
 */
public class DERand1Exp extends DEStrategy {

	@Override
	public void apply(double F, double Cr, int dim, double[] x, double[] gen_best, double[][] g0) {
		prepare(dim);
		do {
			x[i] = g0[0][i] + F * (g0[1][i] - g0[2][i]);
			i = ++i % dim;
		} while ((deRandom.nextDouble() < Cr) && (++counter < dim));
	}
}
