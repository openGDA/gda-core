/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.gda.client.livecontrol;

import java.util.Map;
import java.util.Optional;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.EnumPositionerStatus;
import gda.device.Scannable;
import gda.device.scannable.ScannablePositionChangeEvent;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;
import gda.observable.IObserver;

/**
 * Subclass of {@link CommandControl} to create a button and run commands on the command server.
 * It monitors a scannable and button text and command to run changes based on scannable position.
 * Example use case is to move a shutter in and out as a single button.
 * <br><br>
 * Example bean definition:
 * <pre>
 * {@code
	<bean id="hard_shutter_toggle_button" class="uk.ac.gda.client.livecontrol.JythonMonitorScannableDynamicCommandControl">
		<property name="scannableName" value="fsi1"/>
		<property name="buttonTooltip" value="Move the hard x-ray shutter"/>
		<property name="group" value="Hard X-Ray" />
		<property name="buttonTextMap">
			<util:map map-class="java.util.HashMap">
				<entry key="In" value="Open" />
				<entry key="Out" value="Close" />
			</util:map>
		</property>
		<property name="commandMap">
			<util:map map-class="java.util.HashMap">
				<entry key="In" value="pos fsi1 'Out'" />
				<entry key="Out" value="pos fsi1 'In'" />
			</util:map>
		</property>
	</bean>
 * }
 * </pre>
 */
public class JythonMonitorScannableDynamicCommandControl extends CommandControl {
	private static final Logger logger = LoggerFactory.getLogger(JythonMonitorScannableDynamicCommandControl.class);

	private String scannableName;
	private Scannable scannable;

	protected Map<String, String> commandMap;
	protected Map<String, String> buttonTextMap;

	@Override
	public void createControl(Composite composite) {

		// Get the scannable with the finder
		Optional<Scannable> optionalScannable = Finder.findOptionalOfType(getScannableName(), Scannable.class);
		if (optionalScannable.isEmpty()) {
			logger.warn("Could not get scannable '{}' for live control", getScannableName());
			return;
		}
		scannable = optionalScannable.get();
		//Create local reference which correctly removes observer when disposed
		IObserver updater = this::updateScannable;
		scannable.addIObserver(updater);
		composite.addDisposeListener(e -> scannable.deleteIObserver(updater));

		try {
			setCommand(getCommandMapValue(scannable.getPosition()));
			setButtonText(getButtonTextMapValue(scannable.getPosition()));
		} catch (DeviceException e) {
			logger.error("TODO put description of error here", e);
		}

		super.createControl(composite);
	}

	@Override
	protected void runCommand(String command) {
		logger.debug("Running Jython command: {}", command);
		InterfaceProvider.getCommandRunner().runCommand(command);
	}

	public void updateScannable(final Object theObserved, final Object arg) {
		if (!(theObserved instanceof Scannable)) return;
		Display.getDefault().asyncExec(() -> {
			Object changeCode;
			if (arg.getClass().isArray()) {
				changeCode = ((Object[]) arg)[0];
			} else {
				changeCode = arg;
			}
			if (changeCode instanceof ScannablePositionChangeEvent changeEvent) {
				Object newPosition = changeEvent.newPosition;
				button.setText(getButtonTextMapValue(newPosition));
				setCommand(getCommandMapValue(newPosition));
			} else if (changeCode instanceof EnumPositionerStatus status) {
				if (status != EnumPositionerStatus.IDLE) return;
				try {
					Object newPosition = scannable.getPosition();
					logger.debug("Got new position {}",newPosition);
					logger.debug("Setting button text to {}",getButtonTextMapValue(newPosition));
					button.setText(getButtonTextMapValue(newPosition));
					setCommand(getCommandMapValue(newPosition));
				} catch (DeviceException e) {
					logger.error("Failed to update {}", getName(), e);
				}
			} else if (
				changeCode instanceof String
				|| changeCode instanceof Integer
				|| changeCode instanceof Double
				|| changeCode instanceof Boolean
				|| changeCode instanceof Byte
				|| changeCode instanceof Character
				|| changeCode instanceof Short
				|| changeCode instanceof Long
				|| changeCode instanceof Float
			) {
				button.setText(getButtonTextMapValue(changeCode));
				setCommand(getCommandMapValue(changeCode));
			}
		});
	}

	public Map<String, String> getButtonTextMap() {
		return buttonTextMap;
	}

	public void setButtonTextMap(Map<String, String> stateMap) {
		this.buttonTextMap = stateMap;
	}

	protected String getButtonTextMapValue(Object position) {
		return buttonTextMap.get(getButtonTextMapKey(position));
	}

	protected String getButtonTextMapKey(Object position) {
		return position.toString();
	}

	public Map<String, String> getCommandMap() {
		return commandMap;
	}

	public void setCommandMap(Map<String, String> commandMap) {
		this.commandMap = commandMap;
	}

	protected String getCommandMapValue(Object position) {
		return commandMap.get(getCommandMapKey(position));
	}

	protected String getCommandMapKey(Object position) {
		return position.toString();
	}

	public String getScannableName() {
		return scannableName;
	}

	public void setScannableName(String scannableName) {
		this.scannableName = scannableName;
	}
}
