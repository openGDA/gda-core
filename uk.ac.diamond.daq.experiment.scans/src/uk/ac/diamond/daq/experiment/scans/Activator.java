package uk.ac.diamond.daq.experiment.scans;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private static BundleContext context;

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
	}

	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}
	
	public static <T> T getService(Class<T> serviceClass) {
		return context.getService(context.getServiceReference(serviceClass));
	}

}
