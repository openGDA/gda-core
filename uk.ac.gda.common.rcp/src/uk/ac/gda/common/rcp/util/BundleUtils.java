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

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

/**
 *   BundleUtils
 *   
 *   Assumes that this class can be used before the Logger is loaded, therefore do not put Logging in here.
 *
 *   @author gerring
 *   @date Aug 2, 2010
 *   @project org.edna.common.util
 **/
public class BundleUtils {
	
	
	public static File getBundleLocation(final String bundleName) throws IOException {
		final Bundle bundle = Platform.getBundle(bundleName);
		return BundleUtils.getBundleLocation(bundle);
	}
	
	/**
	 * Get the java.io.File location of a bundle.
	 * @param bundle
	 * @return file
	 * @throws IOException 
	 */
	public static File getBundleLocation(final Bundle bundle) throws IOException {
		
		String  dirPath = BundleUtils.cleanPath(bundle.getLocation());      
        final File dir = new File(dirPath);
        if (dir.exists()) return dir; 

        // Just in case...
        final String eclipseDir = BundleUtils.cleanPath(System.getProperty("eclipse.home.location"));
        final File   bundDir    = new File(eclipseDir+"/"+dirPath);
        if (bundDir.exists()) return bundDir;
        
        final File   plugins = new File(eclipseDir+"/plugins/");
        if (plugins.exists()) {
	        final File[] fa = plugins.listFiles();
	        for (int i = 0; i < fa.length; i++) {
				final File file = fa[i];
				if (file.getName().equals(bundle.getSymbolicName())) return file;
				if (file.getName().startsWith(bundle.getSymbolicName()+"_")) return file;
			}
        }
        return FileLocator.getBundleFile(bundle);
	}

	private static String cleanPath(String loc) {
		
		// Remove reference:file: from the start. TODO find a better way,
	    // and test that this works on windows (it might have ///)
        if (loc.startsWith("reference:file:")){
        	loc = loc.substring(15);
        } else if (loc.startsWith("file:")) {
        	loc = loc.substring(5);
        } else {
        	return loc;
        }
        
        loc = loc.replace("//", "/");
        loc = loc.replace("\\\\", "\\");

        return loc;
	}

	/**
	 * Get the bundle path using eclipse.home.location not loading the bundle.
	 * @param bundleName
	 * @return file
	 */
	public static File getBundlePathNoLoading(String bundleName) {
		
		String home = System.getProperty("eclipse.home.location");
		if (home.startsWith("file:")) home = home.substring("file:".length());
		
		final String path;
		if (home.endsWith("/plugins/")) {
			path = ((new File(home))).getParentFile().getParentFile().getAbsolutePath();
		} else{
			path = home;
		}
		return new File(path+"/plugins/"+bundleName);
	}
}
