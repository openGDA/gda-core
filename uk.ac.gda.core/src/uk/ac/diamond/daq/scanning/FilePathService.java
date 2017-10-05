/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.scanning;

import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

import org.eclipse.scanning.api.scan.IFilePathService;

import gda.configuration.properties.LocalProperties;
import gda.data.NumTracker;
import gda.data.PathConstructor;
import gda.data.metadata.GDAMetadataProvider;
import gda.device.DeviceException;

/**
 * Implementation of the {@link IFilePathService} which determines the next path to write to.
 */
public class FilePathService implements IFilePathService {

	private static final String PROCESSING_TEMPLATES_DIR = "processingTemplates";
	private static final String VISIT_TEMP_DIR_NAME = "tmp";
	private static final String VISIT_PROCESSED_DIR_NAME = "processed";
	private static final String VISIT_PROCESSING_DIR_NAME = "processing";
	private static final String VISIT_CONFIG_DIR_NAME = "xml";
	private static NumTracker tracker;

	private String lastPath = null;

	public FilePathService() {
		// Must have constructor that does no work
	}

	@Override
	public synchronized String getNextPath(String template) throws IOException, InvalidPathException {
		// Get the current data directory
		String dir = PathConstructor.createFromDefaultProperty();
		return getNextPath(dir, template);
	}

	@Override
	public synchronized String getNextPath(String dir, String template) throws IOException, InvalidPathException {
		if (template==null) template = "";
		if (tracker == null) {
			// Make a NumTracker using the property gda.data.numtracker.extension
			tracker = new NumTracker();
		}

		// Get the next file number and update the tracker file on disk
		final int fileNumber = tracker.incrementNumber();

		// Build the file name
		// Default to "base" if gda.beamline.name is not set (behaviour copied from NexusDataWriter). Should never happen!
		if (template.length()>0) template = "-"+template;
		final String filename = String.format("%s-%s%s.nxs", LocalProperties.get(LocalProperties.GDA_BEAMLINE_NAME, "base"), fileNumber, template);

		// Return the full file path
		final String path = dir + "/" + filename;

		Paths.get(path); // Check that the path is a path that is valid.

		final File dirFile = new File(dir);
		if (!dirFile.exists()) {
			dirFile.mkdirs();
		}

		lastPath = path;

		return path;
	}

	@Override
	public String createFolderForLinkedFiles(String filename) throws Exception {
		// Get the current data directory
		// TODO This currently doesn't support sub directories under the visit e.g /sample1/
		String parentDir = PathConstructor.createFromDefaultProperty();
		String bareFilename = getBareFilename(filename);
		File newDir = new File(parentDir, bareFilename);
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

	@Override
	public String getMostRecentPath() {
		if (lastPath == null) { // Should always be called after getNextPath() for that scan
			throw new IllegalStateException("No previous path.");
		}

		return lastPath;
	}

	@Override
	public String getTempDir() {
		// Get the current visit directory and append /tmp.
		return PathConstructor.getVisitSubdirectory(VISIT_TEMP_DIR_NAME);
	}

	@Override
	public String getProcessedFilesDir() {
		// Get the current visit directory and append /processed.
		return PathConstructor.getVisitSubdirectory(VISIT_PROCESSED_DIR_NAME);
	}

	@Override
	public String getProcessingDir() {
		return PathConstructor.getVisitSubdirectory(VISIT_PROCESSING_DIR_NAME);
	}

	@Override
	public String getPersistenceDir() {
		return PathConstructor.createFromProperty(LocalProperties.GDA_VAR_DIR);
	}


	@Override
	public String getProcessingTemplatesDir() {
		return getPersistenceDir() + "/" + PROCESSING_TEMPLATES_DIR;
	}

	@Override
	public int getScanNumber() throws Exception {
		if (tracker == null) {
			// Make a NumTracker using the property gda.data.numtracker.extension
			tracker = new NumTracker();
		}
		return tracker.getCurrentFileNumber();
	}

	@Override
	public String getVisit() throws Exception {
		String visit = null;
		try {
			visit = GDAMetadataProvider.getInstance().getMetadataValue("visit");
		} catch (DeviceException ignored) {
			// We do not mind if this does not work!
		}
		if (visit==null) visit = LocalProperties.get(LocalProperties.GDA_DEF_VISIT, "cm0-0");
		return visit;
	}

	@Override
	public String getVisitConfigDir() {
		// Get the current visit directory and append /tmp.
		return PathConstructor.getVisitSubdirectory(VISIT_CONFIG_DIR_NAME);
	}

}
