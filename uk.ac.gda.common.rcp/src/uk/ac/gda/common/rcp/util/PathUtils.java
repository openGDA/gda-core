/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.common.rcp.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import uk.ac.diamond.daq.util.logging.deprecation.DeprecationLogger;
import uk.ac.gda.api.io.IPathConstructor;


@Deprecated(since="GDA 9.16") // See DAQ-563
public class PathUtils {

	private PathUtils() {

	}

	private static DeprecationLogger logger = DeprecationLogger.getLogger(PathUtils.class);

	/**
	 * Reads extension point and uses an IPathConstructor to call the createFromDefaultProperty() method.
	 *
	 * @return createFromDefaultProperty in PathConstructor or null
	 */
	public static String createFromDefaultProperty() {
		logger.deprecatedMethod("createFromDefaultProperty()");
		try {
		    final IPathConstructor constructor = getPathConstructor();
		    return constructor.getDefaultDataDir();

		} catch (Exception ne) {
			logger.error("Cannot get IPathConstructor", ne);
			return null;
		}
	}


	public static String createFromTemplate(String pattern) {
		logger.deprecatedMethod("createFromTemplate(String)");
		try {
		    final IPathConstructor constructor = getPathConstructor();
		    return constructor.getFromTemplate(pattern);

		} catch (Exception ne) {
			logger.error("Cannot get IPathConstructor", ne);
			return null;
		}
	}


	private static IPathConstructor pathConstructor;


	/**
	 * Returns IPathConstructor or null
	 * @return IPathConstructor
	 * @throws CoreException
	 */
	private static IPathConstructor getPathConstructor() throws CoreException{

		if (pathConstructor!=null) return pathConstructor;

		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor("uk.ac.gda.common.path.constructor");
		pathConstructor = (IPathConstructor)config[0].createExecutableExtension("class");

		return pathConstructor;
	}

}
