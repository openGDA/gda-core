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

package uk.ac.diamond.daq.devices.specs.phoibos;

import java.io.Serializable;

public class SpecsPhoibosCompletedIteration implements Serializable {

	private double[] spectrumData;
	private double[][] imageData;

	public SpecsPhoibosCompletedIteration(double[] spectrumData, double[][] imageData) {
		this.spectrumData = spectrumData;
		this.imageData = imageData;
	}

	public double[] getSpectrumData() {
		return spectrumData;
	}

	public double[][] getImageData() {
		return imageData;
	}
}
