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

import gda.util.CrystalParameters.CrystalMaterial;

/**
 * Utility methods for the I20 XES spectrometer
 */
public class XesUtils {

	public static final double MIN_THETA = 60;
	public static final double MAX_THETA = 86;

	/**
	 * Given a Bragg angle and the Crystal properties, returns the Energy of the reflected X-rays from the crystal.
	 *
	 * @param theta
	 *            - degrees - the Bragg angle
	 * @param crystallCut
	 *            - int[] {1..3, 1..3, 1..3}
	 * @return energy in eV
	 */
	public static double getFluoEnergy(final double theta, final CrystalMaterial material, final int[] crystallCut) {

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
	public static double getBragg(final double requiredFluoEnergy, final CrystalMaterial material, final int[] crystallCut) {

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

	public static double getP(double R, double bragg, double az) {
		double sinTheta = Math.sin(Math.toRadians(bragg));
		return Math.sqrt( Math.pow(R*sinTheta*sinTheta, 2.0) - az*az);
	}

	/**
	 * Calculate ax, ay, pitch and yaw for given Rowland circle radius (R), bragg angle and offset from the central analyser (az)
	 * Input and output angles are all in degrees.
	 *
	 * @param R
	 * @param bragg
	 * @param az
	 * @return array of analyser parameter [ax, ay, yaw, pitch]
	 */
	public static double[] getAnalyserValues(double R, double bragg, double az) {
		double p = getP(R, bragg, az);
		double braggRad = Math.toRadians(bragg);
		double sinTheta = Math.sin(braggRad);
		double cosTheta = Math.cos(braggRad);
		double ax = R * sinTheta*cosTheta*cosTheta + p*sinTheta;
		double ay = R * cosTheta*sinTheta*sinTheta - p*cosTheta;

		double pitch = 0.5*Math.PI - Math.atan(Math.sqrt(az*az + p*p*sinTheta*sinTheta)/(p*cosTheta));
		double yaw = Math.atan(az/(p*sinTheta));
		return new double[] {ax, ay, Math.toDegrees(yaw), Math.toDegrees(pitch) };
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
}
