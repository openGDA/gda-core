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
package org.eclipse.scanning.api.scan;

/**
 * This service provides useful file paths for writing to. It has method to get the
 * path of the next scan file.
 *
 * @author Matthew Gerring
 *
 */
public interface IFilePathService {

	/**
	 * Determine and return the next file path to write to. The file will be in the default output directory.
	 * @param template for instance the sample name, may be null or empty string if no template is required.
	 * @return next file path
	 * @throws Exception if the next path cannot be calculated for any reason
	 */
	String getNextPath(String template) throws Exception;

	/**
	 * Determine and return the next file path to write to within the given output directory.
	 * @param dir directory to create the file in
	 * @param template for instance the sample name, may be null or empty string if no template is required
	 * @return next file path
	 * @throws Exception if the next path cannot be calculated for any reason
	 */
	String getNextPath(String dir, String template) throws Exception;

	/**
	 *
	 * @return the current scan number. Same as NumTracker.getCurrentFileNumber() in GDA8.
	 * @throws Exception
	 */
	int getScanNumber() throws Exception;


	/**
	 *
	 * @return the current visit number LocalProperties.get(LocalProperties.GDA_DEF_VISIT, LocalProperties.DEFAULT_VISIT)
	 * @throws Exception
	 */
	String getVisit() throws Exception;

	/**
	 * Creates and returns the path of a new folder in the same parent folder as file path with
	 * the same name as the given file, minus the file extension. This folder can be used to
	 * write linked files to for the given scan. If the filename given is a filepath, only the
	 * last segment will be used.
	 * @param filename the name of the file
	 * @return path of new folder
	 * @throws Exception if the folder cannot be created for any reason
	 */
	String createFolderForLinkedFiles(String filename) throws Exception;

	/**
	 * Returns the name of the path returned by the most recent call to
	 * {@link #getNextPath()}. Note that this is not guaranteed to be the file for the current
	 * scan.
	 * @return last file path
	 * @throws IllegalStateException if {@link #getNextPath()} has never been called
	 */
	String getMostRecentPath() throws IllegalStateException;

	/**
	 * Returns the full path to the visit directory
	 *
	 * @return full path to the visit directory
	 */
	String getVisitDir();

	/**
	 * Returns the location of the directory to use for temporary files for the current visit.
	 * In GDA this is mapped to the {@code tmp} subdirectory of the current visit directory, the
	 * contents of which are deleted every 2 days.
	 *
	 * @return location of temp directory for the current visit
	 */
	String getTempDir();

	/**
	 * Returns the location of the directory to use for files such configuration files of
	 * any type, saved scan files, etc. In GDA this is mapped to the {@code xml} subdirectory
	 * of the current visit directory, the contents of which live for the lifetime of the visit.
	 *
	 * @return location of the configuration directory for the current visit
	 */
	String getVisitConfigDir();

	/**
	 * Returns the location of the directory in which to place processed files.
	 * TODO: remove this method and just use a subpath of /tmp
	 * @return processed files
	 */
	String getProcessedFilesDir();

	/**
	 * Returns the location of the directory where processing file live.
	 * @return location of directory containing processing files
	 */
	String getProcessingDir();

	/**
	 * Returns the location of persistence directory. This is the location of a global read/write
	 * directory where persistent files can be stored.
	 * @return location of persistence directory.
	 */
	String getPersistenceDir();

	/**
	 * Returns the location of the directory containing templates for cluster processing.
	 * @return location of the processing templates directory
	 */
	String getProcessingTemplatesDir();

}
