/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.api.messaging.messages;

/**
 * A service that can extract metadata from a nexus file at node paths specified by
 * a properties file at the start and optionally the end of a scan.
 */
public interface INexusMetadataExtractor {

	/**
	 * The name of the properties file to use for {@link ScanStatus#STARTED}.
	 */
	public static final String START_SCAN_METADATA_NODE_PATHS_FILE_NAME = "startScanMetadataPaths.txt";

	/**
	 * The name of the properties file to use for {@link ScanStatus#FINISHED}.
	 */
	public static final String END_SCAN_METADATA_NODE_PATHS_FILE_NAME = "endScanMetadataPaths.txt";

	/**
	 * Returns whether this extractor is enabled. This is determined by whether a file is
	 * present within gda-var with the file path with the same name as the value of {@link #START_SCAN_METADATA_NODE_PATHS_FILE_NAME}.
	 * @return <code>true</code> if enabled, <code>false</code> otherwise
	 */
	public boolean isEnabled();

	/**
	 * Creates a {@link ScanMetadataMessage} by extracting metdata from the nexus file at the given path, where the node paths extracted
	 * are determined by the contents of the appropriate properties file for the {@link ScanStatus}.
	 * @param scanStatus either {@link ScanStatus#STARTED} or {@link ScanStatus#FINISHED}
	 * @param nexusFilePath path of the nexus file
	 * @return The {@link ScanMetadataMessage} or <code>null</code> if a metadata message could not be created. An error is logged in this case.
	 */
	public ScanMetadataMessage createScanMetadataMessage(ScanStatus scanStatus, String nexusFilePath);

}
