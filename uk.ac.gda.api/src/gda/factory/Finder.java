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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Finder, a singleton class, allows objects to be retrieved from local store, a name service or created by a factory.
 * <p>
 * For unit testing classes which depend on the Finder, set up the Finder first with a test factory (see TestHelpers in the uk.ac.gda.test.helpers bundle) and
 * add any necessary Findables for the test to it. For example:
 *
 * <pre>
 * <code>
 * public void setUp() throws Exception {
 * 	// .. set up mocks first
 * 	Factory testFactory = TestHelpers.createTestFactory("test");
 * 	testFactory.addFindable(mockFindable);
 * 	Finder.addFactory(testFactory);
 * }
 * </code>
 * </pre>
 */
public enum Finder {
	INSTANCE;

	private static final Logger logger = LoggerFactory.getLogger(Finder.class);

	private final Set<Factory> allFactories = new CopyOnWriteArraySet<>();
	private final Set<Factory> remoteFactories = new CopyOnWriteArraySet<>();
	private final Set<Factory> localFactories = new CopyOnWriteArraySet<>();

	/**
	 * Getter to construct and/or return single instance of the finder.
	 * <p>
	 * This can be used in unit tests independently of the rest of the GDA framework.
	 * <p>
	 * @deprecated No longer needed because all functions are now static. Will be removed in GDA 9.20.
	 *
	 * @return the instance of finder.
	 */
	@Deprecated
	public static Finder getInstance() {
		logger.warn("getInstance() is deprecated and will be removed in GDA 9.20. Please use static access to all Finder functions");
		return INSTANCE;
	}

	/**
	 * Return a named object from any of the factories known to the finder.
	 *
	 * @param <T>
	 *            class of Object being returned
	 * @param name
	 *            object to find.
	 * @return the findable object or null if it cannot be found
	 */
	public static <T extends Findable> T find(String name) {
		T findable = findObjectByName(name, true);

		if (findable == null) {
			logger.warn("Finder could not find an object called '{}'. At some point, this method will throw an exception instead of returning null!", name);
		}

		return findable;
	}

	/**
	 * Return a Optional named object from any of the factories known to the finder.
	 *
	 * @param <T>
	 *            class of Optional<T> Object being returned
	 * @param name
	 *            object to find.
	 * @return the Optional<T> findable object
	 */
	public static <T extends Findable> Optional<T> findOptional(String name) {
		return Optional.ofNullable(findObjectByName(name, false));
	}

	/**
	 * Find an instance of an object given its name
	 *
	 * @param name
	 *            The name of the object to find
	 * @param warn
	 *            True to log a warning message in the case of a FactoryException
	 * @return the findable object or null if it cannot be found
	 */
	private static <T extends Findable> T findObjectByName(String name, boolean warn) {
		T findable = null;

		for (Factory factory : INSTANCE.localFactories) {
			findable = findObjectByNameInFactory(factory, name, warn);
			if (findable != null) {
				return findable;
			}
		}

		for (Factory factory : INSTANCE.remoteFactories) {
			findable = findObjectByNameInFactory(factory, name, warn);
			if (findable != null) {
				return findable;
			}
		}

		return null;
	}

	/**
	 * Find an instance of an object in a factory given its name
	 *
	 * @param factory
	 *            The factory in which to search
	 * @param name
	 *            The name of the object to find
	 * @param warn
	 *            True to log a warning message in the case of a FactoryException
	 * @return the findable object or null if it cannot be found
	 */
	private static <T extends Findable> T findObjectByNameInFactory(Factory factory, String name, boolean warn) {
		T findable = null;

		try {
			findable = factory.getFindable(name);
			if (findable != null) {
				logger.trace("Found '{}' using factory '{}'", name, factory);
				return findable;
			}
		} catch (FactoryException e) {
			if (warn) {
				logger.warn("FactoryException looking for '{}'", name, e);
			}
		}

		return null;
	}

	/**
	 * Adds a factory to the list of searchable factories known by the Finder.
	 *
	 * @param factory
	 *            the factory to add to the list.
	 */
	public static void addFactory(Factory factory) {
		INSTANCE.allFactories.add(factory);

		if (factory.isLocal()) {
			INSTANCE.localFactories.add(factory);
		} else {
			INSTANCE.remoteFactories.add(factory);
		}

		logger.debug("Added factory '{}' now have {} factories", factory, INSTANCE.allFactories.size());
	}

	public static void removeAllFactories(){
		INSTANCE.allFactories.clear();
		INSTANCE.localFactories.clear();
		INSTANCE.remoteFactories.clear();
		logger.debug("Cleared factories");
	}

	/**
	 * List all the interfaces available on the Finder. This method is aimed at users of the scripting environment for
	 * searching for available hardware by using the 'list' command.
	 *
	 * @return array of interface names
	 */
	public static List<String> listAllInterfaces() {
		List<Findable> objects = listAllObjects();
		List<String> usedInterfaces = new ArrayList<>();

		for (Findable findable : objects) {
			// loop through all the interfaces that objects use
			Class<?> superclass = findable.getClass();

			while (superclass != null) {
				for (Class<?> theClass : superclass.getInterfaces()) {
					// if there is a match then add this object
					String name = theClass.getName();
					name = name.substring(name.lastIndexOf('.') + 1);
					if (!usedInterfaces.contains(name)) {
						usedInterfaces.add(name);
					}
				}
				superclass = superclass.getSuperclass();
			}
		}
		return usedInterfaces;
	}

	/**
	 * Returns an array of all the objects in this finder's factories as defined by the XML.
	 *
	 * @return a list of all known Findable objects.
	 */
	private static List<Findable> listAllObjects() {
		List<Findable> allFindables = new ArrayList<>();
		for (Factory factory : INSTANCE.allFactories) {
			allFindables.addAll(factory.getFindables());
		}
		return allFindables;
	}

	/**
	 * Returns a map of all {@link Findable} objects (local & remote) of the given type
	 *
	 * @param <T>
	 * @param clazz
	 *            the class or interface to match
	 * @return a map of matching {@code Findable}s, with the object names as keys and the objects as values
	 */
	public static <T extends Findable> Map<String, T> getFindablesOfType(Class<T> clazz) {
		return getFindablesOfType(clazz, false);
	}

	/**
	 * Returns a map of all local {@link Findable} objects of the given type
	 *
	 * @param clazz
	 *            the class or interface to match
	 * @return a map of matching {@code Findable}s, with the object names as keys and the objects as values
	 */
	public static <T extends Findable> Map<String, T> getLocalFindablesOfType(Class<T> clazz) {
		return getFindablesOfType(clazz, true);
	}

	/**
	 * Returns a map of all {@link Findable} objects of the given type
	 *
	 * @param clazz
	 *            the class or interface to match
	 * @param local True if only local objects are to be returned
	 * @return a map of matching {@code Findable}s, with the object names as keys and the objects as values
	 */
	private static <T extends Findable> Map<String, T> getFindablesOfType(Class<T> clazz, boolean local) {
		Map<String, T> findables = new HashMap<>();
		for (Factory factory : getFactoriesToSearch(local)) {
			findables.putAll(factory.getFindablesOfType(clazz));
		}
		return findables;
	}

	/**
	 * Returns a list of all {@link Findable} objects (local & remote) of the given type
	 *
	 * @param clazz
	 *            the class or interface to match
	 * @return a list of matching {@code Findable}s
	 */
	public static <T extends Findable> List<T> listFindablesOfType(Class<T> clazz) {
		return listFindablesOfType(clazz, false);
	}

	/**
	 * Returns a list of all local {@link Findable} objects of the given type
	 *
	 * @param clazz
	 *            the class or interface to match
	 * @return a list of matching {@code Findable}s
	 */
	public static <T extends Findable> List<T> listLocalFindablesOfType(Class<T> clazz) {
		return listFindablesOfType(clazz, true);
	}

	/**
	 * Returns a list of all {@link Findable} objects of the given type.
	 *
	 * @param clazz
	 *            the class or interface to match
	 * @param local
	 *            True to only search local factories
	 * @return a list of matching {@code Findable}s
	 */
	private static <T extends Findable> List<T> listFindablesOfType(Class<T> clazz, boolean local) {
		return new ArrayList<>(getFindablesOfType(clazz, local).values());
	}

	/**
	 * Returns the singleton of specified type.
	 * This method removes the need for singletons to have a specific name.
	 * @param singletonClass the singleton type
	 * @return the singleton
	 * @throws IllegalArgumentException if multiple/no instances of specified type found
	 */
	public static <T extends Findable> T findSingleton(Class<T> singletonClass) {
		Map<String, T> instances = getFindablesOfType(singletonClass);
		if (instances.size() != 1) {
			throw new IllegalArgumentException("Class '" + singletonClass.getName() + "' is not a singleton: " +
												instances.size() + " instances found");
		}
		return instances.values().iterator().next();
	}

	private static Set<Factory> getFactoriesToSearch(boolean localOnly) {
		if (localOnly) {
			return INSTANCE.localFactories;
		} else {
			return INSTANCE.allFactories;
		}
	}
}
