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
package org.eclipse.scanning.example.file;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;

import org.eclipse.scanning.api.scan.IFilePathService;

public class MockFilePathService implements IFilePathService {

	public static final String MOCK_VISIT_ID = "test-mock";

	private final File dir;
	private String mostRecentPath;
	public MockFilePathService() {
		try {
			dir = Files.createTempDirectory(this.getClass().getSimpleName()).toFile();
			dir.deleteOnExit();
		} catch (IOException e) {
			throw new UncheckedIOException("Could not create temporary directory", e);
		}
	}

	@Override
	public String getNextPath(String template) throws Exception {
		if (template==null) template = "";
		mostRecentPath = getUnique(dir, "Scan-"+template, "nxs").getAbsolutePath();
		return mostRecentPath;
	}

	@Override
	public String getNextPath(String dir, String template) throws Exception {
		if (template==null) template = "";
		mostRecentPath = getUnique(new File(dir), "Scan-"+template, "nxs").getAbsolutePath();
		return mostRecentPath;
	}

	@Override
	public String createFolderForLinkedFiles(String filename) throws Exception {
		String bareFilename = getBareFilename(filename);
		File newDir = new File(dir, bareFilename);
		newDir.mkdir();

		return newDir.toString();
	}

	private String getBareFilename(String filePath) {
		String filename = new File(filePath).getName();
		int dotIndex = filename.indexOf(".");
		if (dotIndex == -1) {
			return filename;
		}
		return filename.substring(0, dotIndex);
	}

	/**
	 * Generates a unique file of the name template or template+an integer
	 *
	 * @param dir
	 * @param template
	 * @param ext
	 *        if null will return a unique directory name
	 * @return a unique file.
	 */
	public static File getUnique(final File dir, final String template, final String ext) {
		String extension = ext != null ? (ext.startsWith(".")) ? ext : "." + ext : null;
		extension = extension != null ? extension : "";
		final File file = new File(dir, template + extension);
		if (!file.exists()) {
			return file;
		}

		return getUnique(dir, template, ext, 1);
	}

	/**
	 * @param dir
	 * @param template
	 * @param ext
	 * @param i
	 * @return file
	 */
	public static File getUnique(final File dir, final String template, final String ext, int i) {
		final String extension = ext == null
				? ""
				: ext.startsWith(".") ? ext : "." + ext;
		File file;
		do {
			file = new File(dir, template + i++ + extension);
		} while (file.exists());

		return file;

	}

	@Override
	public String getMostRecentPath() throws IllegalStateException {
		if (mostRecentPath == null) throw new IllegalStateException("Must call getNextPath() first");
		return mostRecentPath;
	}

	@Override
	public String getVisitDir() {
		return dir.toString();
	}

	@Override
	public String getTempDir() {
		return dir.toString();
	}

	@Override
	public String getProcessedFilesDir() {
		return new File(dir, "processed").toString();
	}

	@Override
	public String getProcessingDir() {
		return new File(dir, "processing").toString();
	}

	@Override
	public String getPersistenceDir() {
		final File persistenceDir = new File(dir, "var");
		persistenceDir.mkdir();
		return persistenceDir.toString();
	}

	@Override
	public String getProcessingTemplatesDir() {
		return new File(getPersistenceDir(), "processingTemplates").toString();
	}

	private static int scanNumber = 0;

	@Override
	public int getScanNumber() throws Exception {
		return scanNumber++;
	}

	@Override
	public String getVisit() throws Exception {
		return MOCK_VISIT_ID;
	}

	@Override
	public String getVisitConfigDir() {
		return new File(dir, "xml").toString();
	}

}
