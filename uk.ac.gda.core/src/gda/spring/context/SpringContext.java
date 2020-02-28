/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package gda.spring.context;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static org.springframework.core.env.AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.DefaultNamespaceHandlerResolver;
import org.springframework.beans.factory.xml.PluggableSchemaResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import gda.configuration.properties.LocalProperties;
import gda.factory.ConfigurableAware;
import gda.factory.Factory;
import gda.factory.FactoryBase;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.spring.OsgiServiceBeanHandler;
import gda.spring.SpringApplicationContextBasedObjectFactory;
import gda.util.LocalPropertiesPropertySource;
import uk.ac.diamond.daq.classloading.GDAClassLoaderService;


/**
 * A wrapper around a Spring application context to allow beans to be configured and services to be registered.
 * The {@link #asFactory()} method allows this context to be used with the GDA Finder mechanism.
 */
public class SpringContext {
	private static final String SPRING_PROFILES_PROPERTY_NAME = "gda." + ACTIVE_PROFILES_PROPERTY_NAME;
	private static final Logger logger = LoggerFactory.getLogger(SpringContext.class);
	private ConfigurableBeanTracker configurables;
	private ConfigurableApplicationContext applicationContext;
	private final OsgiServiceBeanHandler osgiServiceBeanHandler = new OsgiServiceBeanHandler();
	private boolean allowExceptionInConfigure = LocalProperties.check(FactoryBase.GDA_FACTORY_ALLOW_EXCEPTION_IN_CONFIGURE);

	/** Create a SpringContext using default profiles */
	public SpringContext(String... xmlFiles) {
		this(xmlFiles, getDefaultProfiles());
	}

	/** Create a SpringContext with the given profiles */
	public SpringContext(String[] xmlFiles, String[] profiles) {
		xmlFiles = Arrays.stream(xmlFiles).map(s -> "file://" + s).toArray(String[]::new);
		configurables = new ConfigurableBeanTracker();
		applicationContext = loadContext(xmlFiles, profiles, configurables);
	}

	/** Create and refresh the application context. Beans are created here but will not be configured */
	private ConfigurableApplicationContext loadContext(String[] files, String[] profiles, ConfigurableBeanTracker configurables) {
		var context = new GenericApplicationContext();
		ClassLoader cl = GDAClassLoaderService.getClassLoaderService()
				.getClassLoaderForLibraryWithGlobalResourceLoading(XmlBeanDefinitionReader.class, Set.of("org.apache.activemq.activemq-osgi"));
		context.setClassLoader(cl);
		context.addBeanFactoryPostProcessor(beanFactory -> beanFactory.addBeanPostProcessor(configurables));
		context.setAllowBeanDefinitionOverriding(false);

		var environment = context.getEnvironment();
		environment.getPropertySources().addFirst(new LocalPropertiesPropertySource());
		environment.setActiveProfiles(profiles);

		var beanReader = new XmlBeanDefinitionReader(context);
		beanReader.setEntityResolver(new PluggableSchemaResolver(cl));
		beanReader.setNamespaceHandlerResolver(new DefaultNamespaceHandlerResolver(cl));
		beanReader.loadBeanDefinitions(files);

		// Load the context
		context.refresh();
		return context;
	}

	/**
	 * Configure all the beans in the application context. Calling pre and post configure methods for any
	 * {@link ConfigurableAware} beans.
	 * @throws FactoryException if any bean's configure method throws an exception and the
	 *     {@link FactoryBase#GDA_FACTORY_ALLOW_EXCEPTION_IN_CONFIGURE} property is not set.
	 */
	public void configure() throws FactoryException {
		configurables.configureAll(allowExceptionInConfigure);
		injectBeansIntoOsgiServiceRegister(applicationContext);
		dumpListOfBeans();
	}

	/**
	 * Check if any beans need injecting as osgi services. Injection is handled by
	 * {@link OsgiServiceBeanHandler}
	 * @param applicationContext the context containing all the beans
	 */
	private void injectBeansIntoOsgiServiceRegister(ApplicationContext applicationContext) {
		Map<String, Object> allBeans = applicationContext.getBeansOfType(Object.class);
		allBeans.forEach(osgiServiceBeanHandler::processBean);
	}

	/**
	 * Write out a list of all configured beans to the logs.
	 * Sorted by file location and by name. Useful for debugging which objects are loaded from where.
	 */
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
			logger.debug("  {}. {} class {} (location: {})", i+1, name, beanDef.getBeanClassName(), location);
			if (!beansByLocation.containsKey(location)) {
				beansByLocation.put(location, new TreeSet<String>(String.CASE_INSENSITIVE_ORDER));
			}
			final TreeSet<String> beansForThisLocation = beansByLocation.get(location);
			beansForThisLocation.add(name);
		}
		logger.debug("Beans by location:");
		beansByLocation.forEach((location, beans) -> {
			logger.debug("    {}", location);
			beans.forEach(name -> logger.debug("        {}", name));
		});

		// Log which classes Spring is instantiating
		final Map<String, Object> beans = applicationContext.getBeansOfType(Object.class); // Get all beans
		final Map<String, Long> classNameToInstanceCount = beans.values().stream()
				.collect(groupingBy(bean -> bean.getClass().getName(), // key the class name
						TreeMap::new, // TreeMap to put in alphabetical order
						counting())); // value the number of each class
		logger.debug("Classes in use: {}", classNameToInstanceCount);
	}

	/** Return a {@link Factory} to access the Findable beans in this context */
	public Factory asFactory() {
		return new SpringApplicationContextBasedObjectFactory(applicationContext) {
			@Override
			public void configure() throws FactoryException {
				// do nothing as beans are already configured
			}
			@Override
			public boolean isConfigured() {
				return true;
			}
		};
	}

	/** Get the default profiles to use based on current LocalProperties */
	public static String[] getDefaultProfiles() {
		return Arrays.stream(LocalProperties.getStringArray(SPRING_PROFILES_PROPERTY_NAME))
				.filter(s -> !s.isEmpty())
				.toArray(String[]::new);
	}

	public static void registerFactory(String xml) throws FactoryException {
		SpringContext context = new SpringContext(xml);
		Finder.addFactory(context.asFactory());
		context.configure();
	}
}
