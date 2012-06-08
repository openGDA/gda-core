/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.device.adc;

import gda.device.Adc;
import gda.device.DeviceBase;
import gda.device.DeviceException;

import java.util.Random;

/**
 * Dummy implementation of the Adc class
 */
public class DummyAdc extends DeviceBase implements Adc {
	private static final int[] ranges = { 10000, 5000 };

	private static final int BIPOLAR = 0;

	private static final int UNIPOLAR = 1;

	private static final int NCHANNEL = 16;

	private int _polarity[] = new int[NCHANNEL];

	private int _range[] = new int[NCHANNEL];

	private Random rand = new Random();

	private DummyValueSuggester vs = null;

	/**
	 * The Constructor.
	 */
	public DummyAdc() {
		for (int i = 0; i < NCHANNEL; i++) {
			_polarity[i] = BIPOLAR;
			_range[i] = ranges[0];
		}
	}
	
	@Override
	public void configure(){
		// no configuration required
	}

	@Override
	public double getVoltage(int channel) throws DeviceException {
		if (channel < 1 || channel > NCHANNEL)
			throw new DeviceException("Channel is out of range");
		double ran = rand.nextDouble();
		int i = --channel;

		if (vs != null) {
			ran = vs.getSuggestedValue();
		} else if (_range[i] == ranges[0]) {
			ran *= 10.0;
			if (_polarity[i] == BIPOLAR)
				ran = (ran * 2.0) - 10.0;
		} else {
			ran *= 5.0;
			if (_polarity[i] == BIPOLAR)
				ran = (ran * 2.0) - 5.0;
		}
		return ran;
	}

	@Override
	public double[] getVoltages() throws DeviceException {
		double[] v = new double[NCHANNEL];
		for (int i = 0; i < NCHANNEL; i++)
			v[i] = getVoltage(i + 1);

		return v;
	}

	@Override
	public void setRange(int channel, int range) throws DeviceException {
		if (channel < 1 || channel > NCHANNEL)
			throw new DeviceException("Channel is out of range");
		for (int i = 0; i < ranges.length; i++) {
			if (range == ranges[i]) {
				_range[--channel] = range;
				return;
			}
		}
		throw new DeviceException("Invalid range specified");
	}

	@Override
	public int getRange(int channel) throws DeviceException {
		if (channel < 1 || channel > NCHANNEL)
			throw new DeviceException("Channel is out of range");
		return _range[--channel];
	}

	@Override
	public void setUniPolar(int channel, boolean polarity) throws DeviceException {
		if (channel < 1 || channel > NCHANNEL)
			throw new DeviceException("Channel is out of range");
		_polarity[--channel] = (polarity) ? UNIPOLAR : BIPOLAR;
	}

	@Override
	public int[] getRanges() throws DeviceException {
		int[] r = new int[ranges.length];
		for (int i = 0; i < ranges.length; i++)
			r[i] = ranges[i];

		return r;
	}

	@Override
	public boolean isUniPolarSettable() throws DeviceException {
		return true;
	}

	@Override
	public void setSampleCount(int count) throws DeviceException {
		if (count < 1)
			throw new DeviceException("Sample count is out of range");
	}

	/**
	 * Set the method used to generate an appropriate value
	 * 
	 * @param vs
	 *            DummyValueSuggester to be used.
	 */
	public void setValueSuggester(DummyValueSuggester vs) {
		this.vs = vs;
	}
}
