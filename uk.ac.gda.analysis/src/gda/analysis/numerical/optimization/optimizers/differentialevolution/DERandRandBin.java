// unknown copyright

package gda.analysis.numerical.optimization.optimizers.differentialevolution;

/**
 * An interesting strategy that often works well. Authors: Mikal Keenan, Rainer Storn
 */
public class DERandRandBin extends DEStrategy {
	protected double z;

	protected int j;

	@Override
	public void apply(double F, double Cr, int dim, double[] x, double[] gen_best, double[][] g0) {
		prepare(dim); // random i ex [0,dim-1], counter=0
		z = 0; // z is N(0,1) distributed gaussian variable
		for (j = 0; j < 12; j++) {
			z = z + deRandom.nextDouble();
		}
		z = z - 6;

		for (counter = 0; counter < dim; counter++) {
			if (deRandom.nextDouble() < Cr) {
				x[i] = g0[0][i] + z * (g0[1][i] - g0[2][i]);
			} else {
				// x[i] = x[i] + z*(g0[1][i] - g0[2][i]);
			}

			i = (i++) % dim;
		}
	}
}
