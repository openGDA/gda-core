/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.org.myls.scannable;

import gda.device.Scannable;

/**
 *
 */
public class ScannableClassGenerator {

	/**
	 *
	 */
	public ScannableClassGenerator() {
	}

	/**
	 * @param g
	 * @return scannableGaussian
	 */
	public static Scannable generateScannableGaussian(Gaussian g) {
//		ScannableGaussian sg = new ScannableGaussian("test_gaussian", g
//				.getCentre(), "x", "y", g.getCentre(), g.getWidth(), g
//				.getHeight(), g.getNoise(), 3, "%.4f", "%.4f", "mm", "counts");
		ScannableGaussian sg = new ScannableGaussian(g);
		return sg;
	}

	/**
	 * @return scannableGaussian
	 */
	public static Scannable generateScannableGaussian() {
		Gaussian g = new Gaussian(0, 1, 1, 0);
		return generateScannableGaussian(g);
	}

	/**
	 * @param sw
	 * @return scannableSine
	 */
	public static Scannable generateScannableSine(SineWave sw) {
		ScannableSine ss = new ScannableSine(sw);
		System.out.println("ss: " + ss);
		return ss;
	}

	/**
	 * @return scannableSine
	 */
	public static Scannable generateScannableSine() {
		System.out.println("generateScannableSine()");
		SineWave sw = new SineWave(1, 0, 1, 0, 0);
		System.out.println("sw: " + sw);
		return new ScannableSine(sw);
		// return new ScannableSine(sw);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}

}
