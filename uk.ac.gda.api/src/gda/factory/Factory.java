/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

import java.util.List;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;

/**
 * An interface class used to instantiate findable objects, with Castor, via the object server. The factory
 * <ObjectFactory> should be the root tag in the XML file with the factory name as an element within the enclosing tags.
 * 
 * Adding beans directly to a Spring application context is now the
 * preferred method for instantiating objects. Encapsulating objects within a
 * {@link Factory} results in objects that cannot be referenced from the
 * application context. Instead of creating new factories, consider writing a
 * {@link BeanDefinitionParser} to create Spring {@link BeanDefinition}s.
 */
public interface Factory extends Configurable {
	/**
	 * Set or change the name of the factory (as defined in XML).
	 * 
	 * @param name
	 *            the factory name
	 */
	public void setName(String name);

	/**
	 * Get the factory name. Used by Castor to check if the factory name has been set before calling the
	 * {@link #setName(String)} method.
	 * 
	 * @return a String containing the factory name.
	 */
	public String getName();

	/**
	 * Used by Castor to add the constructed findable object to a list of instantiated objects.
	 * 
	 * @param findable
	 *            the object as described in the XML file.
	 */
	public void addFindable(Findable findable);

	/**
	 * Returns the findable objects held in this factory.
	 * 
	 * @return the list of objects.
	 */
	public List<Findable> getFindables();
	
	/**
	 * Returns a list of the names of all findable objects held in this factory.
	 * 
	 * @return a list of findable names
	 */
	public List<String> getFindableNames();

	/**
	 * Return a named object from the list of Castor instantiated objects.
	 * 
	 * @param <T>
	 * @param name
	 *            the name of the object as known by Castor from the XML.
	 * @return the named object of type {@link gda.factory.Findable}
	 * @throws FactoryException
	 */
	public <T extends Findable> T getFindable(String name) throws FactoryException;
	
	/**
	 * Indicates whether this factory contains objects that should be made
	 * remotely accessible.
	 * 
	 * @return whether objects in this factory should be remotely accessible
	 */
	public boolean containsExportableObjects();
	
	/**
	 * Indicates whether this factory contains objects that have been
	 * instantiated locally.
	 * 
	 * @return whether this factory contains local objects
	 */
	public boolean isLocal();
	
}
