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

package gda.device.detector.addetectorprovisional.filewriter;

import java.io.IOException;

import gda.data.NumTracker;
import gda.device.detector.areadetector.v17.NDFile;

public class SingleImagePerFileWriterWithNumTracker extends SingleImagePerFileWriter {

	private NumTracker numTracker = null;

	public SingleImagePerFileWriterWithNumTracker(NDFile ndFile, String detectorName, String numTrackerExtension)
			throws IOException {
		// ndFile, fileTemplate, filePathTemplate, fileNameTemplate, fileNumberAtScanStart, setFileNameAndNumber
		this(ndFile, "%s%s%5.5d.jpg", "$datadir$/snapped-data/" + detectorName, "", numTrackerExtension);
	}

	public SingleImagePerFileWriterWithNumTracker(NDFile ndFile, String fileTemplate, String filePathTemplate,
			String fileNameTemplate, String numTrackerExtension) throws IOException {
		super(ndFile, fileTemplate, filePathTemplate, fileNameTemplate, -1);
		numTracker = new NumTracker(numTrackerExtension);
	}
	
	@Override
	protected void configureNdFile() throws Exception {
		setFileNumberAtScanStart(numTracker.incrementNumber());
		super.configureNdFile();
	}

}
