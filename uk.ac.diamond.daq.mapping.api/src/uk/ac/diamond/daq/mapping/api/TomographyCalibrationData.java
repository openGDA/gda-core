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

package uk.ac.diamond.daq.mapping.api;

import java.util.Objects;

public class TomographyCalibrationData {

	private Double amplitude;
	private Double frequency;
	private Double phase;
	private Double mean;

	public TomographyCalibrationData(Double amplitude, Double frequency, Double phase, Double mean) {
		this.amplitude = amplitude;
		this.frequency = frequency;
		this.phase = phase;
		this.mean = mean;
	}

	public TomographyCalibrationData() {

	}

	public Double getAmplitude() {
		return amplitude;
	}

	public void setAmplitude(Double amplitude) {
		this.amplitude = amplitude;
	}

	public Double getFrequency() {
		return frequency;
	}

	public void setFrequency(Double frequency) {
		this.frequency = frequency;
	}

	public Double getPhase() {
		return phase;
	}

	public void setPhase(Double phase) {
		this.phase = phase;
	}

	public Double getMean() {
		return mean;
	}

	public void setMean(Double mean) {
		this.mean = mean;
	}

	@Override
	public String toString() {
		return "TomographyCalibrationData [amplitude=" + amplitude + ", frequency=" + frequency + ", phase=" + phase
				+ ", mean=" + mean + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(amplitude, frequency, mean, phase);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TomographyCalibrationData other = (TomographyCalibrationData) obj;
		return Objects.equals(amplitude, other.amplitude) && Objects.equals(frequency, other.frequency)
				&& Objects.equals(mean, other.mean) && Objects.equals(phase, other.phase);
	}
}
