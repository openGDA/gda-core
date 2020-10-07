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

package gda.data.scan.nexus.device;

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.IWritableNexusDevice;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.SliceND;

import gda.configuration.properties.LocalProperties;
import gda.data.scan.datawriter.NexusDataWriter;
import gda.device.Detector;

/**
 * An instance of this class wraps a {@link Detector} to implement {@link INexusDevice}, for generic detectors,
 * i.e. those that write to a single dataset (a one-dimensional array, possibly of size 1) at each point in the scan.
 *
 * This code is derived from {@link NexusDataWriter}.makeGenericDetector.
 */
public class GenericDetectorNexusDevice extends AbstractDetectorNexusDeviceAdapter {

	private ILazyWriteableDataset writableDataset;

	public GenericDetectorNexusDevice(Detector detector) {
		super(detector);
	}

	@Override
	protected String getPrimaryDataFieldName() {
		return NXdetector.NX_DATA;
	}

	@Override
	public void writePosition(Object data, SliceND scanSlice) throws NexusException {
		try {
			IWritableNexusDevice.writeDataset(writableDataset, data, scanSlice);
		} catch (DatasetException e) {
			throw new NexusException("Could not write data for detector " + getName());
		}
	}

	@Override
	protected void writeDataFields(NexusScanInfo info, NXdetector detGroup) throws NexusException {
		try {
			final int[] dataDimensions = getDetector().getDataDimensions();
			final int datasetRank = info.getRank() + dataDimensions.length; // always 1?
			writableDataset = detGroup.initializeLazyDataset(NXdetector.NX_DATA, datasetRank, Double.class);
			final String floatFill = LocalProperties.get("gda.nexus.floatfillvalue", "nan"); // Do we need this property? see DAQ-3175
			writableDataset.setFillValue(floatFill.equalsIgnoreCase("nan") ? Double.NaN : Double.parseDouble(floatFill));
			writableDataset.setChunking(info.createChunk(dataDimensions));
			writableDataset.setWritingAsync(true);
		} catch (Exception e) {
			throw new NexusException("Could not create dataset for detector " + getName(), e);
		}
	}

	@Override
	public void scanEnd() throws NexusException {
		writableDataset = null;
	}

}
