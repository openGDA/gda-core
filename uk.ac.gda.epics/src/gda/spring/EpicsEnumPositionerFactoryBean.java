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

import gda.configuration.epics.ConfigurationNotFoundException;
import gda.device.enumpositioner.EpicsEnumPositioner;
import gda.device.enumpositioner.EpicsPneumaticCallback;

import org.springframework.beans.factory.FactoryBean;

/**
 * A {@link FactoryBean} for creating {@link EpicsPneumaticCallback} objects.
 */
public class EpicsEnumPositionerFactoryBean extends EpicsConfigurationFactoryBeanBase<EpicsEnumPositioner> {

	private String pvBase;
	
	@Override
	public Class<?> getObjectType() {
		return EpicsEnumPositioner.class;
	}

	private EpicsEnumPositioner epicsPneumaticCallback;
	
	@Override
	protected void createObject() throws ConfigurationNotFoundException {
		epicsPneumaticCallback = new EpicsEnumPositioner();
		epicsPneumaticCallback.setName(name);
		epicsPneumaticCallback.setPvBase(pvBase);
	}

	@Override
	public EpicsEnumPositioner getObject() throws Exception {
		return epicsPneumaticCallback;
	}

	public String getPvBase() {
		return pvBase;
	}

	public void setPvBase(String pvBase) {
		this.pvBase = pvBase;
	}
}