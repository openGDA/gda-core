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

import gda.jython.accesscontrol.RbacUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A class to instantiate objects defined in the XML, using Castor. Objects are stored in a LinkedHashMap, with the
 * object name as the key, to preserve the order of creation as defined by the XML.
 * 
 * @since GDA 4.0
 */
public class ObjectFactory extends FactoryBase implements Factory, Configurable, Serializable, Reconfigurable {

	private String name;
	private Map<String, Findable> findablesByName = new LinkedHashMap<String, Findable>();
	
	/**
	 * Null argument constructor required by Castor in the instantiation phase.
	 */
	public ObjectFactory() {
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
		findablesByName.put(findable.getName(), findable);
	}

	@Override
	public List<Findable> getFindables() {
		return new ArrayList<Findable>(findablesByName.values());
	}
	
	@Override
	@SuppressWarnings( { "unchecked" })
	public <T extends Findable> T getFindable(String name) {
		return (T) findablesByName.get(name);
	}

	@Override
	public List<String> getFindableNames() {
		return new ArrayList<String>(findablesByName.keySet());
	}

	@Override
	public void reconfigure() throws FactoryException {
		List<Findable> findableList = getFindables();
		for (Findable findable : findableList) {
			if (findable instanceof Reconfigurable) {
				((Reconfigurable) findable).reconfigure();
			}
		}
	}

	/**
	 * Wrap every object inside a proxy object to test for authorisation
	 */
	public void buildProxies() {
		// rebuild every device object inside an RBACProxy
		for (String findableName : findablesByName.keySet()) {
			Findable findable = getFindable(findableName);
			findablesByName.remove(findable);			
			findablesByName.put(findableName, RbacUtils.wrapFindableWithInterceptor(findable));
		}
	}

	@Override
	public boolean containsExportableObjects() {
		// All objects in an ObjectFactory should be exported
		return true;
	}

	@Override
	public boolean isLocal() {
		return true;
	}
}