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
import gda.epics.interfaceSpec.GDAEpicsInterfacePVProvider;

public class XMLPVProvider implements IPVProvider {

	String deviceName;
	
	@Override
	public String getPV(String key) throws Exception {
		GDAEpicsInterfacePVProvider gdaProvider = new GDAEpicsInterfacePVProvider();
		gdaProvider.setDeviceName(getDeviceName());
		gdaProvider.setFieldName(key);
		return gdaProvider.getPV();
	}

	/**
	 * @return Returns the deviceName.
	 */
	public String getDeviceName() {
		return deviceName;
	}

	/**
	 * @param deviceName The deviceName to set.
	 */
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

}