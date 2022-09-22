/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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
package uk.ac.gda.client.tomo;

/**
 *
 */
public interface Measurement {
	double getMeasurementValue();

	String getMeasurementUnit();

	public static class PixelMeasurement implements Measurement {

		private final double value;

		public PixelMeasurement(double value) {
			this.value = value;
		}

		@Override
		public double getMeasurementValue() {
			return value;
		}

		@Override
		public String getMeasurementUnit() {
			return "pixels";
		}

	}

	public static class ScaleLengthMeasurement implements Measurement {

		private final double measurementValue;

		public ScaleLengthMeasurement(double measurementValue) {
			this.measurementValue = measurementValue;
		}

		@Override
		public double getMeasurementValue() {
			if (measurementValue < 1) {
				return measurementValue * 1000;
			}
			return measurementValue;
		}

		@Override
		public String getMeasurementUnit() {
			if (measurementValue < 1) {
				return "µm";
			}
			return "mm";
		}

	}
}
