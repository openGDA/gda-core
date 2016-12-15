/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.jython.InterfaceProvider;

/**
 * Dummy Scannable for Stanford current amplifier, originally written for B18 and
 * moved from a B18 specific plugin to uk.ac.gda.epics.
 * To facilitate exporting from server to client over RMI, it has been modified to implement the
 * {@link StanfordAmplifier} interface and to extend {@link CurrentAmplifierBase}.
 * <p>
 * See also {@link StanfordScannable}.
 *
 * @since 12/10/2016
 */
public class DummyStanfordScannable extends CurrentAmplifierBase implements StanfordAmplifier {
	private static final Logger logger = LoggerFactory.getLogger(DummyStanfordScannable.class);

	private static final String[] positionLabels = { "1", "2", "5", "10", "20", "50", "100", "200", "500" };
	private static final String[] gainUnitLabels = { "pA/V", "nA/V", "uA/V", "mA/V" };
	private static final String[] offsetUnitLabels = { "pA", "nA", "uA" };
	protected List<String> currentOffsetUnits = new ArrayList<String>();

	private String value;
	private boolean offsetCurrentIsOn;
	private int sensitivityUnit;
	private int sensitivityValue;
	private int offsetUnit;
	private int offsetValue;

	private String gainPosString, gainUnitString;

	public DummyStanfordScannable() {
		try {
			gainPositions.addAll(Arrays.asList(positionLabels));
			gainUnits.addAll(Arrays.asList(gainUnitLabels));
			currentOffsetUnits.addAll(Arrays.asList(offsetUnitLabels));

			setSensitivity(0);
			setSensitivityUnit(0);
			setOffset(0);
			setOffsetUnit(0);

			setGain(positionLabels[0]);
			setGainUnit(gainUnitLabels[0]);

		} catch (DeviceException e) {
			// This should not happen!
			logger.warn("Problem setting up DummyStanfordScannable");
		}
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) {
		String sensitivityString = String.valueOf(position);
		sensitivityValue = Integer.parseInt(sensitivityString.substring(0, sensitivityString.indexOf(" ")));
		sensitivityUnit = Integer.parseInt(sensitivityString.substring(sensitivityString.indexOf(" ") + 1));
	}

	@Override
	public Object rawGetPosition() {
		return sensitivityValue + " " + sensitivityUnit;
	}

	@Override
	public Object getPosition() throws DeviceException {
		return internalToExternal(rawGetPosition());
	}

	// Sensitivity/gain settings
	@Override
	public void setSensitivityUnit(int unit) throws DeviceException {
		this.sensitivityUnit = unit;
	}

	@Override
	public int getSensitivityUnit() throws DeviceException {
		return sensitivityUnit;
	}

	@Override
	public void setSensitivity(int sensitivity) throws DeviceException {
		this.sensitivityValue = sensitivity;
	}

	@Override
	public int getSensitivity() throws DeviceException {
		return sensitivityValue;
	}

	// Current offset settings
	@Override
	public void setOffsetCurrentOn(boolean switchOn) throws DeviceException {
		this.offsetCurrentIsOn = switchOn;
	}

	@Override
	public boolean isOffsetCurrentOn() throws DeviceException {
		return offsetCurrentIsOn;
	}

	@Override
	public void setOffset(int offsetValue) throws DeviceException {
		this.offsetValue = offsetValue;
	}

	@Override
	public int getOffset() throws DeviceException {
		return offsetValue;
	}

	@Override
	public void setOffsetUnit(int unit) throws DeviceException {
		this.offsetUnit = unit;
	}

	@Override
	public int getOffsetUnit() throws DeviceException {
		return offsetUnit;
	}

	@Override
	public String[] getOffsetUnits() {
		return offsetUnitLabels;
	}

	@Override
	public String[] getAllowedPositions() {
		return positionLabels;
	}

	@Override
	public String[] getGainUnits() {
		return gainUnitLabels;
	}

	@Override
	public boolean isBusy() {
		return false;
	}

	// CurrentAmplifier interface implementation
	@Override
	public void listGains() throws DeviceException {
		for (String gain : gainPositions) {
			InterfaceProvider.getTerminalPrinter().print(gain);
		}
	}

	@Override
	public double getCurrent() throws DeviceException {
		logger.debug("DummyStanford does not support 'getCurrent()'");
		return 0;
	}

	@Override
	public String getGain() throws DeviceException {
		return gainPosString;
	}

	@Override
	public String getGainUnit() throws DeviceException {
		return gainUnitString;
	}

	@Override
	public Status getStatus() throws DeviceException {
		logger.debug("DummyStanford does not support 'getStatus'");
		return null;
	}

	@Override
	public void setGain(String position) throws DeviceException {
		int index = gainPositions.indexOf(position);
		if (index != -1) {
			gainPosString = position;
			sensitivityValue = index;
		}
	}

	@Override
	public void setGainUnit(String unit) throws DeviceException {
		int index = gainUnits.indexOf(unit);
		if (index != -1) {
			gainUnitString = unit;
			sensitivityUnit = index;
		}
	}

	@Override
	public void setMode(String mode) throws DeviceException {
		logger.debug("DummyStanford does not support 'setMode()'");
	}

	@Override
	public String getMode() throws DeviceException {
		logger.debug("DummyStanford does not support 'getMode()'");
		return null;
	}
}
