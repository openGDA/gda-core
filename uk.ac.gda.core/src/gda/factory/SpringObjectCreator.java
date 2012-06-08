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

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import gda.util.SpringObjectFactory;

/**
 * Object creator that creates a {@link SpringObjectFactory}.
 * 
 * @deprecated Adding Spring beans directly to the application context is the
 * preferred method for instantiating objects. Encapsulating objects within a
 * {@link SpringObjectFactory} created by this class results in objects that
 * cannot be referenced from the application context.
 */
@Deprecated
public class SpringObjectCreator implements IObjectCreator {

	private Logger logger = LoggerFactory.getLogger(SpringObjectCreator.class);
	
	private String name;
	
	/**
	 * Sets the name to use when creating the EPICS device factory.
	 * 
	 * @param name the factory name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	private File xmlFile;
	
	/**
	 * Sets the Spring XML file that will be used to create the
	 * {@link SpringObjectFactory}.
	 * 
	 * @param xmlFile the Spring XML file
	 */
	public void setXmlFile(File xmlFile) {
		this.xmlFile = xmlFile;
	}
	
	protected boolean corbariseObjects=false;

	/**
	 * @param corbariseObjects
	 */
	public void setCorbariseObjects(boolean corbariseObjects) {
		this.corbariseObjects = corbariseObjects;
	}
	
	@Override
	public SpringObjectFactory getFactory() throws FactoryException {
		logger.warn("You are using a SpringObjectCreator that uses file " + StringUtils.quote(xmlFile.getAbsolutePath()) + ".");
		logger.warn("SpringObjectCreator (and SpringObjectFactory) will be removed soon.");
		logger.warn("Please move the objects in the file mentioned above into your 'main' configuration file (the one that contains your object creators).");
		logger.warn("Then delete the empty file, and remove the SpringObjectCreator bean from your 'main' configuration file.");
		SpringObjectFactory springObjFactory = new SpringObjectFactory(xmlFile, corbariseObjects);
		springObjFactory.setName(name);
		return springObjFactory;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[xmlFile=" + xmlFile + "]";
	}

	@Override
	public boolean isLocal() {
		return true;
	}

}
