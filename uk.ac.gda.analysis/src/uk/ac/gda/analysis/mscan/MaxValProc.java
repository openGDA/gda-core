/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.gda.analysis.mscan;

import java.util.Arrays;

import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromSeriesMetadata;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.LazyWriteableDataset;
import org.eclipse.january.dataset.SliceND;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Find the maximum value in the frame
 */
public class MaxValProc implements MalcolmSwmrProcessor<NXdetector> {

	private static final Logger logger = LoggerFactory.getLogger(MaxValProc.class);

	private LazyWriteableDataset maxValDataset;

	private NexusObjectWrapper<NXdetector> nexusProvider;


	@Override
	public void initialise(NexusScanInfo info, NexusObjectWrapper<NXdetector> nexusWrapper) {
		this.nexusProvider = nexusWrapper;
		createDetectorNexusObj(info);
	}

	private void createDetectorNexusObj(NexusScanInfo info) {

		int[] ones = new int[info.getOverallRank()];
		Arrays.fill(ones, 1);

		maxValDataset = new LazyWriteableDataset("global_max", Double.class, ones, info.getOverallShape(), info.getOverallShape(), null);
		nexusProvider.getNexusObject().createDataNode("global_max",maxValDataset);
		nexusProvider.addAdditionalPrimaryDataFieldName("global_max");
	}

	@Override
	public void processFrame(Dataset data, SliceFromSeriesMetadata metaSlice) {
		logger.debug("Start of processFrame");
		Object max = data.max();
		// int[] maxCoords = data.maxPos(); // TODO add this
		Dataset s = DatasetFactory.createFromObject(max);
		SliceND sl = new SliceND(maxValDataset.getShape(), maxValDataset.getMaxShape(), metaSlice.getSliceFromInput());

		try {
			maxValDataset.setSlice(null, s, sl);
		} catch (DatasetException e) {
			logger.error("Error setting slice", e);
		}
		logger.debug("End of processFrame");
	}

}
