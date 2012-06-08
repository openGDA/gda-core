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

import gda.configuration.epics.ConfigurationNotFoundException;
import gda.device.monitor.EpicsMonitor;
import gda.epics.interfaces.SimplePvType;

import org.springframework.beans.factory.FactoryBean;

/**
 * A {@link FactoryBean} for creating {@link EpicsMonitor} objects.
 */
public class EpicsMonitorFactoryBean extends EpicsConfigurationFactoryBeanBase<EpicsMonitor> {

	private String deviceName;
	
	/**
	 * Sets the EPICS device name which will be used to obtain the PV record
	 * name.
	 * 
	 * @param deviceName the EPICS device name
	 */
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}
	
	@Override
	public Class<?> getObjectType() {
		return EpicsMonitor.class;
	}

	private EpicsMonitor epicsMonitor;
	
	@Override
	protected void createObject() throws ConfigurationNotFoundException {
		SimplePvType simplePv = getEpicsConfiguration().getConfiguration(deviceName, SimplePvType.class);
		String recordName = simplePv.getRECORD().getPv();
		epicsMonitor = new EpicsMonitor();
		epicsMonitor.setName(name);
		epicsMonitor.setPvName(recordName);
	}

	@Override
	public EpicsMonitor getObject() throws Exception {
		return epicsMonitor;
	}

}
