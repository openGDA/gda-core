/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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
import java.util.function.Function;

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
 * Intended to be used to append an additional dataset to a Malcolm scan where per point data is provided by Malcolm but
 * requires a transformation using parameters known by GDA.
 *
 */
public class FilterTransmissionProc implements MalcolmSwmrProcessor<NXdetector> {

	private static final Logger logger = LoggerFactory.getLogger(FilterTransmissionProc.class);

	private static final String TRANSMISSION_DATA_NAME = "transmission";

	private boolean enabled = true;

	private Function<Integer, Double> transmissionCalc = Number::doubleValue;

	public Function<Integer, Double> getTransmissionCalc() {
		return transmissionCalc;
	}

	public void setTransmissionCalc(Function<Integer, Double> transmissionCalc) {
		this.transmissionCalc = transmissionCalc;
	}

	private NexusObjectWrapper<NXdetector> nexusProvider;
	private LazyWriteableDataset transmissionDataset;


	@Override
	public void initialise(NexusScanInfo info, NexusObjectWrapper<NXdetector> nexusWrapper) {
		this.nexusProvider = nexusWrapper;
		createDetectorNexusObj(info);

	}

	private void createDetectorNexusObj(NexusScanInfo info) {
		int[] ones = new int[info.getOverallRank()];
		Arrays.fill(ones, 1);

		transmissionDataset = new LazyWriteableDataset(TRANSMISSION_DATA_NAME, Double.class, ones, info.getOverallShape(),
				info.getOverallShape(), null);
		nexusProvider.getNexusObject().createDataNode(TRANSMISSION_DATA_NAME, transmissionDataset);
		nexusProvider.addAdditionalPrimaryDataFieldName(TRANSMISSION_DATA_NAME);

	}

	@Override
	public void processFrame(Dataset data, SliceFromSeriesMetadata metaSlice) {
		logger.debug("Start of processFrame");
		var transmission = transmissionCalc.apply(data.getInt());
		Dataset s = DatasetFactory.createFromObject(transmission);
		SliceND sl = new SliceND(transmissionDataset.getShape(), transmissionDataset.getMaxShape(), metaSlice.getSliceFromInput());

		try {
			transmissionDataset.setSlice(null, s, sl);
		} catch (DatasetException e) {
			logger.error("Error setting slice", e);
		}
		logger.debug("End of processFrame");

	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

}
