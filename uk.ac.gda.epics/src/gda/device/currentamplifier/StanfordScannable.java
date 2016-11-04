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
import gda.epics.CAClient;
import gda.jython.InterfaceProvider;

/**
 * Scannable to drive Stanford current amplifier, originally written for B18 and
 * moved from a B18 specific plugin to uk.ac.gda.epics.
 * To facilitate exporting from server to client over RMI, it has been modified to implement the
 * {@link StanfordAmplifier} interface and to extend {@link CurrentAmplifierBase}.
 * <p>
 * See also {@link DummyStanfordScannable}
 *
 * @since 12/10/2016
 */
public class StanfordScannable extends CurrentAmplifierBase implements StanfordAmplifier {

	private static final Logger logger = LoggerFactory.getLogger(StanfordScannable.class);

	private CAClient ca_client = new CAClient();

	private static final String[] positionLabels = { "1", "2", "5", "10", "20", "50", "100", "200", "500" };
	private static final String[] gainUnitLabels = { "pA/V", "nA/V", "uA/V", "mA/V" };
	private static final String[] offsetUnitLabels = { "pA", "nA", "uA" };
	protected List<String> currentOffsetUnits = new ArrayList<String>();

	private String base_pv;
	private static final String SENS_PV = "SENS:SEL1";
	private static final String SENS_UNIT_PV = "SENS:SEL2";
	private static final String OFFSET_PV = "IOLV:SEL1";
	private static final String OFFSET_UNIT_PV = "IOLV:SEL2";
	private static final String OFFSET_CURRENT_ON_PV = "IOON";
	private static final String MODE_PV = "GNMD";

	private static final String unsupportedOperationMessage = "Stanford SR570 does not support this operation";

	public StanfordScannable() {
		gainPositions.addAll(Arrays.asList(positionLabels));
		gainUnits.addAll(Arrays.asList(gainUnitLabels));
		currentOffsetUnits.addAll(Arrays.asList(offsetUnitLabels));
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {

		String sensitivity = String.valueOf(position);
		String value = sensitivity.substring(0, sensitivity.indexOf(" "));
		String unit = sensitivity.substring(sensitivity.indexOf(" ")+1);

		try {
			setSensitivity(Integer.parseInt(value));
			setSensitivityUnit(Integer.parseInt(unit));
		} catch (Exception e) {
			if( e instanceof DeviceException)
				throw (DeviceException)e;
			throw new DeviceException(getName() +" exception in rawAsynchronousMoveTo", e);
		}
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		this.inputNames = new String[]{getName()};
		try {
			return ca_client.caget(base_pv + SENS_PV) + " " + ca_client.caget(base_pv + SENS_UNIT_PV);
		} catch (Exception e) {
			if( e instanceof DeviceException)
				throw (DeviceException)e;
			throw new DeviceException(getName() +" exception in rawGetPosition", e);
		}
	}

	/**
	 * Perform caput operation, rethrow any exceptions as DeviceException
	 * @param pvSuffix
	 * @param value
	 * @throws DeviceException
	 */
	private void caputWrapper(String pvSuffix, int value ) throws DeviceException {
		String fullPv = base_pv+pvSuffix;
		try {
			ca_client.caput(fullPv, value);
		} catch (Exception throwable) {
			throw new DeviceException(fullPv + " failed to moveTo " + value, throwable);
		}
	}

	/**
	 * Perform caput operation, rethrow any exceptions as DeviceException. Position to move to must match one of the values in allowedValues array.
	 *
	 * @param pvSuffix
	 * @param value
	 * @param allowedValues
	 * @throws DeviceException
	 */
	private void caputAllowedValue(String pvSuffix, String value, List<String> allowedValues) throws DeviceException {
		int enumIndex = allowedValues.indexOf(value);
		if (enumIndex != -1) {
			caputWrapper(pvSuffix, enumIndex);
		} else {
			throw new DeviceException("Position " + value + " not allowed for PV " + base_pv + pvSuffix);
		}
	}

	/**
	 * Perform caget operation, rethrow any exceptions as DeviceException
	 *
	 * @param pvSuffix
	 * @param value
	 * @return value from
	 * @throws DeviceException
	 */
	private String cagetWrapper(String pvSuffix) throws DeviceException {
		String fullPv = base_pv+pvSuffix;
		try {
			return ca_client.caget(fullPv);
		} catch (Exception throwable) {
			throw new DeviceException(fullPv + " failed to return value", throwable);
		}
	}

	// Sensitivity/gain settings
	@Override
	public void setSensitivity(int sensitivityValue) throws DeviceException {
		caputWrapper(SENS_PV, sensitivityValue);
	}

	@Override
	public int getSensitivity() throws DeviceException {
		return Integer.parseInt(cagetWrapper(SENS_PV));
	}

	@Override
	public void setSensitivityUnit(int unit) throws DeviceException {
		caputWrapper(SENS_UNIT_PV, unit);
	}

	@Override
	public int getSensitivityUnit() throws DeviceException{
		return Integer.parseInt(cagetWrapper(SENS_UNIT_PV));
	}

	// Current offset settings
	@Override
	public void setOffsetCurrentOn(boolean switchOn) throws DeviceException {
		caputWrapper(OFFSET_CURRENT_ON_PV, switchOn ? 1 : 0);
	}

	@Override
	public boolean isOffsetCurrentOn() throws DeviceException {
		return Integer.parseInt(cagetWrapper(OFFSET_CURRENT_ON_PV)) == 1 ? true : false;
	}

	@Override
	public void setOffset(int offset) throws DeviceException {
		caputWrapper(OFFSET_PV, offset);
	}

	@Override
	public int getOffset() throws DeviceException {
		return Integer.parseInt(cagetWrapper(OFFSET_PV));
	}

	@Override
	public void setOffsetUnit(int unit) throws DeviceException {
		caputWrapper(OFFSET_UNIT_PV, unit);
	}

	@Override
	public int getOffsetUnit() throws DeviceException {
		return Integer.parseInt(cagetWrapper(OFFSET_UNIT_PV));
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

	public void setBase_pv(String basePv) {
		base_pv = basePv;
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
		logger.debug(unsupportedOperationMessage);
		throw new UnsupportedOperationException(unsupportedOperationMessage);
	}

	@Override
	public void setGain(String position) throws DeviceException {
		caputAllowedValue(SENS_PV, position, gainPositions);
	}

	@Override
	public String getGain() throws DeviceException {
		return cagetWrapper(SENS_PV);
	}

	@Override
	public void setGainUnit(String unit) throws DeviceException {
		caputAllowedValue(SENS_UNIT_PV, unit, gainUnits);
	}

	@Override
	public String getGainUnit() throws DeviceException {
		return cagetWrapper(SENS_UNIT_PV);
	}

	@Override
	public void setMode(String mode) throws DeviceException {
		logger.debug(unsupportedOperationMessage);
		throw new UnsupportedOperationException(unsupportedOperationMessage);
	}

	@Override
	public String getMode() throws DeviceException {
		return cagetWrapper(MODE_PV);
	}

	@Override
	public Status getStatus() throws DeviceException {
		logger.debug(unsupportedOperationMessage);
		throw new UnsupportedOperationException(unsupportedOperationMessage);
	}
}
