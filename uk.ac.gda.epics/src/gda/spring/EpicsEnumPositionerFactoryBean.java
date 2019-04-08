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

package gda.spring;

import org.springframework.beans.factory.FactoryBean;

import gda.configuration.epics.ConfigurationNotFoundException;
import gda.device.DeviceException;
import gda.device.enumpositioner.EpicsEnumPositioner;
import gda.device.enumpositioner.EpicsPneumaticCallback;

/**
 * A {@link FactoryBean} for creating {@link EpicsPneumaticCallback} objects.
 */
public class EpicsEnumPositionerFactoryBean extends EpicsConfigurationFactoryBeanBase<EpicsEnumPositioner> {

	private String pvBase;
	private Integer protectionLevel = null;

	@Override
	public Class<?> getObjectType() {
		return EpicsEnumPositioner.class;
	}

	private EpicsEnumPositioner epicsEnumPositioner;

	@Override
	protected void createObject() throws ConfigurationNotFoundException {
		epicsEnumPositioner = new EpicsEnumPositioner();
		epicsEnumPositioner.setName(name);
		epicsEnumPositioner.setPvBase(pvBase);
		try {
			if (protectionLevel != null)
					epicsEnumPositioner.setProtectionLevel(protectionLevel);
		} catch (DeviceException e) {
			throw new ConfigurationNotFoundException("cannot set protection level for "+name);
		}
	}

	@Override
	public EpicsEnumPositioner getObject() throws Exception {
		return epicsEnumPositioner;
	}

	public String getPvBase() {
		return pvBase;
	}

	public void setPvBase(String pvBase) {
		this.pvBase = pvBase;
	}

	public Integer getProtectionLevel() {
		return protectionLevel;
	}

	public void setProtectionLevel(Integer protectionLevel) {
		this.protectionLevel = protectionLevel;
	}
}