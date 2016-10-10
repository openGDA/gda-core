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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Vector;

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

	private ApplicationContext applicationContext;

	private String name;

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
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
		Map<String, Findable> findables = applicationContext.getBeansOfType(Findable.class);
		return new Vector<Findable>(findables.values());
	}

	@Override
	public List<String> getFindableNames() {
		String[] names = applicationContext.getBeanNamesForType(Findable.class);
		return Arrays.asList(names);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Findable> T getFindable(String name) throws FactoryException {
		final Map<String, Findable> findables = applicationContext.getBeansOfType(Findable.class);
		return (T) findables.get(name);
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
