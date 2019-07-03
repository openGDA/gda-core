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
import java.util.Map;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;

/**
 * An interface class used to provide findable objects.
 * <p>
 * {@link Factory}s are used by the {@link Finder} to find objects they act as different object sources.
 *
 * Adding beans directly to a Spring application context is now the
 * preferred method for instantiating objects. Encapsulating objects within a
 * {@link Factory} results in objects that cannot be referenced from the
 * application context. Instead of creating new factories, consider writing a
 * {@link BeanDefinitionParser} to create Spring {@link BeanDefinition}s.
 */
public interface Factory extends Configurable {

	/**
	 * Used to add the {@link Findable}s to this factory
	 *
	 * @param findable the object to add.
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
	 * Return a named object from the factory or null if this factory can't provide the object.
	 *
	 * @param <T> The type of the object to find
	 * @param name
	 *            the name of the object as defined by the {@link Findable} interface.
	 * @return the named object or <code>null</code> if this factory can't provide it.
	 * @throws FactoryException
	 */
	public <T extends Findable> T getFindable(String name) throws FactoryException;

	/**
	 * Return all objects of the requested type from this factory.
	 *
	 * @param <T> The type of the object to find
	 * @param clazz the type of the objects to be found
	 * @return a map of name to requested objects that can be provided from this factory or an empty map if none can be provided
	 */
	public <T extends Findable> Map<String, T> getFindablesOfType(Class<T> clazz);

	/**
	 * Indicates whether this factory contains objects that have been
	 * instantiated locally.
	 *
	 * @return whether this factory contains local objects
	 */
	public boolean isLocal();

}
