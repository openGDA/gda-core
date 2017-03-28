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

package gda.factory.corba.util;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.factory.Factory;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.jython.accesscontrol.RbacUtils;

/**
 * Remote factory to resolve object references and create client side adapter interfaces
 *
 * This class holds remote objects and prevents those objects being referenced
 * in Spring configuration files. Use {@code <corba:import namespace="..." />} instead,
 * which imports remote objects directly into the Spring application context and allows
 * those objects to be referenced using the {@code ref="..."} attribute.
 */
public class AdapterFactory implements Factory {
	private HashMap<String, Findable> store = new LinkedHashMap<String, Findable>();

	private static final Logger logger = LoggerFactory.getLogger(AdapterFactory.class);

	private NetService netService;

	private String name;

	/**
	 * Create a factory for client side adapters.
	 *
	 * @param name
	 *            the name of the adapter factory
	 * @param netService
	 *            the name service
	 */
	public AdapterFactory(String name, NetService netService) {
		this.name = name;
		this.netService = netService;
	}

	@Override
	public void configure() throws FactoryException {
		// do nothing
	}

	/**
	 * Find adapter object or create it.
	 *
	 * @param <T>
	 * @param objectName
	 *            the name of the object to find
	 * @return the Findable instance of the object else null if not found
	 * @throws FactoryException
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T extends Findable> T getFindable(String objectName) throws FactoryException {
		Findable findable = null;
		String fullName = name + NetService.OBJECT_DELIMITER + objectName;
		Finder finder = Finder.getInstance();
		synchronized(store){
			if ((findable = store.get(fullName)) == null) {
				if ((findable = finder.findLocalNoWarn(objectName)) == null) {
					try {
						findable = createRbacWrappedAdapter(netService, fullName, objectName);
						store.put(fullName, findable);
					} catch (Exception ex) {
						// Should not log here otherwise get unwanted exceptions in the log
						// when attempting to find things that are not there using findNoWarn(...)
						throw new FactoryException("Cannot find '"+objectName+"'.", ex);
					}
				}
			}
			return (T) findable;
		}
	}

	public static Findable createRbacWrappedAdapter(NetService netService, String fullName, String objectName) throws Exception {
		Findable findable = createAdapter(netService, fullName, objectName);
		if (LocalProperties.isAccessControlEnabled()) {
			try {
				findable = RbacUtils.buildProxy(findable);
			} catch (Exception e) {
				logger.error("Could not wrap findable with Rbac Proxy", e);
			}
		}
		return findable;
	}

	public static Findable createAdapter(NetService netService, String fullName, String objectName) throws Exception {
		String adapterName = netService.getType(fullName);
		org.omg.CORBA.Object obj = netService.retrieve(fullName, adapterName);

		// lets try to see if its a current object or just a
		// relic lurking in the nameservice.
		// throws TRANSIENT exception if non existent
		obj._non_existent();

		logger.debug("Creating {} adapter of type {}.", fullName, adapterName);

		Class<?> classDef = Class.forName(adapterName);
		Constructor<?>[] ctor = classDef.getDeclaredConstructors();
		java.lang.Object[] args = new java.lang.Object[3];
		args[0] = obj;
		args[1] = objectName;
		args[2] = netService;

		Findable findable = (Findable) ctor[0].newInstance(args);
		return findable;
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
	}

	@Override
	public ArrayList<Findable> getFindables() {
		try {
			// fill the store so that the getFindable() method work properly
			// from the outset. So get everything from netservice.
			String factoryName = LocalProperties.get(LocalProperties.GDA_FACTORY_NAME);
			List<String> findableNames = netService.listAllFindables(factoryName);
			for (String findableFullName : findableNames) {
				// get the object name
				String shortName = findableFullName.substring(findableFullName.lastIndexOf(NetService.OBJECT_DELIMITER) + 1);
				// then call getFindable which will add this object to 'store'
				getFindable(shortName);
			}
		} catch (Exception ex) {
			logger.error("Exception caught in getFindables()",ex);
		}
		return new ArrayList<Findable>(store.values());
	}

	@Override
	public List<String> getFindableNames() {
		List<String> findableNames = new Vector<String>();
		final List<Findable> findables = getFindables();
		for (Findable findable : findables) {
			findableNames.add(findable.getName());
		}
		return findableNames;
	}

	@Override
	public boolean containsExportableObjects() {
		// An AdapterFactory creates adapters for remote objects, so the
		// adapters shouldn't be exported
		return false;
	}

	@Override
	public boolean isLocal() {
		// An AdapterFactory creates adapters for remote objects - i.e. objects
		// that weren't created by this GDA instance
		return false;
	}
}