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
import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.HDF5Loader;

/**
 *
 */
public class ReconUtil {
	private static final String PROCESSING_DIR_RELATIVE_TO_VISIT_DIR = "processing";
	private static final String HM_SETTINGS_DIR_RELATIVE_TO_VISIT_DIR = IPath.SEPARATOR
			+ PROCESSING_DIR_RELATIVE_TO_VISIT_DIR + "/sino/";
	private static final String RECON_OUTDIR_RELATIVE_TO_VISIT_DIR = IPath.SEPARATOR
			+ PROCESSING_DIR_RELATIVE_TO_VISIT_DIR + "/reconstruction/";
	private static final String NXS_PATH_TO_BEAMLINE_NAME = "/entry1/instrument/name";
	private static final String NXS_FILE_EXTN = "nxs";
	private static final Logger logger = LoggerFactory.getLogger(ReconUtil.class);

	public static File getReconOutDir(String nexusFileLocation) {
		String parentPath = getVisitDirectory(nexusFileLocation);
		File pathToRecon = new File(parentPath, RECON_OUTDIR_RELATIVE_TO_VISIT_DIR);
		return pathToRecon;
	}

	/**
	 * @param nexuFileLocation
	 *            - should be the full path of the form "/dls/ixx/data/yyyy/cmxxxxx/yyyy/yyy"
	 * @return the visit directory
	 */
	public static String getVisitDirectory(String nexuFileLocation) {
		// the path is expected to be of the form /dls/ixx/data/yyyy/cmxxxxx/yyyy/yyy
		Path nexusFilePath = new Path(nexuFileLocation);

		int segmentCount = nexusFilePath.segmentCount();
		String visitDir = null;
		if (segmentCount > 5) {
			IPath visitDirectoryPath = nexusFilePath.removeLastSegments(segmentCount - 5);
			visitDir = visitDirectoryPath.toOSString();
		} else {
			throw new IllegalArgumentException("Unable to get visit directory from nexus file");
		}
		return visitDir;
	}

	public static File getSettingsFileLocation(IFile nexusFile) {
		Path nexusFilePath = new Path(nexusFile.getName());
		String fileNameWithoutExtension = nexusFilePath.removeFileExtension().toOSString();
		String parentPath = getVisitDirectory(nexusFile.getLocation().toOSString());
		File pathToRecon = new File(parentPath, HM_SETTINGS_DIR_RELATIVE_TO_VISIT_DIR + fileNameWithoutExtension);
		return pathToRecon;
	}

	public static IFile getNexusFileFromHmFileLocation(String hmFileLocation) {
		Path path = new Path(hmFileLocation);
		Path path2 = new Path(new Path(hmFileLocation).lastSegment());
		String fileNameWithoutExtension = path2.removeFileExtension().toOSString();
		IPath nxsFilePath = path.removeLastSegments(5).append(fileNameWithoutExtension).addFileExtension(NXS_FILE_EXTN)
				.setDevice(null);
		return ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(URI.create(nxsFilePath.toString()))[0];
	}

	public static IPath getProcessingDir(IFile nexusFile) {
		String parentPath = getVisitDirectory(nexusFile.getLocation().toOSString());
		return new Path(parentPath).append(PROCESSING_DIR_RELATIVE_TO_VISIT_DIR);
	}

	public static File getReducedNexusFile(String nexusFileLocation) {
		String nexusFileName = new Path(nexusFileLocation).lastSegment();
		IPath nxsFileWithoutExtnPath = new Path(nexusFileName).removeFileExtension();
		HDF5Loader hdf5Loader = new HDF5Loader(nexusFileLocation);
		DataHolder loadFile;
		String beamlineName = null;
		try {
			loadFile = hdf5Loader.loadFile();
			beamlineName = loadFile.getDataset(NXS_PATH_TO_BEAMLINE_NAME).getStringAbs(0);
		} catch (Exception ex) {
			logger.error("Problem getting beamline name", ex);
		}
		String nxsFileWithoutExtn = nxsFileWithoutExtnPath.toString();
		String visitDirectory = getVisitDirectory(nexusFileLocation);
		return new File(String.format("%s/tmp/reduced/%s/%s.nxs", visitDirectory, beamlineName, nxsFileWithoutExtn));
	}
}
