/*-
 * Copyright © 2012 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful,
	but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not,
	see <http://www.gnu.org/licenses/>.
 */

package uk.ac.gda.devices.vgscienta.i05_1;

import java.util.Arrays;
import java.util.LinkedHashMap;

import uk.ac.gda.devices.vgscienta.AnalyserCapabilties;

/**
 * This class is designed to provide all the information required for GDA to use the electron analyser which can't be
 * obtained from the EPICS IOC. This includes:
 * <li>The pixel to angle mapping for each lens mode</li>
 * <li>The energy size of a pixel in meV (Assuming PE=1 all others can be calculated from this)</li>
 * <li>The energy width of the detector in pixels allows the total energy range of the detector to be calculated for
 * all pass energies.</li>
 */
public class I05_1AnalyserCapabilties implements AnalyserCapabilties {

	private String name = "AnalyserCapabilties";

	// Map holding the angle array for each lens mode
	private LinkedHashMap<String, double[]> lens2angles;

	// This value is defined for PE=1 and can be calculated from the energy width of a pixel/PE
	private Double energyStepPerPixel; // in meV

	private int detectorEnergyWidthInPixels;

	public void setEnergyStepPerPixel(Double energyStepPerPixel) {
		this.energyStepPerPixel = energyStepPerPixel;
	}

	public int getDetectorEnergyWidthInPixels() {
		return detectorEnergyWidthInPixels;
	}

	public void setDetectorEnergyWidthInPixels(int detectorEnergyWidthInPixels) {
		this.detectorEnergyWidthInPixels = detectorEnergyWidthInPixels;
	}

	public I05_1AnalyserCapabilties() {
		// Would probably be good to add some validation here eg:
		// - Get the lens modes from EPICS and check we have them defined
		// - Check the length of the angle arrays is ok
		// - Get the pass energies from EPICS and check we have the same
	}

	public void setLens2angles(LinkedHashMap<String, double[]> lens2angles) {
		this.lens2angles = lens2angles;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Short[] getPassEnergies() {
		// Array of all possible pass energies
		return new Short[] { 1, 2, 5, 10, 20, 40, 50, 75, 100, 200 };
	}

	// Temporary fix to get i05-1 working initially, need a cleverer way of providing the
	// pass energies depending on both the PSU mode and lens mode!
	@Override
	public Short[] getPassEnergiesLow() {
		// return new Short[] { 1, 2, 5, 10, 20 };
		return getPassEnergies();
	}

	@Override
	public Short[] getPassEnergiesHigh() {
		// return new Short[] { 5, 10, 20, 50, 100 };
		return getPassEnergies();
	}

	/**
	 * This returns the energy width (in eV) of a fixed mode image using the full sensor size
	 */
	@Override
	public double getEnergyWidthForPass(int pass) {
		// Energy width scales with pass energy
		// division by 1000 to convert meV to eV
		return energyStepPerPixel * detectorEnergyWidthInPixels * pass / 1000;
	}

	@Override
	public double getEnergyStepForPass(int pass) {
		// This returns the step size per pixel in the energy axis (in meV)
		return Math.round(energyStepPerPixel * pass * 100000) / 100000.0;
	}

	@Override
	public double[] getAngleAxis(String lensTable, int startChannel, int length) {
		if (!lens2angles.containsKey(lensTable))
			throw new ArrayIndexOutOfBoundsException("unknown lens table " + lensTable);
		double[] doubles = lens2angles.get(lensTable);
		return Arrays.copyOfRange(doubles, startChannel, startChannel + length);
	}

	@Override
	public String[] getLensModes() {
		return lens2angles.keySet().toArray(new String[0]);
	}

	@Override
	public String[] getPsuModes() {
		return new String[] { "Low Pass", "High Pass" };
	}
}
