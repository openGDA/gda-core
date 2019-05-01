package uk.ac.diamond.daq.server;

import java.util.Optional;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
*/
public class GDAServerActivator extends Plugin {

	public static final String PLUGIN_ID = "uk.ac.diamond.daq.server";

	// The shared instance
	private static GDAServerActivator plugin;
	private static BundleContext context;

	public GDAServerActivator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		GDAServerActivator.context = context;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
		GDAServerActivator.context = null;
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static GDAServerActivator getDefault() {
		return plugin;
	}

	public BundleContext getBundleContext() {
		return context;
	}

	public static <T> Optional<T> getService(Class<T> serviceClass) {
		if (context == null) return Optional.empty();
		return Optional.of(serviceClass)
				.map(context::getServiceReference)
				.map(context::getService);
	}
}
