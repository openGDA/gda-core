/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.excalibur.impl;

import gda.device.detector.areadetector.IPVProvider;
import gda.epics.connection.EpicsController;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;

import org.springframework.beans.factory.InitializingBean;

import uk.ac.gda.devices.excalibur.MasterSlaveSync;

/**
 *
 */
public class MasterSlaveSyncImpl implements MasterSlaveSync, InitializingBean {

	private IPVProvider pvProvider;
	private final static EpicsController EPICS_CONTROLLER = EpicsController.getInstance();

	private static final String MISMATCH = "MISMATCH";

	private static final String RESYNC = "RESEND.PROC";

	private Channel mismatchChannel;

	private Channel resyncChannel;

	@Override
	public String getMismatch() throws TimeoutException, CAException, InterruptedException, Exception {
		return EPICS_CONTROLLER.cagetString(getMismatchChannel());
	}

	@Override
	public void resync() throws CAException, InterruptedException, Exception {
		EPICS_CONTROLLER.caput(getResyncChannel(), 1);
	}

	public Channel getMismatchChannel() throws Exception {
		if (mismatchChannel == null) {
			if (pvProvider != null) {
				mismatchChannel = EPICS_CONTROLLER.createChannel(pvProvider.getPV(MISMATCH));
			}
		}
		return mismatchChannel;
	}

	public Channel getResyncChannel() throws Exception {
		if (resyncChannel == null) {
			if (pvProvider != null) {
				resyncChannel = EPICS_CONTROLLER.createChannel(pvProvider.getPV(RESYNC));
			}
		}
		return resyncChannel;
	}

	public IPVProvider getPvProvider() {
		return pvProvider;
	}

	public void setPvProvider(IPVProvider pvProvider) {
		this.pvProvider = pvProvider;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (pvProvider == null) {
			throw new IllegalArgumentException("Either 'pvProvider' or 'basePVName' needs to be provided");
		}
	}

}