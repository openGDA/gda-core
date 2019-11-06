/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.api;

/**
 * The complete tomography calibration data (x & z axes)
 */
public class TomographyCalibrationData {

	private TomographyAxisCalibration xCalibration;
	private TomographyAxisCalibration zCalibration;

	public TomographyCalibrationData() {
		// for JSON
	}

	public TomographyCalibrationData(TomographyAxisCalibration xCalibration, TomographyAxisCalibration zCalibration) {
		this.xCalibration = xCalibration;
		this.zCalibration = zCalibration;
	}

	public TomographyAxisCalibration getxCalibration() {
		return xCalibration;
	}

	public void setxCalibration(TomographyAxisCalibration xCalibration) {
		this.xCalibration = xCalibration;
	}

	public TomographyAxisCalibration getzCalibration() {
		return zCalibration;
	}

	public void setzCalibration(TomographyAxisCalibration zCalibration) {
		this.zCalibration = zCalibration;
	}

	/**
	 * Class to hold the sine wave fitting data for a single axis
	 */
	public static class TomographyAxisCalibration {

		private double amplitude;
		private double frequency;
		private double phase;
		private double mean;

		public TomographyAxisCalibration() {
			// for JSON instantiation
		}

		public TomographyAxisCalibration(double amplitude, double frequency, double phase, double mean) {
			this.amplitude = amplitude;
			this.frequency = frequency;
			this.phase = phase;
			this.mean = mean;
		}

		public double getAmplitude() {
			return amplitude;
		}

		public void setAmplitude(double amplitude) {
			this.amplitude = amplitude;
		}

		public double getFrequency() {
			return frequency;
		}

		public void setFrequency(double frequency) {
			this.frequency = frequency;
		}

		public double getPhase() {
			return phase;
		}

		public void setPhase(double phase) {
			this.phase = phase;
		}

		public double getMean() {
			return mean;
		}

		public void setMean(double mean) {
			this.mean = mean;
		}
	}
}
