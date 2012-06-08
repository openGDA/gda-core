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

package gda.rcp.util;

import gda.scan.ScanDataPointFormatter;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

public final class ScanDataPointFormatterUtils {

	
	/**
	 * 
	 * Reads the extension point so see if the configuration defined has a formatter.
	 * @throws CoreException 
	 * 
	 */
	public static final ScanDataPointFormatter getDefinedFormatter() throws CoreException {
		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor("gda.scan.scan.data.point.formatter");
		if (config != null && config.length > 0) {
				return (ScanDataPointFormatter)config[0].createExecutableExtension("class");
		}
		return null;
	}

}
