/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

import static java.util.stream.Collectors.toList;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.tree.TreeFactory;
import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXcollection;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.january.dataset.DatasetFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Detector;
import gda.device.Scannable;
import gda.device.ScannableMotionUnits;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;

public class BeforeScanSnapshotWriter implements INexusDevice<NXcollection> {

	public static final String BEFORE_SCAN_COLLECTION_NAME = "before_scan";
	public static final String ATTR_NAME_UNITS = "units";

	private String name = BEFORE_SCAN_COLLECTION_NAME;

	private static final Logger logger = LoggerFactory.getLogger(BeforeScanSnapshotWriter.class);

	private Set<String> additionalScannableNames = null;

	public Set<String> getAdditionalScannableNames() {
		return additionalScannableNames == null ? Collections.emptySet() : additionalScannableNames;
	}

	public void setAdditionalScannableNames(Set<String> additionalScannableNames) {
		this.additionalScannableNames = additionalScannableNames;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public NexusObjectProvider<NXcollection> getNexusProvider(NexusScanInfo info) throws NexusException {
		final NXcollection beforeScanCollection = NexusNodeFactory.createNXcollection();

		final List<Scannable> scannables = getScannables(info);
		for (Scannable scannable : scannables) {
			addCollectionForScannable(beforeScanCollection, scannable);
		}

		final NexusObjectWrapper<NXcollection> nexusObjectWrapper =
				new NexusObjectWrapper<>(BEFORE_SCAN_COLLECTION_NAME, beforeScanCollection);
		nexusObjectWrapper.setCategory(NexusBaseClass.NX_INSTRUMENT);
		return nexusObjectWrapper;
	}

	private void addCollectionForScannable(final NXcollection beforeScanCollection, Scannable scannable) {
		final Object[] positionArray = getPositionArray(scannable);
		if (positionArray == null) return;

		final String[] fieldNames = ArrayUtils.addAll(scannable.getInputNames(), scannable.getExtraNames());
		final NXcollection scannableCollection = NexusNodeFactory.createNXcollection();

		for (int fieldIndex = 0; fieldIndex < fieldNames.length; fieldIndex++) {
			if (fieldIndex >= positionArray.length) {
				logger.warn("Field {} from scannable '{}' ({}) missing from positionArray {}", fieldIndex, getName(), fieldNames[fieldIndex], positionArray);
			} else {
				final DataNode dataNode = NexusNodeFactory.createDataNode();
				dataNode.setDataset(DatasetFactory.createFromObject(positionArray[fieldIndex]));
				final String unitsStr = scannable instanceof ScannableMotionUnits ? ((ScannableMotionUnits) scannable).getUserUnits() : null;
				if (unitsStr != null) {
					dataNode.addAttribute(TreeFactory.createAttribute(ATTR_NAME_UNITS, unitsStr));
				}
				scannableCollection.addDataNode(fieldNames[fieldIndex], dataNode);
			}
		}

		beforeScanCollection.addGroupNode(scannable.getName(), scannableCollection);
	}

	private List<Scannable> getScannables(NexusScanInfo scanInfo) {
		final List<String> scannableNames = getScannableNames(scanInfo);
		return scannableNames.stream()
				.map(this::getScannable)
				.filter(Objects::nonNull)
				.collect(toList());
	}

	private List<String> getScannableNames(NexusScanInfo scanInfo) {
		return Stream.of(scanInfo.getScannableNames(), scanInfo.getPerPointMonitorNames(),
				scanInfo.getPerScanMonitorNames(), getAdditionalScannableNames())
				.flatMap(Collection::stream)
				.collect(toList());
	}

	private Scannable getScannable(String scannableName) {
		final Optional<Scannable> optScannable = Finder.findOptionalOfType(scannableName, Scannable.class)
				.or(() -> getScannableFromJythonNamespace(scannableName));
		if (!optScannable.isPresent()) {
			logger.error("Could not find scannable with name: {}", scannableName);
		}

		return optScannable.orElse(null);
	}

	private Optional<Scannable> getScannableFromJythonNamespace(String scannableName) {
		final Object jythonObject = InterfaceProvider.getJythonNamespace().getFromJythonNamespace(scannableName);
		return Optional.ofNullable(jythonObject instanceof Scannable && !(jythonObject instanceof Detector) ?
				(Scannable) jythonObject : null);
	}

	/**
	 * Converts the position of the {@link Scannable} (as returned by
	 * {@link Scannable#getPosition()}) to an array.
	 * <ul>
	 *   <li>The position is not an array, just an object of some kind.
	 *   	A single-valued array is returned containing that object;</li>
	 *   <li>The position is a primitive array. It is converted to an array of Objects, each
	 *      element of which is a wrapper of the primitive at that index of primitive array;</li>
	 *   <li>The position is already an object array. It is returned as is.</li>
	 * </ul>
	 *
	 * @param scannable the scannable to get the position for
	 * @return position as an array
	 * @throws NexusException
	 */
	private Object[] getPositionArray(Scannable scannable) {
		try {
			final Object position = scannable.getPosition();
			if (position instanceof List) {
				final List<?> positionList = (List<?>)position;
				return positionList.toArray();
			}
			if (!position.getClass().isArray()) {
				// position is not an array (i.e. is a double) return array with position as single element
				return new Object[] { position };
			}

			if (position.getClass().getComponentType().isPrimitive()) {
				// position is a primitive array
				final int size = Array.getLength(position);
				Object[] outputArray = new Object[size];
				for (int i = 0; i < size; i++) {
					outputArray[i] = Array.get(position, i);
				}
				return outputArray;
			}

			// position is already an object array
			return (Object[]) position;
		} catch (Exception e) {
			logger.error("Could not get value of scannable: {}", scannable.getName(), e);
			return null;
		}
	}

}
