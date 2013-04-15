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

import gda.device.detector.areadetector.IPVProvider;
import gda.device.detector.areadetector.v17.NDPluginBase;
import gda.device.detector.areadetector.v17.NDROI;
import gda.device.detector.areadetector.v17.impl.NDROIImpl;

/**
 * FactoryBean to make the creation of an bean that implements NDROI easier
 */
public class V17NDROIFactoryBean extends V17PluginFactoryBeanBase<NDROI>{

	@Override
	protected NDROI createObject(NDPluginBase pluginBase, IPVProvider pvProvider) throws Exception {
		NDROIImpl plugin = new NDROIImpl();
		plugin.setPluginBase(pluginBase);
		plugin.setPvProvider(pvProvider);
		plugin.afterPropertiesSet();
		return plugin;
	}

}
