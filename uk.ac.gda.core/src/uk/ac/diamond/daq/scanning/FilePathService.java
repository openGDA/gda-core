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

import gda.configuration.properties.LocalProperties;
import gda.data.NumTracker;
import gda.data.PathConstructor;

import java.io.IOException;

import org.eclipse.scanning.api.scan.IFilePathService;

/**
 * Implementation of the {@link IFilePathService} which determines the next path to write to.
 */
public class FilePathService implements IFilePathService {

	private static NumTracker tracker;

	public FilePathService() {
		// Must have constructor that does no work
	}

	@Override
	public String nextPath() throws IOException {

		if (tracker == null) {
			// Make a NumTracker using the property gda.data.numtracker.extension
			tracker = new NumTracker();
		}

		// Get the current data directory
		// TODO This currently doesn't support sub directories under the visit e.g /sample1/
		String dir = PathConstructor.createFromDefaultProperty();

		// Get the next file number and update the tracker file on disk
		int fileNumber = tracker.incrementNumber();

		// Build the file name
		// Default to "base" if gda.beamline.name is not set (behaviour copied from NexusDataWriter). Should never happen!
		String filename = LocalProperties.get(LocalProperties.GDA_BEAMLINE_NAME, "base") + "-" + fileNumber + ".nxs";

		// Return the full file path
		return dir + "/" + filename;
	}

}
