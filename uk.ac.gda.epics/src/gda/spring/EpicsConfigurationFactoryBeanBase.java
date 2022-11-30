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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import gda.factory.FactoryException;

/**
 * Base {@link FactoryBean} for creating objects that rely on configuration
 * information from the EPICS interface file. This class will retrieve the EPICS
 * configuration from the application context.
 */
public abstract class EpicsConfigurationFactoryBeanBase<T> implements ApplicationContextAware, BeanNameAware, InitializingBean, FactoryBean<T> {

	protected ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	protected String name;

	@Override
	public void setBeanName(String name) {
		this.name = name;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		createObject();
	}

	/**
	 * Creates the object managed by this factory.
	 *
	 */
	protected abstract void createObject() throws FactoryException;

	@Override
	public boolean isSingleton() {
		return true;
	}

	public boolean isLocal() {
		return local;
	}

	public void setLocal(boolean local) {
		this.local = local;
	}

	private boolean local = false;
}
