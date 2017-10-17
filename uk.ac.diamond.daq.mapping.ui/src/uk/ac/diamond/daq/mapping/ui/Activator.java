package uk.ac.diamond.daq.mapping.ui;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "uk.ac.diamond.daq.mapping.ui";

	// The shared instance
	private static Activator plugin = null;

	/**
	 * The constructor
	 */
	public Activator() {
		// do nothing
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static Activator getDefault() {
		return plugin;
	}

	public static <T> T getService(Class<T> serviceClass) {
		final BundleContext context = plugin.getBundle().getBundleContext();
		ServiceReference<T> ref = context.getServiceReference(serviceClass);
		return context.getService(ref);
	}

}