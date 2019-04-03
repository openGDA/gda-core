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
import java.util.HashSet;
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
 * 	Finder.getInstance().addFactory(testFactory);
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
	 *
	 * @return the instance of finder.
	 */
	public static Finder getInstance() {
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
	public <T extends Findable> T find(String name) {
		T findable = findObjectByName(name, true);

		if (findable == null) {
			logger.warn("Finder could not find object. At some point this method will throw an exception instead of returning null.");
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
	public <T extends Findable> Optional<T> findOptional(String name) {
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
	private <T extends Findable> T findObjectByName(String name, boolean warn) {
		T findable = null;

		for (Factory factory : localFactories) {
			findable = findObjectByNameInFactory(factory, name, warn);
			if (findable != null) {
				return findable;
			}
		}

		for (Factory factory : remoteFactories) {
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
	private <T extends Findable> T findObjectByNameInFactory(Factory factory, String name, boolean warn) {
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
	public void addFactory(Factory factory) {
		allFactories.add(factory);

		if (factory.isLocal()) {
			localFactories.add(factory);
		} else {
			remoteFactories.add(factory);
		}

		logger.debug("Added factory '{}' now have {} factories", factory, allFactories.size());
	}

	public void removeAllFactories(){
		allFactories.clear();
		localFactories.clear();
		remoteFactories.clear();
		logger.debug("Cleared factories");
	}


	/**
	 * List all the interfaces available on the Finder. This method is aimed at users of the scripting environment for
	 * searching for available hardware by using the 'list' command.
	 *
	 * @return array of interface names
	 */
	public List<String> listAllInterfaces() {
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
	 * Returns an array of the names of all the objects in this Finder's factories which use the supplied interface
	 * name, as defined by the XML.
	 *
	 * @param interfaceName
	 *            the required interface to search for.
	 * @return the list of Findable object names supporting the named interface.
	 */
	public List<String> listAllNames(String interfaceName) {
		final List<Findable> findableRefs = listAllObjects(interfaceName);
		final List<String> findableNames = new ArrayList<>();
		for (Findable findable : findableRefs) {
			String findableName = findable.getName();
			findableName = findableName.substring(findableName.lastIndexOf('.') + 1);
			findableNames.add(findableName);
		}
		return findableNames;
	}


	/**
	 * Local version of listAllObjects
	 *
	 * @param interfaceName
	 * @return the list of Findable objects supporting the named interface.
	 * @deprecated use {@link #getLocalFindablesOfType(Class)} instead.
	 */
	@Deprecated
	public List<Findable> listAllLocalObjects(String interfaceName) {
		logger.debug("Using deprecated method 'listAllLocalObjects'. Called with '{}'", interfaceName);
		return listAllObjects(interfaceName,true);
	}

	/**
	 * Returns an array of the references of all the objects in this Finder's factories which use the supplied interface
	 * name as defined by the XML.
	 *
	 * @param interfaceName
	 *            the required interface to search for.
	 * @return the list of Findable objects supporting the named interface.
	 * @deprecated use {@link #listFindablesOfType(Class)} instead.
	 */
	@Deprecated
	public List<Findable> listAllObjects(String interfaceName) {
		logger.debug("Using deprecated method 'listAllObjects'. Called with '{}'", interfaceName);
		return listAllObjects(interfaceName, false);
	}

	/**
	 *
	 *
	 * @param interfaceName
	 *            the required interface to search for.
	 * @param localObjectsOnly <code>true</code> to only search local factories
	 * @return the list of Findable objects supporting the named interface.
	 * @deprecated This should be removed once the deprecated public methods have been removed.
	 */
	@Deprecated
	private List<Findable> listAllObjects(String interfaceName, boolean localObjectsOnly) {
		// if no class name given, then supply all objects
		if (interfaceName == null) {
			return listAllObjects();
		}

		List<Findable> objectRefs = new ArrayList<>();

		for (Factory factory : getFactoriesToSearch(localObjectsOnly)) {
			Set<Findable> objectsInFactory = getAllObjectsFromFactory(factory, interfaceName);
			objectRefs.addAll(objectsInFactory);
		}

		return objectRefs;
	}

	/**
	 *
	 * @param factory
	 *            the factory to search in
	 * @param interfaceName
	 *            the required interface to search for.
	 * @return the Set of Findable objects supporting the named interface.
	 * @deprecated This should be removed once the deprecated public methods have been removed.
	 */
	@Deprecated
	private Set<Findable> getAllObjectsFromFactory(Factory factory, String interfaceName) {
		Set<Findable> objectsInFactory = new HashSet<>();

		for (Findable findable : factory.getFindables()) {
			// for this findable, check its class and interfaces to see if they match the requested interface
			if (classOrInterfacesMatchesString(findable.getClass(), interfaceName)) {
				objectsInFactory.add(findable);
			}

			// else loop over superclasses up the hierarchy until java.lang.Object reached, testing each in turn
			else {
				Class<?> superclass = findable.getClass().getSuperclass();
				boolean found = false;

				while (!found && superclass != null) {
					found = classOrInterfacesMatchesString(superclass, interfaceName);
					superclass = superclass.getSuperclass();
				}

				if (found) {
					objectsInFactory.add(findable);
				}
			}
		}

		return objectsInFactory;
	}

	/**
	 * Tests the given class to see if its name, or the name of any interface it uses, matches the given interface name.
	 *
	 * @param classToTest
	 * @param interfaceName
	 * @return true if the same else false
	 */
	private boolean classOrInterfacesMatchesString(Class<?> classToTest, String interfaceName) {
		// first check the actual class
		if (classNameMatchesString(classToTest, interfaceName)) {
			return true;
		}

		// then loop through all the interfaces that object uses
		for (Class<?> objInterface : classToTest.getInterfaces()) {
			if (classOrInterfacesMatchesString(objInterface, interfaceName)) {
				return true;
			}
		}

		// if get here then nothing found
		return false;
	}

	/**
	 * Tests if the given Class has a name which matches the given interface name. This works if the interface name is
	 * either fully resolved or not.
	 *
	 * @param theClass
	 * @param interfaceName
	 * @return true if the same else false
	 */
	private boolean classNameMatchesString(Class<?> theClass, String interfaceName) {
		String className = theClass.getName();
		String shortName = className.substring(className.lastIndexOf('.') + 1);
		return className.compareTo(interfaceName) == 0 || shortName.compareTo(interfaceName) == 0;
	}

	/**
	 * Returns an array of all the objects in this finder's factories as defined by the XML.
	 *
	 * @return a list of all known Findable objects.
	 */
	private List<Findable> listAllObjects() {
		List<Findable> allFindables = new ArrayList<>();
		for (Factory factory : allFactories) {
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
	public <T extends Findable> Map<String, T> getFindablesOfType(Class<T> clazz) {
		return getFindablesOfType(clazz, false);
	}

	/**
	 * Returns a map of all local {@link Findable} objects of the given type
	 *
	 * @param clazz
	 *            the class or interface to match
	 * @return a map of matching {@code Findable}s, with the object names as keys and the objects as values
	 */
	public <T extends Findable> Map<String, T> getLocalFindablesOfType(Class<T> clazz) {
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
	private <T extends Findable> Map<String, T> getFindablesOfType(Class<T> clazz, boolean local) {
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
	public <T extends Findable> List<T> listFindablesOfType(Class<T> clazz) {
		return listFindablesOfType(clazz, false);
	}

	/**
	 * Returns a list of all local {@link Findable} objects of the given type
	 *
	 * @param clazz
	 *            the class or interface to match
	 * @return a list of matching {@code Findable}s
	 */
	public <T extends Findable> List<T> listLocalFindablesOfType(Class<T> clazz) {
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
	private <T extends Findable> List<T> listFindablesOfType(Class<T> clazz, boolean local) {
		return new ArrayList<>(getFindablesOfType(clazz, local).values());
	}

	/**
	 * Returns the singleton of specified type.
	 * This method removes the need for singletons to have a specific name.
	 * @param singletonClass the singleton type
	 * @return the singleton
	 * @throws IllegalArgumentException if multiple/no instances of specified type found
	 */
	public <T extends Findable> T findSingleton(Class<T> singletonClass) {
		Map<String, T> instances = getFindablesOfType(singletonClass);
		if (instances.size() != 1) {
			throw new IllegalArgumentException("Class '" + singletonClass.getName() + "' is not a singleton: " +
												instances.size() + " instances found");
		}
		return instances.values().iterator().next();
	}

	private Set<Factory> getFactoriesToSearch(boolean localOnly) {
		if (localOnly) {
			return localFactories;
		} else {
			return allFactories;
		}
	}
}
