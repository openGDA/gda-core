package uk.ac.diamond.daq.server;

import static org.osgi.framework.Constants.EXPORT_PACKAGE;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.ManifestElement;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

import com.google.common.collect.ImmutableMap;

import gda.jython.GDAJythonClassLoader;
import uk.ac.diamond.daq.server.configuration.ConfigurationLoader;
import uk.ac.diamond.daq.server.configuration.IGDAConfigurationService;

public class ApplicationEnvironment {

	// Extension point element and attribute names
	private static final String JYTHON_VISIBILE_PACKAGES = "uk.ac.diamond.daq.jython.api.visiblePackages";
	private static final String JYTHON_SCRIPT_LOCATIONS = "uk.ac.diamond.daq.jython.api.scriptLocations";
	private static final String PACKAGES = "packages";
	private static final String PACKAGE = "package";
	private static final String LOCATION = "location";
	private static final String FOLDER= "folder";
	private static final String BUNDLE= "bundle";
	private static final String NAME= "name";

	private static final Map<String, Map<Bundle, Boolean>> jythonPackageMap = new HashMap<>();

	private static boolean initialzed = false;

	/**
	 * Initialises the configuration loader, then reads the Jython API extension points to build the supported
	 * API package list and retrieve the standard source folders. Once this done, the results are use to build
	 * the Jython package lookup which is used to initialise the corresponding ClassLoader. This approach may
	 * be extended to Spring classloading in due course.
	 *
	 * @throws Exception
	 */
	public static void initialize() throws Exception {
		if (!initialzed) {
			injectBeamlineConfigurationService();

			final Bundle[] bundles = GDAServerActivator.getDefault().getBundleContext().getBundles();
			final Set<String> jythonSourceFolderNames = new HashSet<>();
			final Set<String> jythonIncludedPackagesNames = new HashSet<>();

			readJythonScriptLocations(jythonSourceFolderNames);
			readJythonVisiblePackageNames(jythonIncludedPackagesNames);
			initializePackageLookups(bundles, jythonIncludedPackagesNames);
			GDAJythonClassLoader.initialize(bundles, jythonSourceFolderNames, jythonPackageMap);
			initialzed = true;
		}
	}

	/**
	 * Clear the existing lookup table(s)
	 */
	public static void release() {
		jythonPackageMap.clear();
	}

	/**
	 * Retrieves the correct configuration service for the beamline via the {@link ConfigurationLoader} component
	 * and then uses it to load the configuration for the beamline.
	 *
	 * @throws Exception    If the {@link ConfigurationLoader} component has not started or the required configuration
	 *                      service cannot be retrieved.
	 */
	private static void injectBeamlineConfigurationService() throws Exception {
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
	 * Read and populate the {@link Set} of Java packages (with their bundles) from the
	 * extension point system.
	 *
	 * @param visiblePackagesNames  The {@link Set} in which to store the valid package/bundle combinations
	 */
	private static void readJythonVisiblePackageNames(final Set<String> visiblePackagesNames) {
		final IExtensionPoint jythonVisibility = Platform.getExtensionRegistry().getExtensionPoint(JYTHON_VISIBILE_PACKAGES);
		if (jythonVisibility != null) {
			for(IExtension extension : jythonVisibility.getExtensions()) {
				final String contributorName = extension.getContributor().getName();
				String bundleName = null;
				IConfigurationElement[] pkgElements = new IConfigurationElement[]{};

				// Read optional 'bundle' and mandatory 'packages' elements first
				for(IConfigurationElement element : extension.getConfigurationElements()) {
					if (element.getName().equals(BUNDLE)) {
						bundleName = element.getAttribute(NAME);
					} else if (element.getName().equals(PACKAGES)) {
						pkgElements = element.getChildren();
					}
				}
				// Then process the packages using the bundle or contributor name as appropriate
				for(IConfigurationElement pkgElement : pkgElements) {
					if (pkgElement.getName().equals(PACKAGE)) {
						final String packageName = pkgElement.getAttribute(NAME);
						visiblePackagesNames.add((bundleName != null ? bundleName.trim() : contributorName) + ":" + packageName.trim());
					}
				}
			}
		}
	}

	/**
	 * Read and populate the {@link Set} of Python script folders from the extension point system.
	 *
	 * @param jythonScriptFolderNames    The {@link Set} in which to store the script folder lications
	 * @throws IOException
	 */
	private static void readJythonScriptLocations(final Set<String> jythonScriptFolderNames) throws IOException {
		final IExtensionPoint jythonScriptLocations = Platform. getExtensionRegistry().getExtensionPoint(JYTHON_SCRIPT_LOCATIONS);
		if (jythonScriptLocations != null) {
			for(IExtension extension : jythonScriptLocations.getExtensions()) {
				for(IConfigurationElement element : extension.getConfigurationElements()) {
					if (element.getName().equals(LOCATION)) {
						jythonScriptFolderNames.add(element.getAttribute(FOLDER));
					}
				}
			}
		}
	}

	/**
	 * Populate the package lookup table for Jython using the set of API packages and the list of context bundles.
	 *
	 * @param bundles                   The full set of bundles available in the OSGi context
	 * @param includedPackagesNames     The {@link Set} of packages that constitute the supported Jython API
	 * @throws BundleException          If the exported packages header of a bundle manifest cannot be parsed
	 */
	private static void initializePackageLookups(final Bundle[] bundles, final Set<String> includedPackagesNames) throws BundleException {
		for (Bundle bundle : bundles) {
			final ManifestElement[] exportPackages = ManifestElement.parseHeader(EXPORT_PACKAGE, bundle.getHeaders().get(EXPORT_PACKAGE));
			if (exportPackages != null) {
				for (ManifestElement element : exportPackages) {
					final String pkgName = element.getValue();
					final boolean included = includedPackagesNames.contains(bundle.getSymbolicName() + ":" + pkgName);
					if (jythonPackageMap.containsKey(pkgName)) {
						jythonPackageMap.get(pkgName).put(bundle, included);
					} else {
						jythonPackageMap.put(pkgName, new HashMap<>(ImmutableMap.of(bundle, included)));
					}
				}
			}
		}
	}
}
