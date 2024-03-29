// unknown copyright

package gda.analysis.numerical.optimization.optimizers.differentialevolution;

/**
 * Another experimental strategy. Due to the ever more gaussian-like distribution this strategy is very greedy and gets
 * trapped easily in a local minimum. Authors: Rainer Storn
 */
public class DEBest3Bin extends DEStrategy {

	@Override
	public void apply(double F, double Cr, int dim, double[] x, double[] gen_best, double[][] g0) {
		prepare(dim);
		while (counter++ < dim) {
			if ((deRandom.nextDouble() < Cr) || (counter == dim))
				x[i] = gen_best[i] + F * (g0[0][i] + g0[1][i] + g0[2][i] - g0[3][i] - g0[4][i] - g0[5][i]);
			i = ++i % dim;
		}
	}
}
