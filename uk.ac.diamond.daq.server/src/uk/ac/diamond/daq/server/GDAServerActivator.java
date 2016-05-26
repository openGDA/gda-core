package uk.ac.diamond.daq.server;

import gda.jython.GDAJythonClassLoader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

import uk.ac.diamond.daq.server.configuration.ConfigurationLoader;
import uk.ac.diamond.daq.server.configuration.IGDAConfigurationService;

/**
 * The activator class controls the plug-in life cycle
*/
public class GDAServerActivator extends Plugin {

	public static final String PLUGIN_ID = "uk.ac.diamond.daq.server";

	// Extension point element and attribute names
	private static final String JYTHON_VISIBILITY = "uk.ac.diamond.daq.jython.api.jythonVisibility";
	private static final String JYTHON_INCLUDED_PACKAGE = "included_java_package";
	private static final String PACKAGE = "package";
	private static final String JYTHON_SOURCE_LOCATION = "script_source_location";
	private static final String FOLDER= "folder";
	private static final String BUNDLE= "bundle";

	// The shared instance
	private static GDAServerActivator plugin;

	public GDAServerActivator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		injectBeamlineConfigurationService();
		initializeJythonVisibility(context);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static GDAServerActivator getDefault() {
		return plugin;
	}

	/**
	 * Retrieves the correct configuration service for the beamline via the {@link ConfigurationLoader} component
	 * and then uses it to load the configuration for the beamline.
	 * 
	 * @throws Exception    If the ConfigurationLoader component has not started or the required configuration
	 *                      service cannot be retrieved.
	 */
	private void injectBeamlineConfigurationService() throws Exception {
		if (ConfigurationLoader.getInstance() == null) {
			throw new Exception("The ConfigurationLoader component was not activated");
		}
		IGDAConfigurationService configurationService = ConfigurationLoader.getInstance().getConfigurationService();
		if (configurationService == null){
			throw new Exception("The Configuration Service could not be retrieved");
		}
		GDAServerApplication.setConfigurationService(configurationService);
	}

	/**
	 * Read the {@link Set}s of script source folders and included java packages (with their bundles) from the
	 * extension point system. These are then used, along with the full list of bundles in the context
	 * to statically initialise the {@link GDAJythonClassLoader} class ready for later instantiation.
	 * 
	 * @param context       The active OSGi bundle context
	 * @throws Exception    if the {@link GDAJythonClassLoader} fails to initialise successfully.
	 */
	private void initializeJythonVisibility(final BundleContext context) throws Exception {
		final Set<String> jythonSourceFolderNames = new HashSet<>();
		final Set<String> jythonIncludedPackagesNames = new HashSet<String>();

		final IExtensionPoint jythonVisibility = Platform. getExtensionRegistry().getExtensionPoint(JYTHON_VISIBILITY);
		if (jythonVisibility != null) {
			final IExtension[] extensions = jythonVisibility.getExtensions();
			for(IExtension extension : extensions) {
				final String contributorName = extension.getContributor().getName();
				for(IConfigurationElement configElement : extension.getConfigurationElements()) {
					if (configElement.getName().equals(JYTHON_INCLUDED_PACKAGE)) {
						final String packageName = configElement.getAttribute(PACKAGE);
						final String bundleName = configElement.getAttribute(BUNDLE);
						jythonIncludedPackagesNames.add((bundleName != null ? bundleName.trim() : contributorName) + ":" + packageName.trim());
					} else if (configElement.getName().equals(JYTHON_SOURCE_LOCATION)) {
						jythonSourceFolderNames.add(configElement.getAttribute(FOLDER));
					}
				}
			}
		}

		GDAJythonClassLoader.initialize(context.getBundles(), jythonSourceFolderNames, jythonIncludedPackagesNames);
	}
}
