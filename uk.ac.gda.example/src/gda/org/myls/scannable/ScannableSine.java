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

import gda.device.DeviceException;
import gda.device.scannable.corba.impl.ScannableAdapter;
import gda.device.scannable.corba.impl.ScannableImpl;
import gda.factory.corba.util.CorbaAdapterClass;
import gda.factory.corba.util.CorbaImplClass;

/**
 *
 */
@CorbaImplClass(ScannableImpl.class)
@CorbaAdapterClass(ScannableAdapter.class)
public class ScannableSine extends SimpleScannable {
	SineWave sineWave;
	// explicit properties (not wrapped in SineWave objcet)
	double period, phase, magnitude, offset, noise;

	/**
	 *
	 */
	public ScannableSine() {
		this(new SineWave(1, 0, 1, 0, 0));
	}

	/*
	 * super(name, xPosition, new String[] { xName }, new String[] { yName },
				level, new String[] { outputFormatx, outputFormaty },
				new String[] { unitsx, unitsy });
	 */
	/**
	 * @param sineWave
	 */
	public ScannableSine(SineWave sineWave) {
		super("sine", 0.0, new String[] { "x" }, new String[] { "y" }, 3,
				new String[] { "%.4f", "%.4f" }, new String[] { "mm", "mm" });
		// System.out.println("ScannableSine(" + sineWave + ")");
		this.setSineWave(sineWave);
	}

	@Override
	public Object getPosition() throws DeviceException {
		double x = (Double) super.getPosition();
		double y = Math.sin((x/period)+phase) * magnitude;
		double noiseVal = y * (Math.random() - 0.5) * sineWave.getNoise();
		y += noiseVal;
		return new Double[] { x, y };
	}

	@Override
	public String toString() {
		return "ScannableSine: " + name + ", sw: " + sineWave;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}

	/**
	 * @return Returns the sineWave.
	 */
	public SineWave getSineWave() {
		return sineWave;
	}

	/**
	 * @param sineWave The sineWave to set.
	 */
	public void setSineWave(SineWave sineWave) {
		this.sineWave = sineWave;
		this.setPeriod(sineWave.getPeriod());
		this.setPhase(sineWave.getPhase());
		this.setMagnitude(sineWave.getMagnitude());
		this.setOffset(sineWave.getOffset());
		this.setNoise(sineWave.getNoise());
	}

	/**
	 * @return Returns the period.
	 */
	public double getPeriod() {
		return period;
	}

	/**
	 * @param period The period to set.
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
	 * @param phase The phase to set.
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
	 * @param magnitude The magnitude to set.
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
	 * @param offset The offset to set.
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
	 * @param noise The noise to set.
	 */
	public void setNoise(double noise) {
		this.noise = noise;
	}
}
