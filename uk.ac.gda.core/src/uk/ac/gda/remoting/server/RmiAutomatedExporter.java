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

package uk.ac.gda.remoting.server;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.remoting.rmi.RmiServiceExporter;

import gda.configuration.properties.LocalProperties;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.observable.IObservable;
import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * This class will automatically create RMI exports when requested via the {@link RmiRemoteObjectProvider} interface.
 * Objects will be exported using the service interface via the {@link ServiceInterface} annotation. In addition it will
 * setup events dispatching if the service interface extends {@link IObservable}.
 * <p>
 * To use this add the following to the server Spring XML configuration:
 *
 * <pre>
 * {@code
 * <bean class="uk.ac.gda.remoting.server.RmiAutomatedExporter" />
 * }
 * </pre>
 *
 * <b><font color="green"> This is the PREFERRED method for exporting objects over RMI. For more details on usage see
 * <a href="https://alfred.diamond.ac.uk/documentation/manuals/GDA_Developer_Guide/master/remoting.html#automated-rmi-exporting"> here.
 * </a></font></b>
 * <p>
 *
 * @author James Mudd
 * @since GDA 9.7
 * @since GDA 9.12 - Converted to be dynamic (DAQ-1264)
 */
public class RmiAutomatedExporter implements RmiRemoteObjectProvider {

	private static final Logger logger = LoggerFactory.getLogger(RmiAutomatedExporter.class);

	/** The name used for this object when exported over RMI */
	public static final String REMOTE_OBJECT_PROVIDER = RmiAutomatedExporter.class.getSimpleName();

	/** Namespace under which all auto-exported URLs are made */
	public static final String AUTO_EXPORT_RMI_PREFIX = "gda-auto-export/";

	/** Property that allows the RMI port to be changed. it defaults to 1099 the default RMI port */
	public static final String RMI_PORT_PROPERTY = "uk.ac.gda.remoting.rmiPort";

	/** The port used to expose both the RMI registry and services */
	private final int rmiPort = LocalProperties.getAsInt(RMI_PORT_PROPERTY, 1099);

	/**
	 * This is the uk.ac.diamond.org.springframework OSGi bundle classloader. It's needed here because you might want to
	 * export any class Spring has instantiated.
	 */
	private static final ClassLoader SPRING_BUNDLE_LOADER = RmiServiceExporter.class.getClassLoader();

	/** List of all the exporters this is to allow them to be unbound at shutdown */
	private final List<RmiServiceExporter> exporters = new ArrayList<>();

	private final ConcurrentMap<String, RmiObjectInfo> exportedObjects = new ConcurrentHashMap<>();

	public RmiAutomatedExporter() throws RemoteException {
		final RmiServiceExporter serviceExporter = new RmiServiceExporter();
		serviceExporter.setRegistryPort(rmiPort);
		serviceExporter.setServicePort(rmiPort);
		serviceExporter.setService(this);
		serviceExporter.setServiceName(AUTO_EXPORT_RMI_PREFIX + REMOTE_OBJECT_PROVIDER);
		serviceExporter.setServiceInterface(RmiRemoteObjectProvider.class);
		serviceExporter.setReplaceExistingBinding(false); // Try to be safe there shouldn't be an existing binding

		// We need to switch the TCCL to the Spring bundle loader here as the first time and RMI registry is created it uses it to load classes.
		final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(SPRING_BUNDLE_LOADER);
			// Actually export the service here
			serviceExporter.prepare();
			logger.debug("Enabled remote object RMI requests");
		}
		finally {
			Thread.currentThread().setContextClassLoader(tccl);
		}

		// Add to list of exporters for unbinding at shutdown
		exporters.add(serviceExporter);
	}

	/**
	 * Export the object over RMI, this includes setting up events if needed.
	 *
	 * @param name
	 *            The name of the object to be exported
	 * @param bean
	 *            The object itself
	 * @param serviceInterface
	 *            The interface to expose over RMI
	 * @return <code>true</code> if events are supported by this object <code>false</code> otherwise
	 */
	private boolean export(String name, Findable bean, Class<?> serviceInterface) {
		logger.trace("Exporting '{}' with interface '{}'...", name, serviceInterface.getName());
		final RmiServiceExporter serviceExporter = new RmiServiceExporter();
		serviceExporter.setRegistryPort(rmiPort);
		serviceExporter.setServicePort(rmiPort);
		serviceExporter.setService(bean);
		serviceExporter.setServiceName(AUTO_EXPORT_RMI_PREFIX + name);
		serviceExporter.setServiceInterface(serviceInterface);
		serviceExporter.setReplaceExistingBinding(false); // Try to be safe there shouldn't be an existing binding
		try {
			// Actually export the service here
			serviceExporter.prepare();
			logger.debug("Exported '{}' with interface '{}'", name, serviceInterface.getName());

			// Add to list of exporters for unbinding at shutdown
			exporters.add(serviceExporter);

			// If the service interface extends IObservable then setup events forwarding
			if (IObservable.class.isAssignableFrom(serviceInterface)) {
				setupEventDispatch(name, (IObservable) bean);
				return true;
			} else {
				logger.debug(
						"No events support added for '{}' as it's service interface '{}' does not implement IObservable",
						name, serviceInterface.getName());
				return false;
			}
		} catch (RemoteException e) {
			logger.error("Exception exporting '{}' with interface '{}'", name, serviceInterface.getName(), e);
			return false;
		}
	}

	private void setupEventDispatch(String name, IObservable observable) {
		ServerSideEventDispatcher observer = new ServerSideEventDispatcher();
		observer.setSourceName(name);
		observer.setObject(observable);
		try {
			observer.afterPropertiesSet();
			logger.debug("Setup events dispatch fo '{}'", name);
		} catch (Exception e) {
			logger.error("Failed to setup event dispatching for '{}' no events will be sent to the client", e);
		}
	}

	/**
	 * Try's to unbind all the RMI services exported. Failures will be logged only.
	 */
	public void shutdown() {
		for (RmiServiceExporter exporter : exporters) {
			try {
				exporter.destroy();
			} catch (Exception e) {
				logger.error("Failed to unbind RMI service: {}", exporter, e);
			}
		}
	}

	@Override
	public RmiObjectInfo getRemoteObject(String name) {
		logger.debug("Request received for '{}'", name);
		return exportedObjects.computeIfAbsent(name, this::findAndExport);
	}

	private RmiObjectInfo findAndExport(String name) {
		// Find using local we are about to export something so it should not be remote already
		final Optional<Findable> optionalFindable = Finder.getInstance().findOptional(name);
		if (!optionalFindable.isPresent()) { // there is no object available in the server with this name
			logger.debug("No object with name '{}' found", name);
			return null;
		}
		final Findable object = optionalFindable.get();
		final Class<?> beanClass = object.getClass();
		final ServiceInterface serviceInterface = beanClass.getAnnotation(ServiceInterface.class);
		if (serviceInterface == null) {
			logger.error("Not exporting '{}' as it has no @ServiceInterface annotation", name);
			return null;
		} else {
			boolean eventsSupported = export(name, object, serviceInterface.value());
			return new RmiObjectInfo(name, AUTO_EXPORT_RMI_PREFIX + name, serviceInterface.value().getCanonicalName(),
					eventsSupported);
		}
	}

	@Override
	public Set<String> getRemoteObjectNamesImplementingType(String clazz) {
		try {
			Class<?> type = SPRING_BUNDLE_LOADER.loadClass(clazz);
			if (Findable.class.isAssignableFrom(type)) {
				@SuppressWarnings("unchecked") // It will extend Findable here we just checked
				Class<? extends Findable> findableType = (Class<? extends Findable>) type;
				Map<String, ? extends Findable> findablesOfType = Finder.getInstance().getLocalFindablesOfType(findableType);

				return findablesOfType.entrySet().stream()
						.filter(this::hasServiceInterfaceAnnotation) // Remove objects which can't be exported
						.map(Entry::getKey) // Just get the names
						.collect(Collectors.toSet()); // Put into a set
			} else {
				throw new IllegalArgumentException("clazz must extend Findable was: " + clazz);
			}
		} catch (ClassNotFoundException e) {
			logger.error("'{}' could not be loaded by the server", clazz, e);
			throw new IllegalArgumentException("clazz must be visiable to the server", e);
		}
	}

	/**
	 * Checks the objects class to see if it has a {@link ServiceInterface} annotation.
	 *
	 * @param entry
	 *            {@link Entry} of name to {@link Findable}
	 * @return <code>true</code> if the object has a {@link ServiceInterface} annotation <code>false</code> otherwise
	 */
	private boolean hasServiceInterfaceAnnotation(Entry<String, ? extends Findable> entry) {
		Class<?> type = entry.getValue().getClass();
		final boolean serviceInterfaceAnnotationDeclared = type.isAnnotationPresent(ServiceInterface.class);

		if (!serviceInterfaceAnnotationDeclared) {
			logger.trace("'{}' not exported as '{}' class doesn't have @ServiceInterface annotation", entry.getKey(),
					type.getName());
			return false; // No @ServiceInterface on class
		}
		return true;
	}
}
