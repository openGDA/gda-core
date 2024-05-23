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

import java.util.SequencedMap;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.IWritableNexusDevice;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.SliceND;

import gda.configuration.properties.LocalProperties;
import gda.data.scan.datawriter.NexusDataWriter;
import gda.device.Detector;
import gda.util.TypeConverters;

/**
 * An instance of this class wraps a {@link Detector} to implement {@link INexusDevice}, for detectors where
 * {@link Detector#getExtraNames()} is not empty. This code is derived from {@link NexusDataWriter}.makeCounterTimer.
 */
public class CounterTimerNexusDevice extends AbstractDetectorNexusDeviceAdapter {

	public static final String PRIMARY_EXTRANAME_DATA_FIELD_INDEX = "gda.nexus.nexusScanDataWriter.primaryExtraNameDataFieldIndex";
	public static final int PRIMARY_EXTRANAME_DATA_FIELD_LAST = -1;
	public static final String ADD_ALL_EXTRA_NAMES_AS_AXES_FIELDS = "gda.nexus.nexusScanDataWriter.addAllExtraNamesAsAxesFields";

	private SequencedMap<String, DataNode> dataNodes = null;

	public CounterTimerNexusDevice(Detector detector) {
		super(detector);
	}

	/**
	 * Configure the nexus wrapper, an object that contains the nexus object and contains additional information
	 * describing how the nexus object should be added to the nexus tree - in particular to {@link NXdata} groups.
	 * <BR><BR>
	 * The default behaviour is it will use the last extra name to add to the primary field in the NXdata group. Please
	 * see the function:
	 * <code>
	 * getPrimaryDataFieldName()
	 * </code>
	 * An additional setting to add all extra names to the NXdata group is by using the following property:
	 * <code>
	 * gda.nexus.nexusScanDataWriter.addAllExtraNamesAsAxesFields
	 * </code>
	 * when set to true. The default is false.
	 *
	 * @param nexusWrapper
	 * @param info
	 * @throws NexusException
	 */
	@Override
	protected void configureNexusWrapper(NexusObjectWrapper<NXdetector> nexusWrapper, NexusScanInfo info)
			throws NexusException {

		super.configureNexusWrapper(nexusWrapper, info);
		if (LocalProperties.check(ADD_ALL_EXTRA_NAMES_AS_AXES_FIELDS, false)) {
			nexusWrapper.addAxisDataFieldNames(getFieldNames());
		}
	}

	/**
	 * Return the extra name field for counter timer Nexus devices.
	 * <BR><BR>
	 * Defaults to the last extra name field (-1)
	 * <BR><BR>
	 * To revert to the previous CounterTimerNexusDevice behaviour, add
	 * <BR><code>
	 * gda.nexus.nexusScanDataWriter.primaryExtraNameDataFieldIndex=0
	 * </code><BR>
	 * to the common_instance_java.properties file for a beamline.
	 */
	@Override
	protected String getPrimaryDataFieldName() {
		final var extraNames = getDetector().getExtraNames();
		int index =  LocalProperties.getAsInt(PRIMARY_EXTRANAME_DATA_FIELD_INDEX, PRIMARY_EXTRANAME_DATA_FIELD_LAST);
		if (index < 0) { // Negative, so count back from last
			index += extraNames.length;
		}
		return extraNames[index];
	}

	@Override
	protected void writeDataFields(NexusScanInfo info, final NXdetector detGroup) {
		this.dataNodes = createExtraNameDataNodes(info);
		dataNodes.forEach(detGroup::addDataNode);
	}

	@Override
	public void writePosition(Object data, SliceND scanSlice) throws NexusException {
		final double[] dataArray = TypeConverters.toDoubleArray(data);

		int fieldIndex = 0;
		for (DataNode dataNode : dataNodes.values()) {
			// we rely on predictable iteration order for the LinkedHashMap of DataNodes
			final ILazyWriteableDataset dataset = dataNode.getWriteableDataset();
			try {
				IWritableNexusDevice.writeDataset(dataset, dataArray[fieldIndex], scanSlice);
			} catch (DatasetException e) {
				throw new NexusException("Could not write data for detector " + getName(), e);
			}
			fieldIndex++;
		}
	}

	@Override
	public String[] getFieldNames() {
		return getDetector().getExtraNames();
	}

	@Override
	public DataNode getFieldDataNode(String fieldName) {
		return dataNodes.get(fieldName);
	}

	@Override
	public void scanEnd() throws NexusException {
		dataNodes = null;
	}

}