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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Simple implementation of {@link Factory} that uses a {@link Map} of
 * {@link Findable}s.
 */
public class MapFactory implements Factory {

	private String name;
	
	private Map<String, Findable> findables;
	
	/**
	 * Creates a {@link MapFactory}.
	 * 
	 * @param name the factory name
	 */
	public MapFactory(String name) {
		this(name, null);
	}
	
	/**
	 * Creates a {@link MapFactory}.
	 * 
	 * @param name the factory name
	 * @param findables the map of findables
	 */
	public MapFactory(String name, Map<String, Findable> findables) {
		setName(name);
		setFindables(findables);
	}
	
	private void setFindables(Map<String, Findable> findables) {
		this.findables = new HashMap<String, Findable>();
		if (findables != null) {
			this.findables.putAll(findables);
		}
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Findable> T getFindable(String name) throws FactoryException {
		return (T) findables.get(name);
	}

	@Override
	public void addFindable(Findable findable) {
		findables.put(findable.getName(), findable);
	}

	@Override
	public List<String> getFindableNames() {
		return new Vector<String>(findables.keySet());
	}

	@Override
	public List<Findable> getFindables() {
		return new Vector<Findable>(findables.values());
	}

	@Override
	public boolean isLocal() {
		return true;
	}

	@Override
	public boolean containsExportableObjects() {
		return false;
	}

	@Override
	public void configure() throws FactoryException {
		// do nothing
	}

}
