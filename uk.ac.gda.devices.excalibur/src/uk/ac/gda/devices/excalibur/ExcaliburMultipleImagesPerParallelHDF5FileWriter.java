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

package uk.ac.gda.devices.excalibur;

import gda.device.detector.addetector.filewriter.MultipleImagesPerParallelHDF5FileWriter;
import gda.epics.LazyPVFactory;
import gda.epics.PV;
import gda.scan.ScanInformation;

public class ExcaliburMultipleImagesPerParallelHDF5FileWriter extends MultipleImagesPerParallelHDF5FileWriter{

	private String gapEnabledPVName;
	PV<Integer> gapEnabledPV;

	@Override
	public void prepareForCollection(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		//setup chunking based on gap being enabled or not
		if( gapEnabledPV == null)
			gapEnabledPV = LazyPVFactory.newIntegerPV(gapEnabledPVName);
		
		boolean gapEnabled = gapEnabledPV.get() != 0;
		setChunkSize0(gapEnabled ? 1 : 4);
		setChunkSize1(gapEnabled ? 259:256);
		setChunkSize2(gapEnabled ? 2069: 2048);
		setDsetSize1(gapEnabled ? 1796: 1536);
		setDsetSize2(gapEnabled ? 2069: 2048);
		super.prepareForCollection(numberImagesPerCollection, scanInfo);
	}

	public String getGapEnabledPVName() {
		return gapEnabledPVName;
	}

	public void setGapEnabledPVName(String gapEnabledPVName) {
		this.gapEnabledPVName = gapEnabledPVName;
	}

}
