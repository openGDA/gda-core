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
 * This motor does not monitor PVs as they are continuously updating even they had reached the target and stopped.
 */
public class FeedbackControlledEpicsMotor extends EpicsMotor {

	@Override
	protected void createChannelAccess() throws FactoryException {
		try {
			rbv = channelManager.createChannel(pvName + ".RBV", false);
			dmov = channelManager.createChannel(pvName + ".DMOV", false);
			dhlm = channelManager.createChannel(pvName + ".DHLM", false);
			dllm = channelManager.createChannel(pvName + ".DLLM", false);
			msta = channelManager.createChannel(pvName + ".MSTA", false);
			// acknowledge that creation phase is completed
			channelManager.creationPhaseCompleted();
			super.createChannelAccess();

		} catch (CAException e) {
			throw new FactoryException("failed to connect to all channels", e);
		}
	}


}
