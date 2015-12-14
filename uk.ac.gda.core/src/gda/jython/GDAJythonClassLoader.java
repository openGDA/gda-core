/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
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

import java.util.Set;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;


/**
 * A classloader for use with Jython in the GDA environment.
 * It is hoped that this class would be moved into a libs.jython
 * project as the use of statics here is to break the dependency
 * on eclipse/osgi of the gda core until it is ready.
 */
public class GDAJythonClassLoader extends ClassLoader {

	private static ClassLoader classLoader;
	private static Set<String> packages;
	private static boolean allowAllPackages = false;
	private static boolean staticInitialized = false;



	/**
	 * Use the return value of this function to get all the packages available
	 * to jython. Call PySystemState.add_package on each one
	 * @return Returns the packages.
	 */
	public Set<String> getJythonPackages() {
		return packages;
	}

	/**
	 * Use this function to check if the GDA Class Loader should be used.
	 *
	 * @return Returns initialized
	 */
	public static boolean useGDAClassLoader() {
		return staticInitialized;
	}

	/**
	 * Statically initialize the Classloader. This is necessary to help break the dependency
	 * from gda.core/gda.libs on eclipse/osgi
	 * @param classLoader The classloader of the bundle containing jython.jar
	 * @param packages the set of packages which is allowed to be accessed (must be non-null)
	 * @param allowAllPackages set to true if the classloader shouldn't restrict which classes can be loaded
	 */
	public static void initialize(ClassLoader classLoader, Set<String> packages, boolean allowAllPackages) {
		GDAJythonClassLoader.classLoader = classLoader;
		GDAJythonClassLoader.packages = packages;
		GDAJythonClassLoader.allowAllPackages = allowAllPackages;
		staticInitialized = true;
	}

	/**
	 * Construct a new GDA Classloader with the access restrictions to the packages as initialized
	 */
	public GDAJythonClassLoader() {
		super(classLoader);
		if (!useGDAClassLoader())
			throw new RuntimeException("The GDAJythonClassLoader was not initialized before construction");
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		if (name == null || name.length() == 0)
			throw new ClassNotFoundException(name);

		// always allow core java packages and python specific ones
		if (allowAllPackages || name.startsWith("java.") || name.startsWith("org.python.")) {

			// TODO: Fix this temporary bodge to support scisoft python classloading
			Class<?> class1 = null;
			try {
				class1 = super.loadClass(name);
			} catch (Exception e) {
				Bundle bundle = Platform.getBundle("uk.ac.diamond.scisoft.python");
				class1 = bundle.loadClass(name);
			}
			return class1;
		}

		int lastDot = name.lastIndexOf('.');
		String packageName;
		if (lastDot - 1 < 0)
			packageName = ".";
		else
			packageName = name.substring(0, lastDot);

		if (packages.contains(packageName))
			return super.loadClass(name);

		// The requested class is not visible to the GDA Jython Class Loader
		throw new ClassNotFoundException(name);
	}

}
