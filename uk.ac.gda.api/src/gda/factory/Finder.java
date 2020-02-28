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

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toMap;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.util.Version;

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

	private static final String GDA_JYTHON_FINDABLES_MODULE_ENABLED = "gda.jython.findables.module.enabled";
	private static final String GDA_JYTHON_FINDABLES_MODULE_NAME = "gda.jython.findables.module.name";
	private static final String GDA_JYTHON_FINDABLES_MODULE_DIR = "gda.jython.findables.module.dir";

	private final Set<Factory> allFactories = new CopyOnWriteArraySet<>();
	private final Set<Factory> remoteFactories = new CopyOnWriteArraySet<>();
	private final Set<Factory> localFactories = new CopyOnWriteArraySet<>();

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
	 * 			class of Optional<T> Object being returned
	 * @param name
	 * 			The object to find
	 * @param findableType
	 * 			The class or interface to match
	 * @return the Optional<T> findable object
	 */
	public static <T extends Findable> Optional<T> findOptionalOfType(String name, Class<T> findableType) {
		Optional<T> findable = Optional.ofNullable(findObjectByName(name, false));
		return findable.filter(findableType::isInstance);
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

	/**
	 * Returns the local singleton of specified type.
	 * This method removes the need for singletons to have a specific name.
	 * @param singletonClass the singleton type
	 * @return the singleton
	 * @throws IllegalArgumentException if multiple/no instances of specified type found
	 */
	public static <T extends Findable> T findLocalSingleton(Class<T> singletonClass) {
		Map<String, T> instances = getLocalFindablesOfType(singletonClass);
		if (instances.size() != 1) {
			throw new IllegalArgumentException("Class '" + singletonClass.getName() + "' is not a singleton: " +
												instances.size() + " instances found");
		}
		return instances.values().iterator().next();
	}

	/**
	 * Returns the singleton of specified type.
	 * This method removes the need for singletons to have a specific name.
	 * @param singletonClass the singleton type
	 * @return the singleton
	 * @throws IllegalArgumentException if multiple/no instances of specified type found
	 */
	public static <T extends Findable> Optional<T> findOptionalSingleton(Class<T> singletonClass) {
		final Map<String, T> instances = getFindablesOfType(singletonClass);
		if (instances.size() > 1) {
			throw new IllegalArgumentException("Class '" + singletonClass.getName() + "' is not a singleton: " +
					instances.size() + " instances found");
		}

		return instances.values().stream().findFirst();
	}

	/**
	 * Returns the local singleton of specified type.
	 * This method removes the need for singletons to have a specific name.
	 * @param singletonClass the singleton type
	 * @return the singleton
	 * @throws IllegalArgumentException if multiple/no instances of specified type found
	 */
	public static <T extends Findable> Optional<T> findOptionalLocalSingleton(Class<T> singletonClass) {
		final Map<String, T> instances = getLocalFindablesOfType(singletonClass);
		if (instances.size() > 1) {
			throw new IllegalArgumentException("Class '" + singletonClass.getName() + "' is not a singleton: " +
					instances.size() + " instances found");
		}

		return instances.values().stream().findFirst();
	}

	private static Set<Factory> getFactoriesToSearch(boolean localOnly) {
		if (localOnly) {
			return INSTANCE.localFactories;
		} else {
			return INSTANCE.allFactories;
		}
	}


	/**
	 * <p>Generates a Jython module of Findable instances.
	 *
	 * <p>GDA properties control whether the module generation is enabled (i.e.
	 * whether the module is generated and written at all) (default:
	 * {@code true}), the name of the module (default: {@code gdaserver}), and
	 * where the module is written (default: <code>${gda.config}/scripts</code>).
	 * If the module is written to a non-default directory, the non-default
	 * directory should be specified in the list of script paths of the Jython
	 * server (i.e. the {@code command_server} bean) so that the module can be
	 * found when referenced from the Jython terminal or by another script.
	 *
	 * <p>Once written, other Jython modules can access Findable objects using, e.g.:
	 * <pre>from gdaserver import gripper_x, gripper_grp as gripper_jaws</pre>
	 *
	 * <p>And in Eclipse, PyDev can provide type-inferred auto-completion on:
	 * <pre>gripper_jaws.[Ctrl-1]</pre>
	 *
	 * <p>Documented further on <a href="http://confluence.diamond.ac.uk/x/IwyvAw">Confluence</a>.
	 * @throws FactoryException if the file could not be written
	 */
	public static Optional<File> writeFindablesJythonModule() throws FactoryException {
		if (!LocalProperties.check(GDA_JYTHON_FINDABLES_MODULE_ENABLED, true)) {
			logger.info("Jython Findables module generation disabled");
			return Optional.empty();
		}
		final String moduleName = LocalProperties.get(GDA_JYTHON_FINDABLES_MODULE_NAME, "gdaserver");
		final String moduleDir = LocalProperties.get(GDA_JYTHON_FINDABLES_MODULE_DIR, LocalProperties.getConfigDir() + File.separator + "scripts");
		final var moduleFile = new File(moduleDir, moduleName + ".py");
		if (!moduleFile.getParentFile().mkdirs() && !moduleFile.getParentFile().exists()) {
			throw new FactoryException("Could not create parent directory '" + moduleFile.getParent() + "' for " + moduleName + ".py file");
		}
		final String modulePath = moduleFile.getAbsolutePath();
		logger.info("Writing '{}' Jython module: {}", moduleName, modulePath);

		final List<Findable> findables = listAllObjects();
		findables.sort((f, o) -> f.getName().compareToIgnoreCase(o.getName())); // alphabetically ordered

		// Valid python identifiers can't start with a number and can only include alphanumeric characters (and _)
		Predicate<String> identifier = Pattern.compile("^(?:\\b[_a-zA-Z]|\\B\\$)[_$a-zA-Z0-9]*+$").asPredicate();
		final Map<String, Class<?>> beanTypes = findables.stream()
				.filter(bean -> identifier.test(bean.getName()))
				.collect(toMap(Findable::getName, // key
						Object::getClass, // value
						(a, b) -> a, // merge function
						LinkedHashMap::new)); // insertion ordered (so alphabetical)

		try (final PrintWriter writer = new PrintWriter(new FileWriter(moduleFile));) {

			// docstring
			writer.print("''' ");
			writer.print(moduleName);
			writer.println(".py: generated by Finder#writeFindablesJythonModule");
			writer.println();
			writer.println("Generated on server start, this Jython module acts as a development-time");
			writer.println("reference for Findable objects created by Spring XML. This trick enables PyDev");
			writer.println("auto-completion of server-side GDA objects and their methods in the IDE and");
			writer.println("Pythonic import syntax of multiple devices in user scripts, e.g.:");
			writer.println("");
			writer.print(">>> from ");
			writer.print(moduleName);
			writer.println(" import gripper_x, gripper_grp as gripper_jaws");
			writer.println("");
			writer.println("See Confluence for rationale: http://confluence.diamond.ac.uk/x/IwyvAw");
			writer.println("");
			writer.println("PLEASE DO NOT COMMIT THIS FILE");
			writer.println("You should ignore it in: .gitignore");
			writer.println("");
			writer.println("ANY CHANGES WILL BE OVERWRITTEN WITHOUT WARNING");
			writer.println("");
			writer.println("'''");
			writer.println();

			writer.println("# generation-time attributes");
			writer.format("__beamline__ = '%s'%n", LocalProperties.get(LocalProperties.GDA_BEAMLINE_NAME, "UNKNOWN").toUpperCase());
			writer.format("__gdaversion__ = '%s'%n", Version.getRelease());
			String[] pidHost = ManagementFactory.getRuntimeMXBean().getName().split("@");
			writer.format("__pid__ = '%s'%n", pidHost[0]);
			writer.format("__hostname__ = '%s'%n", pidHost[1]);
			writer.format("__timestamp__ = '%s'%n", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));
			writer.println();

			if (findables.isEmpty()) {
				writer.println("# No findables are configured");
				logger.info("No findables are configured");
			} else {

				writer.println("# get function (finder.find for now)");
				writer.println("from gda.factory import Finder");
				writer.println("get = Finder.find");
				writer.println("del Finder");
				writer.println();

				writer.format("# not executed so types not in: dir(%s)%n", moduleName);
				writer.println("if False:");
				writer.println("\t");
				writer.println("\t# fake imports for fake assignments below");
				LinkedHashSet<String> imports = new LinkedHashSet<>();
				imports = beanTypes.values().stream()
						.filter(type -> type.getPackage() != null)
						.map(type -> {
								// Proxied classes can have $ in their class names.
								final String className = type.getSimpleName().split("\\$")[0];
								return String.format("\tfrom %s import %s", type.getPackage().getName(), className);
						}).collect(toCollection(LinkedHashSet::new));

				imports.stream().sorted(String.CASE_INSENSITIVE_ORDER).forEach(writer::println);

				writer.println("\t");
				writer.println("\t# fake assignments for PyDev type-inference");
				beanTypes.forEach((name, type) -> {
					String simple = type.getSimpleName().split("\\$")[0];
					writer.format("\t%s = %s()%n", name, simple);
				});
				writer.println("");

				writer.println("# real assignments of module-level attributes");
				beanTypes.forEach((name, type) -> writer.format("%s = get(\"%s\")%n", name, name));
				writer.println("# so you can import identifiers instead of strings");
				writer.println();

				writer.println("# don't leak get function");
				writer.println("del get");
			}
			return Optional.of(moduleFile);
		} catch (IOException e) {
			throw new FactoryException("Could not write " + moduleName + ".py module", e);
		}
	}
}
