/*-
 * Copyright © 2016 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.jython;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

/**
 * This class provides Jython API info which is looked up from the Eclipse
 * extension point system. For example the "standard" script project locations.
 */
public final class GDAJythonScriptApi {

	// Extension point element and attribute names
	private static final String JYTHON_VISIBILE_PACKAGES = "uk.ac.diamond.daq.jython.api.visiblePackages";
	private static final String JYTHON_SCRIPT_LOCATIONS = "uk.ac.diamond.daq.jython.api.scriptLocations";
	private static final String PACKAGES = "packages";
	private static final String PACKAGE = "package";
	private static final String LOCATION = "location";
	private static final String FOLDER = "folder";
	private static final String BUNDLE = "bundle";
	private static final String NAME = "name";

	private final Map<String, String> standardScriptFolders;
	private final Set<String> visiblePackages;

	public GDAJythonScriptApi() {
		this.standardScriptFolders = readJythonScriptLocations();
		this.visiblePackages = readJythonVisiblePackageNames();
	}

	/**
	 * Retrieves the relative paths of the script directories available to Jython in all beamlines as sources of Python scripts
	 *
	 * @return {@link Map} storing the script folder locations, partial path -> name
	 */
	public Map<String, String> getStandardFolders() {
		return standardScriptFolders;
	}

	/**
	 * Get set of packages (in the form "bundle-name:package-name") intended to form the Java API which Jython is
	 * allowed to see/use.
	 * <p>
	 * <b>NOTE:</b> this mechanism is not currently used (or historically used) but is left
	 * in case future work allows for this restriction. Currently any package exported from
	 * a plugin will be visible to Jython.
	 */
	public Set<String> getVisibleJythonPackages() {
		return visiblePackages;
	}

	/**
	 * Read the locations of Python script folders from the extension point system.
	 *
	 * @return {@link Map} storing the script folder locations, partial path -> name
	 * @throws IOException
	 */
	private Map<String, String> readJythonScriptLocations() {
		Map<String, String> jythonScriptDirectories = new HashMap<>();
		final IExtensionPoint jythonScriptLocations = Platform.getExtensionRegistry()
				.getExtensionPoint(JYTHON_SCRIPT_LOCATIONS);
		if (jythonScriptLocations != null) {
			for (IExtension extension : jythonScriptLocations.getExtensions()) {
				for (IConfigurationElement element : extension.getConfigurationElements()) {
					if (element.getName().equals(LOCATION)) {
						jythonScriptDirectories.put(element.getAttribute(FOLDER), element.getAttribute(NAME));
					}
				}
			}
		}
		return jythonScriptDirectories;
	}

	/**
	 * Read the visible Java packages (with their bundles) from the extension point system.
	 *
	 * @return {@link Set} containing the valid package/bundle combinations of the form "plugin-name:package-name"
	 */
	private Set<String> readJythonVisiblePackageNames() {
		Set<String> visiblePackagesNames = new HashSet<>();
		final IExtensionPoint jythonVisibility = Platform.getExtensionRegistry()
				.getExtensionPoint(JYTHON_VISIBILE_PACKAGES);
		if (jythonVisibility != null) {
			for (IExtension extension : jythonVisibility.getExtensions()) {
				final String contributorName = extension.getContributor().getName();
				String bundleName = null;
				IConfigurationElement[] pkgElements = new IConfigurationElement[] {};

				// Read optional 'bundle' and mandatory 'packages' elements first
				for (IConfigurationElement element : extension.getConfigurationElements()) {
					if (element.getName().equals(BUNDLE)) {
						bundleName = element.getAttribute(NAME);
					} else if (element.getName().equals(PACKAGES)) {
						pkgElements = element.getChildren();
					}
				}
				// Then process the packages using the bundle or contributor name as appropriate
				for (IConfigurationElement pkgElement : pkgElements) {
					if (pkgElement.getName().equals(PACKAGE)) {
						final String packageName = pkgElement.getAttribute(NAME);
						visiblePackagesNames.add(
								(bundleName != null ? bundleName.trim() : contributorName) + ":" + packageName.trim());
					}
				}
			}
		}
		return visiblePackagesNames;
	}

}
