/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.test;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;

import org.eclipse.scanning.api.event.EventConstants;

public class ScanningTestUtils {

	private ScanningTestUtils() {
		// prevent instantiation
	}

	public static final String JVM_UNIQUE_ID = ManagementFactory.getRuntimeMXBean().getName();
	public static final String SUBMISSION_QUEUE_WITH_ID = EventConstants.SUBMISSION_QUEUE.concat(JVM_UNIQUE_ID);

	public static void clearTmp() {
		if ("true".equals(System.getenv().get("CLEAR_TMP"))) {
			System.out.println("Clearing /tmp of old nexus files.");
			final File tmp = new File("/tmp/"); // Designed to ensure that old test files are removed on travis docker nodes.
			if (!tmp.exists() || !tmp.isDirectory()) return;
			recursiveDelete(tmp, "nxs", "h5");
		}
	}

	/**
	 * After tests using MVStore (which create a new MVStore per JVM), clear up the created MVStore,
	 * and any others from this JVM that might remain.
	 */
	public static void clearMVStore() {
		System.out.println("Clearing /tmp/var/ of old MVStore database files.");
		final File var = new File("/tmp/var/"); // Designed to ensure that old test files are removed on travis docker nodes.
		if (!var.exists() || !var.isDirectory()) return;
		File[] files = var.listFiles();
		for (int ifile = 0; ifile < files.length; ++ifile) {
			if (getFileExtension(files[ifile]).equals("db") && files[ifile].getName().contains(JVM_UNIQUE_ID)) {
				files[ifile].delete();
			}
		}
	}

	private static boolean recursiveDelete(File parent, String... ext) {
		List<String> extensions = Arrays.asList(ext);
		if (parent.exists()) {
			if (parent.isDirectory()) {

				File[] files = parent.listFiles();
				for (int ifile = 0; ifile < files.length; ++ifile) {
					if (files[ifile].isDirectory()) {
						recursiveDelete(files[ifile], ext);
					}
					if (extensionMatches(files[ifile], extensions)) {
						files[ifile].delete();
					}
				}
			}
		}
		return true;
	}

	private static boolean extensionMatches(File file, List<String> extensions) {
		return file.isDirectory() || extensions.contains(getFileExtension(file));
	}

	private static String getFileExtension(File file) {
		final String fileName = file.getName();
		int posExt = fileName.lastIndexOf(".");
		// No File Extension
		return posExt == -1 ? "" : fileName.substring(posExt + 1);
	}

}
