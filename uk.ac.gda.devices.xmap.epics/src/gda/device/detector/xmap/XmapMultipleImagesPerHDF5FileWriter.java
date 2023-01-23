/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package gda.device.detector.xmap;

import gda.device.detector.addetector.filewriter.MultipleImagesPerHDF5FileWriter;

public class XmapMultipleImagesPerHDF5FileWriter extends MultipleImagesPerHDF5FileWriter {


	@Override
	protected void deriveFullFileName() throws Exception {
		String filePath = getNdFile().getFilePath_RBV();
		filePath = filePath.replace("X:", "/dls/i08/");
		expectedFullFileName = String.format(getNdFile().getFileTemplate_RBV(), filePath, getNdFile().getFileName_RBV(), getNdFile().getFileNumber_RBV());
	}


}
