package uk.ac.diamond.daq.persistence.manager.serializer;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class Activator extends Plugin {

	private static Activator instance;

	@Override
	public void start(BundleContext context) throws Exception {
		instance = this;
		super.start(context);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		instance = null;
		super.stop(context);
	}

	static <T> T getService(Class<T> serviceClass) {
		BundleContext bundleContext = instance.getBundle().getBundleContext();
		return bundleContext.getService(bundleContext.getServiceReference(serviceClass));
	}
}
