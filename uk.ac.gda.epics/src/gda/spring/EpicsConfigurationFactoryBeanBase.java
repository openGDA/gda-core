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
import gda.configuration.epics.EpicsConfiguration;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

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

	protected EpicsConfiguration epicsConfiguration;

	/**
	 * Sets the EpicsConfiguration to use when looking up PV from deviceName.
	 * 
	 * @param epicsConfiguration the EpicsConfiguration
	 */
	public void setEpicsConfiguration(EpicsConfiguration epicsConfiguration) {
		this.epicsConfiguration = epicsConfiguration;
	}

	/**
	 * Retrieves the {@link EpicsConfiguration} from the application context.
	 * 
	 * @return the EPICS configuration
	 */
	protected EpicsConfiguration getEpicsConfiguration() {
		String[] epicsConfigBeans = applicationContext.getBeanNamesForType(EpicsConfiguration.class);
		if (epicsConfigBeans.length == 0) {
			throw new RuntimeException("You have not defined an EpicsConfiguration bean");
		} else if (epicsConfigBeans.length > 1) {
			if (epicsConfiguration != null) {
				return epicsConfiguration;
			}
			throw new RuntimeException("You have defined multiple EpicsConfiguration beans but not defined which one to use");
		} else {
			return (EpicsConfiguration) applicationContext.getBean(epicsConfigBeans[0]);
		}
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		createObject();
	}
	
	/**
	 * Creates the object managed by this factory.
	 * 
	 * @throws ConfigurationNotFoundException if configuration information
	 *         cannot be found for the device
	 */
	protected abstract void createObject() throws ConfigurationNotFoundException;

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
