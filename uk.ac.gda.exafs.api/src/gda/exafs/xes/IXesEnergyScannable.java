/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

import java.util.Map;

import gda.device.DeviceException;
import gda.device.ScannableMotionUnits;
import gda.util.CrystalParameters;
import gda.util.CrystalParameters.CrystalMaterial;

public interface IXesEnergyScannable extends ScannableMotionUnits {

	/**
	 * @return CrystalMaterial ordinal value for the crystal material i.e. 0 = Si, 1 = Ge (see {@link CrystalParameters#CrystalMaterial})
	 * @throws DeviceException
	 */
	CrystalMaterial getMaterialType() throws DeviceException;

	/**
	 * @return Array of crystal cut values
	 * @throws DeviceException
	 */
	int[] getCrystalCut() throws DeviceException;

	/**
	 * @return Radius of spectrometer Rowland circle.
	 * @throws DeviceException
	 */
	double getRadius() throws DeviceException;

	/**
	 * Calculate the positions of all the scannables in the spectrometer for the given energy
	 * (This function is called by the client on the exported XesEnergyScannable object)
	 * @param energy
	 * @return Map with key = scannable name, value = position.
	 * @throws DeviceException
	 */
	Map<String, Double> getPositionsMap(double energy) throws DeviceException;


	/**
	 * @return Return an array containing the energy range allowed for this spectrometer [low energy, high energy]
	 * @throws DeviceException
	 */
	public double[] getEnergyRange() throws DeviceException;
}
