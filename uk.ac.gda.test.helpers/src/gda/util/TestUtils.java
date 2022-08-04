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

package gda.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;

import org.eclipse.core.runtime.FileLocator;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public class TestUtils {

	/**
	 * Prefix to folder in which test files are to be generated
	 */
	public static final String OUTPUT_FOLDER_PREFIX = "test-scratch/";

	/**
	 * Generates a (relative) directory name based on a class name. Uses the appropriate separators for the platform.
	 *
	 * @param classname
	 *            - the name of the class on which to base the directory name (a value something like
	 *            "gda.analysis.io.JPEGTest").
	 * @return - the derived directory name.
	 */
	public static String generateDirectorynameFromClassname(String classname) {
		// the generated directory name is usable on both Linux and Windows
		// (which uses \ as a separator).
		return OUTPUT_FOLDER_PREFIX + classname.replace('.', '/') + '/';
		// File tmp = new File(OUTPUT_FOLDER_PREFIX + classname.replace('.',
		// '/') + '/');
		// return tmp.getAbsolutePath();
	}

	/**
	 * Helper function to (recursively) delete a directory and all its contents
	 *
	 * @param dir
	 *            - path to directory to delete
	 * @return - boolean True if directory no longer exists
	 */
	static boolean deleteDir(File dir) {
		if (!dir.exists()) {
			return true;
		}
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (String element : children) {
				boolean success = deleteDir(new File(dir, element));
				if (!success) {
					System.out.println("1. deleteDir could not delete: " + new File(dir, element));
					return false;
				}
			}
		}
		if (dir.delete()){
			return true;
		}
		System.out.println("2. deleteDir could not delete: " + dir +". Make sure each test creates a uniquely named folder. Otherwise stale NFS locks may prevent folder deletion.");
		return false;
	}


	/**
	 * Creates a scratch directory for test files created by the specified class.
	 *
	 * @param clazz
	 *            the class
	 * @return the directory
	 */
	public static File createClassScratchDirectory(Class<?> clazz) {
		File dir = new File(generateDirectorynameFromClassname(clazz.getCanonicalName()));
		dir.mkdirs();
		return dir;
	}

	/**
	 * Creates an empty directory for use by test code. If the directory exists (from a previous test), the directory
	 * and all its contents are first deleted.
	 *
	 * @param testScratchDirectoryname
	 *            - the name of the directory to create.
	 * @throws Exception
	 */
	public static void makeScratchDirectory(String testScratchDirectoryname) throws Exception {
		// delete any remains from a previous run of this test
		if (!deleteDir(new File(testScratchDirectoryname))) {
			throw new Exception("Unable to delete old test scratch directory " + testScratchDirectoryname);
		}
		// set up for a new run of this test
		if (!((new File(testScratchDirectoryname)).mkdirs())) {
			throw new Exception("Unable to create new test scratch directory " + testScratchDirectoryname);
		}
	}

	/**
	 * Returns a {@link File} for the specified resource, associated with the
	 * specified class.
	 *
	 * @param clazz the class with which the resource is associated
	 * @param name  the desired resource
	 *
	 * @return a {@link File} for the resource, if it is found
	 *
	 * @throws FileNotFoundException if the resource cannot be found
	 */
	public static File getResourceAsFile(Class<?> clazz, String name) throws FileNotFoundException {
		Bundle bundle = FrameworkUtil.getBundle(clazz);
		// Buckminster test run is outside OSGi
		if (bundle== null) {
			URL url = clazz.getResource(name);
			if (url == null) {
				throw new FileNotFoundException(name + " (resource not found)");
			}
			return new File(url.getFile());
		}
		URL urlToResource = null;
		Enumeration<URL> findEntries = bundle.findEntries("/", name, true);
		while (findEntries != null && findEntries.hasMoreElements()) {
			URL nextElement = findEntries.nextElement();
			String packageAsPath = clazz.getPackage().getName().replace(".", File.separator);
			if (nextElement.getPath().contains(packageAsPath)) {
				urlToResource = nextElement;
			}

		}
		if (urlToResource == null) {
			throw new FileNotFoundException(name + " not found in " + clazz.getPackage().getName());
		}
		try {
			File file = new File(FileLocator.resolve(urlToResource).toURI());
			return file;
		} catch (URISyntaxException | IOException e) {
			throw new FileNotFoundException(e.getMessage());
		}

	}

	/**
	 * Sets up of environment for the a test Set property so that output is to Nexus format file Uses
	 * MockJythonServerFacade and MockJythonServer to configure InterfaceProvider Configure logging so that DEBUG and
	 * above go to log.txt in output folder
	 *
	 * @param testClass
	 *            e.g. gda.data.nexus.ScanToNexusTest
	 * @param nameOfTest
	 *            name of test method which the testClass e.g. testCreateScanFile
	 * @param makedir
	 *            if true the scratch dir is deleted and constructed
	 * @return The directory into which output will be sent
	 * @throws Exception
	 *             if setup fails
	 */
	public static String setUpTest(Class<?> testClass, String nameOfTest, boolean makedir) throws Exception {
		String name = testClass.getCanonicalName();
		if (name == null) {
			throw new IllegalArgumentException("getCanonicalName failed for class " + testClass.toString());
		}
		String testScratchDirectoryName = TestUtils.generateDirectorynameFromClassname(testClass.getCanonicalName())
				+ nameOfTest;

		if (makedir) {
			TestUtils.makeScratchDirectory(testScratchDirectoryName);
		}


		return testScratchDirectoryName;
	}

	/**
	 *
	 * @return String pointing to the top of the hierarchy of large test files used in GDA tests
	 * Set using vmarg e.g. -DGDALargeTestFilesLocation=/scratch/largetestfiles/
	 * Note that it contains the final File.separatorChar so simply concat the result with the filename required
	 * to get the full path.
	 */
	public static String getGDALargeTestFilesLocation(){
		return System.getProperty("GDALargeTestFilesLocation");
	}


}