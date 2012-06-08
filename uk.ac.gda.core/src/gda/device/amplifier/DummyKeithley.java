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

package gda.device.amplifier;

import gda.device.DeviceException;

/**
 * Dummy Keithley Current Amplifier
 */
public class DummyKeithley extends AmplifierBase {

	private static final String VOLTAGEBIAS = "B";

	private static final String ZEROCHECK_OR_CORRECT = "C";

	private static final String SETGAIN = "R";

	private static final String FILTER_RISE_TIME = "T";

	private static final String ENLARGEGAIN = "W";

	private static final String FILTER = "P";

	private static final String AUTO_FILTER = "Z";

	private static final String CURRENT_SUPPRESS = "S";

	private static final String CURRENT_SUPPRESS_SWITCH = "N";

	// FIXME: what should these values be in real life
	private static final double MAX_VOLTAGEBIAS = -1000000000;

	private static final double MIN_VOLTAGEBIAS = 1000000000;

	private String[] gainString = { "R03", "R04", "R05", "R06", "R07", "R08", "R09", "R10" };

	private int MAX_GAIN_INDEX = 10;

	private int MIN_GAIN_INDEX = 3;

	private int gainEnlarge = 0;

	private int currentGainIndex = 3;

	private double voltageBiasValue = 0.0;

	private final double[] CURRENT_RANGE = { 0.0, 0.000000005, 0.00000005, 0.0000005, 0.000005, 0.00005, 0.0005, 0.005 };

	private double currentSuppressionValue;

	private final double[] RISE_TIME_RANGE = { 0.00001, 0.00003, 0.0001, 0.0003, 0.001, 0.003, 0.010, 0.030, 0.100,
			0.300 };

	private String[] timeString = { "T0", "T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9" };

	private int timeIndex = 0;

	private StringBuffer statusWord = new StringBuffer("A0B0C1H00J0K0M000N0P0R03S07T0W0Y0Z1");

	@Override
	public void configure(){
		// no configuration required
	}

	@Override
	public void autoCurrentSuppress() throws DeviceException {
		setZeroCheck(false);
		setStatus(CURRENT_SUPPRESS_SWITCH, CURRENT_SUPPRESS_SWITCH + 2);
		// displayString = "SUPPRESSING";

	}

	@Override
	public void autoZeroCorrect() throws DeviceException {
		setStatus(ZEROCHECK_OR_CORRECT, ZEROCHECK_OR_CORRECT + 2);
		// displayString = "CORRECTING";
	}

	@Override
	public double getCurrentSuppressValue() throws DeviceException {
		// FIXME proper value in display string
		// displayString = "";
		return currentSuppressionValue;
	}

	@Override
	public double getFilterRiseTime() throws DeviceException {
		// FIXME proper value for display string
		// displayString = "";
		return RISE_TIME_RANGE[timeIndex];
	}

	@Override
	public double getGain() throws DeviceException {
		// FIXME proper value for display string
		double value = currentGainIndex + gainEnlarge;
		value = Math.pow(10.0, value);
		// displayString = value + " V/A";
		return value;
	}

	@Override
	public String getStatus() throws DeviceException {
		// FIXME needs more stuff
		return "ready";
	}

	/**
	 * Gets the status of the dummy amplifier
	 * 
	 * @param command
	 *            command whose status has to be checked
	 * @return status
	 * @throws DeviceException
	 */
	public boolean getStatus(String command) throws DeviceException {
		if (command.equals(SETGAIN) || command.equals(FILTER_RISE_TIME) || command.equals("H") || command.equals("J")
				|| command.equals("K") || command.equals("M") || command.equals("Y"))
			throw new DeviceException("Incorrect status check argument " + command);

		int index = statusWord.indexOf(command);
		if (index == -1)
			throw new DeviceException("Incorrect status check argument " + command);
		char c = statusWord.charAt(index + 1);
		if (c == '1')
			return true;
		else if (c == '2' && (command.equals(ZEROCHECK_OR_CORRECT) || command.equals(CURRENT_SUPPRESS_SWITCH)))
			return true;
		else
			return false;
	}

	@Override
	public double getVoltageBias() throws DeviceException {
		// FIXME proper value for display string
		return voltageBiasValue;
	}

	@Override
	public void setAutoFilter(boolean onOff) throws DeviceException {
		setStatus(AUTO_FILTER, AUTO_FILTER + (onOff ? 1 : 0));
	}

	@Override
	public void setCurrentSuppress(boolean onOff) throws DeviceException {
		setStatus(CURRENT_SUPPRESS_SWITCH, CURRENT_SUPPRESS_SWITCH + (onOff ? 1 : 0));

	}

	@Override
	public void setCurrentSuppressionParams(double value) throws DeviceException {
		if (value < -CURRENT_RANGE[7] || value > CURRENT_RANGE[7])
			throw new DeviceException("Current Suppress value out of range");
		currentSuppressionValue = value;
		autoRange(value);

	}

	@Override
	public void setCurrentSuppressionParams(double value, int range) throws DeviceException {

		if (value < -CURRENT_RANGE[range] || value > CURRENT_RANGE[range])
			throw new DeviceException("Current Suppress value out of range");
		currentSuppressionValue = value;
		setStatus(CURRENT_SUPPRESS, CURRENT_SUPPRESS + "0" + range);
	}

	@Override
	public void setEnlargeGain(boolean onOff) throws DeviceException {
		gainEnlarge = onOff ? 1 : 0;
		setStatus(ENLARGEGAIN, ENLARGEGAIN + gainEnlarge);
	}

	@Override
	public void setFilter(boolean onOff) throws DeviceException {
		setStatus(FILTER, FILTER + (onOff ? 1 : 0));
	}

	@Override
	public void setFilterRiseTime(int level) throws DeviceException {
		if (level >= 0 && level <= timeString.length - 1) {
			timeIndex = level;
			setStatus(FILTER_RISE_TIME, FILTER_RISE_TIME + level);
		} else
			throw new DeviceException("Filter rise time out of range");
	}

	@Override
	public void setGain(int level) throws DeviceException {
		if (level >= MIN_GAIN_INDEX && level <= MAX_GAIN_INDEX) {
			currentGainIndex = level;
			setStatus(SETGAIN, gainString[level - MIN_GAIN_INDEX]);
		} else
			throw new DeviceException("gain out of range");
	}

	@Override
	public void setVoltageBias(boolean voltageBias) throws DeviceException {
		setStatus(VOLTAGEBIAS, VOLTAGEBIAS + (voltageBias ? 1 : 0));
	}

	@Override
	public void setVoltageBias(double value) throws DeviceException {
		if (value < MIN_VOLTAGEBIAS || value > MAX_VOLTAGEBIAS)
			throw new DeviceException("voltage bias value out of range");
		voltageBiasValue = value;

	}

	@Override
	public void setZeroCheck(boolean onOff) throws DeviceException {
		setStatus(ZEROCHECK_OR_CORRECT, ZEROCHECK_OR_CORRECT + (onOff ? 1 : 0));
	}

	/**
	 * @param value
	 */
	private void autoRange(double value) {
		int i = 0;
		for (; i < CURRENT_RANGE.length; i++) {
			if (value >= -CURRENT_RANGE[i] && value <= CURRENT_RANGE[i]) {
				break;
			}
		}

	}

	private void setStatus(String beginMatch, String commandStatus) {
		int beginIndex = statusWord.indexOf(beginMatch);
		statusWord.replace(beginIndex, (beginIndex + commandStatus.length()), commandStatus);
	}

}
