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
import org.eclipse.core.runtime.Path;

public class ReconUtil {

	public static File getPathToWriteTo(IFile nexusFile) {
		File path = new File(nexusFile.getLocation().toOSString());
		File pathToRecon = new File(path.getParent(), "/processing/reconstruction/");
		return pathToRecon;
	}

	public static File getSettingsFileLocation(IFile nexusFile) {
		File path = new File(nexusFile.getLocation().toOSString());
		Path path2 = new Path(nexusFile.getName());
		String fileNameWithoutExtension = path2.removeFileExtension().toOSString();
		File pathToRecon = new File(path.getParent(), "/processing/sino/" + fileNameWithoutExtension + "_data/settings");
		return pathToRecon;
	}
}
