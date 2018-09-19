package uk.ac.gda.core;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

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

	public static <T> T getService(Class<T> serviceClass) {
		ServiceReference<T> ref = context.getServiceReference(serviceClass);
		return context.getService(ref);
	}
}
