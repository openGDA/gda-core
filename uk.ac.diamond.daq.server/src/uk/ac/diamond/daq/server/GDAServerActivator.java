package uk.ac.diamond.daq.server;

import gda.jython.GDAJythonClassLoader;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.ExportPackageDescription;
import org.eclipse.osgi.service.resolver.State;
import org.eclipse.osgi.util.ManifestElement;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import static org.osgi.framework.Constants.*;

import org.osgi.framework.wiring.BundleWiring;
import org.osgi.framework.wiring.FrameworkWiring;

import com.google.common.collect.Lists;

import uk.ac.diamond.daq.server.configuration.ConfigurationLoader;
import uk.ac.diamond.daq.server.configuration.IGDAConfigurationService;

/**
 * The activator class controls the plug-in life cycle
 * 
 * This class is a slightly modified copy of gda.server.Activator from the uk.ac.gda.server plugin; that class aims to 
 * set up the Jython class loading but looks to be incomplete. So in order to not interfere with any usages of that class
 * that may exist I've produced my own version to get things up and running. I expect the original class to be retired 
 * once I'm more sure of whether anything is infact using it. Keith. 
 */
public class GDAServerActivator extends Plugin {

	/**
	 * The plug-in ID
	 */
	public static final String PLUGIN_ID = "uk.ac.diamond.daq.server";

	// Set to true to make all packages visible
	private static final boolean jythonAllPackagesVisible = true;
	// if not all packages are visible, use one or both of the following methods
	//   use the custom marker on Export Package
	private static final boolean jythonUseExportPackageCustomization = true;
	//   use the visible packages extension point
	private static final boolean jythonUseExtensionPoints = false;

	// these fields are used to read the extension point
	// to get list of packages, this plugin is the only one which references it at the moment (12/10/15)
	private static final String VISIBLE_JYTHON_PACKAGES = "uk.ac.diamond.daq.jython.visible_packages";
	private static final String VISIBLE_PACKAGE = "visible_package";
	private static final String PACKAGE = "package";

	// This constant is used to parse Export-Package directives to see if it should be visible to Jython
	private static final String X_JYTHON = "x-jython";


	// The shared instance
	private static GDAServerActivator plugin;

	/**
	 * The constructor
	 */
	public GDAServerActivator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		// Collect list of packages to make available to Jython by looking through the list
		// of Register Buddys of the Libs plugin. From e4 onwards Platform.getPlatformAdmin().getState
		// is deprecated and doesn't retrieve the dependent correctly, hence the need for 
		// FrameworkWiring.getDependencyClosure()
		Bundle libsBundle = null;
		Bundle[] bundles = context.getBundles();
		for (int i = 0; libsBundle == null && i < bundles.length; i++) {
			Bundle bundle = bundles[i];
			Object name = bundle.getHeaders().get(BUNDLE_SYMBOLICNAME).split(";")[0];
			if ("uk.ac.gda.libs".equals(name)) {
				// we have found the bundle we want
				libsBundle = bundle;
			}
		}

		if (libsBundle == null)
			throw new BundleException("Expected to find the uk.ac.gda.libs plugin, but didn't");

		// Now we have a valid libsBundle. Of course when the code get moved into the libs,
		// all that will be needed to get the libs bundle is:
		//   libsBundle = context.getBundle()


		Set<String> visiblePackages;
		Set<String> allPackages = new HashSet<>();
		processBundle(allPackages, libsBundle);
		FrameworkWiring fwkWiring = context.getBundle(SYSTEM_BUNDLE_ID).adapt(FrameworkWiring.class);
		Collection<Bundle> depsBundles = fwkWiring.getDependencyClosure(Lists.newArrayList(libsBundle));

		for (Bundle dep : depsBundles) {
			processBundle(allPackages, dep);
		}

		if (jythonAllPackagesVisible) {
			visiblePackages = allPackages;
		} else {
			visiblePackages = new HashSet<String>();
			if (jythonUseExtensionPoints) {
				IExtension[] extensions = Platform. getExtensionRegistry().getExtensionPoint(VISIBLE_JYTHON_PACKAGES).getExtensions();
				for(int i=0; i<extensions.length; i++) {
					IExtension extension = extensions[i];
					IConfigurationElement[] configElements = extension.getConfigurationElements();
					for(int j=0; j<configElements.length; j++) {
						IConfigurationElement configElement = configElements[j];
						if (configElement.getName().equals(VISIBLE_PACKAGE)) {
							String pack = configElement.getAttribute(PACKAGE);
							if (allPackages.contains(pack)) {
								visiblePackages.add(pack);
							} else {
								throw new Exception("Package '" + pack + "' is listed as a Jython Visible Package, " +
										"but the package is not exported in any dependent plug-in of gda.libs");
							}
						}
					}
				}
			} else if (jythonUseExportPackageCustomization) {
				processXJythonDirective(visiblePackages, libsBundle);
				for (Bundle dep : depsBundles) {
					processXJythonDirective(visiblePackages, context.getBundle(dep.getBundleId()));
				}
			}

		}

		// Because we are not yet running in gda.libs Activator we can't just do
		//    ClassLoader cl = this.getClass().getClassLoader();
		// Instead we have to create a class via the right classloader
		final Bundle bundle = Platform.getBundle("uk.ac.gda.libs");
		ClassLoader cl = bundle.adapt(BundleWiring.class).getClassLoader();

		GDAJythonClassLoader.initialize(cl, visiblePackages, true);
		injectBeamlineConfigurationService();
	}

	private void processXJythonDirective(Set<String> visiblePackages, Bundle bundle)
			throws BundleException {
		ManifestElement[] exportPackages = getExportedPackages(bundle);
		if (exportPackages != null) {
			for (ManifestElement manifestElement : exportPackages) {
				String jythonDirective = manifestElement.getDirective(X_JYTHON);
				if (Boolean.parseBoolean(jythonDirective)) {
					visiblePackages.add(manifestElement.getValue());
				}
			}
		}
	}
	
	private void processBundle(Set<String> packages, Bundle bundle) throws BundleException {
		ManifestElement[] exportPackages = getExportedPackages(bundle);
		if (exportPackages != null) {
			for (ManifestElement pkg : exportPackages) {
				packages.add(pkg.getValue());
			}
		}
	}
	
	private ManifestElement[] getExportedPackages(final Bundle bundle) throws BundleException {
		String exportPackageString = bundle.getHeaders().get(EXPORT_PACKAGE);
		return ManifestElement.parseHeader(EXPORT_PACKAGE, exportPackageString);
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
	 * @throws Exception	if the ConfigurationLoader component has not started or the required configuration
	 * 						service cannot be retrieved.
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
}
