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

import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.factory.Localizable;
import gda.jython.accesscontrol.RbacUtils;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ClassUtils;
import org.omg.PortableServer.POA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ImplFactory {

	private static final Logger logger = LoggerFactory.getLogger(ImplFactory.class);

	private NetService netService;
	private HashMap<String, CorbaBoundObject> store = new LinkedHashMap<String, CorbaBoundObject>();

	public ImplFactory(NetService netService) {
		this.netService = netService;
	}

	public ImplFactory() {
		// do nothing
	}

	public void setNetService(NetService netService) {
		this.netService = netService;
	}

	/**
	 * Names of objects that should not be made remotely available.
	 */
	private List<String> excludedObjects;

	public void setExcludedObjects(List<String> excludedObjects) {
		this.excludedObjects = excludedObjects;
	}

	/**
	 * Returns the list of {@link Findable}s that will be made remotely available.
	 *
	 * @return a list of Findables
	 */
	protected abstract List<Findable> getFindablesToMakeAvailable();

	/**
	 * Returns the namespace into which the objects will be exported, e.g. "stnBase".
	 *
	 * @return the namespace
	 */
	protected abstract String getNamespace();

	protected void makeObjectsAvailable() throws FactoryException {
		POA poa = netService.getPOA();

		//TODO All errors should lead to throwing a FactoryException - check error then lead to system exit
		org.omg.PortableServer.Servant servant;

		Runtime.getRuntime().addShutdownHook(uk.ac.gda.util.ThreadManager.getThread(new Runnable() {
			@Override
			public void run() {
				shutdown();
			}
		}, getNamespace() + " ImplFactory shutdown"));

		List<Findable> findables = getFindablesToMakeAvailable();
		for (Findable findable : findables) {
			String name = findable.getName();
			if (findable instanceof Localizable && !((Localizable) findable).isLocal()) {

				if (excludedObjects != null && excludedObjects.contains(name)) {
					logger.info(String.format("Not exporting %s - it has been excluded", name));
					continue;
				}

				Class<?> type = findable.getClass();
				if (RbacUtils.objectIsCglibProxy(findable)) {
					// Object has been proxied. Get its original type
					type = type.getSuperclass();
				}

				String implName = CorbaUtils.getImplementationClassName(type);
				String adapterClassName = CorbaUtils.getAdapterClassName(type);

				org.omg.CORBA.Object obj=null;
				try {
					Class<?> classDef = Class.forName(implName);
					Constructor<?>[] ctors = classDef.getDeclaredConstructors();
					Constructor<?> ctor = ctors[0];

					final Object[] args = new Object[] {findable, poa};
					if (!ClassUtils.isAssignable(ClassUtils.toClass(args), ctor.getParameterTypes())) {
						logger.warn("Class " + implName + " is unsuitable for " + name + ", so it will be a local object");
					}

					else {
						servant = (org.omg.PortableServer.Servant) ctor.newInstance(args);
						obj = poa.servant_to_reference(servant);
					}
				} catch (ClassNotFoundException ex) {
					logger.warn("Cannot find class " + implName + ": " + name + " will be a local object");
				} catch (Exception e) {
					logger.error("Could not instantiate class " + implName + ": " + name + " will be a local object", e);
				}

				String fullName = getNamespace() + NetService.OBJECT_DELIMITER + name;
				if (obj != null) {
					// bind throws a factory exception which is not caught
					// but passed back to the caller.
					store.put(fullName, new CorbaBoundObject(fullName, adapterClassName, obj));
					netService.bind(fullName, adapterClassName, obj);
					logger.debug("ImplFactory created Corba object for " + fullName);
				} else {
					logger.warn("No CORBA object created for " + fullName);
				}
			}

			else {
				logger.debug(String.format("Not exporting %s - it is local (or does not implement Localizable)", name));
			}
		}
	}

	/**
	 * Shuts down this ImplFactory, unregistering names from CORBA.
	 */
	public void shutdown() {
		logger.info("Shutting down {}", this);
		Set<String> names = store.keySet();
		for (String name : names) {
			final CorbaBoundObject boundObj = store.get(name);
			try {
				netService.unbind(name, boundObj.type);
			} catch (FactoryException e) {
				// Deliberately do nothing. Its only cleaning up where possible.
			}
		}
		// Clear the name store to avoid exceptions which can be thrown if this method is called again after CORBA has
		// been shut down.
		store.clear();
	}
}

/**
 * Holds an object that is being added to the name server, together with its
 * full name (e.g. {@code "stnBase.GDAHashtable"}) and its type (e.g.
 * {@code gda.util.findableHashtable.FindableHashtable}).
 */
class CorbaBoundObject {
	public String fullName;
	public String type;
	public org.omg.CORBA.Object object;

	public CorbaBoundObject(String fullName, String type, org.omg.CORBA.Object object) {
		this.fullName = fullName;
		this.type = type;
		this.object = object;
	}
}