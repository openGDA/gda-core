/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.exafs.scan;

import gda.util.Element;

/**
 * Utility class to help calculate energies of scan regions in an Exafs scan
 */
public class ExafsScanRegionCalculator {

	/**
	 * Returns the three energies to be used to define the inner two scan regions in an Exafs scan
	 * 
	 * @param elementName
	 * @param edgeName
	 * @param edgeEnergy
	 *            - if null then this will be calculated from the element and edge names
	 * @param arg1
	 *            - if isAB is true this should be the A value, else should be Gaf1
	 * @param arg2
	 *            - if isAB is true this should be the B value, else should be Gaf2
	 * @param isAB
	 *            - true if defining energies byAB rather than Gaf values
	 * @return Double[3]
	 * @throws Exception
	 */
	public static Double[] calculateABC(String elementName, String edgeName, Double edgeEnergy, Double arg1, Double arg2, Double arg3, boolean isAB) throws Exception {
		Double[] abc = new Double[3];
		final Element element = Element.getElement(elementName);

		if (element == null)
			throw new Exception("Element " + element + " not found");

		if (edgeEnergy == null)
			edgeEnergy = element.getEdgeEnergy(edgeName);

		if (!isAB) {
			Double coreHole = element.getCoreHole(edgeName);
			abc[0] = edgeEnergy - (arg1 * coreHole);
			abc[1] = edgeEnergy - (arg2 * coreHole);
			if (arg3 != null)
				abc[2] = edgeEnergy + (arg3 * coreHole);
			else
				abc[2] = edgeEnergy + (arg2 * coreHole);
		} else {
			abc[0] = arg1;
			abc[1] = arg2;
			if (arg3 != null)
				abc[2] = arg3;
			else
				abc[2] = edgeEnergy + (edgeEnergy - arg2);
		}
		return abc;
	}

	/**
	 * Calculates the step sizes during the variable step (A->B) region in an Exafs scan
	 * 
	 * @param aEnergy
	 * @param bEnergy
	 * @param preEdgeStep
	 * @param edgeStep
	 * @return double[]
	 */
	public static double[] calculateVariableStepRegion(final Double aEnergy, 
			                                           final Double bEnergy, 
			                                           final Double preEdgeStep,
			                                           final Double edgeStep) throws ExafsScanPointCreatorException {
		double ds = edgeStep - preEdgeStep;
		double de = bEnergy - aEnergy;
		double di = (edgeStep + preEdgeStep) / 2d;
		if (de > di) {
			double rn = 2d * de / (edgeStep + preEdgeStep);
			double dn = (int) rn + 1;
			if (dn >= 2d) {
				double dh = de - preEdgeStep * dn;
				double aa = (3d * dh / Math.pow(dn, 2d)) - (ds / dn);
				double bb = (-2d * dh / Math.pow(dn, 3d)) + (ds / Math.pow(dn, 2d));

				final int size = (int) dn;
				final double[] ee = new double[size];

				for (int i = 1; i <= size; ++i) {
					ee[i - 1] = aEnergy + preEdgeStep * i + aa * Math.pow(i, 2) + bb * Math.pow(i, 3);
				}
				return ee;
			}
		}
		throw new ExafsScanPointCreatorException("Could not calculate AB region of XasScan");
	}

	private ExafsScanRegionCalculator() {
	}

}
