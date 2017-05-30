/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

import java.io.Serializable;

/**
 * Immutable class to hold live data generated from a SPECS analyser. The idea is this object can be built on the server
 * side and then pushed to clients and it will contain every thing they need to display the GUI.
 *
 * @author James Mudd
 */
public class SpecsPhoibosLiveDataUpdate implements Serializable {

	/**
	 * Generated serial ID
	 */
	private static final long serialVersionUID = 4498165026512874271L;

	final int totalPoints;
	final int currentPoint;
	final double[] spectrum;
	final double[][] image;
	final double[] keEnergyAxis;
	final double[] beEnergyAxis;
	final double[] yAxis;

	public SpecsPhoibosLiveDataUpdate(final int totalPoints, final int currentPoint, final double[] spectrum,
			final double[][] image, final double[] keEnergyAxis, final double[] beEnergyAxis, final double[] yAxis) {

		this.totalPoints = totalPoints;
		this.currentPoint = currentPoint;
		this.spectrum = spectrum;
		this.image = image;
		this.keEnergyAxis = keEnergyAxis;
		this.beEnergyAxis = beEnergyAxis;
		this.yAxis = yAxis;
	}

	public int getTotalPoints() {
		return totalPoints;
	}

	public int getCurrentPoint() {
		return currentPoint;
	}

	public double[] getSpectrum() {
		return spectrum;
	}

	public double[][] getImage() {
		return image;
	}

	public double[] getKeEnergyAxis() {
		return keEnergyAxis;
	}

	public double[] getBeEnergyAxis() {
		return beEnergyAxis;
	}

	public double[] getyAxis() {
		return yAxis;
	}

}
