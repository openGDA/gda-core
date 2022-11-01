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

import static gda.data.scan.nexus.device.GDADeviceNexusConstants.ATTRIBUTE_NAME_DECIMALS;
import static gda.data.scan.nexus.device.GDADeviceNexusConstants.ATTRIBUTE_NAME_UNITS;
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
import org.eclipse.scanning.device.CommonBeamlineDevicesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.scan.datawriter.NexusDataWriterConfiguration;
import gda.device.Detector;
import gda.device.Scannable;
import gda.device.ScannableMotionUnits;
import gda.device.scannable.ScannableUtils;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;

/**
 * A Spring bean which enables NexusScanDataWriter to write a before_scan
 * NXcollection to a scan Nexus file. This has the same format as the
 * before_scan node written by NexusDataWriter.
 * <br><br>
 * The device name defaults to "before_scan" as does the colectionName, which
 * matches NexusDataWriter, but both can both be changed if required.
 * <br><br>
 * All scannables, per point monitors, and per scan monitors in a scan are
 * included by default, so all metadata scannables will be included by default.
 * If scannables not included in a scan needs to be included in the before_scan
 * collection, then these can be configured in an additionalScannableNames Set
 * property.
 * <br><br>
 * Example bean definition:
 * <pre>
 * {@code
<bean class="gda.data.scan.nexus.device.BeforeScanSnapshotWriter" init-method="register" />
 * }
 * </pre>
 * This Nexus Device will also need to be added to the {@link CommonBeamlineDevicesConfiguration} bean:
 * <pre>
 * {@code
<property name="additionalDeviceNames">
	<set>
		<value>before_scan</value>
	</set>
</property>
 * }
 * </pre>
 */
public class BeforeScanSnapshotWriter implements INexusDevice<NXcollection> {

	private static final Logger logger = LoggerFactory.getLogger(BeforeScanSnapshotWriter.class);

	public static final String BEFORE_SCAN_COLLECTION_NAME = "before_scan";

	private String name = BEFORE_SCAN_COLLECTION_NAME;

	private String collectionName = null;

	private Set<String> additionalScannableNames = null;

	@Override
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of this nexus device. This is the name that should be used to add the device to a scan,
	 * for example via {@link NexusDataWriterConfiguration#setMetadataScannables(Set)} or
	 * {@link CommonBeamlineDevicesConfiguration#setAdditionalDeviceNames(Set)}
	 *
	 * @param name name of this device
	 */
	public void setName(String name) {
		this.name = name;
	}

	public String getCollectionName() {
		return collectionName != null ? collectionName : getName();
	}

	/**
	 * Set the name to use for the created {@link NXcollection} within its parent group.
	 * @param collectionName
	 */
	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}

	public Set<String> getAdditionalScannableNames() {
		return additionalScannableNames == null ? Collections.emptySet() : additionalScannableNames;
	}

	public void setAdditionalScannableNames(Set<String> additionalScannableNames) {
		this.additionalScannableNames = additionalScannableNames;
	}

	@Override
	public NexusObjectProvider<NXcollection> getNexusProvider(NexusScanInfo info) throws NexusException {
		final NXcollection beforeScanCollection = NexusNodeFactory.createNXcollection();

		final List<Scannable> scannables = getScannables(info);
		for (Scannable scannable : scannables) {
			addCollectionForScannable(beforeScanCollection, scannable);
		}

		final NexusObjectWrapper<NXcollection> nexusObjectWrapper =
				new NexusObjectWrapper<>(getCollectionName(), beforeScanCollection);
		nexusObjectWrapper.setCategory(NexusBaseClass.NX_INSTRUMENT);
		return nexusObjectWrapper;
	}

	private void addCollectionForScannable(final NXcollection beforeScanCollection, Scannable scannable) {
		final Object[] positionArray = getPositionArray(scannable);
		if (positionArray == null) return;

		final String[] fieldNames = ArrayUtils.addAll(scannable.getInputNames(), scannable.getExtraNames());
		final int[] numDecimals = ScannableUtils.getNumDecimalsArray(scannable);

		final NXcollection scannableCollection = NexusNodeFactory.createNXcollection();

		for (int fieldIndex = 0; fieldIndex < fieldNames.length; fieldIndex++) {
			if (fieldIndex >= positionArray.length) {
				logger.warn("Field '{}' from scannable '{}' (fieldIndex={}) missing from positionArray {}", fieldNames[fieldIndex], getName(), fieldIndex, positionArray);
			} else if (positionArray[fieldIndex] == null) {
				logger.warn("Field '{}' from scannable '{}' has a null value and will not be written.", fieldNames[fieldIndex], getName());
			} else {
				final int fieldNumDecimals = numDecimals == null ? -1 : numDecimals[fieldIndex];
				final DataNode dataNode = createDataNode(scannable, positionArray[fieldIndex], fieldNumDecimals);
				scannableCollection.addDataNode(fieldNames[fieldIndex], dataNode);
			}
		}

		beforeScanCollection.addGroupNode(scannable.getName(), scannableCollection);
	}

	private DataNode createDataNode(Scannable scannable, final Object fieldPos, int numDecimals) {
		final DataNode dataNode = NexusNodeFactory.createDataNode();
		dataNode.setDataset(DatasetFactory.createFromObject(fieldPos));
		addAttributes(scannable, dataNode, numDecimals);

		return dataNode;
	}

	private void addAttributes(Scannable scannable, final DataNode dataNode, int numDecimals) {
		final String unitsStr = scannable instanceof ScannableMotionUnits ? ((ScannableMotionUnits) scannable).getUserUnits() : null;
		if (unitsStr != null) {
			dataNode.addAttribute(TreeFactory.createAttribute(ATTRIBUTE_NAME_UNITS, unitsStr));
		}

		if (numDecimals != -1) {
			dataNode.addAttribute(TreeFactory.createAttribute(ATTRIBUTE_NAME_DECIMALS, numDecimals));
		}
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
			// log a warning if we can't find the scannable. Note that this will happen for scannables
			// that aren't
			logger.warn("Could not find scannable with name: {}", scannableName);
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
