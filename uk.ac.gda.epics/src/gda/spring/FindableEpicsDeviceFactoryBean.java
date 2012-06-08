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

import gda.device.epicsdevice.EpicsDevice;
import gda.device.epicsdevice.FindableEpicsDevice;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

/**
 * Spring {@link FactoryBean} for creating {@link FindableEpicsDevice}s.
 */
public class FindableEpicsDeviceFactoryBean implements FactoryBean<FindableEpicsDevice>, InitializingBean, BeanNameAware {

	protected String name;
	
	@Override
	public void setBeanName(String name) {
		this.name = name;
	}

	protected HashMap<String, String> recordPvs;
	
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
		if (recordPvs == null) {
			throw new IllegalStateException("You have not set the PVs for EPICS device " + StringUtils.quote(name));
		}
		EpicsDevice epicsDevice = new EpicsDevice(name, recordPvs, dummyMode);
		
		String safeName = name.replace(".", "_");
		findableEpicsDevice = new FindableEpicsDevice(safeName, epicsDevice);
		findableEpicsDevice.setRecordPVs(this.recordPvs);
		findableEpicsDevice.setLocal(local);
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
