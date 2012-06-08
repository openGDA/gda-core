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

public class LibsLocationFinder {

	/**
	 * Method returns libs that should be used in the jython path so that the
	 * jython editor autocompletes.
	 * 
	 * @return list of jar paths
	 */
	public static final List<String> findGdaLibs() {
		
		final List<String> libs = new ArrayList<String>(31);
		final File   libsFolder = new File(LocalProperties.getRoot()+"/uk.ac.gda.libs/");
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
		
		// TODO Add more libs so that jython console works.
		
		
		return libs;
	}

	/**
	 * Looks for the gda-script-interface.jar
	 * @return location or null if it does not exist.
	 */
	public static String findGdaInterface() {
		
		String path = System.getProperty("gda.debgug.client.interface");
		if (path!=null) return path;
		
		final File plugins = new File(LocalProperties.getRoot());
		final File gda     = plugins.getParentFile();
		final File client  = new File(gda,    "client");
		final File inter   = new File(client, "gda-script-interface.jar");
		if (inter.exists()) return inter.getAbsolutePath();
		
		return null;
	}
}
