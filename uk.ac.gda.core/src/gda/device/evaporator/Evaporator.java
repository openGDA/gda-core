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

package gda.device.evaporator;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

import java.util.List;

import org.python.core.Py;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.factory.FindableBase;

/**
 * GDA user facing device to control a multi-pocket evaporator
 * <p>
 * This is independent of the underlying hardware and relies on an
 * {@link EvaporatorController} to communicate with the device.
 * <p>
 * This class just needs a name (to be Findable) and a controller.
 * <pre>
 * {@code<bean id="ebe" class="gda.device.evaporator.Evaporator">
 *     <property name="controller" ref="ebe_controller"/>
 * </bean>}
 * </pre>
 */
public class Evaporator extends FindableBase {
	private static final Logger logger = LoggerFactory.getLogger(Evaporator.class);

	private EvaporatorController controller;

	public void enable() throws DeviceException {
		controller.setEnabled(true);
	}

	public void disable() throws DeviceException {
		controller.setEnabled(false);
	}

	public boolean isEnabled() throws DeviceException {
		return controller.isEnabled();
	}

	/**
	 * Enable remote control of the device
	 * <br/>
	 * With remote disabled, the device is not accessible from GDA
	 */
	public void remoteOn() throws DeviceException {
		controller.setRemote(true);
	}

	/**
	 * Disable remote control of the device
	 *
	 * @see #remoteOn
	 */
	public void remoteOff() throws DeviceException {
		controller.setRemote(false);
	}

	/**
	 * Check if this device is controllable from GDA
	 * <br/>
	 * @see #remoteOn
	 */
	public boolean isRemote() throws DeviceException {
		return controller.isRemote();
	}

	/** Switch high voltage on **/
	public void hvOn() throws DeviceException {
		controller.setHighVoltageEnabled(true);
	}

	/** Switch high voltage off **/
	public void hvOff() throws DeviceException {
		controller.setHighVoltageEnabled(false);
	}

	/** Check if high voltage is on or off */
	public boolean isHvEnabled() throws DeviceException {
		return controller.isHighVoltageEnabled();
	}

	/** Get voltage */
	public double getHighVoltage() throws DeviceException {
		return controller.getHighVoltage();
	}

	/** Set voltage */
	public void setHighVoltage(double hv) throws DeviceException {
		controller.setHighVoltage(hv);
	}

	/**
	 * Get the current shutter position
	 * @return the current shutter position name
	 * @throws DeviceException if the device is unreachable
	 */
	public String getShutter() throws DeviceException {
		return controller.getShutter();
	}

	/**
	 * Set the shutter position by name
	 * @param pos must be one of the valid shutter positions for this device
	 * @throws DeviceException if the device is unreachable
	 */
	public void setShutter(String pos) throws DeviceException {
		controller.setShutter(pos);
	}

	/**
	 * Set the shutter position by index
	 * @param shutter The index of the required shutter position
	 * @throws DeviceException if the device is unreachable
	 */
	public void setShutter(int shutter) throws DeviceException {
		controller.setShutter(getShutterPositions().get(shutter));
	}

	/** Clear any error state of the device */
	public void clearError() throws DeviceException {
		 controller.clearError();
	}

	/**
	 * Get a list of valid shutter positions.
	 *
	 * @return Unmodifiable list of valid shutter positions
	 * @throws DeviceException if the device is unreachable
	 */
	public List<String> getShutterPositions() throws DeviceException {
		String[] positions = controller.getShutterPositions();
		return unmodifiableList(asList(positions));
	}

	/**
	 * Get the name of the current shutter position
	 * @return The current shutter position
	 * @throws DeviceException if the device is unreachable
	 */
	public String getShutterPosition() throws DeviceException {
		return controller.getShutter();
	}

	/**
	 * Get a specific pocket from the evaporator. Pockets are 1-indexed.
	 *
	 * @param pocket 1-indexed pocket number
	 * @return EvaporatorPocket
	 */
	public EvaporatorPocket getPocket(int pocket) {
		return controller.getPocket(pocket);
	}

	/**
	 * Convenience method for access from Jython
	 * <br/>
	 * Allows direct access to pockets eg
	 * <pre>>>> ebe[1]</pre>
	 * is equivalent to
	 * <pre>>>> ebe.getPocket(1)</pre>
	 * This should not be used from Java
	 */
	public EvaporatorPocket __getitem__(int pocket) {
		return getPocket(pocket);
	}

	/**
	 * Convenience method for access from Jython
	 * <br/>
	 * Allows direct access to pockets eg
	 * <pre>>>> ebe.p1</pre>
	 * is equivalent to
	 * <pre>>>> ebe.getPocket(1)</pre>
	 * This should not be used from Java
	 */
	public EvaporatorPocket __getattr__(String name) {
		if (name.startsWith("p")) {
			try {
				Integer pocket = Integer.valueOf(name.substring(1));
				return getPocket(pocket);
			} catch (NumberFormatException e) {
				throw Py.AttributeError("Could not parse pocket number");
			}
		}
		throw Py.AttributeError(getClass().getSimpleName() + " has no attribute " + name);
	}

	public void setController(EvaporatorController controller) {
		this.controller = controller;
	}

	@Override
	public String toString() {
		try {
			return "Evaporator("
					+ (isEnabled() ? "Enabled" : "Disabled")
					+ ", " + controller.getNumberOfPockets()
					+ " pockets, shutter='"
					+ getShutter()
					+ "', HighVoltage: "
					+ (isHvEnabled() ? "On" : "Off")
					+ ")";
		} catch (DeviceException e) {
			logger.warn("{}: Couldn't get information for toString", getName(), e);
			return "Evaporator(UNKNOWN STATE)";
		}
	}
}
