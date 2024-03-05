/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.january.dataset.SliceND;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.Findable;

public abstract class AbstractMalcolmSwmrProcessor<T extends NXobject> implements MalcolmSwmrProcessor<T>, Findable {

	private static final Logger logger = LoggerFactory.getLogger(AbstractMalcolmSwmrProcessor.class);

	private boolean enabled = true;
	private String name;

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	protected void writeStatData(final Object stat, ILazyWriteableDataset statDataset, SliceFromSeriesMetadata metaSlice) {
		final Dataset meanData = DatasetFactory.createFromObject(stat);
		final SliceND datasetSlice = new SliceND(statDataset.getShape(), statDataset.getMaxShape());
		final Slice[] inputSlice = metaSlice.getSliceFromInput();
		for (int i = 0; i < statDataset.getRank(); i++) {
			datasetSlice.setSlice(i, inputSlice[i]);
		}

		try {
			statDataset.setSlice(null, meanData, datasetSlice);
		} catch (DatasetException e) {
			logger.error("Error setting slice", e);
		}
	}

}
