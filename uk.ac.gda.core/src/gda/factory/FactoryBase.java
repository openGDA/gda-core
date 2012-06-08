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

package gda.factory;

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceBase;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for object factories.
 */
public abstract class FactoryBase implements Factory {
	
	/**
	 * Property to control if exceptions are allowed in configure method of devices- useful for testing. Values are True or False[Default]
	 */
	public static final String GDA_FACTORY_ALLOW_EXCEPTION_IN_CONFIGURE = "gda.factory.allowExceptionInConfigure";

	private static final Logger logger = LoggerFactory.getLogger(FactoryBase.class);

	boolean allowExceptionInConfigure=LocalProperties.check(GDA_FACTORY_ALLOW_EXCEPTION_IN_CONFIGURE);
	
	/**
	 * Configures this factory.
	 * 
	 * @throws FactoryException if an object cannot be configured
	 */
	@Override
	public void configure() throws FactoryException {
		List<Findable> findables = getFindables();
		for (Findable findable : findables) {
			if (findable instanceof Configurable) {
				Configurable configurable = (Configurable) findable;
				logger.info("Configuring " + findable.getName());
				try{
					if (findable instanceof DeviceBase) {
						if (((DeviceBase) findable).isConfigureAtStartup()) {
							configurable.configure();
						}
					} else {
						configurable.configure();
					}
				} catch (Exception ex){
					if(!allowExceptionInConfigure){
						throw new FactoryException("Error in configure for " + findable.getName(), ex);
					}
					logger.error("Error in configure for " + findable.getName(),ex);
				}
			}
		}
	}
	/**
	 * @return True if exceptions allowed in configure - useful for testing
	 */
	public boolean isAllowExceptionInConfigure() {
		return allowExceptionInConfigure;
	}
	/**
	 * @param allowExceptionInConfigure - True if exceptions allowed in configure - useful for testing
	 */
	public void setAllowExceptionInConfigure(boolean allowExceptionInConfigure) {
		this.allowExceptionInConfigure = allowExceptionInConfigure;
	}
	
}
