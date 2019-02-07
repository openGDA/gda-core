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

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static uk.ac.gda.remoting.server.RmiAutomatedExporter.AUTO_EXPORT_RMI_PREFIX;
import static uk.ac.gda.remoting.server.RmiAutomatedExporter.RMI_PORT_PROPERTY;

import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.rmi.RmiInvocationHandler;
import org.springframework.remoting.rmi.RmiRegistryFactoryBean;

import com.google.common.base.Stopwatch;

import gda.configuration.properties.LocalProperties;
import gda.factory.ConfigurableBase;
import gda.factory.Factory;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.factory.Finder;
import uk.ac.gda.api.remoting.ServiceInterface;
import uk.ac.gda.remoting.server.RmiAutomatedExporter;

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
 */
public class RmiProxyFactory extends ConfigurableBase implements Factory, InitializingBean {
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

	/**
	 * This {@link Map} holds the {@link Findable}s this Factory can provide. It is filled by the
	 * {@link #configure()} method.
	 */
	private final Map<String, Findable> nameToFindable = new ConcurrentHashMap<>();

	@Override
	public void afterPropertiesSet() throws Exception {
		// Spring will call this before configure is called on all objects.
		// Needs to be configured early because the configure() method of other objects may use the Finder.
		configure();
	}

	@Override
	public void configure() throws FactoryException {
		if(isConfigured()) {
			return; // Already configured so do nothing
		}
		logger.info("Configuring RmiProxyFactory...");
		// Time how long doing the importing takes
		final Stopwatch stopwatch = Stopwatch.createStarted();

		// Get the available objects from the server RMI registry
		final List<String> availableRmiObjectsUrls;
		final Registry rmiRegistry;
		try {
			final RmiRegistryFactoryBean rrfb = new RmiRegistryFactoryBean();
			rrfb.setHost(serverHost);
			rrfb.setPort(rmiPort);
			rrfb.afterPropertiesSet();
			logger.debug("Connected to RMI registry at: {}", serviceUrlPrefix);
			rmiRegistry = rrfb.getObject();
			availableRmiObjectsUrls = Arrays.asList(rmiRegistry.list());
			logger.debug("RMI registry contains {} objects", availableRmiObjectsUrls.size());
		} catch (Exception e) {
			logger.error("Error connecting to RMI registry at '{}:{}'", serverHost, rmiPort, e);
			throw new FactoryException("Error connecting to RMI registry at:" + serverHost + ":" + rmiPort, e);
		}

		// Filter out RMI exported objects which are not auto exported
		final List<String> objectNames = availableRmiObjectsUrls.stream()
				.filter(a -> a.startsWith(AUTO_EXPORT_RMI_PREFIX)) // Remove objects not auto exported
				.map(a -> a.substring(AUTO_EXPORT_RMI_PREFIX.length())) // Recover name by stripping off prefix
				.collect(toList());

		logger.debug("Auto importing {} objects", objectNames.size());

		for (String objectName : objectNames) {
			try {
				// This cast should be ok because we are looking at objects exported using Springs RMI Exporter
				final RmiInvocationHandler remote = (RmiInvocationHandler) rmiRegistry
						.lookup(AUTO_EXPORT_RMI_PREFIX + objectName);

				// Use the Spring bundle loader which should be able to see all the needed classes
				final Class<?> serviceInterface = Class.forName(remote.getTargetInterfaceName(), true, SPRING_BUNDLE_LOADER);

				// Make the proxy factory
				final GdaRmiProxyFactoryBean bean = new GdaRmiProxyFactoryBean();
				bean.setServiceUrl(serviceUrlPrefix + objectName);
				bean.setServiceInterface(serviceInterface);
				bean.setRefreshStubOnConnectFailure(true);
				bean.setObjectName(objectName);
				bean.afterPropertiesSet(); // This is where we actually import

				// Use the factory to get the object. We know it's Findable because to be auto-exported it must be.
				// See @ServiceInterface
				final Findable proxyObject = (Findable) bean.getObject();

				nameToFindable.put(objectName, proxyObject);
				logger.debug("Imported '{}' with interface '{}'", objectName, serviceInterface.getName());
			} catch (Exception e) {
				logger.error("Failed to import '{}'", objectName, e);
			}
		}

		// Register as a factory with the finder
		Finder.getInstance().addFactory(this);
		logger.info("Finished importing. {} RMI objects have been imported", nameToFindable.size());
		logger.debug("Importing all objects took {} ms", stopwatch.elapsed(MILLISECONDS));
		setConfigured(true);
	}

	@Override
	public void addFindable(Findable findable) {
		throw new UnsupportedOperationException(
				"Objects can't be added to this factory. It provides access to remote objects");
	}

	@Override
	public List<Findable> getFindables() {
		checkConfigured();
		return new ArrayList<>(nameToFindable.values());
	}

	@Override
	public List<String> getFindableNames() {
		checkConfigured();
		return new ArrayList<>(nameToFindable.keySet());
	}

	@SuppressWarnings("unchecked") // We don't know what type the caller is expecting so this might throw!
	@Override
	public <T extends Findable> T getFindable(String name) throws FactoryException {
		checkConfigured();
		return (T) nameToFindable.get(name);
	}

	@SuppressWarnings("unchecked") // This is safe because we have just ensured the object is an instance of T
	@Override
	public <T extends Findable> Map<String, T> getFindablesOfType(Class<T> clazz) {
		checkConfigured();
		return nameToFindable.entrySet().stream()
		.filter(entry -> clazz.isInstance(entry.getValue()))
		.collect(toMap(Entry::getKey, entry -> (T) entry.getValue()));
	}

	/**
	 * Ensures this is configured and throws an exception if not
	 *
	 * @throws IllegalStateException if not configured
	 */
	private void checkConfigured() {
		if(!isConfigured()) {
			throw new IllegalStateException("RmiProxyFactory is not yet configured");
		}
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
