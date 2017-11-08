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

package gda.spring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationContext;

import gda.factory.Factory;
import gda.factory.FactoryBase;
import gda.factory.FactoryException;
import gda.factory.Findable;

/**
 * A GDA {@link Factory} that wraps a Spring {@link ApplicationContext},
 * providing access to all the {@link Findable}s within the Spring context.
 */
public class SpringApplicationContextBasedObjectFactory extends FactoryBase {

	private String name;

	/** Cache of findables known about by Spring including aliases */
	private final Map<String, Findable> nameToFindable;

	public SpringApplicationContextBasedObjectFactory(ApplicationContext applicationContext) {
		nameToFindable = applicationContext.getBeansOfType(Findable.class);

		// Get all the aliases pointing at Findables
		Map<String, Findable> aliases = nameToFindable.keySet().stream()
				// Get all aliases pointing at Findables
				.flatMap(key -> Arrays.stream(applicationContext.getAliases(key)))
				// Make Map of alias to Findable
				.collect(Collectors.toMap(Function.identity(), // key
						alias -> (Findable) applicationContext.getBean(alias))); // value

		// Add the aliases to the findables
		nameToFindable.putAll(aliases);
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void addFindable(Findable findable) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Findable> getFindables() {
		return new ArrayList<>(nameToFindable.values());
	}

	@Override
	public List<String> getFindableNames() {
		return new ArrayList<>(nameToFindable.keySet());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Findable> T getFindable(String name) throws FactoryException {
		return (T) nameToFindable.get(name);
	}

	@Override
	public boolean containsExportableObjects() {
		return true;
	}

	@Override
	public boolean isLocal() {
		return true;
	}

}
