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

package gda.exafs.xes;

/**
 * Utility methods for the I20 XES spectrometer
 */
public class XesUtils {

	public enum XesMaterial {

		// the lattice parameters
		SILICON(5.431), GERMANIUM(5.658);

		private final double a;

		XesMaterial(double a) {
			this.a = a;
		}

		public double getA() {
			return a;
		}
	}

	public static final double MIN_THETA = 60;
	public static final double MAX_THETA = 85;

	/**
	 * Given a Bragg angle and the Crystal properties, returns the Energy of the reflected X-rays from the crystal.
	 * 
	 * @param theta
	 *            - degrees - the Bragg angle
	 * @param crystallCut
	 *            - int[] {1..3, 1..3, 1..3}
	 * @return energy in eV
	 */
	public static double getFluoEnergy(final double theta, final XesMaterial material, final int[] crystallCut) {

		final double sum = Math.pow(crystallCut[0], 2d) + Math.pow(crystallCut[1], 2d) + Math.pow(crystallCut[2], 2d);
		final double root = 6.1993 * Math.pow(sum, 0.5);
		final double rad = Math.toRadians(theta);
		final double sin = Math.sin(rad);
		return (root / (material.getA() * sin)) * 1000d;
	}

	/**
	 * Returns the Bragg angle required to view X-rays of the given energy on the given crystal
	 * 
	 * @param requiredFluoEnergy
	 *            - eV
	 * @param crystallCut
	 *            - int[] {1..3, 1..3, 1..3}
	 * @return theta - degrees
	 */
	public static double getBragg(final double requiredFluoEnergy, final XesMaterial material, final int[] crystallCut) {

		final double sum = Math.pow(crystallCut[0], 2d) + Math.pow(crystallCut[1], 2) + Math.pow(crystallCut[2], 2d);
		final double root = 6.1993 * Math.pow(sum, 0.5d);
		final double angle = root / (material.getA() * (requiredFluoEnergy / 1000d));
		final double rad = Math.asin(angle);
		return Math.toDegrees(rad);
	}

	/**
	 * Linear distance of the analyser crystal from the sample to achieve XES conditions for the given Bragg angle in
	 * the given Rowland circle
	 * 
	 * @param R
	 *            - units mm
	 * @param bragg
	 *            - degrees
	 * @return L - units as r
	 */
	public static double getL(final double R, final double bragg) {
		return R * Math.sin(Math.toRadians(bragg));
	}

	/**
	 * Rotation of the analyser crystal to achieve XES conditions
	 * 
	 * @param bragg
	 *            - degrees
	 * @return xtalRotation - degrees
	 */
	public static double getCrystalRotation(final double bragg) {
		return 90 - bragg;
	}

	/**
	 * The Bragg angle based on the position of the Crystal rotation motor.
	 * 
	 * @param theta
	 *            - degrees
	 * @return xtalRotation - degrees
	 */
	public static double getBraggFromCrystalPosition(final double theta) {
		return 90 - theta;
	}

	/**
	 * @param R
	 *            - the Rowland radius
	 * @param bragg
	 *            - the Bragg angle
	 * @param ay
	 *            - the horizonal (perpendicular to the Rowland circle) diaplacement of the crystal from the central
	 *            one.
	 * @return double[] - [ax (displacement towards sample), az (vertical displacement), tilt (towards detector),
	 *         rotation (in Rowland circle plane)]
	 */
	public static double[] getAdditionalCrystalPositions(final double R, final double bragg, final double ay) {
		// calculations and variable names based on document
		double braggRadians = Math.toRadians(bragg);
		double dx = R * Math.sin(braggRadians) * (1 + Math.cos(2 * braggRadians));
		double dz = R * Math.sin(braggRadians) * Math.sin(2 * braggRadians);
		double a = 1 + ((dx * dx) / (dz * dz));

		double D = 2 * R * Math.cos(braggRadians) * Math.sin(braggRadians);
		double b = -(D * D * dx) / (dz * dz);

		double L = R * Math.sin(braggRadians);
		double c = -(Math.pow(L, 2) - Math.pow(ay, 2) - (Math.pow(D, 4) / (4 * dz * dz)));

		double ax = (-b + Math.sqrt((b * b) - 4 * a * c)) / (2 * a);
		double az = (Math.pow(D, 2) / 2 - (ax * dx)) / dz;

		// double rsin2thetaSquared = Math.pow((R * Math.sin(braggRadians) * Math.sin(braggRadians)),2);
		// double DAsquared = Math.pow((L - ax), 2) + Math.pow(ay, 2) + Math.pow(az, 2);
		// double tworsin2thetaSquared = 2 * rsin2thetaSquared;
		// double tilt = -Math.acos((DAsquared - tworsin2thetaSquared) / tworsin2thetaSquared);
		// tilt = Math.toDegrees(tilt);
		// if (tilt < 0){
		// tilt = 180+tilt;
		// }
		// tilt *= -1;

		// double rotation = getCrystalRotation(bragg);
		// return new double[]{ax,az,tilt,rotation};

		// double R = radius;

		double targetL = XesUtils.getL(R, bragg);
		ax = (targetL - ax) * -1;

		double braggRad = Math.toRadians(bragg);

		double sin_bragg = Math.sin(braggRad);
		double p1 = R * R * Math.pow(sin_bragg, 4) - (ay * ay);

		double p = Math.sqrt(Math.abs(p1));

		double tilt = Math.toDegrees(Math.atan(ay / (p * sin_bragg)));

		double topLine = (Math.sqrt((ay * ay) + (p * p) * Math.pow(sin_bragg, 2)));
		double bottomLine = (p * Math.cos(braggRad));

		double pitch = 90 - Math.toDegrees((Math.atan(topLine / bottomLine)));

		return new double[] { ax, az, tilt, pitch };
	}

	/**
	 * Horizontal displacement of the detector to achieve XES conditions
	 * 
	 * @param R
	 *            - mm
	 * @param theta
	 * @return dx - mm
	 */
	public static double getDx(final double R, final double theta) {
		return R * Math.sin(Math.toRadians(theta)) * (1 + Math.cos(2d * Math.toRadians(theta)));
	}

	/**
	 * Vertical displacement of the detector to achieve XES conditions
	 * 
	 * @param R
	 * @param theta
	 * @return dy - mm
	 */
	public static double getDy(final double R, final double theta) {
		return R * Math.sin(Math.toRadians(theta)) * Math.sin(2d * Math.toRadians(theta));
	}

	/**
	 * Should move to testing...
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		System.out.println("Theta 82 degress, Si-(1,1,1) ENERGY = "
				+ XesUtils.getFluoEnergy(82, XesMaterial.SILICON, new int[] { 1, 1, 1 }) + " eV");
		System.out.println("Energy 1996.5eV, Si-(1,1,1) THETA = "
				+ XesUtils.getBragg(1996.5, XesMaterial.SILICON, new int[] { 1, 1, 1 }));
		System.out.println("***");
		System.out.println("Theta 82 degress, Si-(1,1,2) ENERGY = "
				+ XesUtils.getFluoEnergy(82, XesMaterial.SILICON, new int[] { 1, 1, 2 }) + " eV");
		System.out.println("Energy 2823.5eV, Si-(1,1,2) THETA = "
				+ XesUtils.getBragg(2823.5, XesMaterial.SILICON, new int[] { 1, 1, 2 }));
		System.out.println("***");
		System.out.println("R 1000mm, Theta 82 L = " + XesUtils.getL(1000, 82));
		System.out.println("R 1000mm, Theta 82 dx = " + XesUtils.getDx(1000, 82));
		System.out.println("R 1000mm, Theta 82 dy = " + XesUtils.getDy(1000, 82));
		System.out.println("R 1000mm, Theta 82 xtal rotation = " + XesUtils.getCrystalRotation(82));
		System.out.println("R 1000mm, Theta 82 detector rotation = 82 (the Bragg angle)");
		System.out.println("***");

		double[] bragg85 = getAdditionalCrystalPositions(1000, 82, 137);
		System.out.println("For R 1000mm, crystal with ay 137mm:");
		System.out.println("Theta 82 ax=" + bragg85[0] + " az=" + bragg85[1] + " tilt=" + bragg85[2]);
	}
}
