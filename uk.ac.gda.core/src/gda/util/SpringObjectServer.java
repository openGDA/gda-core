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

package gda.util;

import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.ConfigurableEnvironment;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;

import gda.configuration.properties.LocalProperties;
import gda.factory.Configurable;
import gda.factory.Factory;
import gda.factory.FactoryBase;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.jython.JythonServer;
import gda.jython.ScriptPaths;
import gda.jython.ScriptProject;
import gda.jython.ScriptProjectType;
import gda.spring.OsgiServiceBeanHandler;
import gda.spring.SpringApplicationContextBasedObjectFactory;
import uk.ac.gda.remoting.client.RmiProxyMarker;

/**
 * A subclass of {@link ObjectServer} that uses a Spring application context.
 */
public class SpringObjectServer extends ObjectServer {

	private static final Logger logger = LoggerFactory.getLogger(SpringObjectServer.class);

	private static final String GDA_JYTHON_FINDABLES_MODULE_ENABLED = "gda.jython.findables.module.enabled";
	private static final String GDA_JYTHON_FINDABLES_MODULE_NAME = "gda.jython.findables.module.name";
	private static final String GDA_JYTHON_FINDABLES_MODULE_DIR = "gda.jython.findables.module.dir";

	private final OsgiServiceBeanHandler osgiServiceBeanHandler = new OsgiServiceBeanHandler();

	boolean allowExceptionInConfigure=LocalProperties.check(FactoryBase.GDA_FACTORY_ALLOW_EXCEPTION_IN_CONFIGURE);

	private FileSystemXmlApplicationContext applicationContext;

	/**
	 * Creates an object server.
	 *
	 * @param xmlFile
	 *            the XML configuration file
	 */
	public SpringObjectServer(File xmlFile) {
		super(xmlFile);

		final String configLocation = "file:" + xmlFile.getAbsolutePath();
		applicationContext = new FileSystemXmlApplicationContext(new String[] {configLocation}, false);
		applicationContext.getEnvironment().getPropertySources().addFirst(new LocalPropertiesPropertySource());
		setApplicationContextActiveProfiles(applicationContext);
		applicationContext.setAllowBeanDefinitionOverriding(false);

		// Load the context
		applicationContext.refresh();

		dumpListOfBeans();
	}

	private static final String GDA_ACTIVE_SPRING_PROFILES_PROPERTY_NAME = String.format("gda.%s", AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME);

	private static void setApplicationContextActiveProfiles(ApplicationContext applicationContext) {
		if (LocalProperties.contains(GDA_ACTIVE_SPRING_PROFILES_PROPERTY_NAME)) {
			final String[] activeProfiles = nonEmptyActiveSpringProfiles();
			if (activeProfiles.length != 0) {
				logger.info("'{}' property is set, so setting active profiles to {}", GDA_ACTIVE_SPRING_PROFILES_PROPERTY_NAME, Arrays.toString(activeProfiles));
				final ConfigurableEnvironment environment = (ConfigurableEnvironment) applicationContext.getEnvironment();
				environment.setActiveProfiles(activeProfiles);
			} else {
				logger.info("'{}' is set to empty list - not using any profiles", GDA_ACTIVE_SPRING_PROFILES_PROPERTY_NAME);
			}
		}
	}

	private static String[] nonEmptyActiveSpringProfiles() {
		return Arrays.stream(LocalProperties.getStringArray(GDA_ACTIVE_SPRING_PROFILES_PROPERTY_NAME))
			.filter(s -> !s.isEmpty())
			.toArray(String[]::new);
	}

	public static List<String> getActiveSpringProfiles() {
		return ImmutableList.copyOf(nonEmptyActiveSpringProfiles());
	}

	private void dumpListOfBeans() {
		logger.debug("{} bean(s) defined in the application context. Beans by name:", applicationContext.getBeanDefinitionCount());
		final String[] names = applicationContext.getBeanDefinitionNames().clone();
		Arrays.sort(names, String.CASE_INSENSITIVE_ORDER);
		final TreeMap<String, TreeSet<String>> beansByLocation = new TreeMap<>();
		for (int i=0; i<names.length; i++) {
			final String name = names[i];
			final BeanDefinition beanDef = applicationContext.getBeanFactory().getBeanDefinition(name);
			String location = beanDef.getResourceDescription();
			if (location == null) {
				location = "unknown";
			}
			logger.debug("  {}. {} (location: {})", i+1, name, location);
			if (!beansByLocation.containsKey(location)) {
				beansByLocation.put(location, new TreeSet<String>(String.CASE_INSENSITIVE_ORDER));
			}
			final TreeSet<String> beansForThisLocation = beansByLocation.get(location);
			beansForThisLocation.add(name);
		}
		logger.debug("Beans by location:");
		for (String location : beansByLocation.keySet()) {
			logger.debug("    {}", location);
			TreeSet<String> beansForThisLocation = beansByLocation.get(location);
			for (String name : beansForThisLocation) {
				logger.debug("        {}", name);
			}
		}

		// Log which classes Spring is instantiating
		final Map<String, Object> beans = applicationContext.getBeansOfType(Object.class); // Get all beans
		final Map<String, Long> classNameToInstanceCount = beans.values().stream()
				.collect(groupingBy(bean -> bean.getClass().getName(), // key the class name
						TreeMap::new, // TreeMap to put in alphabetical order
						counting())); // value the number of each class
		logger.debug("Classes in use: {}", classNameToInstanceCount);
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
	void writeFindablesJythonModule() throws FactoryException {
		if (!LocalProperties.check(GDA_JYTHON_FINDABLES_MODULE_ENABLED, true)) {
			logger.info("Jython Findables module generation disabled");
			return;
		}
		final String moduleName = LocalProperties.get(GDA_JYTHON_FINDABLES_MODULE_NAME, "gdaserver");
		final String moduleDir = LocalProperties.get(GDA_JYTHON_FINDABLES_MODULE_DIR, LocalProperties.getVarDir() + File.separator + "scripts");
		final File moduleFile = new File(moduleDir, moduleName + ".py");
		final String modulePath = moduleFile.getAbsolutePath();

		logger.info("Writing '{}' Jython module: {}", moduleName, modulePath);

		final String[] names = applicationContext.getBeanNamesForType(Findable.class).clone();
		Arrays.sort(names, String.CASE_INSENSITIVE_ORDER); // alphabetically ordered

		final Map<String, Class<?>> beanTypes = new LinkedHashMap<>(); // insertion ordered (so alphabetical)
		for (int i=0; i < names.length; i++) {
			final String name = names[i];

			// skip beans whose names are invalid Java/Jython identifiers
			if (!name.matches("(?:\\b[_a-zA-Z]|\\B\\$)[_$a-zA-Z0-9]*+")) {
				logger.warn("Skipping Findable object named '{}' which is not a valid Python identifier", name);
				continue;
			}

			final Class<?> type = applicationContext.getBeanFactory().getType(name);
			beanTypes.put(name, type);
		}

		moduleFile.getParentFile().mkdirs();
		// with-resource
		try (final PrintWriter writer = new PrintWriter(new FileWriter(moduleFile));) {

			// docstring
			writer.print("''' ");
			writer.print(moduleName);
			writer.println(".py: generated by SpringObjectServer.writeFindablesJythonModule");
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
			writer.format("__beamline__ = '%s'%n", LocalProperties.get(LocalProperties.GDA_BEAMLINE_NAME).toUpperCase());
			writer.format("__gdaversion__ = '%s'%n", Version.getRelease());
			writer.format("__xmlfile__ = '%s'%n", xmlFile);
			String[] pidHost = ManagementFactory.getRuntimeMXBean().getName().split("@");
			writer.format("__pid__ = '%s'%n", pidHost[0]);
			writer.format("__hostname__ = '%s'%n", pidHost[1]);
			writer.format("__timestamp__ = '%s'%n", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));
			writer.println();

			if (names.length == 0) {
				writer.println("# No findables are configured");
				logger.info("No findables are configured");
				return;
			}

			writer.println("# get function (finder.find for now)");
			writer.println("from gda.factory import Finder");
			writer.println("get = Finder.getInstance().find");
			writer.println("del Finder");
			writer.println();

			writer.format("# not executed so types not in: dir(%s)%n", moduleName);
			writer.println("if False:");
			writer.println("\t");
			writer.println("\t# fake imports for fake assignments below");
			LinkedHashSet<String> imports = new LinkedHashSet<>();
			for (Class<?> type : beanTypes.values()) {
				final String className = type.getSimpleName().split("\\$")[0];
				if (type.getPackage()!=null) {
					//workaround dynamic proxy bean of org.springframework.aop.framework.ProxyFactoryBean type does not have package name.
					// see DAQ-1018 for more information.
					imports.add(String.format("\tfrom %s import %s", type.getPackage().getName(), className));
				}
			}
			imports.stream().sorted(String.CASE_INSENSITIVE_ORDER).forEach(writer::println);

			writer.println("\t");
			writer.println("\t# fake assignments for PyDev type-inference");
			for (String name : beanTypes.keySet()) {
				Class<?> type =  beanTypes.get(name);
				String simple = type.getSimpleName().split("\\$")[0];
				writer.format("\t%s = %s()%n", name, simple);
			}
			writer.println("");

			writer.println("# real assignments of module-level attributes");
			for (String name : beanTypes.keySet()) {
				writer.format("%s = get(\"%s\")%n", name, name);
			}
			writer.println("# so you can import identifiers instead of strings");
			writer.println();

			writer.println("# don't leak get function");
			writer.println("del get");
		}
		catch (IOException e) {
			throw new FactoryException("Could not write " + moduleName + ".py module", e);
		}

		// Having written the file, create a ScriptProject for it
		ScriptPaths scriptPaths;
		try {
			scriptPaths = applicationContext.getBean(JythonServer.class).getJythonScriptPaths();
		} catch (BeansException exception) {
			throw new FactoryException("Unable to get Jython Server, cannot add " + moduleName + ".py to script projects.", exception);
		}

		if (scriptPaths == null) {
			throw new FactoryException("ScriptPaths not found, unable to add " + moduleName + ".py");
		}

		scriptPaths.addProject(new ScriptProject(moduleDir, "Scripts: " + moduleName, ScriptProjectType.HIDDEN));
	}

	@Override
	public void shutdown() {
		super.shutdown();
	}

	@Override
	protected void startServer() throws FactoryException {
		addSpringBackedFactoryToFinder(applicationContext);

		configureAllConfigurablesInApplicationContext(applicationContext);
		injectBeansIntoOsgiServiceRegister(applicationContext);
	}


	private void injectBeansIntoOsgiServiceRegister(ApplicationContext applicationContext) {
		Map<String, Object> allBeans = applicationContext.getBeansOfType(Object.class);
		for (Entry<String, Object> entry : allBeans.entrySet()) {
			String beanName = entry.getKey();
			Object bean = entry.getValue();
			osgiServiceBeanHandler.processBean(bean, beanName);
		}
	}

	/**
	 * Adds a Spring-backed {@link Factory} to the {@link Finder}.
	 */
	private void addSpringBackedFactoryToFinder(ApplicationContext applicationContext) {
		SpringApplicationContextBasedObjectFactory springObjectFactory = new SpringApplicationContextBasedObjectFactory(applicationContext);
		factories.add(springObjectFactory);
		Finder.getInstance().addFactory(springObjectFactory);
	}

	private void configureAllConfigurablesInApplicationContext(ApplicationContext applicationContext)
			throws FactoryException {

		// Stats about configuring
		int configuredCounter = 0;
		final Stopwatch configureStopwatch = Stopwatch.createStarted();
		Set<String> failedConfigurables = new LinkedHashSet<>();

		Map<String, Configurable> configurables = applicationContext.getBeansOfType(Configurable.class);
		for (Map.Entry<String, Configurable> entry : configurables.entrySet()) {
			String name = entry.getKey();
			Configurable obj = entry.getValue();

			boolean willConfigure = obj.isConfigureAtStartup();

			// If the object is an RMI proxy, do not call its configure() method
			if (obj instanceof RmiProxyMarker) {
				willConfigure = false;
			}

			if (willConfigure) {
				logger.info("Configuring {}", name);
				try {
					obj.configure();
					configuredCounter++;
				} catch (Exception e) {
					if (!allowExceptionInConfigure) {
						throw new FactoryException("Error in configure for " + name, e);
					}
					failedConfigurables.add(name);
					logger.error("Error in configure for {}", name, e);
				}
			} else {
				logger.info("Not configuring {}", name);
			}
		}

		// Analyse and log stats
		configureStopwatch.stop();
		logger.info("Finished configuring objects. Configured {} objects in {} seconds", configuredCounter,
				configureStopwatch.elapsed(SECONDS));
		if (!failedConfigurables.isEmpty()) {
			logger.warn("Failed to configure {} objects: {}", failedConfigurables.size(), failedConfigurables);
		}
	}

}
