/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.util;

import gda.factory.FactoryBase;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.jython.accesscontrol.RbacUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.mortbay.jetty.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * An object factory that loads beans from a Spring beans XML file.
 */
public class SpringObjectFactory extends FactoryBase {
	
	private static final Logger logger = LoggerFactory.getLogger(SpringObjectFactory.class);
	
	/**
	 * Map of findables, keyed by object name.
	 */
	protected Map<String, Findable> findables = new LinkedHashMap<String, Findable>();
	
	/**
	 * Name of this object factory.
	 */
	protected String name;
	
	protected boolean corbariseObjects=false;


	/**
	 * Name of the Jetty server bean.
	 */
	private static final String JETTY_BEAN_NAME = "jetty";
	
	/**
	 * Creates a new Spring object factory.
	 * 
	 * @param configFile Spring beans file for this object factory
	 * @param corbariseObjects true if the objects in the factory are to be corbarised 
	 */
	public SpringObjectFactory(File configFile, boolean corbariseObjects) {
		this.corbariseObjects = corbariseObjects;
		ListableBeanFactory beanFactory = new FileSystemXmlApplicationContext("file:" + configFile.getAbsolutePath());
		logger.info("Loaded beans: " + Arrays.toString(beanFactory.getBeanDefinitionNames()));
		
		addFindableBeansToFindableMap(beanFactory);
		
		// If the application context contains a Jetty bean, start it
		if (beanFactory.containsBean(JETTY_BEAN_NAME)) {
			Server server = beanFactory.getBean(JETTY_BEAN_NAME, Server.class);
			try {
				server.start();
			} catch (Exception e) {
				logger.error("Unable to start Jetty server", e);
			}
		}
	}
	
	/**
	 * Selects all beans in the specified bean factory that implement
	 * {@link Findable}, and adds them to the map of {@link Findable}s.
	 * 
	 * @param beanFactory the Spring bean factory
	 */
	private void addFindableBeansToFindableMap(ListableBeanFactory beanFactory) {
		final Map<String, Findable> findableBeans = beanFactory.getBeansOfType(Findable.class);
		for (Findable findable : findableBeans.values()) {
			addFindable(findable);
		}
	}
	
	@Override
	public void addFindable(Findable findable) {
		findables.put(findable.getName(), RbacUtils.wrapFindableWithInterceptor(findable));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Findable> T getFindable(String name) throws FactoryException {
		return (T) findables.get(name);
	}

	@Override
	public ArrayList<Findable> getFindables() {
		return new ArrayList<Findable>(findables.values());
	}

	@Override
	public List<String> getFindableNames() {
		return new ArrayList<String>(findables.keySet());
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean containsExportableObjects() {
		return corbariseObjects;
	}

	@Override
	public boolean isLocal() {
		return true;
	}

}
