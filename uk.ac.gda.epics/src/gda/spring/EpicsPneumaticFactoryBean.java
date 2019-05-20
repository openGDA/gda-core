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

import org.springframework.beans.factory.FactoryBean;

import gda.configuration.epics.ConfigurationNotFoundException;
import gda.device.enumpositioner.EpicsPneumaticCallback;

/**
 * A {@link FactoryBean} for creating {@link EpicsPneumaticCallback} objects.
 */
public class EpicsPneumaticFactoryBean extends EpicsConfigurationFactoryBeanBase<EpicsPneumaticCallback> {

	private String pvName;
	private boolean statusPvIndicatesPositionOnly=false;


	/**
	 * Sets the EPICS PV which will be used.
	 *
	 * @param pvName the EPICS PV
	 */
	public void setPvName(String pvName) {
		this.pvName = pvName;
	}

	@Override
	public Class<?> getObjectType() {
		return EpicsPneumaticCallback.class;
	}

	private EpicsPneumaticCallback epicsPneumaticCallback;

	@Override
	protected void createObject() throws ConfigurationNotFoundException {
		epicsPneumaticCallback = new EpicsPneumaticCallback();
		epicsPneumaticCallback.setName(name);
		epicsPneumaticCallback.setStatusPvIndicatesPositionOnly(statusPvIndicatesPositionOnly);
		epicsPneumaticCallback.setPvBase(pvName);
	}

	@Override
	public EpicsPneumaticCallback getObject() throws Exception {
		return epicsPneumaticCallback;
	}

	public boolean isStatusPvIndicatesPositionOnly() {
		return statusPvIndicatesPositionOnly;
	}

	public void setStatusPvIndicatesPositionOnly(boolean statusPvIndicatesPositionOnly) {
		this.statusPvIndicatesPositionOnly = statusPvIndicatesPositionOnly;
	}

}
