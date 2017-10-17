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

package gda.device.scannable;

import gda.device.DeviceException;
import gda.device.ScannableMotionUnits;

import org.apache.commons.configuration.ConfigurationException;
import org.jscience.physics.quantities.Quantity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Partially implemented class, does not extend
public class DummyPersistentUnitsScannable extends DummyPersistentScannable implements ScannableMotionUnits {
	private static final Logger mylogger = LoggerFactory.getLogger(DummyPersistentUnitsScannable.class);

	private String initialUnit = "mm";

	public void setInitialUnit(String initialUnit) {
		this.initialUnit = initialUnit;
	}

	@Override
	public void configure() {
		this.inputNames = new String[] { getName() };
	}
	@Override
	public void addAcceptableUnit(String newUnit) throws DeviceException {
		//empty
	}

	@Override
	public String[] getAcceptableUnits() {
		String[] returnArray = new String[1];
		returnArray[0]=getUserUnits();
		return returnArray;
	}

	@Override
	public String getHardwareUnitString() {
		return getUserUnits(); // Dummy does not maintain hardware units
	}

	@Override
	public String getUserUnits() {
		String propertyName = getName() + "PersistentUnit";
		if (configuration.getProperty(propertyName)== null) {
			mylogger.warn("Value "+propertyName + " does not exist, initializing to " + initialUnit);
			configuration.setProperty(propertyName, initialUnit);
			try {
				configuration.save();
			} catch (ConfigurationException e) {
				mylogger.error("configuration error when saving to UserConfiguration", e);
			}
		}

		return (String) configuration.getProperty(getName()+"PersistentUnit");
	}

	@Override
	public void setHardwareUnitString(String hardwareUnitString) {
		mylogger.warn("Hardware units not settable - set userUnits instead");
	}

	@Override
	public void setUserUnits(String userUnitsString) throws DeviceException {
		configuration.setProperty(getName()+"PersistentUnit",userUnitsString);
		try {
			configuration.save();
			notifyIObservers(getName(), getUserUnits());
		} catch (ConfigurationException e) {
			mylogger.error("Configuration exception in setUserUnits for DummyPersistentUnitsScannable",e);
		}
	}

	@Override
	public Object getAttribute(String attributeName) throws DeviceException {
		if (attributeName.equals(ScannableMotionUnits.USERUNITS)) {
			return this.getUserUnits();
		} else if (attributeName.equals(ScannableMotionUnits.HARDWAREUNITS)) {
			return this.getHardwareUnitString();
		}
		return super.getAttribute(attributeName);
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) {
		try {
			super.rawAsynchronousMoveTo(position.toString());
		} catch (DeviceException e) {
			mylogger.error("DeviceException while calling rawAsynchronousMoveTo:", e);
		}
	}
	@Override
	public Object rawGetPosition() throws DeviceException {
		double toReturn = Double.parseDouble((String) super.rawGetPosition());
		return toReturn;
	}

	@Override
	public String toString() {
		String toReturn = super.toString();
		return toReturn;
	}

	@Override
	public Quantity[] getPositionAsQuantityArray() throws DeviceException {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void setOffset(Object offsetPositionInExternalUnits) {
		throw new RuntimeException("Not implemented");
	}
}

