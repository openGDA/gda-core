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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.SequencedMap;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.tree.TreeFactory;
import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NexusConstants;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.NexusScanInfo.ScanRole;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.scan.IScanDataPoint;

/**
 * A nexus device that writes a 'measurement' {@link NXdata} group, which links to the
 * dataset for scannables and scalar-valued detector fields, (e.g. 'counterTimer').
 */
public class MeasurementGroupWriter implements INexusDevice<NXdata> {

	private static final Logger logger = LoggerFactory.getLogger(MeasurementGroupWriter.class);

	private static final List<ScanRole> SCAN_ROLES_TO_WRITE = List.of(ScanRole.SCANNABLE, ScanRole.MONITOR_PER_POINT, ScanRole.DETECTOR);

	public static final String GROUP_NAME_MEASUREMENT = "measurement";

	private IScanDataPoint firstPoint;

	private Map<ScanRole, List<INexusDevice<?>>> nexusDevices;

	@Override
	public String getName() {
		return GROUP_NAME_MEASUREMENT;
	}

	public void setFirstPoint(IScanDataPoint firstPoint) {
		this.firstPoint = firstPoint;
	}

	public void setNexusDevices(Map<ScanRole, List<INexusDevice<?>>> nexusDevices) {
		this.nexusDevices = nexusDevices;
	}

	/**
	 * A record to gather together the things we need to know about each DataNode to link
	 */
	private record FieldInfo(String deviceName, ScanRole scanRole, String fieldName, DataNode dataNode) {
		// no content
	}

	@Override
	public NexusObjectProvider<NXdata> getNexusProvider(NexusScanInfo scanInfo) throws NexusException {
		final String[] headers = getHeaders(firstPoint);
		final Double[] firstPointData = firstPoint.getAllValuesAsDoubles();

		// get names of fields with non-null values
		final List<FieldInfo> fieldInfos = findDataNodesToLink(headers, firstPointData);

		// write data fields
		final NXdata dataGroup = NexusNodeFactory.createNXdata();

		// use detector field names as signal field names
		List<String> signalFieldNames = fieldInfos.stream()
				.filter(field -> field.scanRole() == ScanRole.DETECTOR)
				.map(FieldInfo::fieldName)
				.collect(Collectors.toCollection(ArrayList::new));
		//I16-905:  should use last element of scan command as signal if possible
		String[] cmd = scanInfo.getScanCommand().split(" ");
		List<FieldInfo> cmdFields = fieldInfos.stream()
				.filter(field -> field.scanRole() != ScanRole.DETECTOR)
				.filter(info -> info.deviceName().equals(cmd[cmd.length-1]))
				.toList();
		if(! cmdFields.isEmpty()) {
			signalFieldNames.add(cmdFields.getLast().fieldName());
		}
		if (signalFieldNames.isEmpty()) signalFieldNames.add(fieldInfos.getLast().fieldName());

		// use the last possible signal field as the main signal field
		dataGroup.addAttribute(TreeFactory.createAttribute(NexusConstants.DATA_SIGNAL, signalFieldNames.getLast()));
		if (signalFieldNames.size() > 1) {
			dataGroup.addAttribute(TreeFactory.createAttribute(NexusConstants.DATA_AUX_SIGNALS,
					signalFieldNames.subList(0, signalFieldNames.size() - 1)));
		}

		dataGroup.addAttribute(TreeFactory.createAttribute(NexusConstants.DATA_AXES, calculateDefaultAxesNames(fieldInfos)));

		final int[] dataIndices = IntStream.range(0, firstPoint.getScanDimensions().length).toArray();
		for (FieldInfo fieldInfo : fieldInfos) {
			dataGroup.addDataNode(fieldInfo.fieldName(), fieldInfo.dataNode());

			if (!signalFieldNames.contains(fieldInfo.fieldName())) {
				final String indicesAttrName = fieldInfo.fieldName() + NexusConstants.DATA_INDICES_SUFFIX;
				dataGroup.addAttribute(TreeFactory.createAttribute(indicesAttrName,
						NexusUtils.createFromObject(dataIndices, indicesAttrName)));
			}
		}

		return new NexusObjectWrapper<>(getName(), dataGroup);
	}

	private List<FieldInfo> findDataNodesToLink(final String[] headers, final Double[] firstPointData) {
		if (headers.length != firstPointData.length) {
			throw new IllegalStateException("Number of headers must match point data size, " + headers.length + ", was " + firstPointData.length);
		}

		final List<FieldInfo> fieldInfos = SCAN_ROLES_TO_WRITE.stream()
				.flatMap(role -> nexusDevices.get(role).stream().map(device -> Pair.of(role, device)))
				.flatMap(roleDevicePair -> getFieldsForNexusDevice(roleDevicePair.getRight()).entrySet().stream()
						.map(namedDatasetEntry -> new FieldInfo(roleDevicePair.getRight().getName(), roleDevicePair.getLeft(), namedDatasetEntry.getKey(), namedDatasetEntry.getValue())))
				.collect(Collectors.toCollection(ArrayList::new));

		for (int i = 0; i < fieldInfos.size(); i++) {
			if (!fieldInfos.get(i).fieldName().equals(headers[i])) {
				throw new IllegalStateException("Unexpected field name:  " + fieldInfos.get(i) + ", expected: " + headers[i]);
			}
		}

		return IntStream.range(0, fieldInfos.size())
				.filter(fieldIndex -> firstPointData[fieldIndex] != null)
				.mapToObj(fieldInfos::get).toList();
	}

	private List<String> calculateDefaultAxesNames(final List<FieldInfo> fieldInfos) {
		final List<String> axesFieldNames = new ArrayList<>();
		for (int i = 0; i < fieldInfos.size(); i++) {
			if (fieldInfos.get(i).scanRole() != ScanRole.SCANNABLE) break;
			if (i == 0 || !fieldInfos.get(i).deviceName().equals(fieldInfos.get(i-1).deviceName())) {
				axesFieldNames.add(fieldInfos.get(i).fieldName());
			}
		}
		return axesFieldNames;
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

	private SequencedMap<String, DataNode> getFieldsForNexusDevice(INexusDevice<?> nexusDevice) {
		if (!(nexusDevice instanceof IGDAScannableNexusDevice)) {
			return Collections.emptyNavigableMap();
		}

		final IGDAScannableNexusDevice<?> scannableNexusDevice = (IGDAScannableNexusDevice<?>) nexusDevice;
		return Arrays.stream(scannableNexusDevice.getFieldNames())
			.collect(toMap(Function.identity(), scannableNexusDevice::getFieldDataNode, (x, y) -> x, LinkedHashMap::new));
	}

}
