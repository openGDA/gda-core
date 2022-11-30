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

package gda.device.motor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.MotorException;
import gda.epics.connection.EpicsController.MonitorType;
import gda.factory.FactoryException;
/**
 * The Extended Epics Motor class bypasses the motor record when moving motors.
 * The EPICS pmac driver replaces tpmac.This provides a Direct Demand PV for each motor record,
 * allowing motors to move without going through the motor record interface.
 * pmac also provides an improved co-ordinated deferred move mechanism that has allows removal of
 * significant error recovery code.
 */
public class EpicsMotorDirect extends EpicsMotor {

	private static final Logger logger = LoggerFactory.getLogger(EpicsMotorDirect.class);

	private String pvNameDirect;

	public void setPvNameDirect(String pvNameDirect) {
		this.pvNameDirect = pvNameDirect;
	}

	public String getPvNameDirect() {
		return pvNameDirect;
	}

	@Override
	public void configure() throws FactoryException {
		if (pvNameDirect == null) {
			throw new IllegalStateException("pvNameDirect is required to configure EpicsMotorDirect");
		}
		super.configure();
	}

	/**
	 * Create Channel access for motor. Note that val now is intended to be assigned the
	 * PV string that sends a position directly to a motor, rather than via the motor record.
	 */
	@Override
	protected void createChannelAccess() throws FactoryException {
		try {
			val = channelManager.createChannel(pvNameDirect, false);
			rbv = channelManager.createChannel(pvName + ".RBV", positionMonitor, MonitorType.TIME, false);
			offset = channelManager.createChannel(pvName + ".OFF", false);
			stop = channelManager.createChannel(pvName + ".STOP", false);
			velo = channelManager.createChannel(pvName + ".VELO", false);
			accl = channelManager.createChannel(pvName + ".ACCL", false);
			dmov = channelManager.createChannel(pvName + ".DMOV", statusMonitor, false);
			lvio = channelManager.createChannel(pvName + ".LVIO");
			hlm = channelManager.createChannel(pvName + ".HLM", highLimitMonitor, false);
			llm = channelManager.createChannel(pvName + ".LLM", lowLimitMonitor, false);

			dhlm = channelManager.createChannel(pvName + ".DHLM", dialHighLimitMonitor, false);
			dllm = channelManager.createChannel(pvName + ".DLLM", dialLowLimitMonitor, false);
			homf = channelManager.createChannel(pvName + ".HOMF", false);

			rdbd = channelManager.createChannel(pvName + ".RDBD", false);
			mres = channelManager.createChannel(pvName + ".MRES", false);
			unitString = channelManager.createChannel(pvName + ".EGU", false);
			msta = channelManager.createChannel(pvName + ".MSTA", mstaMonitorListener, false);
			spmg = channelManager.createChannel(pvName + ".SPMG", false);
			setPv = channelManager.createChannel(pvName + ".SET", setUseListener, false);

			// acknowledge that creation phase is completed
			channelManager.creationPhaseCompleted();

		} catch (Exception th) {
			throw new FactoryException("failed to connect to all channels", th);
		}
	}

	@Override
	public void setSpeed(double mmPerSec) throws MotorException {
		logger.warn("setSpeed({}) on {} - EpicsMotorDirect devices ignore speed", mmPerSec, getName());
		super.setSpeed(mmPerSec);
	}

	@Override
	public double getSpeed() throws MotorException {
		logger.warn("getSpeed() on {} - EpicsMotorDirect devices ignore speed", getName());
		return super.getSpeed();
	}
}
