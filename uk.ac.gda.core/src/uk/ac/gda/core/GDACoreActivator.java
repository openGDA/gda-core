package uk.ac.gda.core;

import static java.util.Optional.ofNullable;

import java.util.Hashtable;
import java.util.Optional;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import uk.ac.diamond.daq.util.logging.deprecation.DeprecationLogger;

/**
 * @deprecated This activator was only used for OSGi service or BundleContext
 * related things. Services can now be obtained from ServiceProvider and the
 * BundleContext can easily be obtained from FrameworkUtil.
 */
@Deprecated(forRemoval = true, since = "GDA 9.34")
public class GDACoreActivator implements BundleActivator {

	private static final DeprecationLogger logger = DeprecationLogger.getLogger(GDACoreActivator.class);

	private static BundleContext context;

	/**
	 * @deprecated see logger message for replacement
	 */
	@Deprecated(forRemoval = true, since = "GDA 9.34")
	public static BundleContext getBundleContext() {
		logger.deprecatedMethod("getBundleContext", "GDA 9.36", "FrameworkUtil#getBundle#getBundleContext");
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext bundleContext) throws Exception {
		GDACoreActivator.context = bundleContext;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		GDACoreActivator.context = null;
	}

	/**
	 * Gets a OSGi service from the context. If no context is set (i.e. if running outside an OSGi framework like JUnit
	 * tests) or if the service is not available an empty {@link Optional} will be returned.
	 *
	 * @param <T> The service interface
	 * @param serviceClass
	 *            the service class
	 * @return {@link Optional} containing the service implementation if found or {@link Optional#empty()} if not
	 * @deprecated see logger message for replacement
	 */
	@Deprecated(forRemoval = true, since = "GDA 9.34")
	public static <T> Optional<T> getService(Class<T> serviceClass) {
		logger.deprecatedMethod("getService(Class<T> serviceClass)", "GDA 9.36", "ServiceProvider");
		if (context == null)
			return Optional.empty();
		return ofNullable(serviceClass)
				.map(context::getServiceReference)
				.map(context::getService);
	}

	/**
	 * Register service with OSGi if possible
	 *
	 * @return service registration object or null if not running in OSGi/Platform
	 * @deprecated see logger message for replacement
	 */
	@Deprecated(forRemoval = true, since = "GDA 9.34")
	public static <T> ServiceRegistration<T> registerService(Class<T> clazz, T instance) {
		logger.deprecatedMethod("registerService(Class<T> clazz, T instance)", "GDA 9.36",
				"Register service directly with a bundle context");
		if (context != null) {
			return context.registerService(clazz, instance, new Hashtable<>());
		}
		return null;
	}


}
