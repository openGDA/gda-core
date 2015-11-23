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
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.util.StringUtils;

import gda.configuration.properties.LocalProperties;
import gda.factory.Factory;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.factory.corba.util.EventService;
import gda.factory.corba.util.ImplFactory;
import gda.factory.corba.util.NetService;
import gda.util.logging.LogbackUtils;
import gda.util.logging.LoggingUtils;

/**
 * A utility class which creates objects for local or remote access.
 */
public abstract class ObjectServer implements Runnable {

	/**
	 * The java property which defines where 'initialisation complete' files are to be stored.
	 */
	public static final String INITIALISATIONCOMPLETEFOLDER = "gda.objectserver.initialisationCompleteFolder";

	protected File xmlFile;

	protected NetService netService;

	protected boolean localObjectsOnly = false;

	private static final Logger logger = LoggerFactory.getLogger(ObjectServer.class);

	/**
	 * Complete list of factories that have been created for this object server.
	 */
	protected List<Factory> factories = new Vector<Factory>();


	/**
	 * All {@link ImplFactory}s that have been created by this object server
	 * to make objects remotely accessible.
	 */
	protected List<ImplFactory> implFactories = new Vector<ImplFactory>();

	/**
	 * Creates an object server.
	 *
	 * @param xmlFile
	 *            the XML configuration file
	 * @param localObjectsOnly
	 *            {@code true} if this object server should run in single application mode
	 */
	public ObjectServer(File xmlFile, boolean localObjectsOnly) {
		this.xmlFile = xmlFile;
		this.localObjectsOnly = localObjectsOnly;
	}

	/**
	 * Returns the default server-side XML file to use if none is specified.
	 *
	 * @return the default server-side XML file
	 */
	private static String getDefaultServerSideXmlFile() {
		return LocalProperties.get(LocalProperties.GDA_OBJECTSERVER_XML);
	}

	/**
	 * Returns the default client side XML file to use if none is specified.
	 *
	 * @return the default client-side XML file
	 */
	private static String getDefaultClientSideXmlFile() {
		return LocalProperties.get(LocalProperties.GDA_GUI_XML);
	}

	/**
	 * Convenience method for constructing server side objects using the xml file names specified in the local
	 * properties file
	 *
	 * @return the instance of the object server.
	 * @throws FactoryException
	 */
	public static ObjectServer createServerImpl() throws FactoryException {
		return ObjectServer.createServerImpl(getDefaultServerSideXmlFile());
	}

	/**
	 * Convenience method for constructing server side objects
	 *
	 * @param xmlFile
	 *            fully qualified fileName to specifying in XML the objects to be created.
	 * @param mappingFile
	 *            the mapping file
	 * @return the instance of the object server.
	 * @throws FactoryException
	 */
	public static ObjectServer createServerImpl(String xmlFile, String mappingFile) throws FactoryException {
		ObjectServer objectServer = createObjectServer(xmlFile, mappingFile, true, false);
		objectServer.configure();
		logger.info("Server initialisation complete. xmlFile = " + xmlFile);
		return objectServer;
	}

	/**
	 * Convenience method for constructing server side objects using the specified XML file.
	 *
	 * @param xmlFile
	 *            the XML configuration file
	 * @return the object server
	 * @throws FactoryException
	 */
	public static ObjectServer createServerImpl(String xmlFile) throws FactoryException {
		return ObjectServer.createServerImpl(xmlFile, null);
	}

	/**
	 * Convenience method for constructing client side objects using the xml file names specified in the local
	 * properties file
	 *
	 * @return the instance of the object server.
	 * @throws FactoryException
	 */
	public static ObjectServer createClientImpl() throws FactoryException {
		return ObjectServer.createClientImpl(getDefaultClientSideXmlFile());
	}

	/**
	 * Convenience method for constructing client side objects, only for test suite programs!!!!!!!!!!!!!
	 *
	 * @param xmlFile
	 *            fully qualified fileName to specifying in XML the objects to be created.
	 * @return the instance of the object server.
	 * @throws FactoryException
	 */
	public static ObjectServer createClientImpl(String xmlFile) throws FactoryException {

		// Nothing specified? - look up default
		if (xmlFile == null) {
			xmlFile = getDefaultClientSideXmlFile();
		}

		// Check an XML file has been specified somewhere
		if (!StringUtils.hasText(xmlFile)) {
			final String msg = "No XML file specified - set the '%s' property, or use the -x option with the launcher";
			throw new IllegalArgumentException(String.format(msg, LocalProperties.GDA_GUI_XML));
		}

		ObjectServer objectServer = createObjectServer(xmlFile, null, false, isLocal());
		objectServer.configure();
		logger.info("Client initialisation complete");
		return objectServer;
	}

	/**
	 * Convenience method for constructing local objects, only for test suite programs!!!!!!!!!!!!!
	 *
	 * @param xmlFile
	 *            fully qualified fileName to specifying in XML the objects to be created.
	 * @return the instance of the object server.
	 * @throws FactoryException
	 */
	public static ObjectServer createLocalImpl(String xmlFile) throws FactoryException {
		ObjectServer objectServer = createObjectServer(xmlFile, null, false, true);
		objectServer.configure();
		logger.info("Local Objects initialisation complete");
		return objectServer;
	}

	@SuppressWarnings("unused")
	private static ObjectServer createObjectServer(String xmlFile, String mappingFile, boolean serverSide, boolean localObjectsOnly) throws FactoryException {
		File file = getAbsoluteFilePath(xmlFile);
		logger.info("Starting ObjectServer using file " + xmlFile);

		if (!file.exists()) {
			throw new FactoryException(String.format("File does not exist: %s", file));
		}

		return new SpringObjectServer(file, localObjectsOnly);
	}

	protected static BeanDefinitionRegistry loadBeanDefinitions(File file) {
		BeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(registry);
		reader.loadBeanDefinitions("file:" + file.getAbsolutePath());
		return registry;
	}

	/**
	 * Configure the object server.
	 * @throws FactoryException
	 */
	public void configure() throws FactoryException {
		if (!localObjectsOnly) {
			logger.info("Calling NetService.getInstance()");
			netService = NetService.getInstance();
			logger.info("Calling EventService.getInstance()");
			EventService.getInstance();
			logger.info("Completed EventService.getInstance()");
		}

		LocalProperties.checkForObsoleteProperties();
		// Dump out all of the properties to the logging system at objectserver startup
		LocalProperties.dumpProperties();

		startServer();

		createNewStartupFile();
	}

	private static File getStartupFile(String xmlFileNameWithExtension) {
		String baseFileName = xmlFileNameWithExtension.substring(xmlFileNameWithExtension.lastIndexOf(File.separator) + 1,
				xmlFileNameWithExtension.length()); //last part at end of path
		String xmlFileNameWithoutExtension = baseFileName.substring(0, baseFileName.indexOf(".xml"));
		String fileDir = LocalProperties.get(INITIALISATIONCOMPLETEFOLDER);
		if (fileDir == null) {
			fileDir = System.getenv("TEMP");
		}
		if (fileDir == null) {
			fileDir = "/tmp";
		}
		String filename = fileDir + File.separator + "object_server_startup_" + xmlFileNameWithoutExtension;
		if (profileName != null) {
			filename = fileDir + File.separator + "object_server_startup_" + xmlFileNameWithoutExtension + "_"+ profileName;
		}
		File oos = new File(filename);
		return oos;
	}
	protected void createNewStartupFile() {
		File oos = getStartupFile(xmlFile.getAbsolutePath());
		try {
			oos.createNewFile();
			logger.info("startup file created: " + oos.getAbsolutePath());
		} catch (IOException e) {
			logger.info("Error creating startup file:" + oos.getAbsolutePath(),e);
		}
	}

	private static File getAbsoluteFilePath(String xmlFile) {
		// Ensure we have an absolute path to the file
		File file = new File(xmlFile);
		if (!file.isAbsolute()) {
			final String configDir = LocalProperties.get(LocalProperties.GDA_CONFIG);
			file = new File(new File(configDir, "xml"), xmlFile);
		}
		String fullPath = file.getAbsolutePath();
		// README - we're outlawing backslashes - since no distinction between
		// string properties and URL/URI/path properties in GDA code
		// so have to do this for all property strings.
		// It would be nice to move client code over to using getPath instead!
		fullPath = fullPath.replace('\\', '/');

		return new File(fullPath);
	}

	/**
	 * Return the object specified by name by looking it up in the Finder.
	 *
	 * @param name
	 *            the name of the Findable object to return
	 * @return the Findable object or null if not found
	 */

	public Findable getFindable(String name) {
		return Finder.getInstance().find(name);
	}

	/**
	 * Return a list of names of all objects created by this instance of the
	 * object server.
	 *
	 * @return a list of names
	 */
	public List<String> getFindableNames() {
		List<String> objectNames = new Vector<String>();
		for (Factory f : factories) {
			if (f.isLocal()) {
				objectNames.addAll(f.getFindableNames());
			}
		}
		return objectNames;
	}

	/**
	 * Starts a new thread that runs this {@code ObjectServer} instance. The
	 * thread continues to run until the ORB shuts down.
	 */
	protected void startOrbRunThread() {
		Thread t = new Thread(this, getClass().getName());
		t.start();
	}

	/**
	 * Initiates the server loop for remote objects. This method never returns.
	 */
	@Override
	public void run() {
		// server loop: never returns from this call!
		if (!localObjectsOnly) {
			netService.serverRun();
		}
	}

	/**
	 * Parse command-line arguments; may alter the xmlFile and/or mappingFile options.
	 *
	 * @param args
	 *            command-line arguments
	 */
	private static void parseArgs(String[] args) {
		int argno = 0;
		int argc = args.length;
		while (argno < argc) {
			if (args[argno].equals("-f") && (argno + 1 < argc)) {
				commandLineXmlFile = args[++argno];
			} else if (args[argno].equals("-m") && (argno + 1 < argc)) {
				commandLineMappingFile = args[++argno];
			} else if (args[argno].equals("-h") || args[argno].equals("--help")) {
				usage();
			} else if (args[argno].equals("-p") && (argno + 1 < argc)) {
				profileName = args[++argno];
			}
			argno++;
		}
	}

	/**
	 * Displays a message on the console describing the correct usage of the main program and all of the optional
	 * parameters.
	 */
	public static void usage() {
		logger.debug("Usage: ObjectServer [options]");
		logger.debug("Options:");
		logger.debug("-f <xml filename>");
		logger.debug("-m <mapping filename>");
		logger.debug("-p <profile name>");
		logger.debug("-h, --help  Display this help message.");
		System.exit(0);
	}

	/**
	 * Determines whether this is a local or server/client setup by examining the {@code gda.oe.oefactory} property.
	 *
	 * @return {@code true} if this is a local setup; {@code false} otherwise
	 */
	public static boolean isLocal() {
		return LocalProperties.get(LocalProperties.GDA_OE_FACTORY, "Remote").equalsIgnoreCase("local");
	}

	private static String commandLineXmlFile;

	private static String commandLineMappingFile;

	private static String profileName;

	/**
	 * Creates an implementation of local and server side objects as described by an XML file and associated mapping
	 * file. Both defined in the local properties file but the command line options give preference.
	 *
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		spawn(args);
	}

	public static ObjectServer spawn(String[] args) {
		LoggingUtils.setLogDirectory();
		LogbackUtils.configureLoggingForServerProcess("objectserver");
		ObjectServer server = null;

		try {
			// Set default options...
			commandLineXmlFile = getDefaultServerSideXmlFile();
			// ...but possibly override them with command-line arguments
			parseArgs(args);

			// Check an XML file has been specified somewhere
			if (!StringUtils.hasText(commandLineXmlFile)) {
				final String msg = "No XML file specified - set the '%s' property, or use the -x option with the launcher";
				throw new IllegalArgumentException(String.format(msg, LocalProperties.GDA_OBJECTSERVER_XML));
			}

			// delete an old file created at the end of the configure phase
			File oos = getStartupFile(commandLineXmlFile);
			if (oos.exists()){
				oos.delete();
			}
			oos = null;

			server = createServerImpl(commandLineXmlFile, commandLineMappingFile);

		} catch (Exception e) {
			final String msg = "Unable to start ObjectServer";
			logger.error(msg, e);
			System.err.println(msg);
			e.printStackTrace(System.err);
		}
		return server;
	}

	/**
	 * Shuts down this object server, unregistering objects from the CORBA
	 * name server.
	 */
	public void shutdown() {
		logger.info("ObjectServer is shutting down");

		// Shutdown ImplFactorys, to unbind CORBA names
		for (ImplFactory implFactory : implFactories) {
			implFactory.shutdown();
		}
	}

	protected abstract void startServer() throws FactoryException;

}
