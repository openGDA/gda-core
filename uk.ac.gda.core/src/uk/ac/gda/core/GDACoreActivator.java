package uk.ac.gda.core;

import static java.util.Optional.ofNullable;

import java.util.Optional;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class GDACoreActivator implements BundleActivator {

	private static BundleContext context;

	public static BundleContext getBundleContext() {
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
	 */
	public static <T> Optional<T> getService(Class<T> serviceClass) {
		if (context == null) return Optional.empty();
		return ofNullable(serviceClass)
				.map(context::getServiceReference)
				.map(context::getService);
	}

}
