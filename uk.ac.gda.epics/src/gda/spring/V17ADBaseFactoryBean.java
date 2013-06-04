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

import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.impl.ADBaseImpl;
import gda.device.detector.areadetector.v17.impl.SimplePVProvider;
/**
 * FactoryBean to make the creation of an bean that implements NDFileHDF5 easier
 */
public class V17ADBaseFactoryBean extends V17FactoryBeanBase<ADBase>{

	private String prefix;
	
	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}


	@Override
	public void afterPropertiesSet() throws Exception {
		ADBaseImpl plugin = new ADBaseImpl();
		SimplePVProvider simplePVProvider = new SimplePVProvider();
		simplePVProvider.setPrefix(prefix);
		simplePVProvider.afterPropertiesSet();		
		plugin.setPvProvider(simplePVProvider);
		plugin.afterPropertiesSet();
		bean  = plugin;
	}


}
