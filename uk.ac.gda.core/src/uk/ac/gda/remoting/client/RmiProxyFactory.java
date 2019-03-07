/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.gda.remoting.client;

import static uk.ac.gda.remoting.server.RmiAutomatedExporter.AUTO_EXPORT_RMI_PREFIX;
import static uk.ac.gda.remoting.server.RmiAutomatedExporter.REMOTE_OBJECT_PROVIDER;
import static uk.ac.gda.remoting.server.RmiAutomatedExporter.RMI_PORT_PROPERTY;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import gda.configuration.properties.LocalProperties;
import gda.factory.ConfigurableBase;
import gda.factory.Factory;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.factory.Finder;
import uk.ac.gda.api.remoting.ServiceInterface;
import uk.ac.gda.remoting.server.RmiAutomatedExporter;
import uk.ac.gda.remoting.server.RmiObjectInfo;
import uk.ac.gda.remoting.server.RmiRemoteObjectProvider;

/**
 * This is a {@link Factory} for making auto-exported RMI objects available via the {@link Finder}. Auto-exported
 * objects will be made available using their name and with the interface declared using the {@link ServiceInterface}
 * annotation.
 * <p>
 * {@link RmiAutomatedExporter} is the server-side class responsible for auto-exporting over RMI the objects which this
 * class will import.
 *
 * To use this add the following to the client Spring XML configuration:
 *
 * <pre>
 * {@code
 * <bean class="uk.ac.gda.remoting.client.RmiProxyFactory" />
 * }
 * </pre>
 *
 * @see RmiAutomatedExporter
 * @author James Mudd
 * @since GDA 9.8
 * @since GDA 9.12 made importing dynamic DAQ-1904
 */
public class RmiProxyFactory extends ConfigurableBase implements Factory {
	private static final Logger logger = LoggerFactory.getLogger(RmiProxyFactory.class);

	/** The location of the GDA server */
	private final String serverHost = LocalProperties.get("gda.server.host");
	/** The RMI port used to export by the server */
	private final int rmiPort = LocalProperties.getAsInt(RMI_PORT_PROPERTY, 1099);
	/** The URL which prefixes the objects names to access the RMI service */
	private final String serviceUrlPrefix = "rmi://" + serverHost + ":" + rmiPort + "/"
			+ AUTO_EXPORT_RMI_PREFIX;

	/**
	 * This is the uk.ac.diamond.org.springframework OSGi bundle classloader. It's needed here because you might want to
	 * import any class Spring has instantiated.
	 */
	private static final ClassLoader SPRING_BUNDLE_LOADER = InitializingBean.class.getClassLoader();

	/** This {@link Map} caches the {@link Object}s this factory has imported */
	private final ConcurrentMap<String, Object> importedObjects = new ConcurrentHashMap<>();

	/** This is the server side object that is asked to provide remote objects */
	private final RmiRemoteObjectProvider remoteObjectProvider;


	public RmiProxyFactory() throws FactoryException {
		logger.info("Creating RmiProxyFactory...");

		try {
			remoteObjectProvider = createProxy(REMOTE_OBJECT_PROVIDER, RmiRemoteObjectProvider.class);
			logger.debug("Connected to server remote object provider");
		} catch (Exception e) {
			throw new FactoryException("Failed to connect to server remote object provider", e);
		}

		// Register as a factory with the finder
		Finder.getInstance().addFactory(this);

		// Its already configured at this point but we have to implement Configurable because of Factory
		setConfigured(true);
		logger.info("Finished creation");
	}

	@SuppressWarnings("unchecked") // Will be safe the proxy will implement the service interface
	private <T> T createProxy(String name, Class<T> serviceInterface) throws Exception {
		final GdaRmiProxyFactoryBean proxyFactory = new GdaRmiProxyFactoryBean();
		proxyFactory.setObjectName(name);
		proxyFactory.setServiceUrl(serviceUrlPrefix + name);
		proxyFactory.setServiceInterface(serviceInterface);
		proxyFactory.setRefreshStubOnConnectFailure(true);
		proxyFactory.afterPropertiesSet(); // This is where we actually import

		// Use the factory to get the proxy. Cast it to the service interface type
		return (T) proxyFactory.getObject();
	}

	@Override
	public void addFindable(Findable findable) {
		throw new UnsupportedOperationException(
				"Objects can't be added to this factory. It provides access to remote objects");
	}

	@Override
	public List<Findable> getFindables() {
		logger.warn("Getting ALL remote findables. This will cause all possible server objects to be exported");
		List<String> findableNames = getFindableNames();
		List<Findable> findables = new ArrayList<>();
		for (String name : findableNames) {
			try {
				findables.add(getFindable(name));
			} catch (FactoryException e) {
				logger.error("Failed getting findable '{}'", name, e);
			}
		}
		return findables;
	}

	@Override
	public List<String> getFindableNames() {
		// Just get everything implementing Findable
		return new ArrayList<>(remoteObjectProvider.getRemoteObjectNamesImplementingType(Findable.class.getCanonicalName()));
	}

	@SuppressWarnings("unchecked") // We don't know what type the caller is expecting so this might throw!
	@Override
	public <T extends Findable> T getFindable(String name) throws FactoryException {
		// If importObject fails somehow it will return null and no mapping will be made.
		return (T) importedObjects.computeIfAbsent(name, this::importObject);
	}

	private Object importObject(String name) {
		try {
			logger.debug("Asking server for '{}'...", name);
			RmiObjectInfo remoteObject = remoteObjectProvider.getRemoteObject(name);
			if (remoteObject != null) {
				logger.debug("Importing '{}'", name);
				Class<?> serviceInterface = SPRING_BUNDLE_LOADER.loadClass(remoteObject.getServiceInterface());
				Object proxy = createProxy(name, serviceInterface);
				logger.debug("Sucessfully imported '{}', '{}'", name, proxy);
				return proxy;
			} else {
				logger.debug("No remote obejct avaliable called '{}'", name);
				return null;
			}
		} catch (Exception e) {
			logger.error("Failed to import remote object '{}'", name, e);
			return null;
		}
	}

	@Override
	public <T extends Findable> Map<String, T> getFindablesOfType(Class<T> clazz) {
		Map<String, T> findables = new HashMap<>();
		Set<String> remoteObjectNamesImplementingType = remoteObjectProvider.getRemoteObjectNamesImplementingType(clazz.getCanonicalName());
		for (String name : remoteObjectNamesImplementingType) {
			try {
				findables.put(name, getFindable(name));
			} catch (FactoryException e) {
				logger.error("Failed to import '{}'", name, e);
			}
		}
		return findables;
	}

	@Override
	public boolean containsExportableObjects() {
		// false because this provides imported objects which should not be re-exported
		return false;
	}

	@Override
	public boolean isLocal() {
		// false because its function is to provide remote objects
		return false;
	}

	@Override
	public String toString() {
		return "RmiProxyFactory [" + serviceUrlPrefix + "]";
	}

}
