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
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.util.StringUtils;

import gda.configuration.properties.LocalProperties;
import gda.factory.ConditionallyConfigurable;
import gda.factory.Configurable;
import gda.factory.Factory;
import gda.factory.FactoryBase;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.factory.corba.util.AdapterFactory;
import gda.factory.corba.util.ImplFactory;
import gda.spring.SpringApplicationContextBasedObjectFactory;

/**
 * A subclass of {@link ObjectServer} that uses a Spring application context.
 */
public class SpringObjectServer extends ObjectServer {

	private static final Logger logger = LoggerFactory.getLogger(SpringObjectServer.class);

	boolean allowExceptionInConfigure=LocalProperties.check(FactoryBase.GDA_FACTORY_ALLOW_EXCEPTION_IN_CONFIGURE);

	private FileSystemXmlApplicationContext applicationContext;

	/**
	 * Creates an object server.
	 *
	 * @param xmlFile
	 *            the XML configuration file
	 */
	public SpringObjectServer(File xmlFile) {
		this(xmlFile, false);
	}

	/**
	 * Creates an object server.
	 *
	 * @param xmlFile
	 *            the XML configuration file
	 * @param localObjectsOnly
	 */
	public SpringObjectServer(File xmlFile, boolean localObjectsOnly) {
		super(xmlFile, localObjectsOnly);
		final String configLocation = "file:" + xmlFile.getAbsolutePath();
		applicationContext = new FileSystemXmlApplicationContext(new String[] {configLocation}, false);
		applicationContext.getEnvironment().getPropertySources().addFirst(new LocalPropertiesPropertySource());
		applicationContext.setAllowBeanDefinitionOverriding(false);
		applicationContext.refresh();

		dumpListOfBeans();
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
	}

	/**
	 * <p>Generates a Jython module of Findable instances in ${gda.config}/scripts/
	 *
	 * <p>Once written, other Jython modules can access Findable objects using, e.g.:
	 * <pre>from gdaserver import gripper_x, gripper_grp as gripper_jaws</pre>
	 *
	 * <p>And in Eclipse, PyDev can provide type-inferred auto-completion on:
	 * <pre>gripper_jaws.[Ctrl-1]</pre>
	 *
	 * <p>Documented further on <a href="http://confluence.diamond.ac.uk/x/IwyvAw">Confluence</a>.
	 */
	void writeFindablesJythonModule() {

		final String moduleName = "gdaserver";

		final File moduleFile = new File(LocalProperties.getConfigDir(), "scripts/"+moduleName+".py");
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

		// with-resource
		try (final PrintWriter writer = new PrintWriter(new FileWriter(moduleFile));) {

			// docstring
			writer.println("''' gdaserver.py: generated by SpringObjectServer.writeFindablesJythonModule");
			writer.println();
			writer.println("Generated on server start, this Jython module 'gdaserver.py', in each config's");
			writer.println("config/scripts directory, acts as a development-time reference for Findable");
			writer.println("objects created by Spring XML. This trick enables PyDev auto-completion of");
			writer.println("server-side GDA objects and their methods in the IDE and Pythonic import");
			writer.println("syntax of multiple device in user scripts, e.g.:");
			writer.println("");
			writer.println(">>> from gdaserver import gripper_x, gripper_grp as gripper_jaws");
			writer.println("");
			writer.println("See Confluence for rationale: http://confluence.diamond.ac.uk/x/IwyvAw");
			writer.println("");
			writer.println("PLEASE DO NOT COMMIT THIS FILE");
			writer.println("You should ignore it in: config/scripts/.gitignore");
			writer.println("");
			writer.println("ANY CHANGES WILL BE OVERWRITTEN WITHOUT WARNING");
			writer.println("");
			writer.println("'''");
			writer.println();

			writer.println("# generation-time attributes");
			writer.format("__beamline__ = '%s'\n", LocalProperties.get(LocalProperties.GDA_BEAMLINE_NAME).toUpperCase());
			writer.format("__gdaversion__ = '%s'\n", Version.getRelease());
			writer.format("__xmlfile__ = '%s'\n", xmlFile);
			String[] pid_host = ManagementFactory.getRuntimeMXBean().getName().split("@");
			writer.format("__pid__ = '%s'\n", pid_host[0]);
			writer.format("__hostname__ = '%s'\n", pid_host[1]);
			writer.format("__timestamp__ = '%s'\n", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));
			writer.println();

			writer.println("# get function (finder.find for now)");
			writer.println("from gda.factory import Finder");
			writer.println("get = Finder.getInstance().find");
			writer.println("del Finder");
			writer.println();

			writer.format("# not executed so types not in: dir(%s)\n", moduleName);
			writer.println("if False:");
			writer.println("\t");
			writer.println("\t# fake imports for fake assignments below");
			LinkedHashSet<String> imports = new LinkedHashSet<>();
			for (Class<?> type : beanTypes.values()) {
				final String className = type.getSimpleName();
				imports.add(String.format("\tfrom %s import %s", type.getPackage().getName(), className));
			}
			String[] _imports = imports.toArray(new String[] {});
			Arrays.sort(_imports, String.CASE_INSENSITIVE_ORDER); // alphabetically ordered
			for (String import_ : _imports) {
				writer.println(import_);
			}
			writer.println("\t");
			writer.println("\t# fake assignments for PyDev type-inference");
			for (String name : beanTypes.keySet()) {
				Class<?> type =  beanTypes.get(name);
				writer.format("\t%s = %s()\n", name, type.getSimpleName());
			}
			writer.println("");

			writer.println("# real assignments of module-level attributes");
			for (String name : beanTypes.keySet()) {
				writer.format("%s = get(\"%s\")\n", name, name);
			}
			writer.println("# so you can import identifiers instead of strings");
			writer.println();

			writer.println("# don't leak get function");
			writer.println("del get");
		}
		catch (IOException e) {
			logger.error("Could not write '{}' Jython module: {}", moduleName, modulePath, e);
		}
	}

	@Override
	public void shutdown() {
		super.shutdown();

		// Get the root ImplFactory Bean started by Jacorb/Spring by name
		ImplFactory root = applicationContext.getBean("ImplFactory#0", ImplFactory.class);
		if (root != null) {
			root.shutdown();
		}
	}

	@Override
	protected void startServer() throws FactoryException {
		addSpringBackedFactoryToFinder(applicationContext);
		/*
		 * We need to add the adapterFactory to the finder if present in the applicationContext to allow remote objects to
		 * be found during subsequent configureAllFindablesInApplicationContext.
		 * The adapterFactory must be added after the spring backed objects as the latter may include those from corba:import. If
		 * the order was otherwise we would duplicate adapters for remote objects.
		 * This change is in anticipation of future changes to corba:import to only import named objects rather than all.
		 */
		addAdapterFactoryToFinder();
		configureAllConfigurablesInApplicationContext(applicationContext);
		startOrbRunThread();
	}


	/**
	 * Adds a Spring-backed {@link Factory} to the {@link Finder}.
	 */
	private void addSpringBackedFactoryToFinder(ApplicationContext applicationContext) {
		SpringApplicationContextBasedObjectFactory springObjectFactory = new SpringApplicationContextBasedObjectFactory();
		springObjectFactory.setApplicationContext(applicationContext);
		factories.add(springObjectFactory);
		Finder.getInstance().addFactory(springObjectFactory);
	}

	private void addAdapterFactoryToFinder() {
		Map<String,AdapterFactory> adapterFactories = applicationContext.getBeansOfType(AdapterFactory.class);
		for (Map.Entry<String, AdapterFactory> entry : adapterFactories.entrySet()) {
			String name = entry.getKey();
			AdapterFactory adapterFactory = entry.getValue();
			logger.info(String.format("Adding AdapterFactory %s (namespace %s) to finder", StringUtils.quote(name), StringUtils.quote(adapterFactory.getName())));
			Finder.getInstance().addFactory(adapterFactory);
		}
	}

	private void configureAllConfigurablesInApplicationContext(ApplicationContext applicationContext)
			throws FactoryException {
		Map<String, Configurable> configurables = applicationContext.getBeansOfType(Configurable.class);
		for (Map.Entry<String, Configurable> entry : configurables.entrySet()) {
			String name = entry.getKey();
			Configurable obj = entry.getValue();

			boolean willConfigure = true;

			if (obj instanceof ConditionallyConfigurable) {
				final ConditionallyConfigurable cc = (ConditionallyConfigurable) obj;
				willConfigure = cc.isConfigureAtStartup();
			}

			if (willConfigure) {
				logger.info("Configuring " + name);
				try {
					obj.configure();
				} catch (Exception e) {
					if (!allowExceptionInConfigure) {
						throw new FactoryException("Error in configure for " + name, e);
					}
					logger.error("Error in configure for " + name, e);
				}
			}

			else {
				logger.info("Not configuring " + name);
			}
		}
	}

}
