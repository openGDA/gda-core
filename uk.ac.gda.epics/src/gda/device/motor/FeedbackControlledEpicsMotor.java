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
import gov.aps.jca.CAException;
/**
 * This motor does not monitor the readback PV as it is continously updating even it had reached the target and stopped.
 */
public class FeedbackControlledEpicsMotor extends EpicsMotor {

	@Override
	protected void createChannelAccess() throws FactoryException {
		super.createChannelAccess();
		try {
			//remove monitor listeners
			rbv.destroy();
			dmov.destroy();
			dhlm.destroy();
			dllm.destroy();
			msta.destroy();
			//recreate channle without monitor listeners
			rbv = channelManager.createChannel(pvName + ".RBV", false);
			dmov = channelManager.createChannel(pvName + ".DMOV", false);
			dhlm = channelManager.createChannel(pvName + ".DHLM", false);
			dllm = channelManager.createChannel(pvName + ".DLLM", false);
			msta = channelManager.createChannel(pvName + ".MSTA", false);

			// acknowledge that creation phase is completed
			channelManager.creationPhaseCompleted();

		} catch (CAException th) {
			throw new FactoryException("failed to connect to all channels", th);
		}
	}


}
