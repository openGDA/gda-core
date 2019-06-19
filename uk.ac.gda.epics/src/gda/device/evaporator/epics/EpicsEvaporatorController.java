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

package gda.device.evaporator.epics;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.enumpositioner.EpicsSimpleBinary;
import gda.device.enumpositioner.EpicsSimpleMbbinary;
import gda.device.evaporator.EvaporatorController;
import gda.device.evaporator.EvaporatorPocket;
import gda.device.scannable.MultiPVScannable;
import gda.epics.connection.EpicsController;
import gda.factory.FactoryException;
import gda.factory.FindableConfigurableBase;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;

/**
 * EPICS controller for a multi pocket evaporator.
 * <br/>
 * This provides access to the individual pockets as well as the general
 * controls (enable/disable, highVoltage on/off etc)
 * </br>
 * To configure in spring, this needs the number of pockets and the base PV up
 * to (but not including) the final ':' (of a general/non-pocket PV). eg:
 * <pre>
 * {@code
 * <bean id="ebe" class="gda.device.evaporator.EpicsEvaporatorController" >
 *     <property name="pvBase" value="BL07I-EA-EBE-01" />
 *     <property name="numberOfPockets" value="4" />
 * </bean>
 * }
 * </pre>
 */
public class EpicsEvaporatorController extends FindableConfigurableBase implements EvaporatorController {

	private static final Logger logger = LoggerFactory.getLogger(EpicsEvaporatorController.class);

	private static final String ENABLED = "Enabled";
	private static final String REMOTE = "Remote";
	private EpicsSimpleBinary enabled;
	private EpicsSimpleBinary remote;
	private EpicsSimpleMbbinary shutters;
	private List<EvaporatorPocket> pockets;
	private EpicsSimpleBinary highVoltageEnabled;
	private MultiPVScannable highVoltage;

	private Channel clearError;

	private int numberOfPockets;
	private String pvBase;

	@Override
	public void setEnabled(boolean enable) throws DeviceException {
		enabled.moveTo(enable ? "Enabled" : "Disabled");
	}

	@Override
	public boolean isEnabled() throws DeviceException {
		return ENABLED.equals(enabled.getPosition());
	}

	@Override
	public void setRemote(boolean remote) throws DeviceException {
		this.remote.moveTo(remote ? "Remote" : "Local");
	}

	@Override
	public boolean isRemote() throws DeviceException {
		return REMOTE.equals(remote.getPosition());
	}

	@Override
	public void setHighVoltage(double hv) throws DeviceException {
		highVoltage.moveTo(hv);
	}

	@Override
	public double getHighVoltage() throws DeviceException {
		return (double) highVoltage.getPosition();
	}

	@Override
	public void setHighVoltageEnabled(boolean enable) throws DeviceException {
		highVoltageEnabled.moveTo(enable ? "On" : "Off");
	}

	@Override
	public boolean isHighVoltageEnabled() throws DeviceException {
		return "On".equals(highVoltageEnabled.getPosition());
	}

	@Override
	public void setShutter(String shutterPosition) throws DeviceException {
		shutters.moveTo(shutterPosition);
	}

	@Override
	public String getShutter() throws DeviceException {
		return (String) shutters.getPosition();
	}

	@Override
	public String[] getShutterPositions() throws DeviceException {
		return shutters.getPositions();
	}

	@Override
	public int getNumberOfPockets() {
		return pockets.size();
	}

	@Override
	public EvaporatorPocket getPocket(int pocket) {
		// Pockets are 1-indexed to user so offset here to match
		return pockets.get(pocket-1);
	}

	@Override
	public void clearError() throws DeviceException {
		// clearError does not yet work correctly in epics. For now, set remoteOn again to reset the connection
		setRemote(true);
//		try {
//			EpicsController.getInstance().caput(clearError, 1);
//		} catch (CAException | InterruptedException e) {
//			throw new DeviceException("Could not clear errors", e);
//		}
	}

	public void setPvBase(String pvBase) {
		this.pvBase = pvBase;
	}

	public void setPocketCount(int pocketCount) {
		this.numberOfPockets = pocketCount;
	}

	@Override
	public void configure() throws FactoryException {
		requireNonNull(pvBase, "pvBase must be set");
		final String name = getName();

		enabled = new EpicsSimpleBinary();
		enabled.setName(name + "_enabled");
		enabled.setPvName(pvBase + ":DISABLE");
		enabled.configure();

		remote = new EpicsSimpleBinary();
		remote.setName(name + "_remote");
		remote.setPvName(pvBase + ":MODE");
		remote.configure();

		shutters = new EpicsSimpleMbbinary();
		shutters.setName(name + "_shutters");
		shutters.setRecordName(pvBase + ":SHUTTER");
		shutters.setReadOnly(false);
		shutters.configure();

		try {
			pockets = range(0,  numberOfPockets)
					.mapToObj(i -> new EpicsEvaporatorPocket(name, pvBase, i+1))
					.collect(toList());
		} catch (RuntimeException re) {
			throw new FactoryException("Could not create pockets", re.getCause());
		}

		highVoltageEnabled = new EpicsSimpleBinary();
		highVoltageEnabled.setName(name + "_hvEnable");
		highVoltageEnabled.setPvName(pvBase + ":VSTATE");
		highVoltageEnabled.configure();

		highVoltage = new MultiPVScannable();
		highVoltage.setName(name + "_hv");
		highVoltage.setWritePV(pvBase + ":VOUT");
		highVoltage.setReadPV(pvBase + ":VMON");
		highVoltage.configure();

		try {
			clearError = EpicsController.getInstance().createChannel(pvBase + ":CLEAR_ERROR");
		} catch (CAException | TimeoutException e) {
			logger.error("Could not create CLEAR_ERROR chanel", e);
			throw new FactoryException("Could not create error channel", e);
		}
	}

	@Override
	public String toString() {
		return String.format("EpicsEvaporatorController: %s", pvBase);
	}
}
