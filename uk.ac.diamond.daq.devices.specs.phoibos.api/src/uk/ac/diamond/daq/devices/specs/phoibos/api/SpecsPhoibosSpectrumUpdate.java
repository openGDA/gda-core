/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.devices.specs.phoibos.api;

/**
 * A class that holds information on the intensity (cps) measured by the analyser.
 * Used in getting cps when operating detector for alignment.
 *
 * @author Dimitrios Ladakis
 */

public class SpecsPhoibosSpectrumUpdate extends SpecsPhoibosLiveUpdate {

	public SpecsPhoibosSpectrumUpdate(double[] spectrum) {
		super(spectrum);
	}

	public int getDataLength() {
		return getSpectrum().length;
	}
}