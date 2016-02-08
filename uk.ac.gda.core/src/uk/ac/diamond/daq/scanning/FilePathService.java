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

import gda.data.NumTracker;
import gda.data.PathConstructor;

import java.io.IOException;

import org.eclipse.scanning.api.scan.IFilePathService;

public class FilePathService implements IFilePathService {

	private static NumTracker tracker;

	public FilePathService() {
		// Must have constructor that does no work
	}

	@Override
	public String nextPath() throws IOException {

		if (tracker == null)
			tracker = new NumTracker();

		// FIXME This service works for mapping but
		// requires more thought for other config(s)
	    String dir = PathConstructor.createFromDefaultProperty();
		int num = tracker.getCurrentFileNumber();
		return dir + "/" + num + ".nxs";
	}

}
