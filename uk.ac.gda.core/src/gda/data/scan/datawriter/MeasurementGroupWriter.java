/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.tree.TreeFactory;
import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NexusConstants;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.NexusScanInfo.ScanRole;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.january.dataset.DatasetFactory;

import gda.scan.IScanDataPoint;

/**
 * A nexus device that writes a 'measurement' {@link NXdata} group, which links to the
 * dataset for scannables and scalar-valued detector fields, (e.g. 'counterTimer').
 */
public class MeasurementGroupWriter implements INexusDevice<NXdata> {

	public static final String MEASUREMENT_GROUP_NAME = "measurement";

	private IScanDataPoint firstPoint;

	private Map<ScanRole, List<INexusDevice<?>>> nexusDevices;

	@Override
	public String getName() {
		return MEASUREMENT_GROUP_NAME;
	}

	public void setFirstPoint(IScanDataPoint firstPoint) {
		this.firstPoint = firstPoint;
	}

	public void setNexusDevices(Map<ScanRole, List<INexusDevice<?>>> nexusDevices) {
		this.nexusDevices = nexusDevices;
	}

	@Override
	public NexusObjectProvider<NXdata> getNexusProvider(NexusScanInfo info) throws NexusException {
		final Set<String> fieldNamesToWrite = calculateFieldNamesToWrite();

		// create map from field name to data node, extracting the data nodes from the nexus devices
		final Map<String, DataNode> dataNodesByFieldName =
				Stream.of(ScanRole.SCANNABLE, ScanRole.MONITOR_PER_POINT, ScanRole.DETECTOR)
				.flatMap(role -> nexusDevices.get(role).stream())
				.map(this::getFieldsForNexusDevice)
				.flatMap(map -> map.entrySet().stream())
				.filter(entry -> fieldNamesToWrite.contains(entry.getKey())) // filter out fields with null values
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> x, LinkedHashMap::new)); // LinkedHashMap for insertion order iteration

		// check field names from the scannable nexus devices are as expected
		if (dataNodesByFieldName.size() != fieldNamesToWrite.size()) {
			throw new IllegalArgumentException("Header names don't match field names from nexus devices");
		}

		// create the data group and add the extracted data nodes
		final NXdata dataGroup = NexusNodeFactory.createNXdata();
		final int[] dataIndices = IntStream.range(0, firstPoint.getScanDimensions().length).toArray();
		dataNodesByFieldName.entrySet().stream().forEach(entry -> {
			dataGroup.addDataNode(entry.getKey(), entry.getValue());
			dataGroup.addAttribute(TreeFactory.createAttribute(entry.getKey() + NexusConstants.DATA_INDICES_SUFFIX,
					DatasetFactory.createFromObject(dataIndices)));
		});

		// use the last field name as the signal field
		final String lastFieldName = dataNodesByFieldName.keySet().stream().reduce((f1, f2) -> f2).orElseThrow();
		dataGroup.addAttribute(TreeFactory.createAttribute(NexusConstants.DATA_SIGNAL, lastFieldName));

		// write the axes fields attribute, assume that the first field for each scannable is the appropriate axis field
		final String[] axesFieldNames = nexusDevices.get(ScanRole.SCANNABLE).stream()
				.map(this::getFieldsForNexusDevice)
				.map(map -> map.keySet().iterator().next())
				.toArray(String[]::new);
		dataGroup.addAttribute(TreeFactory.createAttribute(NexusConstants.DATA_AXES, axesFieldNames));

		return new NexusObjectWrapper<>(getName(), dataGroup);
	}

	private Set<String> calculateFieldNamesToWrite() {
		final String[] headers = getHeaders(firstPoint);
		final Double[] firstPointData = firstPoint.getAllValuesAsDoubles();
		if (headers.length != firstPointData.length) {
			throw new IllegalStateException("Number of headers must match point data size, " + headers.length + ", was " + firstPointData.length);
		}

		// get names of fields with non-null values
		return IntStream.range(0, headers.length)
				.filter(i -> firstPointData[i] != null)
				.mapToObj(i -> headers[i])
				.collect(toSet());
	}

	private String[] getHeaders(IScanDataPoint point) {
		final List<String> positionHeaders = point.getPositionHeader();
		final List<String> detectorHeaders = point.getDetectorHeader();

		// Create a set first to detect duplicates - use a LinkedHashSet to maintain insertion order
		final Set<String> headersSet = Stream.concat(positionHeaders.stream(), detectorHeaders.stream()).collect(toCollection(LinkedHashSet::new));
		if (headersSet.size() != positionHeaders.size() + detectorHeaders.size()) {
			throw new IllegalStateException("Duplicates in position and detector headers: " + positionHeaders + " & " + detectorHeaders);
		}

		return headersSet.toArray(String[]::new); // return as array
	}

	private Map<String, DataNode> getFieldsForNexusDevice(INexusDevice<?> nexusDevice) {
		if (!(nexusDevice instanceof IGDAScannableNexusDevice)) {
			return Collections.emptyMap();
		}

		final IGDAScannableNexusDevice<?> scannableNexusDevice = (IGDAScannableNexusDevice<?>) nexusDevice;
		return Arrays.stream(scannableNexusDevice.getFieldNames())
			.collect(toMap(Function.identity(), scannableNexusDevice::getFieldDataNode, (x, y) -> x, LinkedHashMap::new));
	}

}
