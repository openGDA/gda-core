/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package gda.analysis.numerical.linefunction;

/**
 * A general polynomial of order n
 */
public class Polynomial extends AbstractFunction {
	static String alphabet = "abcdefghijklmnopqrstuvwxyz";

	/**
	 * Initial values for a polynomial This also create Parameter objects for each value. The Parameter objects can
	 * contain upper and lower bounding info
	 * 
	 * @param parms
	 */
	public Polynomial(double... parms) {
		super(getParameterNames(parms));
		// 2 parameters .. i.e. you specify them all
		if (parms.length < 2) {
			throw new IllegalArgumentException("Should be at least 2 parameters");
		}
		for (int i = 0; i < parms.length; i++) {
			this.getParameter(i).setValue(parms[i]);
		}
	}

	private static String[] getParameterNames(double... parms) {
		int length = parms.length;
		String[] names = new String[length];
		for (int i = 0; i < length; i++) {
			names[i] = alphabet.substring(i, i + 1);
		}
		return names;
	}

	/**
	 * Create a polynomial with a given order The parameters of the polynomial are chosen randomly
	 * 
	 * @param order
	 */
	public Polynomial(int order) {
		super(getParameterNames(new double[order]));
		for (int i = 0; i < order; i++) {
			getParameter(i).setValue((Math.random() - 0.5) * 5.0);
		}

	}

	/**
	 * Create a Polynomial with random starting positions, but with a variance defining the boundrys of the parameters,
	 * this variance decreases with the order of the polyunomial, as larger powers require smaller factors
	 * 
	 * @param order
	 *            The Order of the Polynomial
	 * @param Variance
	 *            The ammount the parameter can deviate from zero
	 */
	public Polynomial(int order, double Variance) {
		super(getParameterNames(new double[order]));
		for (int i = 0; i < order; i++) {
			// getParameter(i).setValue(
			// (Math.random() - 0.5) * ((Variance / (i + 1)) * 1.9));
			// decided to get rid of the above, as it made the curve too
			// unstable to begin
			getParameter(i).setValue(0.0);
			getParameter(i).setLowerLimit(-Variance / (i + 1));
			getParameter(i).setUpperLimit(Variance / (i + 1));
		}

	}

	/**
	 * Horners method for evaluating a polynomial
	 * 
	 * @param x
	 *            positions
	 * @return The value of the polynomial at positions x
	 */
	@Override
	public double val(double... x) {
		// an nth order polynomial has n+1 coefficients
		// stored in c[0] through c[n]
		// y = c[0] + c[1]*x + c[2]*x^2 +...+ c[n]*x^n
		int n = getNoOfParameters();
		double y = getParameter(n - 1).getValue();
		for (int i = n - 2; i >= 0; i--)
			y = y * x[0] + getParameter(i).getValue();
		return y;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Polynomial p = new Polynomial(0.1, 0.0001);
		System.out.println(p.val(1.0));
	}

	@Override
	public String toString() {
		String Out = "Polynomial parameters are\n";
		for (int i = 0; i < getNoOfParameters(); i++) {
			Out = Out + i + " " + getParameter(i).getValue() + " [" + getParameter(i).getLowerLimit() + ","
					+ getParameter(i).getUpperLimit() + "]\n";
		}
		return Out;
	}

}
