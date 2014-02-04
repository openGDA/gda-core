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

import gda.configuration.properties.LocalProperties;
import gda.factory.AdapterObjectCreator;
import gda.factory.ClientXmlObjectCreator;
import gda.factory.Factory;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.factory.IObjectCreator;
import gda.factory.ServerXmlObjectCreator;
import gda.factory.corba.util.AdapterFactory;
import gda.factory.corba.util.FactoryImplFactory;
import gda.spring.SpringApplicationContextBasedObjectFactory;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.util.StringUtils;

/**
 * A subclass of {@link ObjectServer} that uses either a default list of
 * {@link IObjectCreator}s (for backwards compatibility) or a list of
 * {@link IObjectCreator}s explicitly defined in a Spring application context. 
 */
public class ObjectCreatorsObjectServer extends ObjectServer {
	
	private static final Logger logger = LoggerFactory.getLogger(ObjectCreatorsObjectServer.class);

	protected static final String OBJECT_CREATORS_BEAN_NAME = "objectCreators";
	
	protected ApplicationContext appContext;
	
	private List<IObjectCreator> objectCreators;
	
	private boolean serverSide = true;

	/**
	 * Creates an object server.
	 * 
	 * @param xmlFile
	 *            the XML configuration file
	 * @param mappingFile
	 *            the mapping file
	 * @param serverSide
	 *            {@code true} if this is a server-side object server
	 * @param localObjectsOnly
	 *            {@code true} if this object server should run in single application mode
	 */
	public ObjectCreatorsObjectServer(File xmlFile, String mappingFile, boolean serverSide, boolean localObjectsOnly) {
		super(xmlFile, localObjectsOnly);
		this.serverSide = serverSide;
		startServerUsingObjectFactoryXmlFile(xmlFile, mappingFile);
	}
	
	/**
	 * Creates an object server by reading a list of object creators from the
	 * specified file.
	 * 
	 * @param objectCreatorsFile the object creators file
	 * @param serverSide {@code true} if this is a server-side object server
	 * @param localObjectsOnly {@code true} if this object server should run in single application mode
	 */
	public ObjectCreatorsObjectServer(File objectCreatorsFile, boolean serverSide, boolean localObjectsOnly) {
		super(objectCreatorsFile, localObjectsOnly);
		this.serverSide = serverSide;
		appContext = new FileSystemXmlApplicationContext("file:" + objectCreatorsFile.getAbsolutePath());
		objectCreators = retrieveObjectCreatorsFromSpringApplicationContext(appContext);
	}
	
	/**
	 * Returns the location of the standard Spring beans file that has the
	 * specified suffix.
	 * 
	 * @param suffix the suffix
	 * 
	 * @return the file
	 */
	private File getDefaultSpringFactoryFile(String suffix) {
		final String configDir = LocalProperties.get(LocalProperties.GDA_CONFIG);
		final String factoryName = LocalProperties.get("gda.factory.factoryName");
		return new File(new File(configDir, "xml"), factoryName + suffix);
	}
	
	/**
	 * Creates an {@link AdapterObjectCreator}.
	 * 
	 * @return the AdapterObjectCreator
	 */
	private AdapterObjectCreator createAdapterObjectCreator() {
		// Add an adapter factory to the finder to allow access to objects created elsewhere. eg. in a
		// standalone object server.
		AdapterObjectCreator creator = new AdapterObjectCreator();
		creator.setName(LocalProperties.get("gda.factory.factoryName"));
		return creator;
	}
	
	/**
	 * Creates a list of object creators that mimics the existing behaviour of
	 * a server-side object server.
	 * 
	 * @param xmlFile the XML file to use for the ObjectCreator
	 * @param mappingFile the mapping file to use for the ObjectCreator
	 * 
	 * @return a list of object creators
	 */
	private List<IObjectCreator> getDefaultServerSideObjectCreators(String xmlFile, String mappingFile) {
		List<IObjectCreator> objectCreators = new Vector<IObjectCreator>();
		
		ServerXmlObjectCreator creator = new ServerXmlObjectCreator();
		creator.setXmlFile(xmlFile);
		creator.setMappingFile(mappingFile);
		creator.setBuildProxies(LocalProperties.isAccessControlEnabled());
		creator.setUseDefaultSchema(true);
		creator.setDoPropertySubstitution(LocalProperties.check("gda.factory.doStringInterpolation", false));
		objectCreators.add(creator);
		
		objectCreators.add(createAdapterObjectCreator());
		
		return objectCreators;
	}
	
	/**
	 * Creates a list of object creators that mimics the existing behaviour of
	 * a client-side object server.
	 * 
	 * @param xmlFile the XML file to use for the ObjectCreator
	 * @param mappingFile the mapping file to use for the ObjectCreator
	 * 
	 * @return a list of object creators
	 */
	private List<IObjectCreator> getDefaultClientSideObjectCreators(String xmlFile, String mappingFile) {
		List<IObjectCreator> objectCreators = new Vector<IObjectCreator>();
		
		ClientXmlObjectCreator creator = new ClientXmlObjectCreator();
		creator.setXmlFile(xmlFile);
		creator.setMappingFile(mappingFile);
		creator.setUseDefaultSchema(true);
		creator.setDoPropertySubstitution(LocalProperties.check("gda.factory.doStringInterpolation", false));
		objectCreators.add(creator);
		
		objectCreators.add(createAdapterObjectCreator());
		
		return objectCreators;
	}
	
	/**
	 * Retrieves the object creators from the supplied Spring application
	 * context.
	 * 
	 * @param applicationContext the Spring application context
	 * 
	 * @return the object creators in the context
	 */
	private static List<IObjectCreator> retrieveObjectCreatorsFromSpringApplicationContext(ApplicationContext applicationContext) {
		List<IObjectCreator> allObjectCreators = new Vector<IObjectCreator>();
		
		Map<String, IObjectCreator> topLevelObjectCreators = applicationContext.getBeansOfType(IObjectCreator.class);
		allObjectCreators.addAll(topLevelObjectCreators.values());
		
		if (applicationContext.containsBean(OBJECT_CREATORS_BEAN_NAME)) {
			List<IObjectCreator> objectCreatorsList = getObjectCreatorsListFromApplicationContext(applicationContext);
			allObjectCreators.addAll(objectCreatorsList);
			logger.warn("Please delete your " + StringUtils.quote(OBJECT_CREATORS_BEAN_NAME) + " list and move your object creators to the top level of your Spring configuration file.");
			logger.warn("Also consider renaming your Spring configuration file, e.g. from \"object-creators-server.xml\" to just \"server.xml\".");
		}
		
		return allObjectCreators;
	}
	
	@SuppressWarnings("unchecked")
	private static final List<IObjectCreator> getObjectCreatorsListFromApplicationContext(ApplicationContext context) {
		return context.getBean(OBJECT_CREATORS_BEAN_NAME, List.class);
	}
	
	private void startServerUsingObjectFactoryXmlFile(File file, String mappingFile) {
		if (serverSide) {
			objectCreators = getDefaultServerSideObjectCreators(file.getAbsolutePath(), mappingFile);
		} else {
			objectCreators = getDefaultClientSideObjectCreators(file.getAbsolutePath(), mappingFile);
		}
	}
	
	@Override
	protected void startServer() throws FactoryException {
		
		Finder finder = Finder.getInstance();
		
		// Use each object creator to create an object factory
		for (IObjectCreator objectCreator : objectCreators) {
		
			// If running in local mode, and object creator creates
			// non-local objects, skip it
			if (localObjectsOnly && !objectCreator.isLocal()) {
				continue;
			}
			
			// Create factory
			Factory factory = objectCreator.getFactory();
			factories.add(factory);
		}
		
		// If the server is based on a Spring configuration file, create a Factory backed by the Spring application
		// context, so that Findables in the Spring context are configured and made remotely available
		if (appContext != null) {
			SpringApplicationContextBasedObjectFactory springObjectFactory = new SpringApplicationContextBasedObjectFactory();
			springObjectFactory.setName(LocalProperties.get(LocalProperties.GDA_FACTORY_NAME));
			springObjectFactory.setApplicationContext(appContext);
			factories.add(springObjectFactory);
			
			/*
			 * We need to add the adapterFactory to the finder if present in the applicationContext to allow remote objects to 
			 * be found during subsequent configureAllFindablesInApplicationContext. 
			 * The adapterFactory must be added after the spring backed objects as the latter may include those from corba:import. If
			 * the order was otherwise we would duplicate adapters for remote objects. 
			 * This change is in anticipation of future changes to corba:import to only import named objects rather than all.
			 */			
			Map<String,AdapterFactory> adapterFactories = appContext.getBeansOfType(AdapterFactory.class);
			for (Map.Entry<String, AdapterFactory> entry : adapterFactories.entrySet()) {
				entry.getKey();
				AdapterFactory adapterFactory = entry.getValue();
				factories.add(adapterFactory);
			}
			
		}
		
		// Add all factories to Finder
		for (Factory factory : factories) {
			finder.addFactory(factory);
		}
		
		// Configure factories
		for (Factory factory : factories) {
			factory.configure();
		}

		// Export local objects to name service
		if (serverSide && !localObjectsOnly) {
			for (Factory f : factories) {
				if (f.containsExportableObjects()) {
					FactoryImplFactory implFactory = new FactoryImplFactory(f, netService);
					implFactories.add(implFactory);
					implFactory.configure();
				}
			}
			startOrbRunThread();
		}
	}
	
}
