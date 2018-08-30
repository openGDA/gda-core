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

import gda.device.detector.areadetector.v17.NDPluginBase;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;
import uk.ac.gda.devices.excalibur.Master;

/**
 *
 */
public class MasterImpl extends BasePvProvidingImpl implements Master {

	private static final String FRAME_DIVISOR_RBV = "FrameDivisor_RBV";
	private static final String FRAME_DIVISOR = "FrameDivisor";
	private static final String SERVER_CONNECTED = "ServerConnected";
	private static final String SERVER_PORT = "ServerPort";
	private static final String SERVER_ADDRESS = "ServerAddress";
	private NDPluginBase pluginBase;

	@Override
	public NDPluginBase getPluginBase() {
		return pluginBase;
	}

	public void setPluginBase(NDPluginBase pluginBase) {
		this.pluginBase = pluginBase;
	}

	@Override
	public String getServerAddress() throws TimeoutException, CAException, InterruptedException, Exception {
		return EPICS_CONTROLLER.caget(getChannel(SERVER_ADDRESS));
	}

	@Override
	public int getServerPort() throws TimeoutException, CAException, InterruptedException, Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(SERVER_PORT));
	}

	@Override
	public boolean isServerConnected() throws TimeoutException, CAException, InterruptedException, Exception {
		return EPICS_CONTROLLER.cagetEnum(getChannel(SERVER_CONNECTED)) == 1;

	}

	@Override
	public int getFrameDivisor() throws TimeoutException, CAException, InterruptedException, Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(FRAME_DIVISOR));
	}

	@Override
	public int getFrameDivisor_RBV() throws TimeoutException, CAException, InterruptedException, Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(FRAME_DIVISOR_RBV));
	}

	@Override
	public void setFrameDivisor(int frameDivisor) throws CAException, InterruptedException, Exception {
		EPICS_CONTROLLER.caput(getChannel(FRAME_DIVISOR), frameDivisor);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		if (pluginBase == null) {
			throw new IllegalArgumentException("'pluginBase' needs to be provided");
		}
	}
}
