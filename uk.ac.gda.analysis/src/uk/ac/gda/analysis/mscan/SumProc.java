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
 * Compute the sum of the frame
 */
public class SumProc implements MalcolmSwmrProcessor {

	private static final Logger logger = LoggerFactory.getLogger(SumProc.class);

	private LazyWriteableDataset sumDataset;

	private NexusObjectWrapper<NXdetector> nexusProvider;

	@Override
	public void initialise(NexusScanInfo info, NexusObjectWrapper<NXdetector> nexusWrapper) {
		this.nexusProvider = nexusWrapper;
		createDetectorNexusObj(info);

	}

	private void createDetectorNexusObj(NexusScanInfo info) {

		int[] ones = new int[info.getShape().length];
		Arrays.fill(ones, 1);

		sumDataset = new LazyWriteableDataset("full_sum", Double.class, ones, info.getShape(), info.getShape(), null);
		nexusProvider.getNexusObject().createDataNode("full_sum", sumDataset);
		nexusProvider.addAdditionalPrimaryDataFieldName("full_sum");
	}

	@Override
	public void processFrame(Dataset data, SliceFromSeriesMetadata metaSlice) {
		logger.debug("Start of processFrame");
		Object sum = data.sum();
		Dataset s = DatasetFactory.createFromObject(sum);
		SliceND sl = new SliceND(sumDataset.getShape(), sumDataset.getMaxShape(), metaSlice.getSliceFromInput());

		try {
			sumDataset.setSlice(null, s, sl);
		} catch (DatasetException e) {
			logger.error("Error setting slice", e);
		}
		logger.debug("End of processFrame");
	}

}