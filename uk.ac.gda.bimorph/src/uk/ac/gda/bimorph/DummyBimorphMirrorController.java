/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.gda.bimorph;

import java.util.Arrays;

import gda.device.DeviceBase;

public class DummyBimorphMirrorController extends DeviceBase implements BimorphMirrorController {

	private double[] voltages = new double[0];
	private int numberOfChannels;

	private double maxVoltage = Double.MAX_VALUE;
	private double minVoltage = -Double.MAX_VALUE;
	private double maxDelta = Double.MAX_VALUE;

	@Override
	public double getVoltage(int channel) {
		try {
			return voltages[channel];
		} catch (IndexOutOfBoundsException ioobe) {
			throw new IllegalArgumentException(String.format("Channel is out of range for this mirror [%d-%d]", 0, getNumberOfChannels()));
		}
	}

	@Override
	public double[] getVoltages() {
		return voltages.clone();
	}

	@Override
	public void setVoltage(int channel, double voltage) {
		try {
			voltages[channel] = voltage;
		} catch (IndexOutOfBoundsException ioobe) {
			throw new IllegalArgumentException(String.format("Channel is out of range for this mirror [%d-%d]", 0, getNumberOfChannels()));
		}
	}

	@Override
	public void setVoltages(double... voltages) {
		this.voltages = voltages;
	}

	public void setNumberOfChannels(int channels) {
		numberOfChannels = channels;
		voltages = Arrays.copyOf(voltages, channels);
	}

	public void setMaxDelta(double maxDelta) {
		if (maxDelta <= 0) {
			throw new IllegalArgumentException("Voltage delta must be >0 (not " + maxDelta + ")");
		}
		this.maxDelta = maxDelta;
	}

	public void setMaxVoltage(double maxVoltage) {
		this.maxVoltage = maxVoltage;
	}

	public void setMinVoltage(double minVoltage) {
		this.minVoltage = minVoltage;
	}

	@Override
	public int getNumberOfChannels() {
		return numberOfChannels;
	}

	@Override
	public boolean isBusy() {
		return false;
	}

	@Override
	public double getMaxDelta() {
		return maxDelta;
	}

	@Override
	public double getMaxVoltage() {
		return maxVoltage;
	}

	@Override
	public double getMinVoltage() {
		return minVoltage;
	}
}
