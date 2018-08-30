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

import org.springframework.beans.factory.InitializingBean;

import gda.device.detector.areadetector.v17.NDPluginBase;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;
import uk.ac.gda.devices.excalibur.Master;

/**
 *
 */
public class DummyMasterImpl  implements Master, InitializingBean {

	private NDPluginBase pluginBase;
	private int frameDivisorRBV;
	private int frameDivisor;
	private boolean serverConnected;
	private int serverPort;
	private String serverAddress="serverAddress";

	@Override
	public NDPluginBase getPluginBase() {
		return pluginBase;
	}

	public void setPluginBase(NDPluginBase pluginBase) {
		this.pluginBase = pluginBase;
	}

	@Override
	public String getServerAddress() throws TimeoutException, CAException, InterruptedException, Exception {
		return serverAddress;
	}

	@Override
	public int getServerPort() throws TimeoutException, CAException, InterruptedException, Exception {
		return serverPort;
	}

	@Override
	public boolean isServerConnected() throws TimeoutException, CAException, InterruptedException, Exception {
		return serverConnected;

	}

	@Override
	public int getFrameDivisor() throws TimeoutException, CAException, InterruptedException, Exception {
		return frameDivisor;
	}

	@Override
	public int getFrameDivisor_RBV() throws TimeoutException, CAException, InterruptedException, Exception {
		return frameDivisorRBV;
	}

	@Override
	public void setFrameDivisor(int frameDivisor) throws CAException, InterruptedException, Exception {
		this.frameDivisorRBV = this.frameDivisor = frameDivisor;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (pluginBase == null) {
			throw new IllegalArgumentException("'pluginBase' needs to be provided");
		}
	}
}
