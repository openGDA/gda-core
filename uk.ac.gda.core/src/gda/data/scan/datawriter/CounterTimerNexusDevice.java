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

package gda.data.scan.datawriter;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXobject;
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

import gda.configuration.properties.LocalProperties;
import gda.data.nexus.INeXusInfoWriteable;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.util.TypeConverters;

/**
 * An instance of this class wraps a {@link Detector} to implement {@link INexusDevice}, for detectors where
 * {@link Detector#getExtraNames()} is not empty. This code is derived from {@link NexusDataWriter}.makeCounterTimer.
 */
class CounterTimerNexusDevice implements IWritableNexusDevice<NXdetector> {

	private static final List<String> SPECIAL_ATTRIBUTES =
			Collections.unmodifiableList(Arrays.asList(Scannable.ATTR_NX_CLASS, Scannable.ATTR_NEXUS_CATEGORY));

	private static final Logger logger = LoggerFactory.getLogger(CounterTimerNexusDevice.class);

	private Detector detector;

	private LinkedHashMap<String, ILazyWriteableDataset> writableDatasets = null;

	CounterTimerNexusDevice(Detector detector) {
		this.detector = detector;
	}

	@Override
	public String getName() {
		return detector.getName();
	}

	@Override
	public NexusObjectProvider<NXdetector> getNexusProvider(NexusScanInfo info) throws NexusException {
		logger.debug("Creating nexus object for detector {}", detector.getName());
		try {
			final NXdetector detGroup = createDetectorGroup(info);
			return new NexusObjectWrapper<NXdetector>(getName(), detGroup, getDataFieldName());
		} catch (DeviceException e) {
			throw new NexusException("Could not create nexus object for TODO put description of error here", e);
		}
	}

	private NXdetector createDetectorGroup(NexusScanInfo info) throws DeviceException, NexusException {
		final NXdetector detGroup = NexusNodeFactory.createNXdetector();

		// TODO add description, type, id metadata?
		detGroup.setDescriptionScalar(detector.getDescription());
		detGroup.setTypeScalar(detector.getDetectorType());
		detGroup.setDataset("id", DatasetFactory.createFromObject(detector.getDetectorID()));

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
		for (String fieldName : detector.getExtraNames()) {
			final ILazyWriteableDataset dataset = detGroup.initializeLazyDataset(fieldName, info.getRank(),
					Double.class);
			final String floatFill = LocalProperties.get("gda.nexus.floatfillvalue", "nan"); // Do we need this property? see
																						// DAQ-3175
			dataset.setFillValue(floatFill.equalsIgnoreCase("nan") ? Double.NaN : Double.parseDouble(floatFill));
			dataset.setChunking(info.createChunk(false, 8));
			dataset.setWritingAsync(true);
			writableDatasets.put(fieldName, dataset);
		}
	}

	/**
	 * Add the attributes for the given attribute container into the given nexus object.
	 *
	 * @param positioner
	 * @param container
	 * @throws NexusException
	 *             if the attributes could not be added for any reason
	 * @throws DeviceException
	 */
	private void registerAttributes(NXobject nexusObject) throws NexusException {
		// TODO: refactor this common code with ScannableNexusDevice

		// TODO write name as we do for scannables? NXdetector has field 'local_name' rather than name
		// nexusObject.setField("name", detector.getName());

		try {
			final Set<String> attributeNames = detector.getScanMetadataAttributeNames();
			for (String attrName : attributeNames) {
				addAttribute(nexusObject, detector, attrName);
			}
		} catch (DeviceException e) {
			throw new NexusException("Could not get attributes of device: " + getName());
		}
	}

	private void addAttribute(NXobject nexusObject, final Scannable scannable, String attrName) throws NexusException {
		if (!SPECIAL_ATTRIBUTES.contains(attrName)) {
			try {
				nexusObject.setField(attrName, scannable.getScanMetadataAttribute(attrName));
			} catch (Exception e) {
				throw new NexusException(MessageFormat.format(
						"An exception occurred attempting to get the value of the attribute ''{0}'' for the device ''{1}''",
						scannable.getName(), attrName));
			}
		}
	}

	private String getDataFieldName() {
		return detector.getExtraNames()[0];
	}

	@Override
	public void writePosition(Object data, SliceND scanSlice) throws NexusException {
		// TODO: this code is very similar to ScannableNexusDevice.writeActualPosition,
		// refactor it to common superclass to remove duplication
		final double[] dataArray = TypeConverters.toDoubleArray(data);
		int fieldIndex = 0;
		for (ILazyWriteableDataset dataset : writableDatasets.values()) {
			// we rely on predictable iteration order for LinkedHashSet of writableDataset
			final IDataset value = DatasetFactory.createFromObject(dataArray[fieldIndex]);
			try {
				dataset.setSlice(null, value, scanSlice);
			} catch (DatasetException e) {
				throw new NexusException("Could not write data for detector " + detector.getName());
			}
			fieldIndex++;
		}
	}

}