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
import gda.device.detector.areadetector.v17.NDProcess;
import gda.device.detector.areadetector.v17.impl.NDProcessImpl;
/**
 * FactoryBean to make the creation of an bean that implements NDProcess easier
 */
public class V17NDProcessFactoryBean extends V17PluginFactoryBeanBase<NDProcess>{

	@Override
	protected NDProcess createObject(NDPluginBase pluginBase, IPVProvider pvProvider) throws Exception {
		NDProcessImpl plugin = new NDProcessImpl();
		plugin.setPluginBase(pluginBase);
		plugin.setPvProvider(pvProvider);
		plugin.afterPropertiesSet();
		return plugin;
	}

}
