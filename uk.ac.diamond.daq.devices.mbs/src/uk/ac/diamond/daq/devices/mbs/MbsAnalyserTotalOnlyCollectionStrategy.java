/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.devices.mbs;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import gda.device.DeviceException;
import gda.device.detector.nxdata.NXDetectorDataAppender;

public class MbsAnalyserTotalOnlyCollectionStrategy extends MbsAnalyserCollectionStrategy {

	@Override
	public List<String> getInputStreamNames() {
		return Arrays.asList(analyser.getName());
	}

	@Override
	public List<String> getInputStreamFormats() {
		return Arrays.asList("%5.5g");
	}

	@Override
	public List<NXDetectorDataAppender> read(int maxToRead)
			throws NoSuchElementException, InterruptedException, DeviceException {

		return Arrays.asList(new MbsNXDetectorTotalOnlyDataAppender(completedRegion));
	}
}
