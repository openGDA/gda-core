/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

import java.util.List;
import java.util.Map;

import gda.device.Scannable;
import gda.device.ScannableMotionUnits;

public interface IXesSpectrometerScannable extends ScannableMotionUnits{

	/**
	 * @return Radius of the Rowland circle
	 */
	double getRadius();

	/**
	 * Set the radius of the Rowland circle
	 * @param radius
	 */
	void setRadius(double radius);


	/**
	 * @param convertEnergyToAngle
	 * @return Map containing position of each scannable in the spectrometer for the given energy.
	 */
	Map<Scannable, Double> getSpectrometerPositions(double convertEnergyToAngle);

	/**
	 * @return The maximum allowed Bragg angle.
	 */
	double getMaxTheta();

	/**
	 * @return The minimum allowed Bragg angle.
	 */
	double getMinTheta();

	/**
	 *
	 * @return List of all scannables controlled by the spectrometer
	 */
	List<Scannable> getScannables();

}
