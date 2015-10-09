/*-
 * Copyright © 2009 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package gda.server;

import gda.jython.GDAJythonClassLoader;

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
import org.osgi.framework.Constants;
import org.python.util.jython;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {

	/**
	 * The plug-in ID
	 */
	public static final String PLUGIN_ID = "gda.server";

	// Set to true to make all packages visible
	private static final boolean jythonAllPackagesVisible = false;
	// if not all packages are visible, use one or both of the following methods
	//   use the custom marker on Export Package
	private static final boolean jythonUseExportPackageCustomization = true;
	//   use the visible packages extension point
	private static final boolean jythonUseExtensionPoints = false;

	// these fields are used to read the extension point
	// to get list of packages
	private static final String VISIBLE_JYTHON_PACKAGES = "uk.ac.gda.jython.visible_packages";
	private static final String VISIBLE_PACKAGE = "visible_package";
	private static final String PACKAGE = "package";

	// This constant is used to parse Export-Package directives to see if it should be visible to Jython
	private static final String X_JYTHON = "x-jython";


	// The shared instance
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		// Collect list of packages to make available to Jython by looking through the list
		// of Register Buddys of the Libs plugin.
		// NOTE: this code is here instead of the Libs plugin so that we don't introduce
		// a dependency on OSGI/Eclipse for the normal server, eventually this would be moved
		// into the libs plugin.
		Bundle libsBundle = null;
		Bundle[] bundles = context.getBundles();
		for (int i = 0; i < bundles.length; i++) {
			Bundle bundle = bundles[i];
			Object name = bundle.getHeaders().get(org.osgi.framework.Constants.BUNDLE_SYMBOLICNAME);
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
		Set<String> allPackages = new HashSet<String>();

		State state = Platform.getPlatformAdmin().getState(false);
		BundleDescription libsBundleDescription = state.getBundle(libsBundle.getBundleId());
		processBundle(allPackages, libsBundleDescription);

		BundleDescription[] dependents = libsBundleDescription.getDependents();
		for (int i = 0; i < dependents.length; i++) {
			processBundle(allPackages, dependents[i]);
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
				for (int i = 0; i < dependents.length; i++) {
					processXJythonDirective(visiblePackages, context.getBundle(dependents[i].getBundleId()));
				}
			}

		}

		// Because we are not yet running in gda.libs Activator we can't just do
		//    ClassLoader cl = this.getClass().getClassLoader();
		// Instead we have to create a class via the right classloader
		ClassLoader cl = (new jython()).getClass().getClassLoader();

		GDAJythonClassLoader.initialize(cl, visiblePackages, false);
	}

	private void processXJythonDirective(Set<String> visiblePackages, Bundle bundle)
			throws BundleException {
		String exportPackageString = bundle.getHeaders().get(Constants.EXPORT_PACKAGE);
		ManifestElement[] exportPackages = ManifestElement.parseHeader(Constants.EXPORT_PACKAGE, exportPackageString);
		if (exportPackages != null) {
			for (int j = 0; j < exportPackages.length; j++) {
				ManifestElement manifestElement = exportPackages[j];
				String jythonDirective = manifestElement.getDirective(X_JYTHON);
				if (Boolean.parseBoolean(jythonDirective)) {
					visiblePackages.add(manifestElement.getValue());
				}
			}
		}
	}

	private void processBundle(Set<String> packages, BundleDescription bd) {
		ExportPackageDescription[] exportPackages = bd.getExportPackages();
		for (int j = 0; j < exportPackages.length; j++) {
			String name = exportPackages[j].getName();
			packages.add(name);
		}
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
	public static Activator getDefault() {
		return plugin;
	}

}
