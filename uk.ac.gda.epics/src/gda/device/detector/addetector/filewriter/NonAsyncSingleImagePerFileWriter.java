/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

import gda.device.DeviceException;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdetector.NonAsynchronousNXPlugin;

import java.util.NoSuchElementException;

public class NonAsyncSingleImagePerFileWriter extends SingleImagePerFileWriter implements NonAsynchronousNXPlugin  {

	public NonAsyncSingleImagePerFileWriter() {
	}
	
	public NonAsyncSingleImagePerFileWriter(String detectorName) {
		super(detectorName);
	}

	@Override
	public NXDetectorDataAppender read() throws NoSuchElementException, InterruptedException, DeviceException {
		return super.readNXDetectorDataAppender();
	}

	@Override
	public String getFullFileName() throws Exception {
		return getNdFile().getFullFileName_RBV();
	}
	
	
}
