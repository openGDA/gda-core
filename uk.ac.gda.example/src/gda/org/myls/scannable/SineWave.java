/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.org.myls.scannable;

/**
 *
 */
public class SineWave {
	double period, phase, magnitude, offset, noise;

	/**
	 *
	 */
	public SineWave() {
		this(1, 0, 1, 0, 0);
	}

	/**
	 * @param period
	 * @param phase
	 * @param magnitude
	 * @param offset
	 * @param noise
	 */
	public SineWave(double period, double phase, double magnitude,
			double offset, double noise) {
		this.setPeriod(period);
		this.setPhase(phase);
		this.setMagnitude(magnitude);
		this.setOffset(offset);
		this.setNoise(noise);
	}

	@Override
	public String toString() {
		return "per:" + period + ", ph:" + phase + ", mag:"
				+ magnitude + ", ofst:" + offset + ", n:" + noise;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}

	/**
	 * @return Returns the period.
	 */
	public double getPeriod() {
		return period;
	}

	/**
	 * @param period
	 *            The period to set.
	 */
	public void setPeriod(double period) {
		this.period = period;
	}

	/**
	 * @return Returns the phase.
	 */
	public double getPhase() {
		return phase;
	}

	/**
	 * @param phase
	 *            The phase to set.
	 */
	public void setPhase(double phase) {
		this.phase = phase;
	}

	/**
	 * @return Returns the magnitude.
	 */
	public double getMagnitude() {
		return magnitude;
	}

	/**
	 * @param magnitude
	 *            The magnitude to set.
	 */
	public void setMagnitude(double magnitude) {
		this.magnitude = magnitude;
	}

	/**
	 * @return Returns the offset.
	 */
	public double getOffset() {
		return offset;
	}

	/**
	 * @param offset
	 *            The offset to set.
	 */
	public void setOffset(double offset) {
		this.offset = offset;
	}

	/**
	 * @return Returns the noise.
	 */
	public double getNoise() {
		return noise;
	}

	/**
	 * @param noise
	 *            The noise to set.
	 */
	public void setNoise(double noise) {
		this.noise = noise;
	}

}
