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
import gda.device.detector.areadetector.v17.ADCommon;
import gda.device.detector.areadetector.v17.NDPluginBase;
import gda.device.detector.areadetector.v17.impl.NDPluginBaseImpl;
import gda.device.detector.areadetector.v17.impl.SimplePVProvider;

/**
 * Base FactoryBean to make the creation of an bean that implements an areaDetector plugin interface easier
 */
abstract public  class  V17PluginFactoryBeanBase <T> extends V17FactoryBeanBase<T>{

	private ADCommon input;

	public ADCommon getInput() {
		return input;
	}

	public void setInput(ADCommon input) {
		this.input = input;
	}

	abstract protected T createObject(NDPluginBase pluginBase, IPVProvider pvProvider) throws Exception;

	@Override
	public void afterPropertiesSet() throws Exception {
		NDPluginBaseImpl pluginBase = new NDPluginBaseImpl();
		SimplePVProvider simplePVProvider = new SimplePVProvider();
		simplePVProvider.setPrefix(getPrefix());
		simplePVProvider.afterPropertiesSet();
		pluginBase.setPvProvider(simplePVProvider);
		pluginBase.afterPropertiesSet();
		bean = createObject( pluginBase, simplePVProvider);
	}

	@Override
	public Class<?> getObjectType() {
		return bean != null ? bean.getClass() : null;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public T getObject() throws Exception {
		return bean;
	}

}
