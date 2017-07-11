/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package gda.mobile.devices.spring;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.StringUtils;
import org.xml.sax.InputSource;

import gda.configuration.properties.LocalProperties;
import gda.device.Scannable;
import gda.factory.ConditionallyConfigurable;
import gda.factory.Configurable;
import gda.factory.Factory;
import gda.factory.FactoryBase;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.factory.corba.util.AdapterFactory;
import gda.jython.IJythonNamespace;
import gda.jython.ITerminalPrinter;
import gda.jython.InterfaceProvider;
import gda.spring.SpringApplicationContextBasedObjectFactory;
import gda.util.LocalPropertiesPropertySource;
import gda.util.SpringObjectServer;

/**
 * instance of this class provides method {@link #loadAdditionalBeans(List)}to create and add additional
 * Spring beans to GDA server and its Jython namespace from Spring bean definition file after GDA server has started.
 *
 * These additional beans are created and added at GDA runtime, without the need to restart GDA servers.
 *
 * Use case: add and remove device support in GDA for mobile equipments, such as users' sample environment.
 *
 * Usage:
 * <ol>
 * <li>This bean must be created in the root application context of the GDA server @see {@link SpringObjectServer}.</li>
 * <li>use this instance's {@link #loadAdditionalBeans(List)} to load bean definition file in a script to add new beans into GDA server</li>
 * <li>if the new beans has reference to existing beans in parent context, the script has to inject these references into new beans in the child context</li>
 * <li>run this script after mobile device has been installed on the beamline</li>
 * </ol>
 * Note: the implementation uses a new Child Spring {@link GenericApplicationContext} to create new beans.
 */
public class LoadAdditionalBeansToObjectServer extends ApplicationObjectSupport implements Configurable, Findable {
	private static final ITerminalPrinter TERMINAL_PRINTER = InterfaceProvider.getTerminalPrinter();
	private static final IJythonNamespace JYTHON_NAMESPACE = InterfaceProvider.getJythonNamespace();
	private static final Logger logger = LoggerFactory.getLogger(LoadAdditionalBeansToObjectServer.class);
	private boolean allowExceptionInConfigure = LocalProperties.check(FactoryBase.GDA_FACTORY_ALLOW_EXCEPTION_IN_CONFIGURE);
	private GenericApplicationContext createdContext;
	private String name;


	public void loadAdditionalBeans(List<String> filepaths) {

		try {
			createdContext = new GenericApplicationContext(getApplicationContext());
			XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(createdContext);
			for (String filepath : filepaths) {
				InputSource r = new InputSource(new FileReader(filepath));
				reader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_XSD);
				int i = reader.loadBeanDefinitions(r);
				logger.debug("{} beans are added to Object Server from file: {}", i, filepath);
			}
			createdContext.getEnvironment().getPropertySources().addFirst(new LocalPropertiesPropertySource());
			createdContext.setAllowBeanDefinitionOverriding(false);
			createdContext.refresh();
			registerFactories();
			configureAllConfigurablesInApplicationContext(createdContext);
			placeInJythonNamescape();
		} catch (FileNotFoundException e) {
			TERMINAL_PRINTER.print(e.getMessage());
			logger.error(e.getMessage(), e);
		} catch (FactoryException e) {
			TERMINAL_PRINTER.print(e.getMessage());
			logger.error(e.getMessage(), e);
		}

	}
	/**
	 * add new beans of type {@link Scannable} to the Jython namespace
	 */
	private void placeInJythonNamescape() {
		String[] beanDefinitionNames = createdContext.getBeanDefinitionNames();
		for (String bean : beanDefinitionNames) {
			Object bean2 = createdContext.getBean(bean);
			if (bean2 instanceof Scannable) {
				JYTHON_NAMESPACE.placeInJythonNamespace(bean, bean2);
			}
		}
	}
	/**
	 * register factories to support GDA Findable interface.
	 */
	private void registerFactories() {
		addSpringBackedFactoryToFinder(createdContext);
		/*
		 * We need to add the adapterFactory to the finder if present in the applicationContext to allow remote objects to be found during subsequent
		 * configureAllFindablesInApplicationContext. The adapterFactory must be added after the spring backed objects as the latter may include those from
		 * corba:import. If the order was otherwise we would duplicate adapters for remote objects. This change is in anticipation of future changes to
		 * corba:import to only import named objects rather than all.
		 */
		addAdapterFactoryToFinder();
	}

	/**
	 * Adds a Spring-backed {@link Factory} to the {@link Finder}.
	 */
	private void addSpringBackedFactoryToFinder(ApplicationContext applicationContext) {
		SpringApplicationContextBasedObjectFactory springObjectFactory = new SpringApplicationContextBasedObjectFactory();
		springObjectFactory.setApplicationContext(applicationContext);
		Finder.getInstance().addFactory(springObjectFactory);
	}

	private void addAdapterFactoryToFinder() {
		Map<String, AdapterFactory> adapterFactories = createdContext.getBeansOfType(AdapterFactory.class);
		for (Map.Entry<String, AdapterFactory> entry : adapterFactories.entrySet()) {
			String name = entry.getKey();
			AdapterFactory adapterFactory = entry.getValue();
			logger.info("Adding AdapterFactory {} (namespace {}) to finder", StringUtils.quote(name), StringUtils.quote(adapterFactory.getName()));
			Finder.getInstance().addFactory(adapterFactory);
		}
	}

	private void configureAllConfigurablesInApplicationContext(ApplicationContext applicationContext) throws FactoryException {
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
			} else {
				logger.info("Not configuring " + name);
			}
		}
	}

	/**
	 * add this object to Jython namespace so it can be accessed in scripting environment.
	 *
	 * @throws FactoryException
	 */
	@Override
	public void configure() throws FactoryException {
		JYTHON_NAMESPACE.placeInJythonNamespace(getName(), this);
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

}
