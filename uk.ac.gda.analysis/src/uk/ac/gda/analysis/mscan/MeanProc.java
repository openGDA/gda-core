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

import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromSeriesMetadata;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.builder.AbstractNexusObjectProvider;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Compute the mean of the frame
 */
public class MeanProc extends AbstractMalcolmSwmrProcessor<NXdetector> {

	public static final String FIELD_NAME_MEAN = "full_mean";

	private static final Logger logger = LoggerFactory.getLogger(MeanProc.class);

	private ILazyWriteableDataset meanDataset;

	@Override
	protected void configureNexusProvider(AbstractNexusObjectProvider<NXdetector> nexusObjectProvider) {
		meanDataset = createField(FIELD_NAME_MEAN, Double.class);
	}

	@Override
	public void processFrame(Dataset data, SliceFromSeriesMetadata metaSlice) {
		logger.debug("Start of processFrame");
		writeStatData(data.mean(), meanDataset, metaSlice);
		logger.debug("End of processFrame");
	}

}
