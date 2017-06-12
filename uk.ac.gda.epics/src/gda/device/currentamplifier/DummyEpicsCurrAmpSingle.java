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

package gda.device.currentamplifier;

import java.util.Arrays;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.CurrentAmplifier;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.factory.FactoryException;
import gda.jython.JythonServerFacade;

/**
 * simulation for a single Channel Current Amplifier device, provide controls for gain and mode, and reading for current value.
 */
public class DummyEpicsCurrAmpSingle extends CurrentAmplifierBase implements Scannable, CurrentAmplifier {

	private static final Logger logger = LoggerFactory.getLogger(DummyEpicsCurrAmpSingle.class);

	private static final String[] positionLabels = { "1", "2", "5", "10", "20", "50", "100", "200", "500" };
	private static final String[] gainUnitLabels = { "pA/V", "nA/V", "uA/V", "mA/V" };
	private static final String[] modeLabels = { "ACDC" };
	// cached values
	private volatile double current = Double.NaN;
	private volatile String gain = "";
	private volatile String gainUnit = "";
	private volatile String mode = "";

	/**
	 * Constructor
	 */
	public DummyEpicsCurrAmpSingle() {
		gainPositions.addAll(Arrays.asList(positionLabels));
		gainUnits.addAll(Arrays.asList(gainUnitLabels));
		modePositions.addAll(Arrays.asList(modeLabels));

	}

	@Override
	public void configure() throws FactoryException {
		if (!configured) {
			try {
				setGain(positionLabels[0]);
			} catch (DeviceException e) {
				logger.error("{}: {}",getName(), e.getMessage());
				throw new FactoryException(e.getMessage(), e);
			}
			try {
				setGainUnit(gainUnitLabels[0]);
			} catch (DeviceException e) {
				logger.error("{}: {}",getName(), e.getMessage());
				throw new FactoryException(e.getMessage(), e);
			}
			// to get scandatapoint haeder correctly named, for a single valued scannable, set input name to its
			// scannable name
			this.inputNames[0] = getName();
			this.outputFormat[0] = "%5.4f";
			configured = true;
		} // end of if (!configured)
	}

	@Override
	public String[] getGainPositions() throws DeviceException {
		return positionLabels;
	}

	/**
	 * returns a parsed list of gains available for this amplifier.
	 *
	 * @throws DeviceException
	 */
	@Override
	public void listGains() throws DeviceException {
		try {
			String[] gainsAvai = getGainPositions();
			for (String gain : gainsAvai) {
				JythonServerFacade f = JythonServerFacade.getInstance();
				f.print(gain);
			}
		} catch (DeviceException e) {
			throw new DeviceException(getName() + " : Cannot list all gain settings for this amplifer.");
		}
	}
	private Random rand = new Random();
	@Override
	public Status getStatus() throws DeviceException {
		int index = rand.nextInt(2);
		return Status.from_int(index);
	}

	@Override
	public void setGain(String position) throws DeviceException {
		if (gainPositions.contains(position)) {
			gain = position;
			return;
		}
		// if get here then wrong position name supplied
		throw new DeviceException("Position called: " + gain + " not found.");
	}

	@Override
	public String getGain() {
		return gain;
	}

	@Override
	public double getCurrent() {
		current=rand.nextDouble();
		return current;
	}

	@Override
	public String getMode() {
		return mode;
	}

	@Override
	public void setMode(String mode) throws DeviceException {
		if (modePositions.contains(mode)) {
			this.mode = mode;
			return;
		}
		// if get here then wrong position name supplied
		throw new DeviceException("Position called: " + mode + " not found.");
	}

	@Override
	public String toFormattedString() {
		try {

			// get the current position as an array of doubles
			Object position = getPosition();

			// if position is null then simply return the name
			if (position == null) {
				logger.warn("getPosition() from {} returns NULL.", getName());
				return getName() + " : NOT AVAILABLE";
			}

			// else build a string of formatted positions
			String output = getName() + " : " + String.format(outputFormat[0], position);
			// output += this.inputNames[0] + " : " + String.format(outputFormat[0], position) + " ";
			// output += this.inputNames[1] + " : " + String.format(outputFormat[1], getGain()) + " ";
			// output += this.inputNames[2] + " : " + String.format(outputFormat[2], getStatus()) + " ";
			// output += this.inputNames[3] + " : " + String.format(outputFormat[3], getMode()) + " ";

			return output.trim();

		} catch (Exception e) {
			logger.info("{}: exception while getting position. {}; {}", getName(),e.getMessage(),e.getCause());
			return getName();
		}
	}

	@Override
	public String getGainUnit() {
		return gainUnit;
	}

	@Override
	public void setGainUnit(String unit) throws DeviceException {
		if (gainUnits.contains(unit)){
			this.gainUnit = unit;
			return;
		}
		throw new DeviceException("Position called: " + unit + " not found.");
	}

}