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

package gda.factory.corba.util;

import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Findable;

import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Extension of {@link ImplFactory} that makes all objects in a Spring {@link ApplicationContext} available.
 * 
 * <p>The {@code <corba:export />} element should be used to add an instance of this class to your
 * Spring context. There is no need to manually add a bean definition.
 */
public class SpringImplFactory extends ImplFactory implements ApplicationContextAware, Configurable {

	private static final Logger logger = LoggerFactory.getLogger(SpringImplFactory.class);
	
	private ApplicationContext applicationContext;
	
	private String namespace;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	@Override
	protected String getNamespace() {
		return namespace;
	}

	@Override
	public void configure() throws FactoryException {
		logger.info("Making objects in Spring context available through CORBA...");
		makeObjectsAvailable();
	}
	
	@Override
	protected List<Findable> getFindablesToMakeAvailable() {
		Map<String, Findable> findables = applicationContext.getBeansOfType(Findable.class);
		return new Vector<Findable>(findables.values());
	}
	
}
