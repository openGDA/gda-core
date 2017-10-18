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

import gda.factory.FactoryException;
/**
 * This motor does not monitor the readback PV as it is continously updating even it had reached the target and stopped.
 */
public class FeedbackControlledEpicsMotor extends EpicsMotor {
	public FeedbackControlledEpicsMotor() {
		super();
	}
	@Override
	public void configure() throws FactoryException {
		super.configure();
	}
	@Override
	protected void createChannelAccess() throws FactoryException {
		try {
			val = channelManager.createChannel(pvName + ".VAL", false);
			rbv = channelManager.createChannel(pvName + ".RBV", false);
			offset = channelManager.createChannel(pvName + ".OFF", false);
			stop = channelManager.createChannel(pvName + ".STOP", false);
			velo = channelManager.createChannel(pvName + ".VELO", false);
			accl = channelManager.createChannel(pvName + ".ACCL", false);
			dmov = channelManager.createChannel(pvName + ".DMOV", false);
			lvio = channelManager.createChannel(pvName + ".LVIO");
			hlm = channelManager.createChannel(pvName + ".HLM", highLimitMonitor, false);
			llm = channelManager.createChannel(pvName + ".LLM", lowLimitMonitor, false);
			dhlm = channelManager.createChannel(pvName + ".DHLM", false);
			dllm = channelManager.createChannel(pvName + ".DLLM", false);
			homf = channelManager.createChannel(pvName + ".HOMF", false);

			rdbd = channelManager.createChannel(pvName + ".RDBD", false);
			mres = channelManager.createChannel(pvName + ".MRES", false);
			unitString = channelManager.createChannel(pvName + ".EGU", false);
			msta = channelManager.createChannel(pvName + ".MSTA", false);
			spmg = channelManager.createChannel(pvName + ".SPMG", false);

			// acknowledge that creation phase is completed
			channelManager.creationPhaseCompleted();

		} catch (Throwable th) {
			// TODO take care of destruction
			throw new FactoryException("failed to connect to all channels", th);
		}
	}


}
