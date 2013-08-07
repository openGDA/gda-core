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

package uk.ac.gda.pydev;

import gda.configuration.properties.LocalProperties;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.common.rcp.util.BundleUtils;

public class LibsLocationFinder {
	private static final Logger logger = LoggerFactory.getLogger(LibsLocationFinder.class);

	/**
	 * Method returns libs that should be used in the jython path so that the
	 * jython editor autocompletes.
	 * 
	 * @return list of jar paths
	 */
	public static final List<String> findGdaLibs() {
		
		final List<String> libs = new ArrayList<String>(31);
		try{
			String gdalibsPath = BundleUtils.getBundleLocation("uk.ac.gda.libs").getAbsolutePath();
			final File   libsFolder = new File(gdalibsPath);
			if (libsFolder.exists()&&libsFolder.isDirectory()) {
				final File[] fa = libsFolder.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.toLowerCase().endsWith(".jar");
					}
				});
				for (int i = 0; i < fa.length; i++) {
					libs.add(fa[i].getAbsolutePath());
				}
			}
		}
		catch(Exception e){
			logger.error("Error building list of jars in uk.ac.gda.libs",e);
		}
		return libs;
	}

	/**
	 * Looks for a jar file named gda-script-interface.jar stored in the client product installation directory.
	 * <p>
	 * If created, this jar file holds a subset of GDA java classes to be made available to PyDev.
	 * <p>
	 * This is a way of making classes available to PyDev without exposing PyDev to the entire GDA codebase which caused
	 * memory problems for PyDev.
	 * 
	 * @return location or null if it does not exist.
	 */
	public static String findGdaInterface() {

		String path = System.getProperty("gda.debug.client.interface");
		if (path != null)
			return path;

		String loc = LocalProperties.getInstallationWorkspaceDir();
		if( loc != null && loc.length()>0){
			final File plugins = new File(loc);
			final File client = new File(plugins, "client");
			final File inter = new File(client, "gda-script-interface.jar");
			if (inter.exists())
				return inter.getAbsolutePath();
		}

		return null;
	}
}
