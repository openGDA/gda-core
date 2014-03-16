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

import static org.junit.Assert.*;
import gda.device.adc.DummyAdc;
import gda.device.adc.DummyValueSuggester;
import gda.device.DeviceException;

import org.junit.Before;
import org.junit.Test;

/**
 * 
 */
public class DummyAdcTest implements DummyValueSuggester {
	private DummyAdc dummyAdc;

	private final int minChan = 1, maxChan = 16, validRanges[] = { 10000, 5000 }, nchan = 16;

	private double suggestedValue = 0;

	/**
	 */
	@Before
	public void setUp() {
		dummyAdc = new DummyAdc();
	}

	/**
	 * 
	 */
	@Test
	public void testDummyAdc() {
		assertNotNull("dummyAdc instance should be non-null", dummyAdc);
	}

	/**
	 * @throws DeviceException
	 */
	@Test
	public void testGetVoltage() throws DeviceException {
		double voltage, voltageMin = 0, voltageMax = 1;
		// set polarity, range and get random values on all chans within
		// expected
		// get 20 values for each channel

		for (int nloops = 0; nloops < 20; nloops++) {
			for (int channel = 1; channel <= nchan; channel++) {
				// range 10000, unipolar
				dummyAdc.setUniPolar(channel, true);
				dummyAdc.setRange(channel, validRanges[0]);
				voltageMin = 0;
				voltageMax = 10;

				voltage = dummyAdc.getVoltage(channel);
				assertTrue("voltage should be within limits", voltage >= voltageMin && voltage <= voltageMax);

				// range 5000, unipolar
				dummyAdc.setUniPolar(channel, true);
				dummyAdc.setRange(channel, validRanges[1]);
				voltageMin = 0;
				voltageMax = 5;

				voltage = dummyAdc.getVoltage(channel);
				assertTrue("voltage should be within limits", voltage >= voltageMin && voltage <= voltageMax);

				// range 10000, bipolar
				dummyAdc.setUniPolar(channel, false);
				dummyAdc.setRange(channel, validRanges[0]);
				voltageMin = -10;
				voltageMax = 10;

				voltage = dummyAdc.getVoltage(channel);
				assertTrue("voltage should be within limits", voltage >= voltageMin && voltage <= voltageMax);

				// range 5000, bipolar
				dummyAdc.setUniPolar(channel, false);
				dummyAdc.setRange(channel, validRanges[1]);
				voltageMin = -5;
				voltageMax = 5;

				voltage = dummyAdc.getVoltage(channel);
				assertTrue("voltage should be within limits", voltage >= voltageMin && voltage <= voltageMax);
			}
		}
	}

	/**
	 * @throws DeviceException
	 */
	@Test
	public void testGetVoltages() throws DeviceException {
		double voltages[] = new double[nchan];
		double voltageMin = 0, voltageMax = 1;

		// range 10000, unipolar
		for (int ichan = 1; ichan <= nchan; ichan++) {
			dummyAdc.setUniPolar(ichan, true);
			dummyAdc.setRange(ichan, validRanges[0]);
		}
		voltageMin = 0;
		voltageMax = 10;

		voltages = dummyAdc.getVoltages();
		for (int i = 0; i < nchan; i++) {
			assertTrue("voltage should be within limits", voltages[i] >= voltageMin && voltages[i] <= voltageMax);
		}

		// range 5000, unipolar
		for (int ichan = 1; ichan <= nchan; ichan++) {
			dummyAdc.setUniPolar(ichan, true);
			dummyAdc.setRange(ichan, validRanges[1]);
		}
		voltageMin = 0;
		voltageMax = 5;

		voltages = dummyAdc.getVoltages();
		for (int i = 0; i < nchan; i++) {
			assertTrue("voltage should be within limits", voltages[i] >= voltageMin && voltages[i] <= voltageMax);
		}

		// range 10000, bipolar
		for (int ichan = 1; ichan <= nchan; ichan++) {
			dummyAdc.setUniPolar(ichan, false);
			dummyAdc.setRange(ichan, validRanges[0]);
		}
		voltageMin = -10;
		voltageMax = 10;

		voltages = dummyAdc.getVoltages();
		for (int i = 0; i < nchan; i++) {
			assertTrue("voltage should be within limits", voltages[i] >= voltageMin && voltages[i] <= voltageMax);
		}

		// range 5000, bipolar
		for (int ichan = 1; ichan <= nchan; ichan++) {
			dummyAdc.setUniPolar(ichan, false);
			dummyAdc.setRange(ichan, validRanges[1]);
		}
		voltageMin = -5;
		voltageMax = 5;

		voltages = dummyAdc.getVoltages();
		for (int i = 0; i < nchan; i++) {
			assertTrue("voltage should be within limits", voltages[i] >= voltageMin && voltages[i] <= voltageMax);
		}
	}

	/**
	 * @throws DeviceException
	 */
	@Test
	public void testSetRange() throws DeviceException {
		int ch1 = 4, ch2 = 12;
		// valid ranges set and verified
		dummyAdc.setRange(ch1, validRanges[0]);
		assertEquals("range not as set", dummyAdc.getRange(ch1), validRanges[0]);

		dummyAdc.setRange(ch2, validRanges[1]);
		assertEquals("range not as set", dummyAdc.getRange(ch2), validRanges[1]);
	}

	/**
	 * @throws DeviceException
	 */
	@Test
	public void testGetRange() throws DeviceException {
		// check initial ranges for limit and sub-limit channel vals
		assertEquals("starting range should be " + validRanges[0], dummyAdc.getRange(minChan), validRanges[0]);

		assertEquals("starting range should be " + validRanges[0], dummyAdc.getRange(maxChan), validRanges[0]);

		assertEquals("starting range should be " + validRanges[0], dummyAdc.getRange(1), validRanges[0]);

		assertEquals("starting range should be " + validRanges[0], dummyAdc.getRange(15), validRanges[0]);
	}

	/**
	 * @throws DeviceException
	 */
	@Test(expected = DeviceException.class)
	public void testGetRangeExc1() throws DeviceException {
		// check exceptions thrown with out of limits values
		dummyAdc.getRange(minChan - 1);
	}

	/**
	 * @throws DeviceException
	 */
	@Test(expected = DeviceException.class)
	public void testGetRangeExc2() throws DeviceException {
		// check exceptions thrown with out of limits values
		dummyAdc.getRange(maxChan + 1);
	}

	/**
	 * @throws DeviceException
	 */
	@Test(expected = DeviceException.class)
	public void testGetRangeExc3() throws DeviceException {
		// check exceptions thrown with out of limits values
		dummyAdc.getRange(-2);
	}

	/**
	 * @throws DeviceException
	 */
	@Test
	public void testSetUniPolar() throws DeviceException {
		int channel = 0;
		boolean unipolarMode = false;

		// change polarity but can't check if it works as not getUnipolar method
		channel = minChan;
		unipolarMode = true;
		dummyAdc.setUniPolar(channel, unipolarMode);

		channel = 7;
		unipolarMode = false;
		dummyAdc.setUniPolar(channel, unipolarMode);

		channel = maxChan;
		unipolarMode = false;
		dummyAdc.setUniPolar(channel, unipolarMode);
	}

	/**
	 * @throws DeviceException
	 */
	@Test(expected = DeviceException.class)
	public void testSetUniPolarExc1() throws DeviceException {
		int channel = 0;
		boolean unipolarMode = false;

		// change polarity and check invalid channel
		channel = 0;
		unipolarMode = true;

		dummyAdc.setUniPolar(channel, unipolarMode);
	}

	/**
	 * @throws DeviceException
	 */
	@Test(expected = DeviceException.class)
	public void testSetUniPolarExc2() throws DeviceException {
		int channel = 0;
		boolean unipolarMode = false;

		// change polarity and check invalid channel
		channel = -1;
		unipolarMode = true;
		dummyAdc.setUniPolar(channel, unipolarMode);
	}

	/**
	 * @throws DeviceException
	 */
	@Test
	public void testGetRanges() throws DeviceException {
		int outRanges[];

		outRanges = dummyAdc.getRanges();
		for (int i = 0; i < outRanges.length; i++) {
			assertEquals("range values not as specified", outRanges[i], validRanges[i]);
		}
	}

	/**
	 * @throws DeviceException
	 */
	@Test
	public void testIsUniPolarSettable() throws DeviceException {
		assertTrue("Unipolar should be selectable", dummyAdc.isUniPolarSettable());
	}

	/**
	 * @throws DeviceException
	 */
	@Test
	public void testSetSampleCount() throws DeviceException {
		// should be ok for values >= 1
		dummyAdc.setSampleCount(1);
		dummyAdc.setSampleCount(99);
		dummyAdc.setSampleCount(729);
		dummyAdc.setSampleCount(java.lang.Integer.MAX_VALUE);
	}

	/**
	 * @throws DeviceException
	 */
	@Test(expected = DeviceException.class)
	public void testSetSampleCountExc1() throws DeviceException {
		// should not accept values less than one
		dummyAdc.setSampleCount(0);
	}

	/**
	 * @throws DeviceException
	 */
	@Test(expected = DeviceException.class)
	public void testSetSampleCountExc2() throws DeviceException {
		// should not accept values less than one
		dummyAdc.setSampleCount(-1);
	}

	/**
	 * @throws DeviceException
	 */
	@Test
	public void testSetValueSuggester() throws DeviceException {
		double voltage = 0;
		int ichan = 1;
		// after this voltages will be returned from this.suggestedValue
		dummyAdc.setValueSuggester(this);

		// set unipolar and 0-10v range
		dummyAdc.setUniPolar(ichan, true);
		dummyAdc.setRange(ichan, validRanges[0]);
		suggestedValue = voltage;
		voltage = dummyAdc.getVoltage(ichan);
		assertEquals("suggested voltage not set", suggestedValue, voltage, 0);

		// set unipolar and 0-5v range
		dummyAdc.setUniPolar(ichan, true);
		dummyAdc.setRange(ichan, validRanges[1]);
		// value should not change
		voltage = dummyAdc.getVoltage(ichan);
		assertEquals("suggested voltage has changed", suggestedValue, voltage, 0);

		// check limits
		suggestedValue = 0;
		voltage = dummyAdc.getVoltage(ichan);
		assertEquals("suggested voltage should be valid", suggestedValue, voltage, 0);

		suggestedValue = 5;
		voltage = dummyAdc.getVoltage(ichan);
		assertEquals("suggested voltage should be valid", suggestedValue, voltage, 0);

		// a silly value will also pass !
		suggestedValue = -100;
		voltage = dummyAdc.getVoltage(ichan);
		assertEquals("suggested voltage should be valid", suggestedValue, voltage, 0);
	}

	// called by DummyAdc when setValueSuggester has been set to this object
	@Override
	public double getSuggestedValue() {
		// dummy current value of this.suggestedValue
		return (suggestedValue);
	}

}
