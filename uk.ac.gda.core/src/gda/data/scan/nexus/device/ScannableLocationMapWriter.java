/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

import static gda.data.scan.nexus.device.GDADeviceNexusConstants.ATTRIBUTE_NAME_UNITS;

import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.tree.TreeFactory;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NXtransformations;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.dawnsci.nexus.builder.CustomNexusEntryModification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.scan.datawriter.NexusDataWriter;
import gda.data.scan.datawriter.NexusDataWriterConfiguration;
import gda.data.scan.datawriter.scannablewriter.ScannableWriter;
import gda.data.scan.datawriter.scannablewriter.SingleScannableWriter;
import gda.data.scan.datawriter.scannablewriter.TransformationWriter;
import gda.device.Scannable;

/**
 * This class is responsible for creating linking to the datasets for a {@link Scannable}
 * from the locations specified in the location map of a GDA8 spring configuration file
 * as found for the entry with the name of this scannable in the
 * {@link NexusDataWriterConfiguration#getLocationMap()}. Note that although the
 * location map contains the {@link SingleScannableWriter}s used directly by {@link NexusDataWriter},
 * this class, as used for new nexus writing, instead simply reads the paths and units arrays
 * configured within that object and creates the links itself.
 *
 * @author Matthew Dickie
 */
class ScannableLocationMapWriter implements CustomNexusEntryModification {

	private static final Logger logger = LoggerFactory.getLogger(ScannableLocationMapWriter.class);

	protected final AbstractScannableNexusDevice<?> scannableNexusDevice;
	protected final SingleScannableWriter scannableWriter;

	private boolean entryModified = false;

	private NXentry entry = null;

	public static ScannableLocationMapWriter createScannableLocationMapWriter(
			AbstractScannableNexusDevice<?> scannableNexusDevice, ScannableWriter scannableWriter) {
		// Note, as of writing there are exactly three implementations of ScannableWriter:
		// SingleScannableWriter and two subclasses, TransformationWriter and EnergyScannableWriter.
		// This hasn't changed for many years and is fairly unlikely to change in future.
		if (!(scannableWriter instanceof SingleScannableWriter)) {
			throw new UnsupportedOperationException("Cannot write scannable, unsupported writer class: " + scannableWriter.getClass());
		}

		if (scannableWriter.getClass().equals(TransformationWriter.class)) {
			return new TransformationLocationMapWriter(scannableNexusDevice, (TransformationWriter) scannableWriter);
		}

		if (!(scannableWriter.getClass().equals(SingleScannableWriter.class))) {
			// the scannable writer is a subclass of SingleScannableWriter that we don't fully support
			// TODO DAQ-3869 - add suppport for EnergyScannableWriter
			logger.warn("NexusDataWriter location map entry for device {} is not fully supported: {}",
					scannableNexusDevice.getScannable().getName(), scannableWriter.getClass());
		}

		return new ScannableLocationMapWriter(scannableNexusDevice, ((SingleScannableWriter) scannableWriter));
	}

	private ScannableLocationMapWriter(AbstractScannableNexusDevice<?> scannableNexusDevice,
			SingleScannableWriter scannableWriter) {
		this.scannableNexusDevice = scannableNexusDevice;
		this.scannableWriter = scannableWriter;
	}

	@Override
	public final void modifyEntry(NXentry entry) throws NexusException {
		if (entryModified) { // sanity check, this method should only ever be called once
			throw new IllegalStateException("modifyEntry() has already been called");
		}

		// check that the nexus object for the wrapper has been created. This should be the case
		// if we're in a scan, unless this scannable is a per-scan monitor that is unavailable
		if (!scannableNexusDevice.isNexusObjectCreated()) return;

		this.entry = entry;
		final String scannableName = scannableNexusDevice.getScannable().getName();
		final String[] paths = scannableWriter.getPaths();
		final String[] fieldNames = scannableNexusDevice.getFieldNames();
		if (paths.length > fieldNames.length) {
			throw new NexusException(MessageFormat.format("Number of configured paths ({0}) larger than number of fields ({1}) for scannable {2}",
					paths.length, fieldNames.length, scannableNexusDevice.getName()));
		}

		for (int fieldIndex = 0; fieldIndex < paths.length; fieldIndex++) {
			logger.info("Adding link from '{}' to field '{}' of scannable '{}'", paths[fieldIndex], fieldNames[fieldIndex], scannableName);
			final DataNode dataNode = scannableNexusDevice.getFieldDataNode(fieldNames[fieldIndex]);
			// link to existing data node
			addLink(dataNode, paths[fieldIndex], fieldIndex);
		}

		entryModified = true;
	}

	/**
	 * Adds the {@link DataNode} to the entry at the given augmented path.
	 * @param dataNode
	 * @param augmentedPath
	 * @param fieldIndex
	 * @throws NexusException
	 */
	private void addLink(DataNode dataNode, String augmentedPath, int fieldIndex) throws NexusException {
		final String newFieldName = NexusUtils.getName(augmentedPath); // the final segment of the path
		final String parentGroupPath = augmentedPath.substring(0, augmentedPath.length() - newFieldName.length());
		// get the parent group, creating it if it doesn't already exist
		final NXobject parentGroup = NexusUtils.getGroupNode(entry, parentGroupPath, true);
		parentGroup.addDataNode(newFieldName, dataNode);

		writeCustomAttributes(dataNode, fieldIndex);
	}

	protected void writeCustomAttributes(DataNode dataNode, int fieldIndex) {
		// An error getting the position of a per-scan monitor is not treated as fatal, but no Nexus object
		// (and therefore no data node) will be created, so check for that condition here.
		// add units if specified
		writeAttribute(dataNode, ATTRIBUTE_NAME_UNITS, scannableWriter.getUnits(), fieldIndex);
	}

	protected <T> void writeAttribute(DataNode dataNode, String attributeName, T[] valueArray, int fieldIndex) {
		if (valueArray != null && valueArray.length > fieldIndex && valueArray[fieldIndex] != null &&
				!(valueArray[fieldIndex] instanceof String && StringUtils.isBlank((String) valueArray[fieldIndex]))) {
			dataNode.addAttribute(TreeFactory.createAttribute(attributeName, valueArray[fieldIndex]));
		}
	}

	private static class TransformationLocationMapWriter extends ScannableLocationMapWriter {

		TransformationLocationMapWriter(AbstractScannableNexusDevice<?> scannableNexusDevice, TransformationWriter transformationWriter) {
			super(scannableNexusDevice, transformationWriter);
		}

		@Override
		protected void writeCustomAttributes(DataNode dataNode, int fieldIndex) {
			super.writeCustomAttributes(dataNode, fieldIndex);

			final TransformationWriter transformationWriter = (TransformationWriter) scannableWriter;
			writeAttribute(dataNode, NXtransformations.NX_AXISNAME_ATTRIBUTE_TRANSFORMATION_TYPE,
					transformationWriter.getTransformation(), fieldIndex);
			writeAttribute(dataNode, NXtransformations.NX_AXISNAME_ATTRIBUTE_DEPENDS_ON,
					transformationWriter.getDependsOn(), fieldIndex);
			writeAttribute(dataNode, NXtransformations.NX_AXISNAME_ATTRIBUTE_OFFSET,
					transformationWriter.getOffset(), fieldIndex);
			writeAttribute(dataNode, NXtransformations.NX_AXISNAME_ATTRIBUTE_OFFSET_UNITS,
					transformationWriter.getOffsetUnits(), fieldIndex);
			writeAttribute(dataNode, NXtransformations.NX_AXISNAME_ATTRIBUTE_VECTOR,
					transformationWriter.getVector(), fieldIndex);
		}

	}
}