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

import java.util.LinkedHashMap;

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.SliceND;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.INeXusInfoWriteable;
import gda.data.scan.datawriter.NexusDataWriter;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.util.TypeConverters;

/**
 * An instance of this class wraps a {@link Detector} to implement {@link INexusDevice}, for detectors where
 * {@link Detector#getExtraNames()} is not empty. This code is derived from {@link NexusDataWriter}.makeCounterTimer.
 */
public class CounterTimerNexusDevice extends AbstractNexusDeviceAdapter<NXdetector> {

	private static final Logger logger = LoggerFactory.getLogger(CounterTimerNexusDevice.class);

	private LinkedHashMap<String, ILazyWriteableDataset> writableDatasets = null;

	public CounterTimerNexusDevice(Detector detector) {
		super(detector);
	}

	private Detector getDetector() {
		return (Detector) super.getDevice();
	}

	@Override
	public NexusObjectProvider<NXdetector> getNexusProvider(NexusScanInfo info) throws NexusException {
		logger.debug("Creating nexus object for detector {}", getName());
		final NXdetector detGroup = createNexusObject(info);
		return new NexusObjectWrapper<NXdetector>(getName(), detGroup, getDataFieldName());
	}

	@Override
	protected String getPrimaryDataFieldName() {
		return getDetector().getExtraNames()[0];
	}

	@Override
	protected NXdetector createNexusObject(NexusScanInfo info) throws NexusException {
		final NXdetector detGroup = NexusNodeFactory.createNXdetector();

		final Detector detector = getDetector();
		try {
			detGroup.setDescriptionScalar(detector.getDescription());
			detGroup.setTypeScalar(detector.getDetectorType());
			detGroup.setDataset("id", DatasetFactory.createFromObject(detector.getDetectorID()));
		} catch (DeviceException e) {
			throw new NexusException("Error reading device metadata", e);
		}

		// Note: unlike NexusDataWriter, we do not support INeXusInfoWriteable
		// this seems to be little used in practise
		if (detector instanceof INeXusInfoWriteable) {
			logger.warn("INeXusInfoWriteable is not supported by data writer {}", getClass().getSimpleName());
		}

		writeDataFields(info, detGroup);

		// add fields for attributes, e.g. name, description. This allows custom metadata to be added
		registerAttributes(detGroup);

		return detGroup;
	}

	private void writeDataFields(NexusScanInfo info, final NXdetector detGroup) {
		writableDatasets = new LinkedHashMap<>();
		for (String fieldName : getDetector().getExtraNames()) {
			final ILazyWriteableDataset dataset = detGroup.initializeLazyDataset(fieldName, info.getRank(), Double.class);
			dataset.setFillValue(getFillValue(Double.class));
			dataset.setChunking(info.createChunk(false, 8));
			dataset.setWritingAsync(true);
			writableDatasets.put(fieldName, dataset);
		}
	}

	private String getDataFieldName() {
		return getDetector().getExtraNames()[0];
	}

	@Override
	public void writePosition(Object data, SliceND scanSlice) throws NexusException {
		final double[] dataArray = TypeConverters.toDoubleArray(data);

		int fieldIndex = 0;
		for (ILazyWriteableDataset dataset : writableDatasets.values()) {
			// we rely on predictable iteration order for LinkedHashSet of writableDataset
			final IDataset value = DatasetFactory.createFromObject(dataArray[fieldIndex]);
			try {
				dataset.setSlice(null, value, scanSlice);
			} catch (DatasetException e) {
				throw new NexusException("Could not write data for detector " + getName());
			}
			fieldIndex++;
		}
	}

	@Override
	public void scanEnd() throws NexusException {
		writableDatasets = null;
	}

}