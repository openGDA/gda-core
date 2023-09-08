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

public class SpecsPhoibosLiveIterationSpectraUpdate extends SpecsPhoibosLiveDataUpdate {

	private int iterationNumber;
	private double[] iterationSpectrum;

	private SpecsPhoibosLiveIterationSpectraUpdate(Builder builder) {
		super(builder);
		iterationNumber = builder.iterationNumber;
		iterationSpectrum = builder.iterationSpectrum;
	}

	public int getIterationNumber() {
		return iterationNumber;
	}

	public double[] getIterationSpectrum() {
		return iterationSpectrum;
	}

	public static class Builder extends SpecsPhoibosLiveDataUpdate.Builder {

		private int iterationNumber;
		private double[] iterationSpectrum;

		public Builder iterationNumber(int val) {
			iterationNumber = val;
			return this;
		}

		public Builder iterationSpectrum(double[] val) {
			iterationSpectrum = val;
			return this;
		}

		@Override
		public SpecsPhoibosLiveIterationSpectraUpdate build() {
			return new SpecsPhoibosLiveIterationSpectraUpdate(this);
		}
	}
}
