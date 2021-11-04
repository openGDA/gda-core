/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

import gda.epics.connection.EpicsController.MonitorType;
import gda.factory.FactoryException;
/**
 * This uses READY instead of DMOV to tell if the motor is available to move. This may be needed if additional functionality
 * is needed on a motor, such as preventing it moving when over temperature, without affecting the default behaviour.
 *
 * This motor does not monitor the readback PV as it is continuously updating even it had reached the target and stopped.
 */
public class EpicsReadyMotor extends EpicsMotor {

	@Override
	protected void createChannelAccess() throws FactoryException {
		try {
			val = channelManager.createChannel(pvName + ".VAL", false);
			rbv = channelManager.createChannel(pvName + ".RBV", positionMonitor, MonitorType.TIME, false);

			direction = channelManager.createChannel(pvName + ".DIR", false);
			offset = channelManager.createChannel(pvName + ".OFF", false);

			stop = channelManager.createChannel(pvName + ".STOP", false);
			velo = channelManager.createChannel(pvName + ".VELO", false);
			vmax = channelManager.createChannel(pvName + ".VMAX", false);
			accl = channelManager.createChannel(pvName + ".ACCL", false);
			dmov = channelManager.createChannel(pvName + ":READY", statusMonitor, false); // Non-standard
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

		} catch (Exception ex) {
			throw new FactoryException("failed to connect to all channels", ex);
		}
	}
}
