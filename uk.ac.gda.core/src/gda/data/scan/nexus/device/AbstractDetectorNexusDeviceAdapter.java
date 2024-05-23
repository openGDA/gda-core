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

import static gda.data.scan.nexus.device.GDADeviceNexusConstants.ATTRIBUTE_NAME_LOCAL_NAME;
import static gda.data.scan.nexus.device.GDADeviceNexusConstants.ATTRIBUTE_NAME_SCAN_ROLE;
import static gda.device.scannable.ScannableUtils.getNumDecimalsArray;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.SequencedMap;

import org.apache.commons.lang.StringUtils;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.tree.TreeFactory;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.NexusScanInfo.ScanRole;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.LazyWriteableDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.INeXusInfoWriteable;
import gda.device.Detector;
import gda.device.DeviceException;

public abstract class AbstractDetectorNexusDeviceAdapter extends AbstractNexusDeviceAdapter<NXdetector> {

	protected static final String FIELD_NAME_ID = "id";

	protected static final String[] NO_FIELDS = new String[0];

	private static final Logger logger = LoggerFactory.getLogger(AbstractDetectorNexusDeviceAdapter.class);

	protected Object firstPointData;

	protected AbstractDetectorNexusDeviceAdapter(Detector detector) {
		super(detector);
	}

	public void setFirstPointData(Object detectorData) {
		firstPointData = detectorData;
	}

	protected Detector getDetector() {
		return (Detector) super.getDevice();
	}

	protected SequencedMap<String, DataNode> createExtraNameDataNodes(NexusScanInfo info) {
		final SequencedMap<String, DataNode> extraNameDataNodes = new LinkedHashMap<>();

		final int[] numDecimals = getNumDecimalsArray(getDetector());
		final String[] fieldNames = getDetector().getExtraNames();
		for (int fieldIndex = 0; fieldIndex < fieldNames.length; fieldIndex++) {
			final String fieldName = fieldNames[fieldIndex];

			final int[] maxShape = new int[info.getOverallRank()];
			Arrays.fill(maxShape, ILazyWriteableDataset.UNLIMITED);
			final int[] shape = new int[info.getOverallRank()];
			final ILazyWriteableDataset dataset = new LazyWriteableDataset(fieldName, Double.class, shape, maxShape, null,  null);

			dataset.setFillValue(getFillValue(Double.class));
			dataset.setChunking(NexusUtils.estimateChunking(info.getOverallShape(), DOUBLE_DATA_BYTE_SIZE));
			dataset.setWritingAsync(true);

			final int fieldNumDecimals = numDecimals == null ? -1 : numDecimals[fieldIndex];
			final DataNode dataNode = NexusNodeFactory.createDataNode();
			dataNode.setDataset(dataset);
			addAttributesToDataNode(fieldName, fieldNumDecimals, null, dataNode);
			extraNameDataNodes.put(fieldName, dataNode);
		}

		return extraNameDataNodes;
	}

	@Override
	protected NXdetector createNexusObject(NexusScanInfo info) throws NexusException {
		final NXdetector detGroup = NexusNodeFactory.createNXdetector();

		final Detector detector = getDetector();
		addDetectorAttributes(detGroup);
		writeMetaDataFields(detGroup, detector);

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

	protected void addDetectorAttributes(NXdetector detGroup) {
		detGroup.addAttribute(TreeFactory.createAttribute(ATTRIBUTE_NAME_LOCAL_NAME, getName()));
		detGroup.addAttribute(TreeFactory.createAttribute(ATTRIBUTE_NAME_SCAN_ROLE, ScanRole.DETECTOR.toString().toLowerCase()));
	}

	protected void writeMetaDataFields(NXdetector detGroup, Detector detector) throws NexusException {
		try {
			if (StringUtils.isNotEmpty(detector.getDescription())) {
				detGroup.setDescriptionScalar(detector.getDescription());
			}
			if (StringUtils.isNotEmpty(detector.getDetectorType())) {
				detGroup.setTypeScalar(detector.getDetectorType());
			}
			if (StringUtils.isNotEmpty(detector.getDetectorID())) {
				detGroup.setDataset(FIELD_NAME_ID, NexusUtils.createFromObject(detector.getDetectorID(), FIELD_NAME_ID));
			}

			// TODO DAQ-3207 add any metadata added by appenders. See NexusDataWriter.addDeviceMetadata
		} catch  (DeviceException e) {
			throw new NexusException("Error reading properties of detector: {}" + detector.getName(), e);
		}
	}

	protected abstract void writeDataFields(NexusScanInfo info, NXdetector detGroup) throws NexusException;

	@Override
	public String[] getFieldNames() {
		return NO_FIELDS;
	}

	@Override
	public DataNode getFieldDataNode(String fieldName) {
		return null;
	}

}
