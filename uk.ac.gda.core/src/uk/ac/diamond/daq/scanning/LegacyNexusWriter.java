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

package uk.ac.diamond.daq.scanning;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.analysis.api.tree.NodeLink;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.dawnsci.nexus.builder.CustomNexusEntryModification;

import gda.data.scan.datawriter.scannablewriter.SingleScannableWriter;

/**
 * This class is responsible for linking the datasets for a GDA8 scannable to the
 * locations specified in the location map of a legacy GDA8 spring configuration file.
 *
 * @param <N> type of nexus object
 * @author Matthew Dickie
 */
class LegacyNexusWriter<N extends NXobject> implements CustomNexusEntryModification {

	private final ScannableNexusWrapper<N> scannableNexusWrapper;
	private final SingleScannableWriter scannableWriter;

	private boolean entryModified = false;

	private NXentry entry = null;

	public LegacyNexusWriter(ScannableNexusWrapper<N> scannableNexusWrapper,
			SingleScannableWriter scannableWriter) {
		this.scannableNexusWrapper = scannableNexusWrapper;
		this.scannableWriter = scannableWriter;
	}

	@Override
	public void modifyEntry(NXentry entry) throws NexusException {
		if (entryModified) { // sanity check, this method should only ever be called once
			throw new IllegalStateException("modifyEntry() has already been called");
		}

		this.entry = entry;

		String[] paths = scannableWriter.getPaths();
		String[] units = scannableWriter.getUnits();

		// relies on predictable iteration order of LinkedHashMap
		Iterator<String> fieldNameIter = scannableNexusWrapper.getFieldNames().iterator();
		for (int i = 0; i < paths.length; i++) {
			final String fieldName = fieldNameIter.next();
			String fieldUnits = (units != null && units.length > i) ? units[i] : null;
			createField(fieldName, paths[i], fieldUnits);
		}

		entryModified = true;
	}

	/**
	 * Get the group node with the given augmented path within the given {@link NXentry},
	 * creating it if it does not already exist.
	 * @param augmentedPath
	 * @return group node with given augmented path
	 * @throws NexusException
	 */
	private NXobject getGroupNode(String augmentedPath) throws NexusException {
		// check to see if the group node already exists and we can just return it
		final String plainPath = NexusUtils.stripAugmentedPath(augmentedPath);
		final NodeLink link = entry.findNodeLink(plainPath);
		if (link != null) {
			if (link.isDestinationGroup()) {
				GroupNode dest = (GroupNode) link.getDestination();
				if (!(dest instanceof NXobject)) {
					throw new NexusException("Group node is not an NXobject " + augmentedPath);
				}
				return (NXobject) dest;
			}
			throw new NexusException("A node already exists at the specified path which is not a group node: " + augmentedPath);
		}

		// we have to create the group node
		// first parse the augmented path into segments
		final List<Pair<String, String>> parsedPath = parseAugmentedPath(augmentedPath);
		NXobject groupNode = entry;
		for (Pair<String, String> parsedPathSegment : parsedPath) {
			final String name = parsedPathSegment.getFirst();
			final String nxClass = parsedPathSegment.getSecond();

			GroupNode childNode = groupNode.getGroupNode(name);
			if (childNode == null) {
				// group node does not exist, so we create it
				NexusBaseClass baseClass = getBaseClassForName(nxClass);
				childNode = NexusNodeFactory.createNXobjectForClass(baseClass);
				groupNode.addGroupNode(name, childNode);
			} else {
				// group already exists, check it has the expected NX_class
				if (!(childNode instanceof NXobject)) {
					throw new NexusException(MessageFormat.format("The group ''{0}'' already exists and is not an NXobject", name));
				}

				NexusBaseClass expectedBaseClass = getBaseClassForName(nxClass);
				NexusBaseClass actualNexusBaseClass = ((NXobject) childNode).getNexusBaseClass();
				if (expectedBaseClass != actualNexusBaseClass) {
					throw new NexusException(MessageFormat.format("The group ''{0}'' already exists and has NX_class ''{1}'', expected ''{2}''",
							name, actualNexusBaseClass, expectedBaseClass));
				}
			}

			groupNode = (NXobject) childNode;
		}

		return groupNode;
	}

	private NexusBaseClass getBaseClassForName(String nxClass) {
		// NXgoniometer is a special case - some beamlines use it, but it's not an
		// official NeXus base class
		if (nxClass.equals("NXgoniometer")) {
			return NexusBaseClass.NX_COLLECTION;
		}

		return NexusBaseClass.getBaseClassForName(nxClass);
	}

	private List<Pair<String, String>> parseAugmentedPath(String augmentedPath) {
		String[] segments = augmentedPath.split(Node.SEPARATOR);
		List<Pair<String, String>> parsedSegments = new ArrayList<Pair<String, String>>(segments.length);
		for (String segment : segments) {
			String[] pair = segment.split(NexusFile.NXCLASS_SEPARATOR, 2);
			parsedSegments.add(new Pair<>(pair[0], pair.length > 1 ? pair[1] : null));
		}

		return parsedSegments;
	}

	/**
	 * Creates the field with the given name to the given location
	 * @param fieldName
	 * @param path
	 * @param unit
	 * @throws NexusException
	 */
	private void createField(String fieldName, String path, String unit) throws NexusException {
		String newFieldName = NexusUtils.getName(path); // the final segment of the path
		String parentGroupPath = path.substring(0, path.length() - newFieldName.length());
		// get the parent group, creating it if it doesn't already exist
		NXobject parentGroup = getGroupNode(parentGroupPath);

		// link to existing data node
		final DataNode dataNode = this.scannableNexusWrapper.getDataNode(fieldName);
		parentGroup.addDataNode(newFieldName, dataNode);

		// also add units if not already present
		if (StringUtils.isNotBlank(unit)) {
			parentGroup.setAttribute(newFieldName, ScannableNexusWrapper.ATTR_NAME_UNITS, unit);
		}
	}
}