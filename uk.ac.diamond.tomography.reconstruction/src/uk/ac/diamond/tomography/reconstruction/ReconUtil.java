/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.diamond.tomography.reconstruction;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class ReconUtil {
	private static final String PROCESSING_DIR_RELATIVE_TO_VISIT_DIR = "processing";
	private static final String HM_SETTINGS_DIR_RELATIVE_TO_VISIT_DIR = IPath.SEPARATOR
			+ PROCESSING_DIR_RELATIVE_TO_VISIT_DIR + "/sino/";
	private static final String RECON_OUTDIR_RELATIVE_TO_VISIT_DIR = IPath.SEPARATOR
			+ PROCESSING_DIR_RELATIVE_TO_VISIT_DIR + "/reconstruction/";
	private static final Logger logger = LoggerFactory.getLogger(ReconUtil.class);

	public static String RECONSTRUCTED_IMAGE_FILE_FORMAT = "recon_%05d.tif";

	public static File getReconOutDir(String nexusFileLocation) {
		String parentPath = getVisitDirectory(nexusFileLocation);

		String reconUserSpecificDir = String.format("%s%s%s", RECON_OUTDIR_RELATIVE_TO_VISIT_DIR, File.separator,
				getUserId());
		File pathToRecon = new File(parentPath, reconUserSpecificDir);
		return pathToRecon;
	}

	/**
	 * @param nexusFileLocation
	 *            - should be the full path of the form "/dls/ixx/data/yyyy/cmxxxxx/yyyy/yyy"
	 * @return the visit directory
	 */
	public static String getVisitDirectory(String nexusFileLocation) {
		// the path is expected to be of the form /dls/ixx/data/yyyy/cmxxxxx/yyyy/yyy
		Path nexusFilePath = new Path(nexusFileLocation);

		int segmentCount = nexusFilePath.segmentCount();
		String visitDir = null;
		if (segmentCount > 5) {
			IPath visitDirectoryPath = nexusFilePath.removeLastSegments(segmentCount - 5);
			visitDir = visitDirectoryPath.toOSString();
		} else {
			throw new IllegalArgumentException("Unable to get visit directory from nexus file:" + nexusFilePath);
		}
		return visitDir;
	}

	/**
	 * @param nexusFile
	 * @return the location for the settings file - most likely to be of the form
	 *         /dls/i12/data/2013/cm5936-1/processing/sino/rsr31645/
	 */
	public static File getSettingsFileLocation(IFile nexusFile) {
		Path nexusFilePath = new Path(nexusFile.getName());
		String fileNameWithoutExtension = nexusFilePath.removeFileExtension().toOSString();
		String parentPath = getVisitDirectory(nexusFile.getLocation().toOSString());

		String hmSettingsDirForUser = String.format("%s%s%s%s", HM_SETTINGS_DIR_RELATIVE_TO_VISIT_DIR, File.separator,
				getUserId(), File.separator);
		File pathToRecon = new File(parentPath, hmSettingsDirForUser + fileNameWithoutExtension);
		return pathToRecon;
	}

	private static String getUserId() {
		return System.getProperty("user.name");
	}

	/**
	 * @param nexusFile
	 * @return the processing dir for the visit - of the form /dls/i12/data/2013/cm5936-1/processing/
	 */
	public static IPath getProcessingDir(IFile nexusFile) {
		String parentPath = getVisitDirectory(nexusFile.getLocation().toOSString());
		return new Path(parentPath).append(PROCESSING_DIR_RELATIVE_TO_VISIT_DIR);
	}

	public static File getReducedNexusFile(String nexusFileLocation) {
		String nexusFileName = new Path(nexusFileLocation).lastSegment();
		IPath nxsFileWithoutExtnPath = new Path(nexusFileName).removeFileExtension();
		String nxsFileWithoutExtn = nxsFileWithoutExtnPath.toString();
		String visitDirectory = getVisitDirectory(nexusFileLocation);
		return new File(String.format("%s/tmp/reduced/%s.nxs", visitDirectory, nxsFileWithoutExtn));
	}

	/**
	 * @param nexusFileLocation
	 * @return the dir for the quick reconstruction - of the form
	 *         /dls/i12/data/2013/cm5936-1/tmp/reduced/rsr31645/16077_data_quick
	 */
	public static String getReconstructedReducedDataDirectoryPath(String nexusFileLocation) {
		File reducedNexusFile = ReconUtil.getReducedNexusFile(nexusFileLocation);
		String reducedNxsFileName = new Path(reducedNexusFile.getPath()).removeFileExtension().toOSString();
		logger.debug("reducedNexusFile {}", reducedNexusFile);
		File pathToImages = new File(String.format("%s/%s_data_quick", getUserId(), reducedNxsFileName));
		return pathToImages.toString();
	}

	/**
	 * @param nexusFullPath
	 * @return the dir for the centre of rotation - of the form
	 *         /dls/i12/data/2013/cm5936-1/tmp/reduced/centerofrotation/rsr31645/
	 */
	public static String getCentreOfRotationDirectory(String nexusFullPath) {
		String visitDirectory = getVisitDirectory(nexusFullPath);
		String nexusFileName = new Path(nexusFullPath).lastSegment();
		IPath nxsFileWithoutExtnPath = new Path(nexusFileName).removeFileExtension();
		return String.format("%s/tmp/reduced/centerofrotation/%s/%s/", visitDirectory, getUserId(),
				nxsFileWithoutExtnPath.toString());
	}
}
