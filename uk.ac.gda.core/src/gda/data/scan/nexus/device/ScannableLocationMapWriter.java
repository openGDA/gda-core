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

import static gda.data.scan.nexus.device.AbstractScannableNexusDevice.ATTR_NAME_UNITS;

import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.tree.TreeFactory;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.dawnsci.nexus.builder.CustomNexusEntryModification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.scan.datawriter.NexusDataWriter;
import gda.data.scan.datawriter.NexusDataWriterConfiguration;
import gda.data.scan.datawriter.scannablewriter.SingleScannableWriter;
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
 * @param <N> type of nexus object
 * @author Matthew Dickie
 */
class ScannableLocationMapWriter<N extends NXobject> implements CustomNexusEntryModification {

	private static final Logger logger = LoggerFactory.getLogger(ScannableLocationMapWriter.class);

	private final AbstractScannableNexusDevice<N> scannableNexusDevice;
	private final SingleScannableWriter scannableWriter;

	private boolean entryModified = false;

	private NXentry entry = null;

	public ScannableLocationMapWriter(AbstractScannableNexusDevice<N> scannableNexusDevice,
			SingleScannableWriter scannableWriter) {
		this.scannableNexusDevice = scannableNexusDevice;
		this.scannableWriter = scannableWriter;
	}

	@Override
	public void modifyEntry(NXentry entry) throws NexusException {
		if (entryModified) { // sanity check, this method should only ever be called once
			throw new IllegalStateException("modifyEntry() has already been called");
		}

		// check that the nexus object for the wrapper has been created. This should be the case
		// if we're in a scan, unless this scannable is a per-scan monitor that is unavailable
		if (!scannableNexusDevice.isNexusObjectCreated()) return;

		this.entry = entry;
		final String scannableName = scannableNexusDevice.getScannable().getName();
		final String[] paths = scannableWriter.getPaths();
		final String[] units = scannableWriter.getUnits();
		final String[] fieldNames = scannableNexusDevice.getFieldNames();
		if (paths.length > fieldNames.length) {
			throw new NexusException(MessageFormat.format("Number of configured paths ({0}) larger than number of fields ({1}) for scannable {2}",
					paths.length, fieldNames.length, scannableNexusDevice.getName()));
		}

		for (int fieldIndex = 0; fieldIndex < paths.length; fieldIndex++) {
			logger.info("Adding link from '{}' to field '{}' of scannable '{}'", paths[fieldIndex], fieldNames[fieldIndex], scannableName);
			final DataNode dataNode = scannableNexusDevice.getFieldDataNode(fieldNames[fieldIndex]);
			final String fieldUnits = (units != null && units.length > fieldIndex) ? units[fieldIndex] : null;
			// link to existing data node
			createField(dataNode, paths[fieldIndex], fieldUnits);
		}

		entryModified = true;
	}

	/**
	 * Creates the field with the given name to the given location
	 * @param fieldName
	 * @param path
	 * @param unitStr
	 * @throws NexusException
	 */
	private void createField(DataNode dataNode, String path, String unitStr) throws NexusException {
		final String newFieldName = NexusUtils.getName(path); // the final segment of the path
		final String parentGroupPath = path.substring(0, path.length() - newFieldName.length());
		// get the parent group, creating it if it doesn't already exist
		final NXobject parentGroup = NexusUtils.getGroupNode(entry, parentGroupPath, true);

		// An error getting the position of a per-scan monitor is not treated as fatal, but no Nexus object
		// (and therefore no data node) will be created, so check for that condition here.
		// add units if specified
		if (StringUtils.isNotBlank(unitStr)) {
			dataNode.addAttribute(TreeFactory.createAttribute(ATTR_NAME_UNITS, unitStr));
		}

		parentGroup.addDataNode(newFieldName, dataNode);
	}
}