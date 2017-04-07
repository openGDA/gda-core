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

package gda.device.detector.addetector.filewriter;

import java.io.IOException;

import gda.configuration.properties.LocalProperties;
import gda.data.NumTracker;

public class SingleImagePerFileWriterWithNumTracker extends SingleImagePerFileWriter {

	private NumTracker numTracker = null;

	/**
	 * Creates a SingleImagePerFileWriterWithNumTracker which writes folders of files alongside the current file in the 'standard'
	 * location (ndFile and numTracker must still be configured). e.g. <blockquote>
	 *
	 * <pre>
	 * datadir
	 *    snapped-data
	 *       pilatus100k
	 *          00001.tif
	 * @param detectorName
	 */
	public SingleImagePerFileWriterWithNumTracker(String detectorName) {
		setFileTemplate("%s%s%05d.tif");
		setFilePathTemplate("$datadir$/snapped-data/" + detectorName);
		setFileNameTemplate("");
		setFileNumberAtScanStart(1);
	}

	/**
	 * 	/**
	 * Creates a SingleImageFileWriter with ndFile, fileTemplate, filePathTemplate, fileNameTemplate,
	 * fileNumberAtScanStart, and numTrackerExtension yet to be set.
	 */
	public SingleImagePerFileWriterWithNumTracker() {
		super();
	}

	public void setNumTrackerExtension(String numTrackerExtension) {
		try {
			// workaround DAQ-560, we need to pass in the directry name to NumTracker, otherwise it will not respect
			// the passed extension name. Unfortunately, this means we duplicate some logic.
			String fallbackDirname = LocalProperties.get(LocalProperties.GDA_VAR_DIR);
			String dirname = LocalProperties.get(LocalProperties.GDA_DATA_NUMTRACKER, fallbackDirname);
			numTracker = new NumTracker(numTrackerExtension, dirname);
		} catch (IOException e) {
			throw new IllegalArgumentException("NumTracker with extension '" + numTrackerExtension + "' could not be created.", e);
		}
	}

		//this(ndFile, "%s%s%5.5d.jpg", "$datadir$/snapped-data/" + detectorName, "", numTrackerExtension);

	@Override
	protected void configureNdFile() throws Exception {
		setFileNumberAtScanStart(numTracker.incrementNumber());
		super.configureNdFile();
	}

}
