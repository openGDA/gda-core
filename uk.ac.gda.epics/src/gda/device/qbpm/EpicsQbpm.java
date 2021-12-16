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

package gda.device.qbpm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.ControllerRecord;
import gda.device.DeviceException;
import gda.device.EnumPositionerStatus;
import gda.device.Monitor;
import gda.device.Qbpm;
import gda.device.Scannable;
import gda.device.enumpositioner.EnumPositionerBase;
import gda.device.enumpositioner.EpicsCurrAmpQuadController;
import gda.device.monitor.EpicsBpmController;
import gda.epics.connection.InitializationListener;
import gda.epics.util.PVNameUtil;
import gda.factory.FactoryException;
import gda.factory.Finder;

/**
 * EpicsQbpm Class
 */
public class EpicsQbpm extends EnumPositionerBase implements Monitor, InitializationListener, Qbpm, ControllerRecord {

	private static final Logger logger = LoggerFactory.getLogger(EpicsQbpm.class);

	private String bpmName = null;

	private String currAmpQuadName = null;

	private EpicsCurrAmpQuadController currAmpController;

	private EpicsBpmController bpmController;

	@Override
	public void configure() throws FactoryException {
		if (!isConfigured()) {

			List<String> inputNamesList = new ArrayList<>();
			List<String> extraNamesList = new ArrayList<>();

			if (currAmpController == null) {
				if (getCurrAmpQuadName() != null) {
					currAmpController = (EpicsCurrAmpQuadController) Finder.find(getCurrAmpQuadName());
					// set Input Names
				} else {
					logger.error("Missing EPICS interface configuration for the current amplifier " + getName());
					throw new FactoryException("Missing EPICS interface configuration for the current amplifier "
							+ getName());
				}
			}
			inputNamesList.addAll(Arrays.asList(currAmpController.getInputNames()));
			extraNamesList.addAll(Arrays.asList(currAmpController.getExtraNames()));

			if (bpmController == null) {
				if (getBpmName() != null) {
					bpmController = (EpicsBpmController) Finder.find(getBpmName());
					extraNames = bpmController.getInputNames();
				} else {
					logger.error("Missing EPICS interface configuration for the BPM device " + getBpmName());
					throw new FactoryException("Missing EPICS interface configuration for the BPM device " + getBpmName());
				}
			}
			inputNamesList.addAll(Arrays.asList(bpmController.getInputNames()));
			extraNamesList.addAll(Arrays.asList(bpmController.getExtraNames()));

			inputNames = inputNamesList.toArray(new String[] {});
			extraNames = extraNamesList.toArray(new String[] {});

			setConfigured(true);
		}// end of if (!configured)
	}

	@Override
	public String getBpmName() {
		return bpmName;
	}

	@Override
	public void setBpmName(String name) {
		this.bpmName = name;
	}

	@Override
	public String getCurrAmpQuadName() {
		return currAmpQuadName;
	}

	@Override
	public void setCurrAmpQuadName(String name) {
		this.currAmpQuadName = name;
	}

	@Override
	public int getElementCount() throws DeviceException {
		return 8;
	}

	@Override
	public String getUnit() throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initializationCompleted() {
		// this page intentionally left blank
	}

	@Override
	public EnumPositionerStatus getStatus() throws DeviceException {
		return this.currAmpController.getStatus();
	}

	@Override
	public void moveTo(Object position) throws DeviceException {
		currAmpController.moveTo(position);
	}

	@Override
	public void rawAsynchronousMoveTo(Object externalPosition) throws DeviceException {
		currAmpController.moveTo(externalPosition);
	}

	@Override
	public double getCurrent1() throws DeviceException {
		return this.currAmpController.getCurrent1();
	}

	@Override
	public double getCurrent2() throws DeviceException {
		return this.currAmpController.getCurrent2();
	}

	@Override
	public double getCurrent3() throws DeviceException {
		return this.currAmpController.getCurrent3();
	}

	@Override
	public double getCurrent4() throws DeviceException {
		return this.currAmpController.getCurrent4();
	}

	@Override
	public String getRangeValue() throws DeviceException {
		return this.currAmpController.getRangeValue();
	}

	@Override
	public double getIntensityTotal() throws DeviceException {
		return this.bpmController.getIntensityTotal();
	}

	@Override
	public double getXPosition() throws DeviceException {
		return this.bpmController.getXPosition();
	}

	@Override
	public double getYPosition() throws DeviceException {
		return this.bpmController.getYPosition();
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		String[] value = new String[8];
		value[0] = getRangeValue();
		value[1] = String.format(getOutputFormat()[0], getCurrent1());
		value[2] = String.format(getOutputFormat()[0], getCurrent2());
		value[3] = String.format(getOutputFormat()[0], getCurrent3());
		value[4] = String.format(getOutputFormat()[0], getCurrent4());
		value[5] = String.format(getOutputFormat()[0], getIntensityTotal());
		value[6] = String.format(getOutputFormat()[0], getXPosition());
		value[7] = String.format(getOutputFormat()[0], getYPosition());
		return value;
	}

	@Override
	public String toFormattedString() {
		try {
			// get the current position as an array of doubles
			Object position = getPosition();

			// if position is null then simply return the name
			if (position == null) {
				logger.warn("getPosition() from " + getName() + " returns NULL.");
				return getName() + " : NOT AVAILABLE";
			}

			String[] positionAsArray = getCurrentPositionArray(this);

			// if cannot create array of doubles then use position's toString
			// method
			if (positionAsArray == null || positionAsArray.length == 1) {
				return getName() + " : " + position.toString();
			}

			// else build a string of formatted positions
			String output = getName() + " : ";
			int i = 0;
			for (; i < this.inputNames.length; i++) {
				output += this.inputNames[i] + ": " + positionAsArray[i] + " ";
			}

			for (int j = 0; j < this.extraNames.length; j++) {
				output += this.extraNames[j] + ": " + positionAsArray[i + j] + " ";
			}
			return output.trim();
		} catch (Exception e) {
			logger.warn("{}: exception while getting position. ", getName(), e);
			return valueUnavailableString();
		}
	}

	/**
	 * converts object to String array
	 *
	 * @param scannable
	 * @return String Array
	 * @throws Exception
	 */
	public static String[] getCurrentPositionArray(Scannable scannable) throws Exception {

		// get object returned by getPosition
		Object currentPositionObj = scannable.getPosition();

		// if its null or were expecting it to be null from the arrays, return
		// null
		if (currentPositionObj == null
				|| (scannable.getInputNames().length == 0 && scannable.getExtraNames().length == 0)) {
			return null;
		}

		// else create an array of the expected size and fill it
		String[] currentPosition = new String[scannable.getInputNames().length + scannable.getExtraNames().length];
		currentPosition = (String[]) currentPositionObj;

		return currentPosition;

	}

	public EpicsCurrAmpQuadController getCurrAmpController() {
		return currAmpController;
	}

	public void setCurrAmpController(EpicsCurrAmpQuadController currAmpController) {
		this.currAmpController = currAmpController;
	}

	public EpicsBpmController getBpmController() {
		return bpmController;
	}

	public void setBpmController(EpicsBpmController bpmController) {
		this.bpmController = bpmController;
	}

	@Override
	public String getControllerRecordName() {
		// assume that the common prefix of the pv names is the base pv name
		return PVNameUtil.getBasePvName(
				currAmpController.getControllerRecordName(), bpmController.getControllerRecordName());
	}

}