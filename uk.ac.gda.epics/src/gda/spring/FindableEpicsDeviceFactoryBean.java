/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.spring;

import java.util.HashMap;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

import gda.device.epicsdevice.EpicsDevice;
import gda.device.epicsdevice.FindableEpicsDevice;

/**
 * Spring {@link FactoryBean} for creating {@link FindableEpicsDevice}s.
 *
 * Either recordPvs or deviceName must be specified, setting both or neither will cause an exception.
 */
public class FindableEpicsDeviceFactoryBean implements FactoryBean<FindableEpicsDevice>, InitializingBean, BeanNameAware {

	protected String name;

	@Override
	public void setBeanName(String name) {
		this.name = name;
	}

	protected HashMap<String, String> recordPvs;
	private String deviceName;

	boolean local=false;

	public boolean isLocal() {
		return local;
	}

	public void setLocal(boolean local) {
		this.local = local;
	}

	protected boolean dummyMode;

	/**
	 * Sets the PVs held by the EPICS device.
	 *
	 * @param recordPvs the PVs
	 */
	public void setRecordPvs(HashMap<String, String> recordPvs) {
		this.recordPvs = recordPvs;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	/**
	 * Sets whether the EPICS device should be in dummy mode.
	 *
	 * @param dummyMode {@code true} to put the EPICS device into dummy mode
	 */
	public void setDummyMode(boolean dummyMode) {
		this.dummyMode = dummyMode;
	}

	protected FindableEpicsDevice findableEpicsDevice;

	@Override
	public void afterPropertiesSet() throws Exception {
		if (recordPvs == null && deviceName == null) {
			throw new IllegalStateException("Neither recordPVs nor deviceName have been set for EPICS device " + StringUtils.quote(name));
		}
		if (recordPvs != null && deviceName != null) {
			throw new IllegalStateException("Both recordPVs and deviceName have been set for EPICS device " + StringUtils.quote(name));
		}

		String safeName = name.replace(".", "_");
		if (deviceName != null) {
			findableEpicsDevice = new FindableEpicsDevice();
			findableEpicsDevice.setName(safeName);
			findableEpicsDevice.setDeviceName(deviceName);
			findableEpicsDevice.setDummy(dummyMode);
		}
		if (recordPvs != null) {
			EpicsDevice epicsDevice = new EpicsDevice(name, recordPvs, dummyMode);
			findableEpicsDevice = new FindableEpicsDevice(safeName, epicsDevice);
			findableEpicsDevice.setRecordPVs(this.recordPvs);
		}
	}

	@Override
	public Class<?> getObjectType() {
		return FindableEpicsDevice.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public FindableEpicsDevice getObject() throws Exception {
		return findableEpicsDevice;
	}

}
