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

import uk.ac.diamond.tomography.localtomo.LocalTomoType;
import uk.ac.diamond.tomography.localtomo.util.LocalTomoUtil;

public class ReconUtil {

	public static File getPathToWriteTo(IFile nexusFile) {

		String parentPath = getPathRelativeToNxsForProcessing(nexusFile.getLocation().toOSString());
		File pathToRecon = new File(parentPath, "/processing/reconstruction/");
		return pathToRecon;
	}

	private static String getPathRelativeToNxsForProcessing(String path) {
		int segmentsToRemove = 0;

		LocalTomoType localTomoObject = LocalTomoUtil.getLocalTomoObject();

		if (localTomoObject != null) {
			segmentsToRemove = localTomoObject.getTomodo().getSegmentsToRemoveRelativeToNexusForOutdir();
		}

		Path fileFullPath = new Path(path);
		IPath parentPath = fileFullPath.removeLastSegments(1);
		if (segmentsToRemove > 0) {
			parentPath = parentPath.removeLastSegments(segmentsToRemove);
		}
		return parentPath.toOSString();
	}

	public static File getSettingsFileLocation(IFile nexusFile) {
		Path path2 = new Path(nexusFile.getName());
		String fileNameWithoutExtension = path2.removeFileExtension().toOSString();
		String parentPath = getPathRelativeToNxsForProcessing(nexusFile.getLocation().toOSString());

		String postFixDir = "";
		LocalTomoType localTomoObject = LocalTomoUtil.getLocalTomoObject();
		if (localTomoObject != null) {
			String postFixDirObj = localTomoObject.getTomodo().getSettingsfile().getSettingsDirPostfix();
			if (postFixDirObj != null && postFixDirObj.length() > 0) {
				postFixDir = postFixDirObj;
			}
		}
		File pathToRecon = new File(parentPath, "/processing/sino/" + fileNameWithoutExtension + postFixDir);
		return pathToRecon;
	}

	public static IFile getNexusFileFromHmFileLocation(String hmFileLocation) {
		Path path = new Path(hmFileLocation);
		Path path2 = new Path(new Path(hmFileLocation).lastSegment());
		String fileNameWithoutExtension = path2.removeFileExtension().toOSString();
		IPath nxsFilePath = path.removeLastSegments(5).append(fileNameWithoutExtension).addFileExtension("nxs")
				.setDevice(null);
		return ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(URI.create(nxsFilePath.toString()))[0];
	}

	public static IPath getProcessingDir(IFile nexusFile) {
		String parentPath = getPathRelativeToNxsForProcessing(nexusFile.getLocation().toOSString());
		return new Path(parentPath).append("processing");
	}
}
